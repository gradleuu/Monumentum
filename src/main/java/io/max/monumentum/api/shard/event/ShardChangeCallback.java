package io.max.monumentum.api.shard.event;

import io.max.monumentum.api.shard.ShardContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when the shard, shard number, or world changes, or when the shard list finishes loading
 * and a shard becomes <i>known</i>.
 *
 * @apiNote
 *     <p>A shard switch usually triggers this twice: first when the player leaves and the context
 *     becomes {@code UNKNOWN}, then when the new shard is known. On first join, the world may
 *     appear before the shard is identified, causing an extra update.
 *     <p>World changes within the same shard also trigger this. Compare {@code previous} and {@code
 *     current} to see what changed.
 * @implNote
 *     <p>Most mods only need {@link ShardEnterCallback} and {@link ShardLeaveCallback}. Use this
 *     callback to observe every intermediate change.
 *     <p>When events occur together, this callback runs first, followed by the leave and enter
 *     callbacks.
 */
@FunctionalInterface
public interface ShardChangeCallback {
  /**
   * Registers listeners for shard change:
   *
   * <pre>{@code
   * ShardChangeCallback.EVENT.register(previous, current -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<ShardChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          ShardChangeCallback.class,
          listeners ->
              (previous, current) -> {
                for (ShardChangeCallback l : listeners) {
                  l.onChange(previous, current);
                }
              });

  /**
   * @param previous the state before the change
   * @param current the state now
   * @apiNote Called on the client thread, so do not block. Do slow work somewhere else.
   */
  void onChange(ShardContext previous, ShardContext current);
}
