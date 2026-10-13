package io.max.monumentum.api;

import io.max.monumentum.api.server.ServerInfoApi;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.strike.StrikeApi;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.ApiStatus;

public final class MonumentumApi {
  private static final Map<Class<?>, Object> SERVICES = new ConcurrentHashMap<>();

  private MonumentumApi() {}

  /**
   * @throws IllegalStateException if the service is not registered (yet).
   */
  public static <T> T get(Class<T> type) {
    return find(type)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    type.getSimpleName()
                        + " is not available (Monumentum's not initialized yet?)"));
  }

  public static <T> Optional<T> find(Class<T> type) {
    return Optional.ofNullable(type.cast(SERVICES.get(type)));
  }

  public static ShardApi shards() {
    return get(ShardApi.class);
  }

  public static StrikeApi strikes() {
    return get(StrikeApi.class);
  }

  public static ServerInfoApi serverInfo() {
    return get(ServerInfoApi.class);
  }

  @ApiStatus.Internal
  public static <T> void register(Class<T> type, T service) {
    Objects.requireNonNull(service, "service");
    if (!type.isInterface()) {
      throw new IllegalArgumentException("Only interfaces can be exposed: " + type.getName());
    }
    if (SERVICES.putIfAbsent(type, service) != null) {
      throw new IllegalStateException("Already registered: " + type.getName());
    }
  }
}
