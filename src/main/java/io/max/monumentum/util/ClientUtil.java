package io.max.monumentum.util;

import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class ClientUtil {
  private ClientUtil() {}

  public static boolean isReady() {
    MinecraftClient client = MinecraftClient.getInstance();
    return client.player != null && client.world != null;
  }

  public static void withPlayer(Consumer<ClientPlayerEntity> code) {
    MinecraftClient client = MinecraftClient.getInstance();
    ClientPlayerEntity player = client.player;
    if (player != null && client.world != null) {
      code.accept(player);
    }
  }
}
