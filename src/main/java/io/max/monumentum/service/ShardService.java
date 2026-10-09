package io.max.monumentum.service;

import io.max.monumentum.api.shard.Shard;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.event.ShardChangeCallback;
import io.max.monumentum.api.shard.ShardContext;
import io.max.monumentum.api.shard.ShardId;
import io.max.monumentum.api.shard.WorldId;
import io.max.monumentum.api.shard.event.ShardEnterCallback;
import io.max.monumentum.api.shard.event.ShardLeaveCallback;
import io.max.monumentum.data.ShardCatalog;
import io.max.monumentum.util.Signal;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public final class ShardService implements ShardApi {
  private final ShardCatalog catalog;
  private final Signal<WorldId> world;
  private final Signal<ShardId> tablist;
  private volatile ShardContext context = ShardContext.UNKNOWN;

  private ShardService(ShardCatalog catalog, Signal<WorldId> world, Signal<ShardId> tablist) {
    this.catalog = catalog;
    this.world = world;
    this.tablist = tablist;
  }

  public static ShardService create(
      ShardCatalog catalog, Signal<WorldId> world, Signal<ShardId> tablist) {
    ShardService service = new ShardService(catalog, world, tablist);
    catalog.subscribe(service::rebuild);
    world.subscribe(service::rebuild);
    tablist.subscribe(service::rebuild);
    service.rebuild();
    return service;
  }

  private void rebuild() {
    WorldId w = world.current();
    if (w.equals(WorldId.UNKNOWN)) {
      publish(ShardContext.UNKNOWN);
      return;
    }
    ShardId t = tablist.current();
    Shard fromWorld = catalog.byId(w.name());
    Shard shard = fromWorld.isUnknown() ? catalog.byId(t.name()) : fromWorld;
    publish(new ShardContext(shard, t, w));
  }

  private void publish(ShardContext next) {
    ShardContext previous = context;
    if (next.equals(previous)) {
      return;
    }
    context = next;
    ShardChangeCallback.EVENT.invoker().onChange(previous, next);

    boolean wasIn = previous.isComplete();
    boolean isIn = next.isComplete();
    boolean samePlace =
        wasIn
            && isIn
            && previous.shardId().equals(next.shardId())
            && previous.world().equals(next.world());

    if (wasIn && !samePlace) {
      ShardLeaveCallback.EVENT.invoker().onLeave(previous);
    }
    if (isIn && !samePlace) {
      ShardEnterCallback.EVENT.invoker().onEnter(next);
    }
  }

  @Override
  public @NotNull ShardContext context() {
    return context;
  }

  @Override
  public Shard byId(String id) {
    return catalog.byId(id);
  }

  @Override
  public Collection<Shard> all() {
    return catalog.all();
  }
}
