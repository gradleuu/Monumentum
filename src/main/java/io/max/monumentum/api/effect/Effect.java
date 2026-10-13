package io.max.monumentum.api.effect;

import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;

/**
 * @apiNote
 *     <p>An effect never changes after you get it, except that {@link #remainingTicks()} counts
 *     down by itself. When the server renews an effect, you get a new object for it in {@link
 *     EffectApi#effects()}.
 *     <p>Two effects are equal if they look the same. The remaining time is <b>not</b> compared.
 */
public final class Effect {
  private static final long TICK_NANOS = 50_000_000L;

  private final String id;
  private final String name;
  private final double power;
  private final boolean positive;
  private final boolean percentage;
  private final int displayPriority;
  private final long expiresAtNanos;

  @ApiStatus.Internal
  public Effect(
      String id,
      String name,
      double power,
      boolean positive,
      boolean percentage,
      int displayPriority,
      int durationTicks,
      long nowNanos) {
    this.id = Objects.requireNonNull(id, "id");
    this.name = Objects.requireNonNull(name, "name");
    this.power = power;
    this.positive = positive;
    this.percentage = percentage;
    this.displayPriority = displayPriority;
    this.expiresAtNanos = nowNanos + durationTicks * TICK_NANOS;
  }

  /**
   * @return an ID that stays the same for the same effect from the same source. Use this to tell
   *     effects apart. Two different effects can have the same {@link #name()}.
   */
  public String id() {
    return id;
  }

  /**
   * @return the name shown to the player, like {@code Speed}
   */
  public String name() {
    return name;
  }

  /**
   * @return how strong the effect is. If {@link #percentage()} is {@code true}, this is already a
   *     percentage ({@code 12.5} means 12.5%). It is {@code 0} when the effect has no strength to
   *     show.
   */
  public double power() {
    return power;
  }

  /**
   * @return {@code true} if the effect is good for the player, {@code false} if it is bad
   */
  public boolean positive() {
    return positive;
  }

  /**
   * @return {@code true} if {@link #power()} should be shown as a percentage
   */
  public boolean percentage() {
    return percentage;
  }

  /**
   * @return how early the effect should be shown. Higher numbers go first. Can be negative.
   */
  public int displayPriority() {
    return displayPriority;
  }

  /**
   * How many game ticks (20 per second) are left, counted from when the server last told us. This
   * counts down by itself, so you can call it every frame.
   *
   * <p>The server removes the effect when it ends. This number can reach {@code 0} a moment before
   * that happens.
   *
   * @return the ticks left, never below {@code 0}
   */
  public int remainingTicks() {
    long left = expiresAtNanos - System.nanoTime();
    return left <= 0 ? 0 : (int) ((left + TICK_NANOS - 1) / TICK_NANOS);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Effect e
        && id.equals(e.id)
        && name.equals(e.name)
        && Double.compare(power, e.power) == 0
        && positive == e.positive
        && percentage == e.percentage
        && displayPriority == e.displayPriority;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, power, positive, percentage, displayPriority);
  }

  @Override
  public String toString() {
    return "Effect[" + name + " " + power + (percentage ? "%" : "") + " id=" + id + "]";
  }
}
