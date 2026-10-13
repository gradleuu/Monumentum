package io.max.monumentum.service;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.shard.Shard;
import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.event.ShardChangeCallback;
import io.max.monumentum.api.shard.ShardContext;
import io.max.monumentum.api.shard.event.ShardEnterCallback;
import io.max.monumentum.api.shard.ShardId;
import io.max.monumentum.api.shard.event.ShardLeaveCallback;
import io.max.monumentum.api.shard.WorldId;
import io.max.monumentum.data.ShardCatalog;
import io.max.monumentum.util.Signal;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public final class ShardService implements ShardApi {
  private final ShardCatalog catalog;
  private final Signal<WorldId> world;
  private final Signal<ShardId> location;
  private final Signal<ShardId> tablist;
  private volatile ShardContext context = ShardContext.UNKNOWN;

  private ShardService(
      ShardCatalog catalog,
      Signal<WorldId> world,
      Signal<ShardId> location,
      Signal<ShardId> tablist) {
    this.catalog = catalog;
    this.world = world;
    this.location = location;
    this.tablist = tablist;
  }

  public static ShardService create(
      ShardCatalog catalog,
      Signal<WorldId> world,
      Signal<ShardId> location,
      Signal<ShardId> tablist) {
    ShardService service = new ShardService(catalog, world, location, tablist);
    catalog.subscribe(service::rebuild);
    world.subscribe(service::rebuild);
    location.subscribe(service::rebuild);
    tablist.subscribe(service::rebuild);
    service.rebuild();
    return service;
  }

  private void rebuild() {
    WorldId w = world.current();
    if (WorldId.UNKNOWN.equals(w)) {
      // no world means no place
      publish(ShardContext.UNKNOWN);
      return;
    }

    ShardId fromLocation = location.current();
    ShardId fromTablist = tablist.current();
    // the server's location message is primary, the tablist is only a fallback
    ShardId id = ShardId.UNKNOWN.equals(fromLocation) ? fromTablist : fromLocation;

    if (!ShardId.UNKNOWN.equals(fromLocation)
        && !ShardId.UNKNOWN.equals(fromTablist)
        && !fromLocation.equals(fromTablist)) {
      LOGGER.debug("Shard sources disagree: location={} tablist={}", fromLocation, fromTablist);
    }

    publish(new ShardContext(catalog.byId(id.name()), id, w, catalog.byId(w.name())));
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
    // same place if both are complete and neither the shard number nor the world differs
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
