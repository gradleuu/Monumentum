package io.max.monumentum.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@FunctionalInterface
public interface TablistHeaderCallback {
  Event<TablistHeaderCallback> EVENT =
      EventFactory.createArrayBacked(
          TablistHeaderCallback.class,
          listeners ->
              header -> {
                for (TablistHeaderCallback l : listeners) {
                  l.onHeader(header);
                }
              });

  void onHeader(@Nullable Text header);
}
