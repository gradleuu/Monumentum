package io.max.monumentum.api.playerclass;

public interface ClassApi {
  /**
   * Gives the current player's class.
   *
   * @apiNote
   *     <p>The result never changes after you get it.
   * @return the class, never {@code null}. It is {@link ClassInfo#NONE} until the server has said
   *     which class the player has, and after the player disconnects. Changing shard does not reset
   *     it.
   */
  ClassInfo current();
}
