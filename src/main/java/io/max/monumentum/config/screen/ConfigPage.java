package io.max.monumentum.config.screen;

import io.wispforest.owo.ui.core.Component;
import java.util.List;
import net.minecraft.text.Text;

record ConfigPage(
    String key, Text title, Text subtitle, ConfigGroup group, List<Component> components) {
  ConfigPage {
    components = List.copyOf(components);
  }
}
