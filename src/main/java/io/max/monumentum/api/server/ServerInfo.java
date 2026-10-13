package io.max.monumentum.api.server;

/**
 * @apiNote
 *     <p>The server sends this once per connection, not on every shard change, so it stays the same
 *     while the player moves between shards. It is cleared when the player disconnects.
 * @param known {@code true} if the server has told us. {@code false} if nothing has arrived yet, or
 *     if the server does not send this at all.
 * @param allowPublicizeContent What exactly this covers is up to Monumenta. I found less than
 *     nothing information about. It is always {@code false} when {@code known} is {@code false}, so
 *     you can just check this one value.
 */
public record ServerInfo(boolean known, boolean allowPublicizeContent) {
  public static final ServerInfo UNKNOWN = new ServerInfo(false, false);

  public ServerInfo {
    if (!known) {
      allowPublicizeContent = false;
    }
  }
}
