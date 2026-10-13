package io.max.monumentum.net;

import static io.max.monumentum.Monumentum.LOGGER;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ClientChannel {
  public static final Identifier ID = new Identifier("monumenta", "client_channel_v1");

  private final MessageRouter router;
  private final Set<String> reportedUnknown = ConcurrentHashMap.newKeySet();
  private final AtomicBoolean reportedMalformed = new AtomicBoolean();

  public ClientChannel(MessageRouter router) {
    this.router = router;
  }

  public void start() {
    boolean registered =
        ClientPlayNetworking.registerGlobalReceiver(
            ID,
            (client, handler, buf, responseSender) ->
                onPacket(client, buf.toString(StandardCharsets.UTF_8)));
    if (!registered) {
      LOGGER.error("Another mod already handles {}; Monumentum will not receive server data", ID);
    }
  }

  /** Runs on the network thread. */
  private void onPacket(MinecraftClient mc, String json) {
    ServerMessage message;
    try {
      message = ServerMessageDecoder.decode(json);
    } catch (RuntimeException e) {
      if (reportedMalformed.compareAndSet(false, true)) {
        // log the first
        LOGGER.warn("Malformed packet on {} (later ones not logged): {}", ID, shorten(json), e);
      }
      return;
    }
    if (message instanceof ServerMessage.Unknown unknown) {
      if (reportedUnknown.add(unknown.type())) {
        LOGGER.info("Ignoring unknown packet type '{}'", unknown.type());
      }
      return;
    }
    mc.execute(() -> router.dispatch(message));
  }

  private static String shorten(String s) {
    return s.length() > 300 ? s.substring(0, 300) + "…" : s;
  }
}
