package io.max.monumentum.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import io.max.monumentum.api.shard.ShardApi;
import io.max.monumentum.api.shard.ShardContext;
import io.max.monumentum.api.shard.ShardId;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public final class ShardCommand {
  private ShardCommand() {}

  public static void register(ShardApi shardApi) {
    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) ->
            dispatcher.register(
                literal("monumentum")
                    .then(
                        literal("shard")
                            .executes(
                                command -> {
                                  ShardContext context = shardApi.context();

                                  if (context.shard().isUnknown()) {
                                    command
                                        .getSource()
                                        .sendFeedback(
                                            Text.translatable(
                                                "monumentum.command.shard.unavailable"));
                                    return 0;
                                  }

                                  Text instance =
                                      ShardId.UNKNOWN.equals(context.shardId())
                                          ? Text.translatable("monumentum.command.shard.unknown")
                                          : Text.literal(
                                              String.valueOf(context.shardId().instance()));

                                  command
                                      .getSource()
                                      .sendFeedback(
                                          Text.translatable(
                                              "monumentum.command.shard.current",
                                              context.shard().id(),
                                              instance));

                                  return 1;
                                }))));
  }
}
