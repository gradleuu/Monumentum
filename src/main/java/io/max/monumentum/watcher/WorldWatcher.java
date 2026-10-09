package io.max.monumentum.watcher;

import io.max.monumentum.api.shard.WorldId;
import io.max.monumentum.client.event.WorldChangeCallback;
import io.max.monumentum.util.Signal;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

public final class WorldWatcher extends Signal<WorldId> {
  public WorldWatcher() {
    super(WorldId.UNKNOWN);
  }

  public void register() {
    WorldChangeCallback.EVENT.register(this::onWorld);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> emit(WorldId.UNKNOWN));
  }

  private void onWorld(@Nullable ClientWorld world) {
    emit(
        world == null
            ? WorldId.UNKNOWN
            : WorldId.parse(world.getRegistryKey().getValue().getPath()));
  }
}
