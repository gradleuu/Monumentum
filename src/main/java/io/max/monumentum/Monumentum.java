package io.max.monumentum;

import io.max.monumentum.config.ConfigStore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Monumentum implements ModInitializer {
  public static final String MOD_ID = "monumentum";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final ConfigStore CONFIG =
          ConfigStore.createAndLoad(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json"));

  @Override
  public void onInitialize() {}
}