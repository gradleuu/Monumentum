package io.max.monumentum.feature;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.api.MonumentumApi;
import io.max.monumentum.feature.features.CrimsonContractSwitchReminderFeature;
import io.max.monumentum.feature.features.StrikeChestsHudFeature;
import io.max.monumentum.hud.MonumentumHud;
import io.max.monumentum.util.Ticker;
import io.wispforest.owo.config.ConfigWrapper;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class Features {
  private final ConfigWrapper<?> config;
  private final MonumentumHud hud;
  private final Ticker ticker;
  private final Set<String> usedNames = new HashSet<>();

  private Features(ConfigWrapper<?> config, MonumentumHud hud, Ticker ticker) {
    this.config = config;
    this.hud = hud;
    this.ticker = ticker;
  }

  public static void startAll(ConfigWrapper<?> config, MonumentumHud hud, Ticker ticker) {
    Features all = new Features(config, hud, ticker);

    all.start(() -> new StrikeChestsHudFeature(MonumentumApi.strikes()));
    all.start(CrimsonContractSwitchReminderFeature::new);
  }

  private void start(Supplier<Feature> createFeature) {
    Feature feature;
    try {
      feature = createFeature.get();
    } catch (RuntimeException | LinkageError e) {
      LOGGER.error("A feature can't be created", e);
      return;
    }
    try {
      if (!usedNames.add(feature.name)) {
        throw new IllegalStateException("the name " + feature.name + " is used twice");
      }
      feature.startFeature(config, hud, ticker);
    } catch (RuntimeException | LinkageError e) {
      LOGGER.error("Feature {} can't be started", feature.name, e);
    }
  }
}
