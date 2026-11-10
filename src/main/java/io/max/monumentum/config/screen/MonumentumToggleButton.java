package io.max.monumentum.config.screen;

import io.max.monumentum.util.Kaleidoscope;
import io.wispforest.owo.config.ui.component.OptionValueProvider;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class MonumentumToggleButton extends ButtonComponent implements OptionValueProvider {
  private static final int WIDTH = 28;
  private static final int HEIGHT = 14;
  private static final int BORDER = 1;
  private static final int KNOB = 8;
  private static final int KNOB_INSET = 2;
  private static final float SLIDE_SECONDS = 0.10f;

  private boolean enabled;
  private float knobPosition;
  private long lastDrawMillis;

  public MonumentumToggleButton() {
    super(Text.empty(), button -> {});
    this.sizing(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
    this.renderer((context, button, delta) -> drawSwitch(context));
  }

  @Override
  public void onPress() {
    this.enabled = !this.enabled;
    super.onPress();
  }

  public MonumentumToggleButton enabled(boolean enabled) {
    this.enabled = enabled;
    if (lastDrawMillis == 0) knobPosition = enabled ? 1f : 0f;
    return this;
  }

  private void drawSwitch(OwoUIDrawContext context) {
    long now = Util.getMeasuringTimeMs();
    float step =
        lastDrawMillis == 0 ? 1f : Math.min(1f, (now - lastDrawMillis) / 1000f / SLIDE_SECONDS);
    lastDrawMillis = now;
    float target = enabled ? 1f : 0f;
    knobPosition += Math.max(-step, Math.min(step, target - knobPosition));

    boolean hovered = this.isHovered();
    int border =
        Kaleidoscope.blendArgb(
            hovered ? Kaleidoscope.CONFIG_SWITCH_BORDER_HOVER : Kaleidoscope.CONFIG_SWITCH_BORDER,
            hovered ? Kaleidoscope.CONFIG_ACCENT_BRIGHT : Kaleidoscope.CONFIG_ACCENT,
            knobPosition);
    int fill =
        Kaleidoscope.WHITE_TRANSPARENT.argb();
    int knob =
        Kaleidoscope.blendArgb(
            Kaleidoscope.CONFIG_KNOB_OFF, Kaleidoscope.CONFIG_KNOB_ON, knobPosition);
    if (!this.active) {
      border = Kaleidoscope.CONFIG_HAIRLINE.argb();
      fill = Kaleidoscope.CONFIG_CONTROL_DISABLED.argb();
      knob = Kaleidoscope.CONFIG_TEXT_FAINT.argb();
    }

    int x = this.getX();
    int y = this.getY();
    context.fill(x, y, x + WIDTH, y + HEIGHT, border);
    context.fill(x + BORDER, y + BORDER, x + WIDTH - BORDER, y + HEIGHT - BORDER, fill);

    int travel = WIDTH - 2 * BORDER - 2 * KNOB_INSET - KNOB;
    int knobX = x + BORDER + KNOB_INSET + Math.round(travel * knobPosition);
    int knobY = y + (HEIGHT - KNOB) / 2;
    context.fill(knobX, knobY, knobX + KNOB, knobY + KNOB, knob);
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public Object parsedValue() {
    return this.enabled;
  }
}
