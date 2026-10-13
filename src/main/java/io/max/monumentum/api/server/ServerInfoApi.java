package io.max.monumentum.api.server;

public interface ServerInfoApi {
  /**
   * @apiNote The result never changes after you get it.
   * @return the current information, never {@code null}. It is {@link ServerInfo#UNKNOWN} until the
   *     server has sent it, and again after the player disconnects.
   */
  ServerInfo info();
}
