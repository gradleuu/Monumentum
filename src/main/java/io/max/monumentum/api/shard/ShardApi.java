package io.max.monumentum.api.shard;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface ShardApi {

  /**
   * @return The current snapshot, never {@code null}. Parts of it can be {@code UNKNOWN}, for
   *     example before joining a server, while the shard list is still loading, or just after the
   *     player switched shards.
   */
  @NotNull
  ShardContext context();

  /**
   * Looks up a shard by its <b>name</b>.
   *
   * @param id The shard name, like {@code ring}, {@code lightblue} or {@code playerplots}. Do not
   *     include numbers; {@code ring-2} and {@code ring123} etc. will result into returning {@link
   *     Shard#UNKNOWN}.
   * @return The shard, or {@link Shard#UNKNOWN} if there is no shard with that name
   */
  Shard byId(String id);

  /**
   * @apiNote
   *     <p>{@link Shard#UNKNOWN} is not part of this list. The list can be empty until the shard
   *     data has finished loading.
   * @return {@link Collection} of every shard, in the same order as the source data.
   */
  Collection<Shard> all();
}
