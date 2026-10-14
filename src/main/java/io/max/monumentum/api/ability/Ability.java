package io.max.monumentum.api.ability;

import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * What one ability is doing right now: how much cooldown is left, how many charges it has, and
 * whether it is active.
 *
 * @apiNote
 *     <p>This never changes after you get it, except that the remaining times count down by
 *     themselves. When the server tells us something new - therefore you get a new object.
 *     <p>The fixed numbers (full cooldown, most charges) are in {@link
 *     io.max.monumentum.api.playerclass.AbilityInfo}.
 * @implNote Abilities have no ID, so tell them apart by {@link #name()}.
 */
public final class Ability {
  private static final long TICK_NANOS = 50_000_000L;
  private static final long NONE = Long.MIN_VALUE;

  private final String name;
  private final int charges;
  private final Optional<String> mode;
  private final long cooldownEndsNanos;
  private final long durationEndsNanos;

  @ApiStatus.Internal
  public Ability(
      String name,
      int charges,
      @Nullable String mode,
      int remainingCooldownTicks,
      int remainingDurationTicks,
      long nowNanos) {
    this.name = Objects.requireNonNull(name, "name");
    this.charges = charges;
    this.mode = Optional.ofNullable(mode).filter(m -> !m.isBlank());
    this.cooldownEndsNanos =
        remainingCooldownTicks > 0 ? nowNanos + remainingCooldownTicks * TICK_NANOS : NONE;
    this.durationEndsNanos =
        remainingDurationTicks > 0 ? nowNanos + remainingDurationTicks * TICK_NANOS : NONE;
  }

  /**
   * @return the ability's name, like {@code "Swiftness"}
   */
  public String name() {
    return name;
  }

  /**
   * @return how many charges the ability has now. {@code 0} for an ability that has no charges.
   */
  public int charges() {
    return charges;
  }

  /**
   * @return the ability's current mode, if it has one. The Monumenta usually decides what the modes
   *     are.
   */
  public Optional<String> mode() {
    return mode;
  }

  /**
   * How many game ticks (20 per second) of cooldown are left, counted from when the server last
   * told us.
   *
   * @apiNote Counts down by itself.
   * @return the ticks left, {@code 0} when the ability is not on cooldown
   */
  public int remainingCooldownTicks() {
    return remaining(cooldownEndsNanos, System.nanoTime());
  }

  /**
   * @return {@code true} if {@link #remainingCooldownTicks()} is more than {@code 0}
   */
  public boolean isOnCooldown() {
    return remainingCooldownTicks() > 0;
  }

  /**
   * How many game ticks (20 per second) the ability still runs, for abilities that last for a
   * while.
   *
   * @apiNote Counts down by itself.
   * @return the ticks left, {@code 0} when the ability is not running
   */
  public int remainingDurationTicks() {
    return remaining(durationEndsNanos, System.nanoTime());
  }

  /**
   * @return {@code true} if the ability is active, which means {@link #remainingDurationTicks()} is
   *     more than {@code 0}
   */
  public boolean isActive() {
    return remainingDurationTicks() > 0;
  }

  @ApiStatus.Internal
  public boolean onCooldownAt(long nanos) {
    return cooldownEndsNanos != NONE && cooldownEndsNanos > nanos;
  }

  @ApiStatus.Internal
  public boolean activeAt(long nanos) {
    return durationEndsNanos != NONE && durationEndsNanos > nanos;
  }

  private static int remaining(long endsNanos, long nowNanos) {
    if (endsNanos == NONE) {
      return 0;
    }
    long left = endsNanos - nowNanos;
    return left <= 0 ? 0 : (int) ((left + TICK_NANOS - 1) / TICK_NANOS);
  }

  @Override
  public String toString() {
    return "Ability["
        + name
        + " charges="
        + charges
        + " cd="
        + remainingCooldownTicks()
        + " dur="
        + remainingDurationTicks()
        + "]";
  }
}
