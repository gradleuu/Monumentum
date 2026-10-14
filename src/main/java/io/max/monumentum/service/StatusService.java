package io.max.monumentum.service;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.status.PlayerStatus;
import io.max.monumentum.api.status.StatusApi;
import io.max.monumentum.api.status.StatusChange;
import io.max.monumentum.api.status.StatusChange.Kind;
import io.max.monumentum.api.status.event.StatusChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class StatusService implements StatusApi {
  private volatile PlayerStatus status = PlayerStatus.NONE;
  private boolean silencedPublished;

  private StatusService() {}

  public static StatusService create(MessageRouter router) {
    StatusService service = new StatusService();
    router.subscribe(
        ServerMessage.PlayerStatus.class,
        message -> {
          service.status = new PlayerStatus(message.silenceDuration(), System.nanoTime());
          service.sync();
        });
    // if the end of a silence was never observed, the timer is what ends it
    ClientTickEvents.END_CLIENT_TICK.register(
        mc -> {
          if (service.silencedPublished) {
            service.sync();
          }
        });
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) ->
            client.execute(
                () -> {
                  service.status = PlayerStatus.NONE;
                  service.sync();
                }));
    return service;
  }

  private void sync() {
    boolean silenced = status.isSilenced();
    if (silenced == silencedPublished) {
      return;
    }
    silencedPublished = silenced;
    try {
      StatusChangeCallback.EVENT
          .invoker()
          .onChange(new StatusChange(silenced ? Kind.SILENCED : Kind.UNSILENCED, status));
    } catch (RuntimeException e) {
      LOGGER.error("Status listener failed", e);
    }
  }

  @Override
  public PlayerStatus status() {
    return status;
  }
}
