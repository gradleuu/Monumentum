package io.max.monumentum.data.shard;

import static io.max.monumentum.Monumentum.LOGGER;

import com.google.gson.reflect.TypeToken;
import io.max.monumentum.api.shard.Shard;
import io.max.monumentum.api.shard.ShardType;
import io.max.monumentum.data.ApiClient;
import io.max.monumentum.data.Endpoints;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class Shards {
  public static final Shard UNKNOWN =
      new Shard("unknown", "Unknown", ShardType.OVERWORLD, OptionalInt.empty(), "\uE000");

  private static final TypeToken<Map<String, ShardDto>> TYPE = new TypeToken<>() {};
  private static volatile Map<String, Shard> shards = Map.of();

  private Shards() {}

  public static CompletableFuture<Void> refresh() {
    return ApiClient.get(Endpoints.SHARDS, TYPE)
        .thenAccept(
            raw -> {
              Map<String, Shard> clean = new LinkedHashMap<>();
              raw.forEach(
                  (id, dto) -> {
                    if ("unknown".equals(id)) {
                      return;
                    }
                    Shard s = convert(id, dto);
                    if (s != null) {
                      clean.put(id, s);
                    }
                  });
              shards = Collections.unmodifiableMap(clean);
            });
  }

  public static Shard byId(String id) {
    Shard s = shards.get(id);
    return s != null ? s : UNKNOWN;
  }

  public static Collection<Shard> all() {
    return shards.values();
  }

  private static Shard convert(String id, ShardDto dto) {
    if (dto == null || dto.officialName() == null || dto.shardType() == null) {
      LOGGER.warn("Skipping malformed shard entry '{}'", id);
      return null;
    }
    ShardType type;
    try {
      type = ShardType.valueOf(dto.shardType());
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Skipping shard '{}': unknown type '{}'", id, dto.shardType());
      return null;
    }
    OptionalInt chests =
        dto.maxChests() == null ? OptionalInt.empty() : OptionalInt.of(dto.maxChests());
    String emoji = dto.emoji() == null ? UNKNOWN.emoji() : dto.emoji();
    return new Shard(id, dto.officialName(), type, chests, emoji);
  }
}
