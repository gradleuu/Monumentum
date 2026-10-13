package io.max.monumentum.api.server.event;

import io.max.monumentum.api.server.ServerInfo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/** It is <b>not</b> called when the player changes shard. */
@FunctionalInterface
public interface ServerInfoChangeCallback {
  /**
   * Registers listeners:
   *
   * <pre>{@code
   * ServerInfoChangeCallback.EVENT.register(previous, current -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<ServerInfoChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          ServerInfoChangeCallback.class,
          listeners ->
              (previous, current) -> {
                for (ServerInfoChangeCallback l : listeners) {
                  l.onChange(previous, current);
                }
              });

  /**
   * @param previous Information before the change
   * @param current Information now
   * @apiNote Called on the client thread.
   */
  void onChange(ServerInfo previous, ServerInfo current);
}
