package io.max.monumentum.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class ResourceFeed implements Feed {
  private final String path;

  public ResourceFeed(String path) {
    this.path = Objects.requireNonNull(path, "path");
  }

  @Override
  public CompletableFuture<String> fetch() {
    return CompletableFuture.supplyAsync(
        () -> {
          try (InputStream in = ResourceFeed.class.getResourceAsStream(path)) {
            if (in == null) {
              throw new IOException("Missing resource " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
          } catch (IOException e) {
            throw new CompletionException(e);
          }
        });
  }
}
