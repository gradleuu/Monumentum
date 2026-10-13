package io.max.monumentum.api.strike;

import java.util.OptionalInt;

/**
 * Total and current chest counts.
 *
 * <p>This comes from the server while the player is in a strike. It <i>can</i> be more up to date than
 * {@link io.max.monumentum.api.shard.Shard#maxChests()}, which is fixed data from the shard list.
 *
 * @param limit the most chests this strike has, or {@link #NO_LIMIT} if not known
 * @param count how many chests the player has opened. Empty means the server did not say.
 */
public record StrikeChests(int limit, OptionalInt count) {
  /** Used for {@link #limit()} when nothing is known. */
  public static final int NO_LIMIT = -1;

  /** Used instead of {@code null} */
  public static final StrikeChests NONE = new StrikeChests(NO_LIMIT, OptionalInt.empty());

  /**
   * @return {@code true} if the limit is known. Check this before using the other values.
   */
  public boolean isKnown() {
    return limit != NO_LIMIT;
  }
}
