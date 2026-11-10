package io.max.monumentum.feature;

import io.max.monumentum.hud.HudPosition;
import io.max.monumentum.hud.HudWidget;
import io.wispforest.owo.config.Option;

public abstract class HudFeature extends Feature implements HudWidget {
  private volatile Runnable redrawRequest = () -> {};
  private Option<Float> anchorX;
  private Option<Float> anchorY;
  private Option<Integer> offsetX;
  private Option<Integer> offsetY;

  protected HudFeature(String name) {
    super(name);
  }

  protected abstract boolean hasSomethingToShow();

  protected abstract void listenForChanges();

  protected final void requestRedraw() {
    redrawRequest.run();
  }

  @Override
  protected final void hook() {
    anchorX = savedOption("anchorX");
    anchorY = savedOption("anchorY");
    offsetX = savedOption("offsetX");
    offsetY = savedOption("offsetY");
    hud().addWidget(this);
    listenForChanges();
  }

  @Override
  public final boolean isVisibleNow() {
    return isTurnedOnAndNotBroken() && hasSomethingToShow();
  }

  @Override
  public String editorLabel() {
    return name.replace('_', ' ');
  }

  @Override
  public final HudPosition position() {
    return new HudPosition(anchorX.value(), anchorY.value(), offsetX.value(), offsetY.value());
  }

  @Override
  public final void setPosition(HudPosition position) {
    anchorX.set(position.anchorX());
    anchorY.set(position.anchorY());
    offsetX.set(position.offsetX());
    offsetY.set(position.offsetY());
  }

  @Override
  public final void setRedrawRequest(Runnable redrawRequest) {
    this.redrawRequest = redrawRequest;
  }
}
