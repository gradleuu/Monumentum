package io.max.monumentum.api;

import io.max.monumentum.api.data.ApiSources;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface MonumentumEntrypoint {
  /**
   * @param sources Use this to add data sources
   * @apiNote Do not use {@link MonumentumApi} services here; they are not ready yet.
   */
  void configureSources(ApiSources sources);
}
