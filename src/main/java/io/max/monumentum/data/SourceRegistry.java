package io.max.monumentum.data;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.data.ApiFile;
import io.max.monumentum.api.data.ApiSources;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SourceRegistry {
  private final Map<ApiFile, List<URI>> before = new EnumMap<>(ApiFile.class);
  private final Map<ApiFile, List<URI>> after = new EnumMap<>(ApiFile.class);
  private boolean frozen;

  public ApiSources forMod(String modId) {
    return new ApiSources() {
      @Override
      public void prepend(ApiFile file, URI uri) {
        add(before, "before", modId, file, uri);
      }

      @Override
      public void append(ApiFile file, URI uri) {
        add(after, "after", modId, file, uri);
      }
    };
  }

  public void freeze() {
    frozen = true;
  }

  public List<URI> before(ApiFile file) {
    return List.copyOf(before.getOrDefault(file, List.of()));
  }

  public List<URI> after(ApiFile file) {
    return List.copyOf(after.getOrDefault(file, List.of()));
  }

  private void add(
      Map<ApiFile, List<URI>> target, String where, String modId, ApiFile file, URI uri) {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(uri, "uri");
    if (frozen) {
      throw new IllegalStateException("Sources can only be added in configureSources");
    }
    if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
      throw new IllegalArgumentException("Only absolute https:// addresses are allowed: " + uri);
    }
    target.computeIfAbsent(file, f -> new ArrayList<>()).add(uri);
    LOGGER.info("Mod '{}' added a source {} the default for {}: {}", modId, where, file, uri);
  }
}
