package io.max.monumentum.data;

import java.util.concurrent.CompletableFuture;

public interface Feed {
	CompletableFuture<String> fetch();
}