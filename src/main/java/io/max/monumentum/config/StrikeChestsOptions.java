package io.max.monumentum.config;

import io.wispforest.owo.config.annotation.ExcludeFromScreen;

public class StrikeChestsOptions {
  public boolean enabled = true;
  @ExcludeFromScreen public float anchorX = 0.5f;
  @ExcludeFromScreen public float anchorY = 0.05f;
  @ExcludeFromScreen public int offsetX = 0;
  @ExcludeFromScreen public int offsetY = 0;
}
