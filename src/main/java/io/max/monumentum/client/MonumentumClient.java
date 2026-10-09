package io.max.monumentum.client;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.MonumentumApi;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.data.ApiHost;
import io.max.monumentum.data.ShardCatalog;
import io.max.monumentum.service.ShardService;
import io.max.monumentum.watcher.TablistWatcher;
import io.max.monumentum.watcher.WorldWatcher;
import java.net.URI;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public final class MonumentumClient implements ClientModInitializer {
  private static final String DEFAULT_API = "https://gradleuu.github.io/api/monumentum/";

  @Override
  public void onInitializeClient() {
    try {
      initShards(MinecraftClient.getInstance());
    } catch (RuntimeException e) {
      LOGGER.error("Shards failed to init", e);
    }
  }

  private static void initShards(MinecraftClient mc) {
    // -Dmonumentum.api=http://localhost:8080/ points a dev client at a local server
    ApiHost host =
        new ApiHost(
            URI.create(System.getProperty("monumentum.api", DEFAULT_API)), "/data/monumentum/");
    ShardCatalog catalog = new ShardCatalog(host, mc);

    WorldWatcher world = new WorldWatcher();
    TablistWatcher tablist = new TablistWatcher();

    MonumentumApi.register(ShardApi.class, ShardService.create(catalog, world, tablist));

    world.register();
    tablist.register();
    catalog
        .load()
        .exceptionally(
            t -> {
              LOGGER.error("No shard data available", t);
              return null;
            });
  }
}
