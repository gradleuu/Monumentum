package io.max.monumentum.data;

import java.net.URI;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class ApiHost {
  private final URI base;
  private final String bundledRoot;

  public ApiHost(URI base, String bundledRoot) {
    String s = base.toString();
    this.base = s.endsWith("/") ? base : URI.create(s + "/");
    this.bundledRoot = bundledRoot.endsWith("/") ? bundledRoot : bundledRoot + "/";
  }

  /**
   * @param file Path relative to the base, e.g. {@code shards.json}
   */
  public <T> Dataset<T> dataset(
      String file, T empty, Function<String, T> decoder, Executor callbackExecutor) {
    return new Dataset<>(
        file,
        empty,
        new RemoteFeed(base.resolve(file)),
        new ResourceFeed(bundledRoot + file),
        decoder,
        callbackExecutor);
  }
}
