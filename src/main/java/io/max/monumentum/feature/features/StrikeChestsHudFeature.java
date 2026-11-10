package io.max.monumentum.feature.features;

import io.max.monumentum.api.strike.StrikeApi;
import io.max.monumentum.api.strike.StrikeChests;
import io.max.monumentum.api.strike.event.StrikeChestsChangeCallback;
import io.max.monumentum.feature.OneLineTextHudFeature;
import io.max.monumentum.util.font.Glyph;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class StrikeChestsHudFeature extends OneLineTextHudFeature {
  private final StrikeApi strikeApi;

  public StrikeChestsHudFeature(StrikeApi strikeApi) {
    super("strike_chests");
    this.strikeApi = strikeApi;
  }

  @Override
  protected void listenForChanges() {
    StrikeChestsChangeCallback.EVENT.register((before, after) -> requestRedraw());
  }

  @Override
  protected boolean hasSomethingToShow() {
    return strikeApi.chests().isKnown();
  }

  @Override
  protected Text textToShow() {
    StrikeChests chests = strikeApi.chests();
    String openedCount =
        chests.count().isPresent() ? String.valueOf(chests.count().getAsInt()) : "??";

    return Glyph.CHEST
        .get()
        .append(Text.literal(" " + openedCount + "/" + chests.limit()).formatted(Formatting.WHITE));
  }
}
