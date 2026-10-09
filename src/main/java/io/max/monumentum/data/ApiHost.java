package io.max.monumentum.data;

import io.max.monumentum.api.data.ApiFile;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class ApiHost {
  private final URI base;
  private final String bundledRoot;
  private final SourceRegistry sources;

  public ApiHost(URI base, String bundledRoot, SourceRegistry sources) {
    String s = base.toString();
    this.base = s.endsWith("/") ? base : URI.create(s + "/");
    this.bundledRoot = bundledRoot.endsWith("/") ? bundledRoot : bundledRoot + "/";
    this.sources = sources;
  }

  /**
   * @param file Path relative to the base, e.g. {@code shards.json}
   */
  public <T> Dataset<T> dataset(
      ApiFile file, T empty, Function<String, T> decoder, Executor callbackExecutor) {
    List<Feed> feeds = new ArrayList<>();
    sources.before(file).forEach(u -> feeds.add(new RemoteFeed(u)));
    feeds.add(new RemoteFeed(base.resolve(file.fileName())));
    sources.after(file).forEach(u -> feeds.add(new RemoteFeed(u)));
    feeds.add(new ResourceFeed(bundledRoot + file.fileName()));
    return new Dataset<>(file.fileName(), empty, feeds, decoder, callbackExecutor);
  }
}
