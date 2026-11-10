package io.max.monumentum.feature.features;

import io.max.monumentum.feature.Feature;
import io.max.monumentum.hud.HudMessageSpot;
import io.max.monumentum.util.Kaleidoscope;
import io.max.monumentum.util.Messenger;
import io.wispforest.owo.config.Option;
import net.minecraft.text.Text;

public final class CrimsonContractSwitchReminderFeature extends Feature {

  public CrimsonContractSwitchReminderFeature() {
    super("crimson_contract_switch_reminder");
  }

  @Override
  protected void hook() {
    Option<Boolean> showOnHud;
    Option<Integer> showForSeconds;
    Option<Integer> levelCapAfterWhichPlayerGetsReminded;
    levelCapAfterWhichPlayerGetsReminded = savedOption("levelCapAfterWhichPlayerGetsReminded");
    showForSeconds = savedOption("showForSeconds");
    showOnHud = savedOption("showOnHud");
    whenBecomesTrue(
        "level check",
        player -> player.experienceLevel > levelCapAfterWhichPlayerGetsReminded.value(),
        () -> {
          Text text =
              Text.translatable("monumentum.feature.crimson_contract_switch_reminder.message")
                  .styled(style -> style.withColor(Kaleidoscope.CRIMSON_RED.rgb()).withBold(true));
          if (Boolean.TRUE.equals(showOnHud.value())) {
            hud().showTextForSeconds(text, HudMessageSpot.CENTER, showForSeconds.value());
          } else {
            Messenger.sendClientMessage(text);
          }
        });
  }
}
