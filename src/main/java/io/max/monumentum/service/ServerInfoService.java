package io.max.monumentum.service;

import io.max.monumentum.api.server.ServerInfo;
import io.max.monumentum.api.server.ServerInfoApi;
import io.max.monumentum.api.server.event.ServerInfoChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ServerInfoService implements ServerInfoApi {
  private volatile ServerInfo info = ServerInfo.UNKNOWN;

  private ServerInfoService() {}

  public static ServerInfoService create(MessageRouter router) {
    ServerInfoService service = new ServerInfoService();
    router.subscribe(
        ServerMessage.ServerInfo.class,
        message -> service.set(new ServerInfo(true, message.allowPublicizeContent())));
    // per connection, not per world: observed once at join, never on shard hops
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> client.execute(() -> service.set(ServerInfo.UNKNOWN)));
    return service;
  }

  private void set(ServerInfo next) {
    ServerInfo previous = info;
    if (next.equals(previous)) {
      return;
    }
    info = next;
    ServerInfoChangeCallback.EVENT.invoker().onChange(previous, next);
  }

  @Override
  public ServerInfo info() {
    return info;
  }
}
