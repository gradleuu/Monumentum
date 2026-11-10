package io.max.monumentum.util;

import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

import java.util.Arrays;
import java.util.Optional;

public enum Kaleidoscope {
  CRIMSON_RED(0xDC143C),
  BLACK(0x000000, 0xFF),
  BLACK_TRANSPARENT(0x000, 0x00),
  WHITE(0x000000, 0xFF),
  WHITE_TRANSPARENT(0xFFFFFF, 0x00),

  CONFIG_HOVER(0x14161A),
  CONFIG_SELECTED(0x161A21),
  CONFIG_HAIRLINE(0x1A1D22),
  CONFIG_TEXT(0xA2A9B5),
  CONFIG_TEXT_MUTED(0x3A3A3C),
  CONFIG_TEXT_FAINT(0x565B64),
  CONFIG_ACCENT(0xA2A9B5),
  CONFIG_ACCENT_BRIGHT(0xFAFAFA),
  CONFIG_INVALID(0xE5484D),
  CONFIG_WARNING(0xD6A84F),
  CONFIG_CONTROL(0xFFFFFF, 0x00),
  CONFIG_CONTROL_HOVER(0xFFFFFF, 0x00),
  CONFIG_CONTROL_DISABLED(0xFFFFFF, 0x00),
  CONFIG_SWITCH_BORDER(0x2A2E36),
  CONFIG_SWITCH_BORDER_HOVER(0x3A3F49),
  CONFIG_SWITCH_ON_FILL(0x14233B),
  CONFIG_KNOB_OFF(0x8A8F98),
  CONFIG_KNOB_ON(0xFFFFFF);

  private final int rgb;
  private final int alpha;

  Kaleidoscope(int rgb) {
    this(rgb, 0xFF);
  }

  Kaleidoscope(int rgb, int alpha) {
    this.rgb = rgb & 0xFFFFFF;
    this.alpha = alpha & 0xFF;
  }

  public int rgb() {
    return rgb;
  }

  public int argb() {
    return argb(alpha);
  }

  public int argb(int alpha) {
    return ((alpha & 0xFF) << 24) | rgb;
  }

  public int red() {
    return (rgb >> 16) & 0xFF;
  }

  public int green() {
    return (rgb >> 8) & 0xFF;
  }

  public int blue() {
    return rgb & 0xFF;
  }

  public String hex() {
    return String.format("#%06X", rgb);
  }

  public static Optional<Kaleidoscope> fromRgb(int rgb) {
    int normalized = rgb & 0xFFFFFF;

    return Arrays.stream(values()).filter(color -> color.rgb == normalized).findFirst();
  }

  public static int blend(int start, int end, float progress) {
    float t = Math.max(0.0F, Math.min(1.0F, progress));

    int red = interpolate((start >> 16) & 0xFF, (end >> 16) & 0xFF, t);
    int green = interpolate((start >> 8) & 0xFF, (end >> 8) & 0xFF, t);
    int blue = interpolate(start & 0xFF, end & 0xFF, t);

    return (red << 16) | (green << 8) | blue;
  }

  public static int blendArgb(Kaleidoscope start, Kaleidoscope end, float progress) {
    float t = Math.max(0.0F, Math.min(1.0F, progress));

    int rgb = blend(start.rgb, end.rgb, t);
    int blendedAlpha = interpolate(start.alpha, end.alpha, t);

    return ((blendedAlpha & 0xFF) << 24) | rgb;
  }

  public static int blend(Kaleidoscope start, Kaleidoscope end, float progress) {
    return blend(start.rgb, end.rgb, progress);
  }

  private static int interpolate(int start, int end, float progress) {
    return Math.round(start + (end - start) * progress);
  }

  public static int withAlpha(int rgb, int alpha) {
    return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
  }

  public static Style style(int argb) {
    return Style.EMPTY.withColor(TextColor.fromRgb(argb & 0xFFFFFF));
  }

  public static Style style(Kaleidoscope kaleidoscopeColor) {
    return Style.EMPTY.withColor(TextColor.fromRgb(kaleidoscopeColor.rgb & 0xFFFFFF));
  }
}
