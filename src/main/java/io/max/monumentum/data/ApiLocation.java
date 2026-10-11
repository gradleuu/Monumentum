package io.max.monumentum.data;

import static io.max.monumentum.Monumentum.LOGGER;

import java.net.URI;
import org.jetbrains.annotations.Nullable;

public final class ApiLocation {
  public static final URI DEFAULT = URI.create("https://gradleuu.github.io/api/monumentum/");

  private ApiLocation() {}

  // dev property -> config override -> built-in
  public static URI resolve(@Nullable String configOverride) {
    // any scheme
    String dev = System.getProperty("monumentum.api");
    if (dev != null && !dev.isBlank()) {
      try {
        return URI.create(dev.trim());
      } catch (IllegalArgumentException e) {
        LOGGER.warn("invalid -Dmonumentum.api value: {}", dev);
      }
    }
    if (configOverride != null && !configOverride.isBlank()) {
      URI uri = parseHttps(configOverride.trim());
      if (uri != null) {
        return uri;
      }
      LOGGER.warn("Ignoring invalid API address in config, USE \"https://\": {}", configOverride);
    }
    return DEFAULT;
  }

  private static @Nullable URI parseHttps(String s) {
    try {
      URI uri = URI.create(s);
      return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null ? uri : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
