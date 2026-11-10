package io.max.monumentum.feature;

import io.max.monumentum.hud.HudTextBox;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;

public abstract class OneLineTextHudFeature extends HudFeature {
  private HudTextBox textBox;

  protected OneLineTextHudFeature(String name) {
    super(name);
  }

  protected abstract Text textToShow();

  @Override
  public final Component buildComponent() {
    textBox = new HudTextBox(textToShow());
    return textBox.root();
  }

  @Override
  public final void updateComponent(Component component) {
    textBox.setText(textToShow());
  }
}
