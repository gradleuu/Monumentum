package io.max.monumentum.util.font;

import io.max.monumentum.Monumentum;
import io.max.monumentum.util.Kaleidoscope;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public enum Glyph {
  MONUMENTUM_LOGO('\uEC00'),
  GLYPH_EC01('\uEC01'),
  DEBUG('\uEC02'),
  API('\uEC03'),
  GITHUB('\uEC04'),
  WEB('\uEC05'),
  GLYPH_EC06('\uEC06'),

  GLYPH_EC07('\uEC07'),
  GLYPH_EC08('\uEC08'),
  GLYPH_EC09('\uEC09'),
  GLYPH_EC0A('\uEC0A'),
  GLYPH_EC0B('\uEC0B'),
  GLYPH_EC0C('\uEC0C'),
  GLYPH_EC0D('\uEC0D'),

  CHEST('\uEC0E', 0xF1C40F),
  GLYPH_EC0F('\uEC0F'),
  GLYPH_EC10('\uEC10'),
  GLYPH_EC11('\uEC11'),
  GLYPH_EC12('\uEC12'),
  GLYPH_EC13('\uEC13'),
  GLYPH_EC14('\uEC14'),

  GLYPH_EC15('\uEC15'),
  GLYPH_EC16('\uEC16'),
  GLYPH_EC17('\uEC17'),
  GLYPH_EC18('\uEC18'),
  GLYPH_EC19('\uEC19'),
  GLYPH_EC1A('\uEC1A'),
  GLYPH_EC1B('\uEC1B'),

  GLYPH_EC1C('\uEC1C'),
  GLYPH_EC1D('\uEC1D'),
  GLYPH_EC1E('\uEC1E'),
  GLYPH_EC1F('\uEC1F'),
  GLYPH_EC20('\uEC20'),
  GLYPH_EC21('\uEC21'),
  GLYPH_EC22('\uEC22'),

  GLYPH_EC23('\uEC23'),
  GLYPH_EC24('\uEC24'),
  GLYPH_EC25('\uEC25'),
  GLYPH_EC26('\uEC26'),
  GLYPH_EC27('\uEC27'),
  GLYPH_EC28('\uEC28'),
  GLYPH_EC29('\uEC29'),

  GLYPH_EC2A('\uEC2A'),
  GLYPH_EC2B('\uEC2B'),
  GLYPH_EC2C('\uEC2C'),
  GLYPH_EC2D('\uEC2D'),
  GLYPH_EC2E('\uEC2E'),
  GLYPH_EC2F('\uEC2F'),
  GLYPH_EC30('\uEC30');

  private static final Identifier FONT_ID = new Identifier(Monumentum.MOD_ID, "custom");
  private final char character;
  @Nullable private final TextColor defaultColor;

  Glyph(char character) {
    this(character, (TextColor) null);
  }

  Glyph(char character, int hexRgb) {
    this(character, TextColor.fromRgb(hexRgb));
  }

  Glyph(char character, Formatting formatting) {
    this(character, TextColor.fromFormatting(formatting));
  }

  Glyph(char character, @Nullable TextColor defaultColor) {
    this.character = character;
    this.defaultColor = defaultColor;
  }

  /** Glyph with its default configured color */
  public MutableText get() {
    return text();
  }

  /**
   * @return Uncolored Glyph
   */
  public MutableText rawText() {
    return Text.literal(String.valueOf(character)).setStyle(Style.EMPTY.withFont(FONT_ID));
  }

  /**
   * @return Glyph with color of hex RGB integer
   * @param hexRgb RGB integer
   */
  public MutableText text(int hexRgb) {
    return Text.literal(String.valueOf(character))
        .setStyle(Style.EMPTY.withFont(FONT_ID).withColor(TextColor.fromRgb(hexRgb)));
  }

  public MutableText text(Kaleidoscope kaleidoscope) {
    return Text.literal(String.valueOf(character))
            .setStyle(Style.EMPTY.withFont(FONT_ID).withColor(TextColor.fromRgb(kaleidoscope.rgb())));
  }

  /**
   * @return Glyph overridden with a specific Formatting
   * @param formatting Formatting to override with
   */
  public MutableText text(Formatting formatting) {
    return Text.literal(String.valueOf(character))
        .setStyle(Style.EMPTY.withFont(FONT_ID).withColor(formatting));
  }

  public char getCharacter() {
    return character;
  }

  private MutableText text() {
    MutableText text =
        Text.literal(String.valueOf(character)).setStyle(Style.EMPTY.withFont(FONT_ID));
    if (defaultColor != null) {
      return text.styled(style -> style.withColor(defaultColor));
    }
    return text;
  }
}
