package io.max.monumentum.config.screen;

import io.max.monumentum.util.Kaleidoscope;
import io.wispforest.owo.ui.core.Surface;

final class ConfigSurfaces {
  static final Surface BOTTOM_HAIRLINE =
      (context, component) ->
          context.fill(
              component.x(),
              component.y() + component.height() - 1,
              component.x() + component.width(),
              component.y() + component.height(),
              Kaleidoscope.CONFIG_HAIRLINE.rgb());

  private ConfigSurfaces() {}

  static Surface leftGuide(int x, int color) {
    return (context, component) ->
        context.fill(
            component.x() + x,
            component.y() + 2,
            component.x() + x + 1,
            component.y() + component.height() - 2,
            color);
  }
}
