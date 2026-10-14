package io.max.monumentum.api.ability.event;

import io.max.monumentum.api.ability.Ability;
import io.max.monumentum.api.ability.AbilityChange;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Called when an ability is added or removed, starts or stops running, enters or leaves cooldown,
 * or changes its charges or mode.
 *
 * @apiNote
 *     <p>Not called as cooldowns or durations count down, or when an existing cooldown restarts.
 *     Use {@link Ability#remainingCooldownTicks()} and {@link Ability#remainingDurationTicks()} to
 *     check the time left.
 * @implNote
 *     <p>Changes are reported once per game tick, grouped together. Switching classes triggers one
 *     call per ability in the same tick.
 *     <p>If the server does not report when an ability or cooldown ends, the corresponding event
 *     may be delayed by up to ~2 (maybe) ticks.
 */
@FunctionalInterface
public interface AbilityChangeCallback {
  /**
   * Registers listeners for ability change:
   *
   * <pre>{@code
   * AbilityChangeCallback.EVENT.register(change -> {
   *    // do something
   * });
   * }</pre>
   */
  Event<AbilityChangeCallback> EVENT =
      EventFactory.createArrayBacked(
          AbilityChangeCallback.class,
          listeners ->
              change -> {
                for (AbilityChangeCallback l : listeners) {
                  l.onChange(change);
                }
              });

  /**
   * @param change what changed
   */
  void onChange(AbilityChange change);
}
