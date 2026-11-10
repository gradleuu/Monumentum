package io.max.monumentum.api.playerclass;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public enum PlayerClass {
  ALCHEMIST(
          "Alchemist",
          new Specialization("Harbinger", "Taboo", "Scorched Earth"),
          new Specialization("Apothecary", "Panacea", "Transmutation Ring")),
  CLERIC(
          "Cleric",
          new Specialization("Paladin", "Holy Javelin", "Choir Bells"),
          new Specialization("Seraph", "Ethereal Ascension", "Hallowed Beam")),
  MAGE(
          "Mage",
          new Specialization("Arcanist", "Astral Omen", "Cosmic Moonblade"),
          new Specialization("Elementalist", "Blizzard", "Elemental Spirits")),
  ROGUE(
          "Rogue",
          new Specialization("Swordsage", "Blade Dance", "Deadly Ronde"),
          new Specialization("Assassin", "Bodkin Blitz", "Cloak and Dagger")),
  SCOUT(
          "Scout",
          new Specialization("Ranger", "Gale Shot", "Rending Razor"),
          new Specialization("Hunter", "Lockdown", "Quiver Storm")),
  SHAMAN(
          "Shaman",
          new Specialization("Soothsayer", "Spiritual Combos", "Whirlwind Totem"),
          new Specialization("Hexbreaker", "Spiritcatcher Orbs", "Decayed Totem")),
  WARLOCK(
          "Warlock",
          new Specialization("Reaper", "Dark Pact", "Judgement Chain"),
          new Specialization("Tenebrist", "Haunting Shades", "Restless Souls")),
  WARRIOR(
          "Warrior",
          new Specialization("Berserker", "Glorious Battle", "Meteor Slam"),
          new Specialization("Guardian", "Bodyguard", "Challenge"));

  private final String serverName;
  private final List<Specialization> specializations;

  PlayerClass(String serverName, Specialization first, Specialization second) {
    this.serverName = serverName;
    this.specializations = List.of(first, second);
  }

  /**
   * @return the class name the server uses, which is also the name shown to players
   */
  public String serverName() {
    return serverName;
  }

  /**
   * @return the two specializations of this class
   */
  public List<Specialization> specializations() {
    return specializations;
  }

  /**
   * Finds a class from the name the server uses. Upper and lower case do not matter.
   *
   * @param name the class name from the server, or {@code null}
   * @return the class, or empty if the name is {@code null} or not one of the eight
   */
  public static Optional<PlayerClass> fromServerName(@Nullable String name) {
    if (name == null) {
      return Optional.empty();
    }
    for (PlayerClass playerClass : values()) {
      if (playerClass.serverName.equalsIgnoreCase(name)) {
        return Optional.of(playerClass);
      }
    }
    return Optional.empty();
  }
}
