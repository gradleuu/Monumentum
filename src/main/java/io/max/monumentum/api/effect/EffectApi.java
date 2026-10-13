package io.max.monumentum.api.effect;

import java.util.List;

public interface EffectApi {
  /**
   * Gives the effects the player has right now.
   *
   * @apiNote The list never changes after you get it, but each effect's {@link
   *     Effect#remainingTicks()} keeps counting down.
   * @return the effects, never {@code null}, with the highest {@link Effect#displayPriority()}
   *     first. Effects with the same priority are ordered by name. The list is empty when the
   *     player has no effects, when the server didn't tell yet, and while the player is moving
   *     between shards.
   */
  List<Effect> effects();
}
