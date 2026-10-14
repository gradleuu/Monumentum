package io.max.monumentum.api.status;

import org.jetbrains.annotations.ApiStatus;

/**
 * States/Statuses of the player.
 *
 * @apiNote
 *     <p>This never changes after you get it, except that the time left counts down by itself. When
 *     the server tells something new, you get a new object.
 */
public final class PlayerStatus {
  private static final long TICK_NANOS = 50_000_000L;
  private static final long NONE_NANOS = Long.MIN_VALUE;

  public static final PlayerStatus NONE = new PlayerStatus(0, System.nanoTime());

  private final long silenceEndsNanos;

  @ApiStatus.Internal
  public PlayerStatus(int silenceTicks, long nowNanos) {
    this.silenceEndsNanos = silenceTicks > 0 ? nowNanos + silenceTicks * TICK_NANOS : NONE_NANOS;
  }

  /**
   * How many game ticks (20 per second) the player stays silenced.
   *
   * @return the ticks left, {@code 0} when the player is not silenced
   */
  public int silenceRemainingTicks() {
    if (silenceEndsNanos == NONE_NANOS) {
      return 0;
    }
    long left = silenceEndsNanos - System.nanoTime();
    return left <= 0 ? 0 : (int) ((left + TICK_NANOS - 1) / TICK_NANOS);
  }

  /**
   * @return {@code true} if the player is silenced right now
   */
  public boolean isSilenced() {
    return silenceRemainingTicks() > 0;
  }

  @Override
  public String toString() {
    return "PlayerStatus[silence=" + silenceRemainingTicks() + "]";
  }
}
