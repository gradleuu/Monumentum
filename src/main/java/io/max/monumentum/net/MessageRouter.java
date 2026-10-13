package io.max.monumentum.net;

import static io.max.monumentum.Monumentum.LOGGER;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class MessageRouter {
  private final Map<Class<? extends ServerMessage>, List<Consumer<ServerMessage>>> handlers =
      new ConcurrentHashMap<>();

  public <T extends ServerMessage> void subscribe(Class<T> type, Consumer<? super T> handler) {
    handlers
        .computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
        .add(m -> handler.accept(type.cast(m)));
  }

  void dispatch(ServerMessage message) {
    for (Consumer<ServerMessage> handler : handlers.getOrDefault(message.getClass(), List.of())) {
      try {
        handler.accept(message);
      } catch (RuntimeException e) {
        // one bad handler must not block the others
        LOGGER.error("Handler for {} failed", message.getClass().getSimpleName(), e);
      }
    }
  }
}
