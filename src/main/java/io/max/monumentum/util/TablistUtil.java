package io.max.monumentum.util;

import io.max.monumentum.api.shard.ShardId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public final class TablistUtil {
  // proxy: <proxyId> shard: <shardId>
  private static final Pattern HEADER_SHARD = Pattern.compile("shard:\\s*<([^>]*)>");

  private TablistUtil() {}

  public static ShardId parseShard(@Nullable String header) {
    if (header == null) {
      return ShardId.UNKNOWN;
    }
    Matcher m = HEADER_SHARD.matcher(header);
    if (!m.find() || m.group(1).isBlank()) {
      return ShardId.UNKNOWN;
    }
    return ShardId.parse(m.group(1).trim());
  }
}
