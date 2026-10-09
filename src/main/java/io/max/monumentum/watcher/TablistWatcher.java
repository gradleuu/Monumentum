package io.max.monumentum.watcher;

import io.max.monumentum.api.shard.ShardId;
import io.max.monumentum.client.event.TablistHeaderCallback;
import io.max.monumentum.client.event.WorldChangeCallback;
import io.max.monumentum.util.Signal;
import io.max.monumentum.util.TablistUtil;

public final class TablistWatcher extends Signal<ShardId> {
  public TablistWatcher() {
    super(ShardId.UNKNOWN);
  }

  public void register() {
    TablistHeaderCallback.EVENT.register(
        header -> emit(TablistUtil.parseShard(header == null ? null : header.getString())));
    WorldChangeCallback.EVENT.register(
        world -> {
          if (world == null) {
            emit(ShardId.UNKNOWN);
          }
        });
  }
}
