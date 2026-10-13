package io.max.monumentum.util;

import io.max.monumentum.Monumentum;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.jetbrains.annotations.Nullable;

public final class TickCoalescer<T> {
  private final Consumer<T> sink;
  private @Nullable T pending;

  public TickCoalescer(Consumer<T> sink) {
    this.sink = sink;
  }

  public void start() {
    ClientTickEvents.END_CLIENT_TICK.register(mc -> flush());
  }

  public void offer(T value) {
    pending = value;
  }

  public void clear() {
    pending = null;
  }

  private void flush() {
    T value = pending;
    if (value == null) {
      return;
    }
    pending = null;
    try {
      sink.accept(value);
    } catch (RuntimeException e) {
      Monumentum.LOGGER.error("Tick handler failed", e);
    }
  }
}
