package io.max.monumentum.data;

import java.net.URI;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class Endpoints {
  private static final URI BASE = URI.create("https://gradleuu.github.io/api/monumentum/");
  public static final URI SHARDS = BASE.resolve("shards.json");

  private Endpoints() {}
}
