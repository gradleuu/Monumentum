package io.max.monumentum.config.screen;

import java.util.Locale;
import net.minecraft.text.Text;

enum ConfigGroup {
  GENERAL,
  FEATURES,
  DEBUG,
  DEVELOPER;

  Text title() {
    return Text.translatable("monumentum.config.group." + name().toLowerCase(Locale.ROOT));
  }

  // new pages land in FEATURES unless this rule below says otherwise
  static ConfigGroup of(String pageKey) {
    String key = pageKey.toLowerCase(Locale.ROOT);
    if (key.equals("general") || key.equals("api")) return GENERAL;
    if (key.startsWith("debug") || key.contains("_debug")) return DEBUG;
    if (key.equals("dev") || key.startsWith("dev_") || key.startsWith("developer"))
      return DEVELOPER;
    return FEATURES;
  }
}
