package io.max.monumentum.api.effect.event;

import io.max.monumentum.api.effect.Effect;
import java.util.List;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * @implNote
 *     <p>It is <b>not</b> called when the server only renews an effect, and not for the countdown.
 *     Read {@link Effect#remainingTicks()} when you need the time left.
 */
@FunctionalInterface
public interface EffectsChangeCallback {
  /**
   * Registers listeners for effects change:
   *
   * <pre>{@code
   * EffectsChangeCallback.EVENT.register(previous, current -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<EffectsChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          EffectsChangeCallback.class,
          listeners ->
              (previous, current) -> {
                for (EffectsChangeCallback l : listeners) {
                  l.onChange(previous, current);
                }
              });

  /**
   * Called on the client thread, so do not block.
   *
   * @param previous the effects before the change
   * @param current the effects now
   */
  void onChange(List<Effect> previous, List<Effect> current);
}
