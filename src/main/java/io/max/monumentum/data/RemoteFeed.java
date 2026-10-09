package io.max.monumentum.data;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class RemoteFeed implements Feed {
  private static final HttpClient HTTP =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

  private final URI uri;

  public RemoteFeed(URI uri) {
    this.uri = Objects.requireNonNull(uri, "uri");
  }

  @Override
  public CompletableFuture<String> fetch() {
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "application/json")
            .header("User-Agent", "Monumentum")
            .GET()
            .build();

    return HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(
            res -> {
              if (res.statusCode() != 200) {
                throw new CompletionException(
                    new IOException("HTTP " + res.statusCode() + " for " + uri));
              }
              return res.body();
            });
  }
}
