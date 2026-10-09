package io.max.monumentum.api.shard.event;

import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.ShardContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called once each time the player leaves a shard that was reported by {@link ShardEnterCallback}.
 *
 * @apiNote
 *     <p>While this runs, {@link ShardApi#context()} already shows the <b>new</b> state. The shard
 *     the player just left is the {@code context} you receive.
 * @implNote
 *     <p>Use it to clean up or save things you started in {@link ShardEnterCallback}. It is also
 *     called when the player disconnects, and when the player moves to a different world or shard
 *     number.
 */
@FunctionalInterface
public interface ShardLeaveCallback {
  /**
   * Registers listeners for shard "departures":
   *
   * <pre>{@code
   * ShardLeaveCallback.EVENT.register(context -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<ShardLeaveCallback> EVENT =
      EventFactory.createArrayBacked(
          ShardLeaveCallback.class,
          listeners ->
              context -> {
                for (ShardLeaveCallback l : listeners) {
                  l.onLeave(context);
                }
              });

  /**
   * @param context where the player was. All parts are known.
   */
  void onLeave(ShardContext context);
}
