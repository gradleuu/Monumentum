package io.max.monumentum.api.status.event;

import io.max.monumentum.api.status.PlayerStatus;
import io.max.monumentum.api.status.StatusChange;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when the player becomes silenced or stops being silenced.
 *
 * @implNote
 *     <p>It is <b>not</b> called when a silence is renewed or made longer while it is already
 *     running, and not for the countdown. Read {@link PlayerStatus#silenceRemainingTicks()} when
 *     you need the time left.
 * @apiNote
 *     <p>The "stopped" call comes when the time runs out, up to one tick late.
 */
@FunctionalInterface
public interface StatusChangeCallback {
  /**
   * Registers listeners for status change:
   *
   * <pre>{@code
   * StatusChangeCallback.EVENT.register(change -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<StatusChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          StatusChangeCallback.class,
          listeners ->
              change -> {
                for (StatusChangeCallback l : listeners) {
                  l.onChange(change);
                }
              });

  /**
   * @param change what changed
   */
  void onChange(StatusChange change);
}
