package io.max.monumentum.service;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.ShardContext;
import io.max.monumentum.api.shard.ShardType;
import io.max.monumentum.api.shard.event.ShardChangeCallback;
import io.max.monumentum.api.strike.StrikeApi;
import io.max.monumentum.api.strike.StrikeChests;
import io.max.monumentum.api.strike.event.StrikeChestsChangeCallback;
import io.max.monumentum.net.MessageRouter;
import io.max.monumentum.net.ServerMessage;
import java.util.OptionalInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class StrikeChestService implements StrikeApi {
  private static final long HOLD_MS = 200;

  private final ShardApi shards;
  private volatile StrikeChests chests = StrikeChests.NONE;

  private ServerMessage.@Nullable StrikeChest pending;
  private long pendingAtMs;

  private StrikeChestService(ShardApi shards) {
    this.shards = shards;
  }

  public static StrikeChestService create(ShardApi shards, MessageRouter router) {
    StrikeChestService service = new StrikeChestService(shards);
    router.subscribe(ServerMessage.StrikeChest.class, service::onMessage);
    ShardChangeCallback.EVENT.register(service::onContextChange);
    return service;
  }

  private void onMessage(ServerMessage.StrikeChest message) {
    if (inStrike(shards.context())) {
      pending = null;
      set(from(message));
    } else {
      pending = message;
      pendingAtMs = nowMs();
    }
  }

  private void onContextChange(ShardContext previous, ShardContext current) {
    boolean worldChanged = !previous.world().equals(current.world());

    if (!inStrike(current)) {
      if (worldChanged) {
        // it was meant for a strike that never came
        pending = null;
      }
      set(StrikeChests.NONE);
      return;
    }
    if (worldChanged // always replaces the old numbers, never keeps them
        || !chests.isKnown() // for example the shard list just finished loading
    ) {
      set(takePending(current));
    }
  }

  private StrikeChests takePending(ShardContext current) {
    ServerMessage.StrikeChest message = pending;
    pending = null;
    if (message == null || nowMs() - pendingAtMs > HOLD_MS) {
      return StrikeChests.NONE;
    }
    StrikeChests candidate = from(message);
    OptionalInt expected = current.content().maxChests();
    if (expected.isPresent() && expected.getAsInt() != candidate.limit()) {
      LOGGER.debug(
          "Ignoring held chest packet: limit {} does not match {} for {}",
          candidate.limit(),
          expected.getAsInt(),
          current.content().id());
      return StrikeChests.NONE;
    }
    return candidate;
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
