package io.max.monumentum.util;

import static io.max.monumentum.Monumentum.LOGGER;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Signal<T> {
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
  private volatile T value;

  protected Signal(T initial) {
    this.value = Objects.requireNonNull(initial, "initial");
  }

  public final T current() {
    return value;
  }

  public final void subscribe(Runnable listener) {
    listeners.add(Objects.requireNonNull(listener, "listener"));
  }

  protected final void emit(T next) {
    if (next.equals(value)) {
      return;
    }
    value = next;
    for (Runnable listener : listeners) {
      try {
        listener.run();
      } catch (RuntimeException e) {
        // one bad listener must not block the rest
        LOGGER.error("Signal listener failed", e);
      }
    }
  }
}
