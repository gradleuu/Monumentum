package io.max.monumentum.feature;

import io.max.monumentum.Monumentum;
import io.max.monumentum.config.HudFeatureOptions;
import io.max.monumentum.hud.HudPosition;
import io.max.monumentum.hud.HudWidget;
import java.util.function.Supplier;

public abstract class HudFeature extends Feature implements HudWidget {
  private final Supplier<? extends HudFeatureOptions> hudOptions;
  private volatile Runnable redrawRequest = () -> {};

  protected HudFeature(String name, Supplier<? extends HudFeatureOptions> options) {
    super(name, options);
    this.hudOptions = options;
  }

  protected abstract boolean hasSomethingToShow();

  protected abstract void listenForChanges();

  protected final void requestRedraw() {
    redrawRequest.run();
  }

  @Override
  protected final void hook() {
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
    HudFeatureOptions o = hudOptions.get();
    return new HudPosition(o.anchorX, o.anchorY, o.offsetX, o.offsetY);
  }

  @Override
  public final void setPosition(HudPosition position) {
    Monumentum.CONFIG.edit(
            () -> {
              HudFeatureOptions o = hudOptions.get();
              o.anchorX = position.anchorX();
              o.anchorY = position.anchorY();
              o.offsetX = position.offsetX();
              o.offsetY = position.offsetY();
            });
  }

  @Override
  public final void setRedrawRequest(Runnable redrawRequest) {
    this.redrawRequest = redrawRequest;
  }
}