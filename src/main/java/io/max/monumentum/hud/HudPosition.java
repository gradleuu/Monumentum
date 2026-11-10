package io.max.monumentum.hud;

public record HudPosition(float anchorX, float anchorY, int offsetX, int offsetY) {
  public HudPosition {
    anchorX = Math.max(0f, Math.min(1f, anchorX));
    anchorY = Math.max(0f, Math.min(1f, anchorY));
  }

  public static HudPosition of(float anchorX, float anchorY, int offsetX, int offsetY) {
    return new HudPosition(anchorX, anchorY, offsetX, offsetY);
  }

  public static HudPosition topLeft(int x, int y) {
    return new HudPosition(0f, 0f, x, y);
  }

  public int leftEdge(int screenWidth, int itemWidth) {
    return Math.round((screenWidth - itemWidth) * anchorX) + offsetX;
  }

  public int topEdge(int screenHeight, int itemHeight) {
    return Math.round((screenHeight - itemHeight) * anchorY) + offsetY;
  }

  public static HudPosition fromCorner(
      int left, int top, int itemWidth, int itemHeight, int screenWidth, int screenHeight) {
    float anchorX = snapToNearestAnchor((left + itemWidth * 0.5f) / screenWidth);
    float anchorY = snapToNearestAnchor((top + itemHeight * 0.5f) / screenHeight);
    return new HudPosition(
        anchorX,
        anchorY,
        Math.round(left - (screenWidth - itemWidth) * anchorX),
        Math.round(top - (screenHeight - itemHeight) * anchorY));
  }

  private static float snapToNearestAnchor(float ratio) {
    if (ratio < 0.15f) return 0f;
    if (ratio < 0.375f) return 0.25f;
    if (ratio < 0.5f) return 0.45f;
    if (ratio < 0.525f) return 0.5f;
    if (ratio < 0.6f) return 0.55f;
    if (ratio < 0.85f) return 0.75f;
    return 1f;
  }
}
