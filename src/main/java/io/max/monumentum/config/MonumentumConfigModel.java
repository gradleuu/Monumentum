package io.max.monumentum.config;

import io.max.monumentum.Monumentum;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;

@Modmenu(modId = Monumentum.MOD_ID)
@Config(name = "Monumentum", wrapperName = "MonumentumConfig")
public final class MonumentumConfigModel {
  @Nest public ApiOptions api = new ApiOptions();
}