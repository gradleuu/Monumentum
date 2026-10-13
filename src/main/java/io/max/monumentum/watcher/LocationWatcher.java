package io.max.monumentum.watcher;

import io.max.monumentum.api.shard.ShardId;
import io.max.monumentum.client.event.WorldChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import io.max.monumentum.util.Signal;

public final class LocationWatcher extends Signal<ShardId> {
  public LocationWatcher() {
    super(ShardId.UNKNOWN);
  }

  public void register(MessageRouter router) {
    router.subscribe(
        ServerMessage.Location.class,
        message ->
            emit(message.shard().isBlank() ? ShardId.UNKNOWN : ShardId.parse(message.shard())));
    // every shard hop passes through a null world before the new location arrives
    WorldChangeCallback.EVENT.register(
        world -> {
          if (world == null) {
            emit(ShardId.UNKNOWN);
          }
        });
  }
}
