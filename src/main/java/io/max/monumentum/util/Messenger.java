package io.max.monumentum.util;

import io.max.monumentum.config.MonumentumConfig;
import io.max.monumentum.util.font.Glyph;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public final class Messenger {
  private static final Text MONUMENTUM_MESSAGE_PREFIX =
      Text.empty()
          .append(Glyph.MONUMENTUM_LOGO.get())
          .append(" ")
          .styled(
              style ->
                  style.withHoverEvent(
                      new HoverEvent(
                          HoverEvent.Action.SHOW_TEXT,
                          Text.empty()
                              .append(Glyph.MONUMENTUM_LOGO.get())
                              .append(
                                  Text.translatable(
                                      "monumentum.ahtung", "message", "sent", "Monumentum"))
                              .formatted(Formatting.WHITE))));

  private Messenger() {}

  public static void sendClientMessage(@NotNull Text message) {
    ClientUtil.withPlayer(
        player -> {
          Text coloredMessage =
              message
                  .copy()
                  .styled(
                      style ->
                          style.withColor(
                              Kaleidoscope.style(MonumentumConfig.monumentumMessageColour)
                                  .getColor()));

          player.sendMessage(
              Text.empty().append(MONUMENTUM_MESSAGE_PREFIX).append(coloredMessage), false);
        });
  }

  public static void sendClientMessage(@NotNull String message) {
    sendClientMessage(Text.literal(message));
  }
}
