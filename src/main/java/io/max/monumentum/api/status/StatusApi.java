package io.max.monumentum.api.status;

public interface StatusApi {
  /**
   * Gives the player's states right now.
   *
   * @return the states, never {@code null}. It is {@link PlayerStatus#NONE} until the server has
   *     told something else, and after the player disconnects.
   * @apiNote Changing shard does not reset it.
   */
  PlayerStatus status();
}
