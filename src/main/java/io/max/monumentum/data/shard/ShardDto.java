package io.max.monumentum.data.shard;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public record ShardDto(
		String officialName, String shardType, @Nullable Integer maxChests, String emoji) {}