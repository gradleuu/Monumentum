package io.max.monumentum.api.playerclass;

import java.util.Objects;

public record Specialization(String name, String firstAbility, String secondAbility) {
  public Specialization {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(firstAbility, "1");
    Objects.requireNonNull(secondAbility, "2");
  }
}