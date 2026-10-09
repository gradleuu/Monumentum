package io.max.monumentum.api.shard.event;

import io.max.monumentum.api.shard.ShardContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called once each time the player arrives in a shard, after everything about it is known.
 *
 * <p>Unlike {@link ShardChangeCallback}, it is not called for the in-between steps of a shard
 * switch. It is called once the shard, the shard number and the world are all known ({@link
 * ShardContext#isComplete()}), which is about a second after the player transfers.
 *
 * <p>If the shard number never becomes known, this is not called for that arrival. Use {@link
 * ShardChangeCallback} if you only need the shard and not it's number.
 *
 * @apiNote
 *     <p>Every call to {@link ShardLeaveCallback} is matched with an earlier call to this event.
 */
@FunctionalInterface
public interface ShardEnterCallback {
  /**
   * Registers listeners for completed shard transfer:
   *
   * <pre>{@code
   * ShardEnterCallback.EVENT.register(context -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<ShardEnterCallback> EVENT =
      EventFactory.createArrayBacked(
          ShardEnterCallback.class,
          listeners ->
              context -> {
                for (ShardEnterCallback l : listeners) {
                  l.onEnter(context);
                }
              });

  /**
   * @param context where the player is now. All parts are known.
   */
  void onEnter(ShardContext context);
}
