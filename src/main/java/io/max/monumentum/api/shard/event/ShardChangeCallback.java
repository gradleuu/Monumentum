package io.max.monumentum.api.shard.event;

import io.max.monumentum.api.shard.ShardContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when the shard, the shard number or the world changes, or when the shard list finishes
 * loading and a shard becomes <i>known</i>.
 *
 * @apiNote
 *     <p>It can be called twice for one shard switch. The world changes first. The tablist follows
 *     a moment later and can change the shard number. Compare {@code previous} and {@code current}
 *     to see what changed.
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
