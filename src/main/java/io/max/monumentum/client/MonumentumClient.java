package io.max.monumentum.client;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.Monumentum;
import io.max.monumentum.api.MonumentumApi;
import io.max.monumentum.api.MonumentumEntrypoint;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.data.ApiHost;
import io.max.monumentum.data.ShardCatalog;
import io.max.monumentum.data.SourceRegistry;
import io.max.monumentum.service.ShardService;
import io.max.monumentum.watcher.TablistWatcher;
import io.max.monumentum.watcher.WorldWatcher;
import java.net.URI;
import java.util.Comparator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
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

  private static ApiHost createHost() {
    SourceRegistry sources = new SourceRegistry();

    // sorted by mod id so the order of sources is the same on every run
    FabricLoader.getInstance().getEntrypointContainers(Monumentum.MOD_ID, MonumentumEntrypoint.class).stream()
        .sorted(
            Comparator.comparing(
                (EntrypointContainer<MonumentumEntrypoint> c) ->
                    c.getProvider().getMetadata().getId()))
        .forEach(
            c -> {
              String modId = c.getProvider().getMetadata().getId();
              try {
                c.getEntrypoint().configureSources(sources.forMod(modId));
              } catch (RuntimeException | LinkageError e) {
                LOGGER.error("Mod '{}' failed in its monumentum entrypoint", modId, e);
              }
            });
    sources.freeze();

    // -Dmonumentum.api=http://localhost:8080/ points a dev client at a local server
    return new ApiHost(
        URI.create(System.getProperty("monumentum.api", DEFAULT_API)),
        "/data/monumentum/",
        sources);
  }

  private static void initShards(MinecraftClient mc) {
    ApiHost host = createHost();
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
