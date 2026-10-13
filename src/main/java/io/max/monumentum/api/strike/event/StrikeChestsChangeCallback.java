package io.max.monumentum.api.strike.event;

import io.max.monumentum.api.strike.StrikeChests;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when the chest numbers change:
 * <li>When a strike starts
 * <li>When a chest is opened
 * <li>When player leaves the strike.
 */
@FunctionalInterface
public interface StrikeChestsChangeCallback {
  /**
   * Registers listeners for chests in strike change:
   *
   * <pre>{@code
   * StrikeChestsChangeCallback.EVENT.register(previous, current -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<StrikeChestsChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          StrikeChestsChangeCallback.class,
          listeners ->
              (previous, current) -> {
                for (StrikeChestsChangeCallback l : listeners) {
                  l.onChange(previous, current);
                }
              });

  /**
   * Called on the client thread, so do not block.
   *
   * @param previous the numbers before the change
   * @param current the numbers now
   */
  void onChange(StrikeChests previous, StrikeChests current);
}
