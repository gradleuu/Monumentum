package io.max.monumentum.hud;

public enum HudMessageSpot {
  TOP(0.5f, 0.08f),
  CENTER(0.5f, 0.40f),
  BOTTOM(0.5f, 0.78f);

  private final HudPosition position;

  HudMessageSpot(float anchorX, float anchorY) {
    this.position = HudPosition.of(anchorX, anchorY, 0, 0);
  }

  public HudPosition position() {
    return position;
  }
}
