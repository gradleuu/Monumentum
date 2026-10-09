package io.max.monumentum.data;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.util.Signal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class Dataset<T> extends Signal<T> {
  private final String name;
  private final Feed primary;
  private final Feed fallback;
  private final Function<String, T> decoder;
  private final Executor callbackExecutor;

  public Dataset(
      String name,
      T empty,
      Feed primary,
      Feed fallback,
      Function<String, T> decoder,
      Executor callbackExecutor) {
    super(empty);
    this.name = Objects.requireNonNull(name, "name");
    this.primary = Objects.requireNonNull(primary, "primary");
    this.fallback = Objects.requireNonNull(fallback, "fallback");
    this.decoder = Objects.requireNonNull(decoder, "decoder");
    this.callbackExecutor = Objects.requireNonNull(callbackExecutor, "callbackExecutor");
  }

  public CompletableFuture<Void> load() {
    return primary
        .fetch()
        .thenApply(decoder)
        .exceptionallyCompose(
            t -> {
              LOGGER.warn("Dataset '{}': primary failed, using fallback", name, t);
              return fallback.fetch().thenApply(decoder);
            })
        .thenAcceptAsync(this::emit, callbackExecutor);
  }
}
