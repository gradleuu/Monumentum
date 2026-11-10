package io.max.monumentum.feature;

import static io.max.monumentum.Monumentum.LOGGER;

import io.max.monumentum.Monumentum;
import io.max.monumentum.hud.MonumentumHud;
import io.max.monumentum.util.Ticker;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class Feature {
  final String name;
  private final Identifier id;
  private ConfigWrapper<?> config;
  private MonumentumHud hud;
  private Ticker ticker;
  private Option<Boolean> turnedOnOption;
  private volatile boolean brokenByError;

  protected Feature(String name) {
    this.name = name;
    this.id = new Identifier(Monumentum.MOD_ID, name);
  }

  public final Identifier id() {
    return id;
  }

  public final boolean isTurnedOn() {
    return turnedOnOption.value();
  }

  public final void setTurnedOn(boolean turnedOn) {
    turnedOnOption.set(turnedOn);
  }

  protected final MonumentumHud hud() {
    return hud;
  }

  protected final <T> Option<T> savedOption(String optionName) {
    Option<T> option = config.optionForKey(new Option.Key(name + "." + optionName));
    if (option == null) {
      throw new IllegalStateException("the config has no option " + name + "." + optionName);
    }
    return option;
  }

  final void startFeature(ConfigWrapper<?> config, MonumentumHud hud, Ticker ticker) {
    this.config = config;
    this.hud = hud;
    this.ticker = ticker;
    turnedOnOption = savedOption("enabled");
    try {
      hook();
    } catch (RuntimeException e) {
      disableBecauseOf("starting", e);
    }
  }

  protected abstract void hook();

  protected final boolean isTurnedOnAndNotBroken() {
    return !brokenByError && isTurnedOn();
  }

  protected final void runOrDisableFeature(String where, Runnable code) {
    if (!isTurnedOnAndNotBroken()) {
      return;
    }
    try {
      code.run();
    } catch (RuntimeException e) {
      disableBecauseOf(where, e);
    }
  }

  protected final <T> T getOrDisableFeature(String where, T fallback, Supplier<T> code) {
    if (!isTurnedOnAndNotBroken()) {
      return fallback;
    }
    try {
      return code.get();
    } catch (RuntimeException e) {
      disableBecauseOf(where, e);
      return fallback;
    }
  }

  protected final void everyTickWhileInGame(String where, Consumer<ClientPlayerEntity> code) {
    everyNTicksWhileInGame(where, 1, code);
  }

  protected final void everyNTicksWhileInGame(
      String where, int everyTicks, Consumer<ClientPlayerEntity> code) {
    ticker.everyNTicks(everyTicks, player -> runOrDisableFeature(where, () -> code.accept(player)));
  }

  protected final void whenBecomesTrue(
      String where, Predicate<ClientPlayerEntity> condition, Runnable action) {
    ticker.whenBecomesTrue(
        player -> getOrDisableFeature(where, false, () -> condition.test(player)),
        () -> runOrDisableFeature(where, action));
  }

  private void disableBecauseOf(String where, RuntimeException e) {
    brokenByError = true;
    LOGGER.error("Feature {} broke in {} and is switched off", name, where, e);
  }
}
