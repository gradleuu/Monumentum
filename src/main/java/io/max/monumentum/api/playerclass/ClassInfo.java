package io.max.monumentum.api.playerclass;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The player's class, and the abilities that the server tells.
 *
 * @apiNote
 *     <p>The server does not send the class name by itself. It is worked out from the abilities:
 *     the class name most of them share. If no ability has a class name, the name is {@value
 *     #UNKNOWN_NAME}.
 *     <p>The list is not every skill of the class. The server only sends abilities that have a
 *     cooldown or charges, plus a few special passive ones.
 * @param name the class name, like {@code "Scout"}, or {@value #UNKNOWN_NAME}
 * @param abilities the abilities, in order of name.
 */
public record ClassInfo(String name, List<AbilityInfo> abilities) {
  public static final String UNKNOWN_NAME = "unknown";

  public static final ClassInfo NONE = new ClassInfo("none", List.of());

  public ClassInfo {
    Objects.requireNonNull(name, "name");
    abilities = List.copyOf(abilities);
  }

  /**
   * @param name the ability's name
   * @return the fixed information about that ability, or empty if the player does not have it
   */
  public Optional<AbilityInfo> ability(String name) {
    return abilities.stream().filter(a -> a.name().equals(name)).findFirst();
  }

  /**
   * @return {@code true} if this is {@link #NONE}: before the server has said anything, and after
   *     the player disconnects
   */
  public boolean isNone() {
    return NONE.equals(this);
  }
}
