package io.max.monumentum.hud;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.text.Text;

public final class HudTextBox {
  private final LabelComponent label;
  private final FlowLayout box;

  public HudTextBox(Text text) {
    label = Components.label(text);
    box = Containers.horizontalFlow(Sizing.content(), Sizing.content());
    box.padding(Insets.of(3));
    box.surface(Surface.flat(0x80000000));
    box.child(label);
  }

  public Component root() {
    return box;
  }

  public void setText(Text text) {
    label.text(text);
  }
}
