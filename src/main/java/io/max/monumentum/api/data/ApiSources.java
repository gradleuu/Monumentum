package io.max.monumentum.api.data;

import java.net.URI;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ApiSources {
  /**
   * Adds a source that is tried <b>before</b> Monumentum's own server.
   *
   * @param file Which file this source provides
   * @param uri The full address of the file. It must start with {@code https://}.
   * @throws IllegalArgumentException if the address is not an {@code https://} address
   * @implNote
   *     <p>If your source gives valid data, Monumentum uses only that and ignores the own data. If
   *     your file is missing shards that then will be resolved by Monumentum - those shards will be
   *     unknown.
   */
  void prepend(ApiFile file, URI uri);

  /**
   * Adds a source that is tried <b>after</b> Monumentum's own server, but before the built-in copy.
   *
   * @param file Which file this source provides
   * @param uri The full address of the file. It must start with {@code https://}.
   * @throws IllegalArgumentException if the address is not an {@code https://} address
   * @implNote Use this for backup copies.
   */
  void append(ApiFile file, URI uri);
}
