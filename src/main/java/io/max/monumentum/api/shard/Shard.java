package io.max.monumentum.api.shard;

import java.util.OptionalInt;

/**
 * @apiNote
 *     <p>This is data from the shard list. It does <b>not</b> say which shard the player is in
 *     right now. For that, use {@link ShardApi#context()}.
 * @param id The short shard name, like {@code "ring"} or {@code "playerplots"}. This is the same
 *     name that starts a world name {@code ring123123} and the tablist name {@code ring-2}.
 * @param displayName The display name of this shard, like {@code "Architect's Ring"}
 * @param type What type of shard this is
 * @param maxChests How many chests the shard has at most. Empty means "doesn't apply" or "not
 *     known"; there is no way to tell which.
 * @param emoji The for-monumentum character for this shard.
 */
public record Shard(
        String id, String displayName, ShardType type, OptionalInt maxChests, String emoji) {

  /** Used instead of {@code null}. */
  public static final Shard UNKNOWN =
      new Shard("unknown", "Unknown", ShardType.OVERWORLD, OptionalInt.empty(), "\uE000");

  /**
   * @return {@code true} if this is {@link #UNKNOWN}.
   */
  public boolean isUnknown() {
    return UNKNOWN.equals(this);
  }
}
