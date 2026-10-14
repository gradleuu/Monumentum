package io.max.monumentum.api.status;

/**
 * One change to the player's states.
 *
 * @param kind what changed
 * @param status the states right after the change
 */
public record StatusChange(Kind kind, PlayerStatus status) {
  public enum Kind {
    SILENCED,
    UNSILENCED
  }
}
