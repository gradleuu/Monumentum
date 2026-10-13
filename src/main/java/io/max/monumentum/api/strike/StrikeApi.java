package io.max.monumentum.api.strike;

public interface StrikeApi {
  /**
   * Gives the current chest numbers.
   *
   * @implNote
   *     <p>The result never changes after you get it.
   * @return The current number, never {@code null}. It is {@link StrikeChests#NONE} when the player
   *     is not in a strike, or when the server hasn't sent anything yet. The numbers usually arrive
   *     right as the strike world does.
   */
  StrikeChests chests();
}
