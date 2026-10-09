package io.max.monumentum.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@FunctionalInterface
public interface WorldChangeCallback {
  Event<WorldChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          WorldChangeCallback.class,
          listeners ->
              world -> {
                for (WorldChangeCallback l : listeners) {
                  l.onWorldChange(world);
                }
              });

  /** {@code world} is null when the client leaves the world. */
  void onWorldChange(@Nullable ClientWorld world);
}
