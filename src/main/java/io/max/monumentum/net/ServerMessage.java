package io.max.monumentum.net;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public sealed interface ServerMessage {
  record ServerInfo(boolean allowPublicizeContent) implements ServerMessage {}

  record Location(String shard, @Nullable String content) implements ServerMessage {}

  record ClassUpdate(List<AbilityState> abilities) implements ServerMessage {}

  record AbilityUpdate(
      String name,
      int remainingCooldown,
      int remainingCharges,
      @Nullable String mode,
      @Nullable Integer remainingDuration,
      @Nullable Integer initialDuration)
      implements ServerMessage {}

  record PlayerStatus(int silenceDuration) implements ServerMessage {}

  record StrikeChest(int newLimit, @Nullable Integer count) implements ServerMessage {}

  record MassEffectUpdate(List<Effect> effects) implements ServerMessage {}

  /** {@code duration == 0} means the effect was removed */
  record EffectUpdate(Effect effect) implements ServerMessage {}

  record Unknown(String type) implements ServerMessage {}

  record AbilityState(
      String name,
      @Nullable String className,
      int remainingCooldown,
      int initialCooldown,
      int remainingCharges,
      int maxCharges,
      @Nullable String mode,
      @Nullable Integer remainingDuration,
      @Nullable Integer initialDuration) {}

  record Effect(
      String uuid,
      int displayPriority,
      String name,
      int duration,
      double power,
      boolean positive,
      boolean percentage) {}
}
