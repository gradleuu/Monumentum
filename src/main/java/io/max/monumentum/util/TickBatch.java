package io.max.monumentum.util;

import static io.max.monumentum.Monumentum.LOGGER;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class TickBatch<T> {
  private final List<T> pending = new ArrayList<>();
  private final Consumer<List<T>> sink;

  public TickBatch(Consumer<List<T>> sink) {
    this.sink = sink;
  }

  public void start() {
    ClientTickEvents.END_CLIENT_TICK.register(mc -> flush());
  }

  public void offer(T value) {
    pending.add(value);
  }

  public void offerReplacing(T value) {
    pending.clear();
    pending.add(value);
  }

  public void clear() {
    pending.clear();
  }

  private void flush() {
    List<T> batch = pending.isEmpty() ? List.of() : List.copyOf(pending);
    pending.clear();
    try {
      sink.accept(batch);
    } catch (RuntimeException e) {
      LOGGER.error("Tick handler failed", e);
    }
  }
}
