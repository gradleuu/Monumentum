package io.max.monumentum.data;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.util.Signal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class Dataset<T> extends Signal<T> {
  private final String name;
  private final List<Feed> feeds;
  private final Function<String, T> decoder;
  private final Executor callbackExecutor;

  public Dataset(
      String name,
      T empty,
      List<Feed> feeds,
      Function<String, T> decoder,
      Executor callbackExecutor) {
    super(empty);
    if (feeds.isEmpty()) {
      throw new IllegalArgumentException("No feeds for " + name);
    }
    this.name = Objects.requireNonNull(name, "name");
    this.feeds = List.copyOf(feeds);
    this.decoder = Objects.requireNonNull(decoder, "decoder");
    this.callbackExecutor = Objects.requireNonNull(callbackExecutor, "callbackExecutor");
  }

  public CompletableFuture<Void> load() {
    return attempt(0).thenAcceptAsync(this::emit, callbackExecutor);
  }

  private CompletableFuture<T> attempt(int index) {
    if (index >= feeds.size()) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("All " + feeds.size() + " sources failed for " + name));
    }
    return feeds
        .get(index)
        .fetch()
        .thenApply(decoder)
        .exceptionallyCompose(
            t -> {
              LOGGER.warn("Dataset '{}': source {}/{} failed", name, index + 1, feeds.size(), t);
              return attempt(index + 1);
            });
  }
}
