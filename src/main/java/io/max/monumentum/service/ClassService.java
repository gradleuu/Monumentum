package io.max.monumentum.service;

import io.max.monumentum.api.playerclass.AbilityInfo;
import io.max.monumentum.api.playerclass.ClassApi;
import io.max.monumentum.api.playerclass.event.ClassChangeCallback;
import io.max.monumentum.api.playerclass.ClassInfo;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import io.max.monumentum.util.TickCoalescer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ClassService implements ClassApi {
  private volatile ClassInfo info = ClassInfo.NONE;

  private ClassService() {}

  public static ClassService create(MessageRouter router) {
    ClassService service = new ClassService();
    TickCoalescer<ServerMessage.ClassUpdate> coalescer = new TickCoalescer<>(service::apply);
    coalescer.start();
    router.subscribe(ServerMessage.ClassUpdate.class, coalescer::offer);
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) ->
            client.execute(
                () -> {
                  coalescer.clear();
                  service.set(ClassInfo.NONE);
                }));
    return service;
  }

  private void apply(ServerMessage.ClassUpdate update) {
    set(derive(update.abilities()));
  }

  private void set(ClassInfo next) {
    ClassInfo previous = info;
    if (next.equals(previous)) {
      return;
    }
    info = next;
    ClassChangeCallback.EVENT.invoker().onChange(previous, next);
  }

  private static ClassInfo derive(List<ServerMessage.AbilityState> states) {
    List<ServerMessage.AbilityState> named =
        states.stream().filter(s -> !s.name().isBlank()).toList();
    if (named.isEmpty()) {
      return ClassInfo.NONE;
    }
    List<AbilityInfo> abilities =
        named.stream()
            .map(ClassService::toInfo)
            .sorted(Comparator.comparing(AbilityInfo::name))
            .toList();
    return new ClassInfo(className(named), abilities);
  }

  /** The class name most abilities share. A tie goes to the name that sorts first. */
  private static String className(List<ServerMessage.AbilityState> states) {
    Map<String, Long> counts =
        states.stream()
            .map(ServerMessage.AbilityState::className)
            .filter(Objects::nonNull)
            .filter(n -> !n.isBlank())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    return counts.entrySet().stream()
        .max(
            Map.Entry.<String, Long>comparingByValue()
                .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
        .map(Map.Entry::getKey)
        .orElse(ClassInfo.UNKNOWN_NAME);
  }

  private static AbilityInfo toInfo(ServerMessage.AbilityState s) {
    Integer duration = s.initialDuration();
    return new AbilityInfo(
        s.name(),
        Optional.ofNullable(s.className()).filter(n -> !n.isBlank()),
        s.initialCooldown(),
        s.maxCharges(),
        duration == null ? OptionalInt.empty() : OptionalInt.of(duration));
  }

  @Override
  public ClassInfo current() {
    return info;
  }
}
