package io.max.monumentum.api.shard;

/**
 * @apiNote Nothing here is ever {@code null}. When something is not <i>known</i>, you get its
 *     {@code UNKNOWN} value instead.
 * @param shard The shard the player is in, or {@link Shard#UNKNOWN}
 * @param shardId The shard name and shard number from the tablist, or {@link ShardId#UNKNOWN}
 * @param world The world name and world number, or {@link WorldId#UNKNOWN}
 */
public record ShardContext(Shard shard, ShardId shardId, WorldId world) {
  public static final ShardContext UNKNOWN =
      new ShardContext(Shard.UNKNOWN, ShardId.UNKNOWN, WorldId.UNKNOWN);

  /**
   * @return {@code true} if the shard, the shard number and the world are all known. This is the
   *     state the player is in once a shard switch has fully finished.
   */
  public boolean isComplete() {
    return !shard.isUnknown() && !ShardId.UNKNOWN.equals(shardId) && !WorldId.UNKNOWN.equals(world);
  }
}
