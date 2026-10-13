package io.max.monumentum.service;

import io.max.monumentum.api.effect.Effect;
import io.max.monumentum.api.effect.EffectApi;
import io.max.monumentum.api.effect.event.EffectsChangeCallback;
import io.max.monumentum.client.event.WorldChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class EffectService implements EffectApi {
  private static final Comparator<Effect> ORDER =
      Comparator.comparingInt(Effect::displayPriority)
          .reversed()
          .thenComparing(Effect::name)
          .thenComparing(Effect::id);

  private final Map<String, Effect> active = new HashMap<>();
  private volatile List<Effect> effects = List.of();

  private EffectService() {}

  public static EffectService create(MessageRouter router) {
    EffectService service = new EffectService();
    router.subscribe(ServerMessage.MassEffectUpdate.class, service::onMass);
    router.subscribe(ServerMessage.EffectUpdate.class, service::onSingle);
    // effects belong to the shard we just left; the server resends the full list after a hop
    WorldChangeCallback.EVENT.register(
        world -> {
          if (world == null) {
            service.active.clear();
            service.publish();
          }
        });
    return service;
  }

  private void onMass(ServerMessage.MassEffectUpdate message) {
    // full list: replaces everything
    active.clear();
    long now = System.nanoTime();
    for (ServerMessage.Effect wire : message.effects()) {
      if (wire.duration() > 0) {
        active.put(wire.uuid(), toEffect(wire, now));
      }
    }
    publish();
  }

  private void onSingle(ServerMessage.EffectUpdate message) {
    ServerMessage.Effect wire = message.effect();
    if (wire.duration() <= 0) {
      // removal: the other fields are not reliable here
      active.remove(wire.uuid());
    } else {
      active.put(wire.uuid(), toEffect(wire, System.nanoTime()));
    }
    publish();
  }

  private void publish() {
    List<Effect> next = active.values().stream().sorted(ORDER).toList();
    List<Effect> previous = effects;
    // always swap, so remainingTicks() stays fresh after a renewal
    effects = next;
    if (!next.equals(previous)) {
      // equality ignores the countdown
      EffectsChangeCallback.EVENT.invoker().onChange(previous, next);
    }
  }

  private static Effect toEffect(ServerMessage.Effect wire, long nowNanos) {
    return new Effect(
        wire.uuid(),
        wire.name(),
        wire.power(),
        wire.positive(),
        wire.percentage(),
        wire.displayPriority(),
        wire.duration(),
        nowNanos);
  }

  @Override
  public List<Effect> effects() {
    return effects;
  }
}
