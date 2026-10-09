package io.max.monumentum.api.shard;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WorldId(String name, int instance) {

  /** Used for {@link #instance()} when the world name has no number. */
  public static final int NO_INSTANCE = -1;

  /** Used instead of {@code null}. */
  public static final WorldId UNKNOWN = new WorldId("unknown", NO_INSTANCE);

  private static final Pattern PATTERN = Pattern.compile("^(.*?)(\\d+)?$");

  public WorldId {
    Objects.requireNonNull(name, "name");
  }

  public static WorldId parse(String raw) {
    Matcher matcher = PATTERN.matcher(raw);
    if (matcher.matches() && matcher.group(2) != null && !matcher.group(1).isEmpty()) {
      try {
        return new WorldId(matcher.group(1), Integer.parseInt(matcher.group(2)));
      } catch (NumberFormatException ignored) {
        // digits overflow int: treat whole string as the name
      }
    }
    return new WorldId(raw, NO_INSTANCE);
  }

  /**
   * @return {@code true} if the world name had a number
   */
  public boolean hasInstance() {
    return instance != NO_INSTANCE;
  }
}
