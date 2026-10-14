package io.max.monumentum.service;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.ability.Ability;
import io.max.monumentum.api.ability.AbilityApi;
import io.max.monumentum.api.ability.AbilityChange;
import io.max.monumentum.api.ability.AbilityChange.Kind;
import io.max.monumentum.api.ability.event.AbilityChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import io.max.monumentum.util.TickBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class AbilityService implements AbilityApi {
  private static final long GRACE_NANOS = 100_000_000L; // 2 ticks

  private record Pending(ServerMessage message, long atNanos) {}

  private record Signature(
      int charges, Optional<String> mode, boolean onCooldown, boolean active) {}

  private final Map<String, Ability> state = new HashMap<>();
  private Map<String, Signature> published = Map.of();

  // ordered by name
  private volatile Map<String, Ability> snapshot = Map.of();

  private AbilityService() {}

  public static AbilityService create(MessageRouter router) {
    AbilityService service = new AbilityService();
    TickBatch<Pending> batch = new TickBatch<>(service::onTick);
    batch.start();
    // a full class update replaces everything, including updates received earlier this tick
    router.subscribe(
        ServerMessage.ClassUpdate.class,
        m -> batch.offerReplacing(new Pending(m, System.nanoTime())));
    router.subscribe(
        ServerMessage.AbilityUpdate.class, m -> batch.offer(new Pending(m, System.nanoTime())));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) ->
            client.execute(
                () -> {
                  batch.clear();
                  service.reset();
                }));
    return service;
  }

  private void onTick(List<Pending> batch) {
    long now = System.nanoTime();
    if (!batch.isEmpty()) {
      for (Pending pending : batch) {
        apply(pending);
      }
      publish(now);
    } else if (signaturesChanged(now)) {
      // fallback: something ended locally
      publish(now);
    }
  }

  private void reset() {
    state.clear();
    publish(System.nanoTime());
  }

  private void apply(Pending pending) {
    if (pending.message() instanceof ServerMessage.ClassUpdate update) {
      state.clear();
      for (ServerMessage.AbilityState s : update.abilities()) {
        if (!s.name().isBlank()) {
          state.put(
              s.name(),
              new Ability(
                  s.name(),
                  s.remainingCharges(),
                  s.mode(),
                  s.remainingCooldown(),
                  orZero(s.remainingDuration()),
                  pending.atNanos()));
        }
      }
    } else if (pending.message() instanceof ServerMessage.AbilityUpdate u && !u.name().isBlank()) {
      state.put(
          u.name(),
          new Ability(
              u.name(),
              u.remainingCharges(),
              u.mode(),
              u.remainingCooldown(),
              orZero(u.remainingDuration()),
              pending.atNanos()));
    }
  }

  private boolean signaturesChanged(long now) {
    for (Ability a : state.values()) {
      if (!signature(a, now).equals(published.get(a.name()))) {
        return true;
      }
    }
    return false;
  }

  private void publish(long now) {
    Map<String, Ability> before = snapshot;
    Map<String, Ability> sorted = new TreeMap<>(state);
    Map<String, Signature> nextSignatures = new HashMap<>();
    List<AbilityChange> changes = new ArrayList<>();

    for (Ability a : sorted.values()) {
      Signature s = signature(a, now);
      nextSignatures.put(a.name(), s);
      Set<Kind> kinds = diff(published.get(a.name()), s);
      if (!kinds.isEmpty()) {
        changes.add(
            new AbilityChange(
                a.name(), Optional.ofNullable(before.get(a.name())), Optional.of(a), kinds));
      }
    }
    for (Map.Entry<String, Ability> gone : before.entrySet()) {
      if (!sorted.containsKey(gone.getKey())) {
        changes.add(
            new AbilityChange(
                gone.getKey(),
                Optional.of(gone.getValue()),
                Optional.empty(),
                EnumSet.of(Kind.REMOVED)));
      }
    }

    published = nextSignatures;
    snapshot = Collections.unmodifiableMap(sorted);
    for (AbilityChange change : changes) {
      try {
        AbilityChangeCallback.EVENT.invoker().onChange(change);
      } catch (RuntimeException e) {
        LOGGER.error("Ability listener failed for {}", change.name(), e);
      }
    }
  }

  private static Signature signature(Ability a, long now) {
    long t = now - GRACE_NANOS;
    return new Signature(a.charges(), a.mode(), a.onCooldownAt(t), a.activeAt(t));
  }

  private static Set<Kind> diff(@Nullable Signature old, Signature now) {
    if (old == null) {
      return EnumSet.of(Kind.ADDED);
    }
    EnumSet<Kind> kinds = EnumSet.noneOf(Kind.class);
    if (old.charges() != now.charges()) {
      kinds.add(Kind.CHARGES);
    }
    if (!old.mode().equals(now.mode())) {
      kinds.add(Kind.MODE);
    }
    if (!old.active() && now.active()) {
      kinds.add(Kind.ACTIVATED);
    }
    if (old.active() && !now.active()) {
      kinds.add(Kind.DEACTIVATED);
    }
    if (!old.onCooldown() && now.onCooldown()) {
      kinds.add(Kind.COOLDOWN_STARTED);
    }
    if (old.onCooldown() && !now.onCooldown()) {
      kinds.add(Kind.COOLDOWN_ENDED);
    }
    return kinds;
  }

  private static int orZero(@Nullable Integer value) {
    return value == null ? 0 : value;
  }

  @Override
  public List<Ability> abilities() {
    return List.copyOf(snapshot.values());
  }

  @Override
  public Optional<Ability> ability(String name) {
    return Optional.ofNullable(snapshot.get(name));
  }
}
