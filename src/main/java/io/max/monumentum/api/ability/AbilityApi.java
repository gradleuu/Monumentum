package io.max.monumentum.api.ability;

import java.util.List;
import java.util.Optional;

public interface AbilityApi {
  /**
   * Gives the abilities the player has right now.
   *
   * @return the abilities in order of name, never {@code null}. Empty before the server has said
   *     anything, and after the player disconnects.
   * @apiNote Changing shard does not clear it.
   * @implNote
   *     <p>The list never changes after you get it, but each ability's remaining times keep
   *     counting down.
   */
  List<Ability> abilities();

  /**
   * @param name the ability's name, like {@code "Swiftness"}
   * @return the ability, or empty if the player does not have it
   */
  Optional<Ability> ability(String name);
}
