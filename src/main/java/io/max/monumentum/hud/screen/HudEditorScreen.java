package io.max.monumentum.hud.screen;

import io.max.monumentum.hud.HudPosition;
import io.max.monumentum.hud.HudWidget;
import io.max.monumentum.hud.MonumentumHud;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class HudEditorScreen extends Screen {
  private static final long FADE_HOLD_MS = 600;
  private static final long FADE_DURATION_MS = 400;

  private static final int BG_IDLE = 0x99_08_08_08;
  private static final int BG_ACTIVE = 0x10_00_00_00;

  private static final int FILL_IDLE = 0x88213448;
  private static final int FILL_HOT = 0x11_10_3A_70;
  private static final int FILL_OFF = 0x12F9361E;
  private static final int BORDER_IDLE = 0x44ACBAC4;
  private static final int BORDER_HOT = 0x16_88_CC_FF;

  private static final int TEXT_IDLE = 0xFF_BB_BB_BB;
  private static final int TEXT_HOT = 0x11_FF_FF_FF;
  private static final int TEXT_OFF = 0x37EB895B;
  private static final int COORD_COLOR = 0xFF_88_AA_CC;

  private static final int HINT_BASE = 0xCC_FF_FF_FF;
  private static final int RULE_BASE = 0x55_AA_BB_CC;

  private static final int HINT_Y = 8;
  private static final int RULE_MARGIN = 12;
  private static final int RULE_GAP = 6;
  private static final int RULE_THICKNESS = 1;

  private static final int PAD_X = 6;
  private static final int PAD_Y = 3;

  private record Box(HudWidget widget, Text label, int boxWidth, int boxHeight) {}

  private final MonumentumHud hud;
  private final Screen returnTo;
  private final List<Box> boxes = new ArrayList<>();
  private final Map<HudWidget, HudPosition> movedPositions = new HashMap<>();

  private Box dragging;
  private int dragOffsetX;
  private int dragOffsetY;
  private long dragEndedAt = -1;

  public HudEditorScreen(MonumentumHud hud, Screen returnTo) {
    super(Text.literal("HUD Editor"));
    this.hud = hud;
    this.returnTo = returnTo;
  }

  @Override
  protected void init() {
    boxes.clear();
    for (HudWidget widget : hud.movableWidgets()) {
      Text label = plain(widget.editorLabel());
      int boxWidth = textRenderer.getWidth(label) + PAD_X * 2;
      int boxHeight = textRenderer.fontHeight + PAD_Y * 2;
      boxes.add(new Box(widget, label, boxWidth, boxHeight));
    }
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    float idleAlpha = computeIdleAlpha();
    context.fill(0, 0, width, height, lerpColor(BG_ACTIVE, BG_IDLE, idleAlpha));
    renderHint(context, idleAlpha);
    for (Box box : boxes) {
      renderBox(context, box, mouseX, mouseY);
    }
    super.render(context, mouseX, mouseY, delta);
  }

  @Override
  public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

  private void renderHint(DrawContext context, float idleAlpha) {
    if (idleAlpha < 0.02f) return;

    Text hint = plain("Drag to move • Right-click to turn on/off • ESC to exit");
    int textWidth = textRenderer.getWidth(hint);
    int textX = (width - textWidth) / 2;
    int ruleY = HINT_Y + textRenderer.fontHeight / 2;
    int textColor = scaledAlpha(HINT_BASE, idleAlpha);
    int ruleColor = scaledAlpha(RULE_BASE, idleAlpha);

    fillRule(context, RULE_MARGIN, textX - RULE_GAP, ruleY, ruleColor);
    fillRule(context, textX + textWidth + RULE_GAP, width - RULE_MARGIN, ruleY, ruleColor);
    context.drawText(textRenderer, hint, textX, HINT_Y, textColor, false);
  }

  private void fillRule(DrawContext context, int x1, int x2, int y, int color) {
    if (x2 > x1) context.fill(x1, y, x2, y + RULE_THICKNESS, color);
  }

  private void renderBox(DrawContext context, Box box, int mouseX, int mouseY) {
    boolean turnedOn = box.widget().isTurnedOn();
    boolean hot = turnedOn && (contains(box, mouseX, mouseY) || box == dragging);
    int left = leftOf(box);
    int top = topOf(box);

    int fill = !turnedOn ? FILL_OFF : hot ? FILL_HOT : FILL_IDLE;
    context.fill(left, top, left + box.boxWidth(), top + box.boxHeight(), fill);
    renderOutline(
        context,
        left,
        top,
        box.boxWidth(),
        box.boxHeight(),
        hot ? BORDER_HOT : turnedOn ? BORDER_IDLE : TEXT_OFF);

    int textColor = !turnedOn ? TEXT_OFF : hot ? TEXT_HOT : TEXT_IDLE;
    context.drawText(textRenderer, box.label(), left + PAD_X, top + PAD_Y, textColor, true);

    if (hot) {
      HudPosition p = positionOf(box);
      Text coords =
          plain(
              String.format(
                  "X:%d Y:%d A:%.2f;%.2f", p.offsetX(), p.offsetY(), p.anchorX(), p.anchorY()));
      context.drawText(textRenderer, coords, left, top + box.boxHeight() + 2, COORD_COLOR, true);
    }
  }

  private void renderOutline(DrawContext context, int x, int y, int w, int h, int color) {
    context.fill(x, y, x + w, y + 1, color);
    context.fill(x, y + h - 1, x + w, y + h, color);
    context.fill(x, y, x + 1, y + h, color);
    context.fill(x + w - 1, y, x + w, y + h, color);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    for (Box box : boxes) {
      if (!contains(box, mouseX, mouseY)) continue;
      if (button == 0 && box.widget().isTurnedOn()) {
        dragging = box;
        dragOffsetX = (int) mouseX - leftOf(box);
        dragOffsetY = (int) mouseY - topOf(box);
        return true;
      }
      if (button == 1) {
        box.widget().setTurnedOn(!box.widget().isTurnedOn());
        return true;
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (button == 0 && dragging != null) {
      int left = clamp((int) mouseX - dragOffsetX, 0, width - dragging.boxWidth());
      int top = clamp((int) mouseY - dragOffsetY, 0, height - dragging.boxHeight());
      movedPositions.put(
          dragging.widget(),
          HudPosition.fromCorner(
              left, top, dragging.boxWidth(), dragging.boxHeight(), width, height));
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == 0 && dragging != null) {
      dragEndedAt = System.currentTimeMillis();
      dragging = null;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public void close() {
    movedPositions.forEach(HudWidget::setPosition);
    client.setScreen(returnTo);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }

  private static Text plain(String text) {
    return Text.literal(text).setStyle(Style.EMPTY);
  }

  private float computeIdleAlpha() {
    if (dragging != null) return 0f;
    if (dragEndedAt < 0) return 0.5f;
    long elapsed = System.currentTimeMillis() - dragEndedAt;
    if (elapsed < FADE_HOLD_MS) return 0f;
    return Math.max(0f, Math.min(0.5f, (elapsed - FADE_HOLD_MS) / (float) FADE_DURATION_MS));
  }

  private static int scaledAlpha(int base, float t) {
    int alpha = Math.round(((base >>> 24) & 0xFF) * t);
    return (alpha << 24) | (base & 0x00_FF_FF_FF);
  }

  private static int lerpColor(int a, int b, float t) {
    int aa = (a >>> 24) & 0xFF, ra = (a >>> 16) & 0xFF, ga = (a >>> 8) & 0xFF, ba = a & 0xFF;
    int ab = (b >>> 24) & 0xFF, rb = (b >>> 16) & 0xFF, gb = (b >>> 8) & 0xFF, bb = b & 0xFF;
    return (Math.round(aa + (ab - aa) * t) << 24)
        | (Math.round(ra + (rb - ra) * t) << 16)
        | (Math.round(ga + (gb - ga) * t) << 8)
        | Math.round(ba + (bb - ba) * t);
  }

  private HudPosition positionOf(Box box) {
    return movedPositions.getOrDefault(box.widget(), box.widget().position());
  }

  private int leftOf(Box box) {
    return positionOf(box).leftEdge(width, box.boxWidth());
  }

  private int topOf(Box box) {
    return positionOf(box).topEdge(height, box.boxHeight());
  }

  private boolean contains(Box box, double mouseX, double mouseY) {
    int left = leftOf(box);
    int top = topOf(box);
    return mouseX >= left
        && mouseX < left + box.boxWidth()
        && mouseY >= top
        && mouseY < top + box.boxHeight();
  }

  private static int clamp(int value, int low, int high) {
    return Math.max(low, Math.min(Math.max(low, high), value));
  }
}
