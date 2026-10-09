package io.max.monumentum.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ApiClient {
  private static final Gson GSON = new Gson();
  private static final HttpClient HTTP =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

  private ApiClient() {}

  public static <T> CompletableFuture<T> get(URI uri, TypeToken<T> type) {
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
              T parsed = GSON.fromJson(res.body(), type.getType());
              return Objects.requireNonNull(parsed, "empty body: " + uri);
            });
  }
}
