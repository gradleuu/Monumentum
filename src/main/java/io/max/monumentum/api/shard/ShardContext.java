package io.max.monumentum.api.shard;

/**
 * @implNote
 *     <p>This is a snapshot: it never changes after it is created. When the player moves, a new
 *     snapshot is made. Read all the values from the same snapshot so they match each other.
 *     <p>There are two different kinds of "place" here:
 *     <ul>
 *       <li>The <b>shard</b> is the server the player is on, like {@code ring-4}. {@link #shard()}
 *           gives information about it and {@link #shardId()} gives its name and number.
 *       <li>The <b>content</b> is a special place inside a shard, like a strike. The player can
 *           enter and leave it without changing shard. {@link #content()} gives information about
 *           it. It is {@link Shard#UNKNOWN} when the player is not in any known special place.
 *     </ul>
 *     <p>Where each value comes from:
 *     <ul>
 *       <li>{@link #shardId()} comes from the server, which tells the client where the player is.
 *           If that message is missing, the tablist is used instead.
 *       <li>{@link #shard()} is the shard list entry for the name in {@link #shardId()}.
 *       <li>{@link #world()} and {@link #content()} come from the world the player is in. The world
 *           can change without the shard changing, for example when entering a strike.
 *     </ul>
 *     <p>Nothing here is ever {@code null}. When something is not known, you get its {@code
 *     UNKNOWN} value instead. When the player is in no world at all (for example, in between two
 *     shards), everything is {@code UNKNOWN}.
 * @param shard information about the shard the player is on, or {@link Shard#UNKNOWN}
 * @param shardId the shard name and shard number, or {@link ShardId#UNKNOWN}
 * @param world the world name and world number, or {@link WorldId#UNKNOWN}
 * @param content information about the special place the player is in, or {@link Shard#UNKNOWN}
 */
public record ShardContext(Shard shard, ShardId shardId, WorldId world, Shard content) {
  public static final ShardContext UNKNOWN =
      new ShardContext(Shard.UNKNOWN, ShardId.UNKNOWN, WorldId.UNKNOWN, Shard.UNKNOWN);

  /**
   * @return {@code true} if the shard, the shard number and the world are all known. The content is
   *     not checked.
   */
  public boolean isComplete() {
    return !shard.isUnknown() && !ShardId.UNKNOWN.equals(shardId) && !WorldId.UNKNOWN.equals(world);
  }
}
