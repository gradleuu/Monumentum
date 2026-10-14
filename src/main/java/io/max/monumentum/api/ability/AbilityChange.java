package io.max.monumentum.api.ability;

import java.util.Optional;
import java.util.Set;

/**
 * @param name the ability's name
 * @param previous the ability before the change. Empty if it was just added.
 * @param current the ability after the change. Empty if it was just removed.
 * @param kinds what changed. Never empty.
 */
public record AbilityChange(
    String name,
    Optional<Ability> previous,
    Optional<Ability> current,
    Set<AbilityChange.Kind> kinds) {

  /** What can change about an ability. */
  public enum Kind {
    /** The player now has this ability. */
    ADDED,
    /** The player no longer has this ability. */
    REMOVED,
    /** The number of charges changed. */
    CHARGES,
    /** The mode changed. */
    MODE,
    /** The ability started running. */
    ACTIVATED,
    /** The ability stopped running. */
    DEACTIVATED,
    /** The ability went on cooldown. */
    COOLDOWN_STARTED,
    /** The ability came off cooldown. */
    COOLDOWN_ENDED
  }

  public AbilityChange {
    kinds = Set.copyOf(kinds);
  }

  /**
   * @param kind what to look for
   * @return {@code true} if this change includes {@code kind}
   */
  public boolean has(Kind kind) {
    return kinds.contains(kind);
  }
}
