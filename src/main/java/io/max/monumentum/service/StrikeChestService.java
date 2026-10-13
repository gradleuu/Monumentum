package io.max.monumentum.service;

import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.ShardContext;
import io.max.monumentum.api.shard.ShardType;
import io.max.monumentum.api.shard.WorldId;
import io.max.monumentum.api.shard.event.ShardChangeCallback;
import io.max.monumentum.api.strike.StrikeApi;
import io.max.monumentum.api.strike.StrikeChests;
import io.max.monumentum.api.strike.event.StrikeChestsChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;

public final class StrikeChestService implements StrikeApi {

  private static final long HOLD_MS = 2000;

  private final ShardApi shards;
  private volatile StrikeChests chests = StrikeChests.NONE;

  private ServerMessage.@Nullable StrikeChest last;
  private long lastAtMs;

  private StrikeChestService(ShardApi shards) {
    this.shards = shards;
  }

  public static StrikeChestService create(ShardApi shards, MessageRouter router) {
    StrikeChestService service = new StrikeChestService(shards);
    router.subscribe(ServerMessage.StrikeChest.class, service::onMessage);
    ShardChangeCallback.EVENT.register(
        (previous, current) -> service.onContextChange(previous, current));
    return service;
  }

  private void onMessage(ServerMessage.StrikeChest message) {
    last = message;
    lastAtMs = nowMs();
    if (inStrike(shards.context())) {
      // already in a strike
      set(from(message));
    }
    // otherwise hold it, onContextChange applies it when the strike world appears
  }

  private void onContextChange(ShardContext previous, ShardContext current) {
    boolean noWorld = WorldId.UNKNOWN.equals(current.world());
    if (noWorld) {
      last = null;
    }

    StrikeChests next;
    if (noWorld || !inStrike(current)) {
      next = StrikeChests.NONE;
    } else if (!previous.world().equals(current.world())) {
      // a new strike world: use the packet that arrived just before it, if any
      next = isFresh() ? from(last) : StrikeChests.NONE;
      // use it once
      last = null;
    } else {
      // same world (for example the shard list just finished loading)
      next = chests.isKnown() ? chests : isFresh() ? from(last) : StrikeChests.NONE;
    }
    set(next);
  }

  private boolean isFresh() {
    return last != null && nowMs() - lastAtMs <= HOLD_MS;
  }

  private static boolean inStrike(ShardContext context) {
    return !context.content().isUnknown() && context.content().type() == ShardType.STRIKE;
  }

  private static StrikeChests from(ServerMessage.StrikeChest message) {
    Integer count = message.count();
    return new StrikeChests(
        message.newLimit(), count == null ? OptionalInt.empty() : OptionalInt.of(count));
  }

  private void set(StrikeChests next) {
    StrikeChests previous = chests;
    if (next.equals(previous)) {
      return;
    }
    chests = next;
    StrikeChestsChangeCallback.EVENT.invoker().onChange(previous, next);
  }

  private static long nowMs() {
    return System.nanoTime() / 1_000_000L;
  }

  @Override
  public StrikeChests chests() {
    return chests;
  }
}
