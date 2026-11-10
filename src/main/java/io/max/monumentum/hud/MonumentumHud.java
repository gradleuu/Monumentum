package io.max.monumentum.hud;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.Monumentum;
import io.max.monumentum.hud.screen.HudEditorScreen;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.hud.Hud;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class MonumentumHud {
  private final List<Entry> entries = new ArrayList<>();
  private final Map<HudMessageSpot, MessageWidget> messages = new EnumMap<>(HudMessageSpot.class);
  private volatile long tickCount;

  public MonumentumHud() {
    for (HudMessageSpot spot : HudMessageSpot.values()) {
      MessageWidget message = new MessageWidget(spot);
      messages.put(spot, message);
      addEntry(message, false);
    }
  }

  public void startTicking() {
    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          tickCount = tickCount + 1;
          messages.values().forEach(MessageWidget::checkIfExpired);
          int screenWidth = client.getWindow().getScaledWidth();
          int screenHeight = client.getWindow().getScaledHeight();
          for (Entry entry : entries) {
            entry.update(screenWidth, screenHeight);
          }
        });
  }

  public void addWidget(HudWidget widget) {
    addEntry(widget, true);
  }

  public void showText(Text text, HudMessageSpot spot, int forTicks) {
    messages.get(spot).show(text, tickCount + forTicks);
  }

  public void showTextForSeconds(Text text, HudMessageSpot spot, int seconds) {
    showText(text, spot, Math.max(1, seconds) * 20);
  }

  public void clearText(HudMessageSpot spot) {
    messages.get(spot).clear();
  }

  public List<HudWidget> movableWidgets() {
    return entries.stream().filter(entry -> entry.movable).map(entry -> entry.widget).toList();
  }

  public void openEditor() {
    MinecraftClient client = MinecraftClient.getInstance();
    client.setScreen(new HudEditorScreen(this, client.currentScreen));
  }

  private void addEntry(HudWidget widget, boolean movable) {
    Entry entry = new Entry(widget, movable);
    entries.add(entry);
    try {
      widget.setRedrawRequest(() -> entry.dirty = true);
    } catch (RuntimeException e) {
      entry.hideBecauseOf(e);
    }
  }

  private static void showInGame(Identifier id, Component component) {
    Hud.add(id, () -> component);
  }

  private static void hideInGame(Identifier id) {
    Hud.remove(id);
  }

  private static final class Entry {
    private final HudWidget widget;
    private final boolean movable;
    private volatile boolean dirty = true;
    private boolean broken;
    private boolean shown;
    private boolean lastTurnedOn;
    private Component component;
    private int appliedLeft = Integer.MIN_VALUE;
    private int appliedTop = Integer.MIN_VALUE;

    private Entry(HudWidget widget, boolean movable) {
      this.widget = widget;
      this.movable = movable;
    }

    private void update(int screenWidth, int screenHeight) {
      if (broken) {
        return;
      }
      try {
        boolean turnedOn = widget.isTurnedOn();
        if (dirty || turnedOn != lastTurnedOn) {
          dirty = false;
          lastTurnedOn = turnedOn;
          showHideAndRefresh(turnedOn);
        }
        if (shown) {
          placeOnScreen(screenWidth, screenHeight);
        }
      } catch (RuntimeException e) {
        hideBecauseOf(e);
      }
    }

    private void showHideAndRefresh(boolean turnedOn) {
      if (!turnedOn || !widget.isVisibleNow()) {
        hide();
        return;
      }
      if (component == null) {
        component = widget.buildComponent();
      }
      widget.updateComponent(component);
      if (!shown) {
        showInGame(widget.id(), component);
        shown = true;
        appliedLeft = Integer.MIN_VALUE;
      }
    }

    private void placeOnScreen(int screenWidth, int screenHeight) {
      HudPosition position = widget.position();
      int left = position.leftEdge(screenWidth, component.width());
      int top = position.topEdge(screenHeight, component.height());
      if (left != appliedLeft || top != appliedTop) {
        component.positioning(Positioning.absolute(left, top));
        appliedLeft = left;
        appliedTop = top;
      }
    }

    private void hide() {
      if (shown) {
        hideInGame(widget.id());
        shown = false;
      }
    }

    private void hideBecauseOf(RuntimeException e) {
      broken = true;
      LOGGER.error("HUD widget {} broke and is hidden", widget.id(), e);
      try {
        hide();
      } catch (RuntimeException ignored) {
      }
    }
  }

  private final class MessageWidget implements HudWidget {
    private final HudMessageSpot spot;
    private final Identifier id;
    private volatile Text text = Text.empty();
    private volatile long expiresAtTick;
    private volatile boolean wasVisible;
    private volatile Runnable redrawRequest = () -> {};
    private HudTextBox textBox;

    private MessageWidget(HudMessageSpot spot) {
      this.spot = spot;
      this.id = new Identifier(Monumentum.MOD_ID, "message_" + spot.name().toLowerCase(Locale.ROOT));
    }

    private void show(Text newText, long untilTick) {
      text = newText;
      expiresAtTick = untilTick;
      wasVisible = true;
      redrawRequest.run();
    }

    private void clear() {
      expiresAtTick = 0;
      redrawRequest.run();
    }

    private void checkIfExpired() {
      boolean visible = isVisibleNow();
      if (visible != wasVisible) {
        wasVisible = visible;
        redrawRequest.run();
      }
    }

    @Override
    public Identifier id() {
      return id;
    }

    @Override
    public String editorLabel() {
      return "message " + spot.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isTurnedOn() {
      return true;
    }

    @Override
    public void setTurnedOn(boolean turnedOn) {}

    @Override
    public boolean isVisibleNow() {
      return tickCount < expiresAtTick;
    }

    @Override
    public Component buildComponent() {
      textBox = new HudTextBox(text);
      return textBox.root();
    }

    @Override
    public void updateComponent(Component component) {
      textBox.setText(text);
    }

    @Override
    public HudPosition position() {
      return spot.position();
    }

    @Override
    public void setPosition(HudPosition position) {}

    @Override
    public void setRedrawRequest(Runnable redrawRequest) {
      this.redrawRequest = redrawRequest;
    }
  }
}
