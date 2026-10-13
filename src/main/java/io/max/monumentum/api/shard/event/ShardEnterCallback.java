package io.max.monumentum.api.shard.event;

import io.max.monumentum.api.shard.ShardContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called once when the player enters a shard, after its context is fully known.
 *
 * <p>Unlike {@link ShardChangeCallback}, it is not called for intermediate steps of a shard switch.
 * It runs once the shard, shard number, and world are all known ({@link
 * ShardContext#isComplete()}), usually within half a second of the new world appearing.
 *
 * @apiNote
 *     <p>World changes within the same shard, such as entering a strike, also trigger this event.
 *     Use {@link ShardContext#content()} to identify the location.
 *     <p>Every {@link ShardLeaveCallback} is matched with an earlier call to this event.
 * @implNote
 *     <p>If the shard number never becomes known, this event is not called. Use {@link
 *     ShardChangeCallback} if you do not need the shard number.
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
