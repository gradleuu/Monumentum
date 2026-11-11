package io.max.monumentum.feature.features;

import io.max.monumentum.Monumentum;
import io.max.monumentum.config.CrimsonContractSwitchReminderOptions;
import io.max.monumentum.feature.Feature;
import io.max.monumentum.hud.HudMessageSpot;
import io.max.monumentum.util.Kaleidoscope;
import io.max.monumentum.util.Messenger;
import java.util.function.Supplier;
import net.minecraft.text.Text;

public final class CrimsonContractSwitchReminderFeature extends Feature {
  private static final Supplier<CrimsonContractSwitchReminderOptions> OPTIONS =
      () -> Monumentum.CONFIG.get().crimsonContractSwitchReminder;

  public CrimsonContractSwitchReminderFeature() {
    super("crimson_contract_switch_reminder", OPTIONS);
  }

  @Override
  protected void hook() {
    whenBecomesTrue(
        "level check",
        player -> player.experienceLevel > OPTIONS.get().levelCapAfterWhichPlayerGetsReminded,
        () -> {
          CrimsonContractSwitchReminderOptions options = OPTIONS.get();
          Text text =
              Text.translatable("monumentum.feature.crimson_contract_switch_reminder.message")
                  .styled(style -> style.withColor(Kaleidoscope.CRIMSON_RED.rgb()).withBold(true));
          if (options.showOnHud) {
            hud().showTextForSeconds(text, HudMessageSpot.CENTER, options.showForSeconds);
          } else {
            Messenger.sendClientMessage(text);
          }
        });
  }
}
