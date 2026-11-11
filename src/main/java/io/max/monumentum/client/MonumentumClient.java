package io.max.monumentum.client;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.Monumentum;
import io.max.monumentum.api.MonumentumApi;
import io.max.monumentum.api.MonumentumEntrypoint;
import io.max.monumentum.api.ability.AbilityApi;
import io.max.monumentum.api.data.ApF;
import io.max.monumentum.api.data.ApiSources;
import io.max.monumentum.api.effect.EffectApi;
import io.max.monumentum.api.playerclass.ClassApi;
import io.max.monumentum.api.server.ServerInfoApi;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.status.StatusApi;
import io.max.monumentum.api.strike.StrikeApi;
import io.max.monumentum.command.ShardCommand;
import io.max.monumentum.config.ApiOptions;
import io.max.monumentum.data.ApiHost;
import io.max.monumentum.data.ApiLocation;
import io.max.monumentum.data.ShardCatalog;
import io.max.monumentum.data.SourceRegistry;
import io.max.monumentum.feature.Features;
import io.max.monumentum.hud.MonumentumHud;
import io.max.monumentum.net.ClientChannel;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.service.AbilityService;
import io.max.monumentum.service.ClassService;
import io.max.monumentum.service.EffectService;
import io.max.monumentum.service.ServerInfoService;
import io.max.monumentum.service.ShardService;
import io.max.monumentum.service.StatusService;
import io.max.monumentum.service.StrikeChestService;
import io.max.monumentum.util.Ticker;
import io.max.monumentum.watcher.LocationWatcher;
import io.max.monumentum.watcher.TablistWatcher;
import io.max.monumentum.watcher.WorldWatcher;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class MonumentumClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    MessageRouter router = new MessageRouter();
    new ClientChannel(router).start();

    MonumentumApi.register(ServerInfoApi.class, ServerInfoService.create(router));
    MonumentumApi.register(EffectApi.class, EffectService.create(router));
    MonumentumApi.register(ClassApi.class, ClassService.create(router));
    MonumentumApi.register(AbilityApi.class, AbilityService.create(router));
    MonumentumApi.register(StatusApi.class, StatusService.create(router));
    initShards(MinecraftClient.getInstance(), router);
    initFeatures();
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
    ApiOptions api = Monumentum.CONFIG.get().api;
    ApiHost host = createHost(api.baseUrl, api.shardMirrors);
    ShardCatalog catalog = new ShardCatalog(host, mc);
    WorldWatcher world = new WorldWatcher();
    LocationWatcher location = new LocationWatcher();
    TablistWatcher tablist = new TablistWatcher();

    ShardService shards = ShardService.create(catalog, world, location, tablist);

    MonumentumApi.register(ShardApi.class, shards);
    // 🍝
    ShardCommand.register(shards);
    MonumentumApi.register(StrikeApi.class, StrikeChestService.create(shards, router));

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

  private static void initFeatures() {
    MonumentumHud hud = new MonumentumHud();
    hud.startTicking();
    Ticker ticker = new Ticker();
    ticker.start();
    Features.startAll(hud, ticker);

    KeyBinding openHudEditor =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.monumentum.hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.monumentum"));
    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          while (openHudEditor.wasPressed()) {
            hud.openEditor();
          }
        });
  }
}
