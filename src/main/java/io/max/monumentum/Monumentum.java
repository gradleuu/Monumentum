package io.max.monumentum;

import io.max.monumentum.config.MonumentumConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Monumentum implements ModInitializer {
  public static final String MOD_ID = "monumentum";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final MonumentumConfig CONFIG = MonumentumConfig.createAndLoad();

  @Override
  public void onInitialize() {}
}
