package io.max.monumentum.data;

import static io.max.monumentum.Monumentum.LOGGER;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.max.monumentum.api.data.ApF;
import io.max.monumentum.api.shard.Shard;
import io.max.monumentum.api.shard.ShardType;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.Nullable;

public final class ShardCatalog {
  private record Wire(
      String officialName, String shardType, @Nullable Integer maxChests, String emoji) {}

  private static final Gson GSON = new Gson();
  private static final TypeToken<Map<String, Wire>> TYPE = new TypeToken<>() {};

  private final Dataset<Map<String, Shard>> dataset;

  public ShardCatalog(ApiHost host, Executor callbackExecutor) {
    this.dataset = host.dataset(ApF.SHARDS, Map.of(), ShardCatalog::decode, callbackExecutor);
  }

  public Shard byId(String id) {
    Shard s = dataset.current().get(id);
    return s != null ? s : Shard.UNKNOWN;
  }

  public Collection<Shard> all() {
    return dataset.current().values();
  }

  public void subscribe(Runnable listener) {
    dataset.subscribe(listener);
  }

  public CompletableFuture<Void> load() {
    return dataset.load();
  }

  private static Map<String, Shard> decode(String json) {
    Map<String, Wire> raw = GSON.fromJson(json, TYPE.getType());
    if (raw == null) {
      throw new IllegalStateException("Empty shard JSON");
    }
    Map<String, Shard> clean = new LinkedHashMap<>();
    raw.forEach(
        (id, wire) -> {
          if (Shard.UNKNOWN.id().equals(id)) {
            return;
          }
          Shard shard = toShard(id, wire);
          if (shard != null) {
            clean.put(id, shard);
          }
        });
    if (clean.isEmpty()) {
      throw new IllegalStateException("No valid shards in feed");
    }
    return Collections.unmodifiableMap(clean);
  }

  private static @Nullable Shard toShard(String id, @Nullable Wire wire) {
    if (wire == null || wire.officialName() == null || wire.shardType() == null) {
      LOGGER.warn("Skipping malformed shard entry '{}'", id);
      return null;
    }
    ShardType type;
    try {
      type = ShardType.valueOf(wire.shardType());
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Skipping shard '{}': unknown type '{}'", id, wire.shardType());
      return null;
    }
    OptionalInt chests =
        wire.maxChests() == null ? OptionalInt.empty() : OptionalInt.of(wire.maxChests());
    String emoji = wire.emoji() == null ? Shard.UNKNOWN.emoji() : wire.emoji();
    return new Shard(id, wire.officialName(), type, chests, emoji);
  }
}
