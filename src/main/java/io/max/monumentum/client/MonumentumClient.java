package io.max.monumentum.client;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.Monumentum;
import io.max.monumentum.api.MonumentumApi;
import io.max.monumentum.api.MonumentumEntrypoint;
import io.max.monumentum.api.data.ApF;
import io.max.monumentum.api.data.ApiSources;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.event.ShardChangeCallback;
import io.max.monumentum.api.shard.event.ShardEnterCallback;
import io.max.monumentum.api.shard.event.ShardLeaveCallback;
import io.max.monumentum.command.ShardCommand;
import io.max.monumentum.data.ApiHost;
import io.max.monumentum.data.ApiLocation;
import io.max.monumentum.data.ShardCatalog;
import io.max.monumentum.data.SourceRegistry;
import io.max.monumentum.net.ClientChannel;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.service.ShardService;
import io.max.monumentum.watcher.LocationWatcher;
import io.max.monumentum.watcher.TablistWatcher;
import io.max.monumentum.watcher.WorldWatcher;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;

public final class MonumentumClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    MessageRouter router = new MessageRouter();
    new ClientChannel(router).start();

    initShards(MinecraftClient.getInstance(), router);
  }

  private static ApiHost createHost(String baseUrlOverride, List<String> shardMirrors) {
    SourceRegistry sources = new SourceRegistry();

    // sorted by mod id so the order of sources is the same on every run
    FabricLoader.getInstance()
        .getEntrypointContainers(Monumentum.MOD_ID, MonumentumEntrypoint.class)
        .stream()
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

    // -Dmonumentum.api=http://localhost:8080/ points a dev client at a local server
    ApiSources user = sources.forMod("monumentum-config");
    for (String mirror : shardMirrors) {
      try {
        user.append(ApF.SHARDS, URI.create(mirror.trim()));
      } catch (IllegalArgumentException e) { // not https, or not a valid address
        LOGGER.warn("Ignoring invalid shard mirror in config: {}", mirror);
      }
    }
    sources.freeze();

    return new ApiHost(ApiLocation.resolve(baseUrlOverride), "/data/monumentum/", sources);
  }

  private static void initShards(MinecraftClient mc, MessageRouter router) {
    ApiHost host =
        createHost(Monumentum.CONFIG.api.baseUrl(), Monumentum.CONFIG.api.shardMirrors());
    ShardCatalog catalog = new ShardCatalog(host, mc);
    WorldWatcher world = new WorldWatcher();
    LocationWatcher location = new LocationWatcher();
    TablistWatcher tablist = new TablistWatcher();

    ShardApi shardApi = ShardService.create(catalog, world, location, tablist);

    MonumentumApi.register(ShardApi.class, shardApi);
    // 🍝
    ShardCommand.register(shardApi);

    world.register();
    location.register(router);
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
