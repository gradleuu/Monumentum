package io.max.monumentum.api.playerclass;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Fixed information about one ability the player has.
 *
 * @apiNote
 *     <p>What the ability is doing right now, like how much cooldown is left, is not here.
 *     Abilities have no ID. They are told apart by {@link #name()}.
 * @param name the ability's name, like {@code Swiftness}. Never empty.
 * @param className the class this ability belongs to, if the server said
 * @param cooldownTicks the full cooldown in game ticks (20 per second), with the player's bonuses.
 *     {@code 0} means no cooldown.
 * @param maxCharges the most charges the ability can hold. {@code 0} means it has no charges.
 * @param durationTicks how long the ability lasts in game ticks, if it lasts for a while
 */
public record AbilityInfo(
    String name,
    Optional<String> className,
    int cooldownTicks,
    int maxCharges,
    OptionalInt durationTicks) {
  public AbilityInfo {
    Objects.requireNonNull(name, "name");
  }
}
