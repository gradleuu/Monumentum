package io.max.monumentum.api.playerclass.event;

import io.max.monumentum.api.playerclass.ClassInfo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when the player's class or any numbers of its abilities change.
 *
 * <p>This is <b>not</b> only for switching class. To react only to a class switch, compare {@code
 * previous.name()} with {@code current.name()}.
 *
 * @apiNote
 *     <p>If the server sends several updates at once, you get one call with the final result.
 *     <p>It is not called for what an ability is doing right now, like cooldown left.
 */
@FunctionalInterface
public interface ClassChangeCallback {
  /**
   * Registers listeners for class change:
   *
   * <pre>{@code
   * ClassChangeCallback.EVENT.register(previous, current -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<ClassChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          ClassChangeCallback.class,
          listeners ->
              (previous, current) -> {
                for (ClassChangeCallback l : listeners) {
                  l.onChange(previous, current);
                }
              });

  /**
   * @param previous the class before the change
   * @param current the class now
   */
  void onChange(ClassInfo previous, ClassInfo current);
}
