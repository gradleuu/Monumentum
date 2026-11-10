package io.max.monumentum.util;

import static io.max.monumentum.Monumentum.LOGGER;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;

public final class Ticker {
  private static final class Task {
    private final int everyTicks;
    private final Consumer<ClientPlayerEntity> code;
    private final Runnable whenNotInGame;
    private int ticksSinceRun;

    private Task(int everyTicks, Consumer<ClientPlayerEntity> code, Runnable whenNotInGame) {
      this.everyTicks = everyTicks;
      this.code = code;
      this.whenNotInGame = whenNotInGame;
    }

    private void tick(ClientPlayerEntity player) {
      if (++ticksSinceRun >= everyTicks) {
        ticksSinceRun = 0;
        code.accept(player);
      }
    }

    private void leftGame() {
      ticksSinceRun = 0;
      whenNotInGame.run();
    }
  }

  private final List<Task> tasks = new CopyOnWriteArrayList<>();

  public void start() {
    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          ClientPlayerEntity player = client.player;
          boolean inGame = player != null && client.world != null;
          for (Task task : tasks) {
            try {
              if (inGame) {
                task.tick(player);
              } else {
                task.leftGame();
              }
            } catch (RuntimeException e) {
              LOGGER.error("An in-game tick task failed", e);
            }
          }
        });
  }

  public void everyNTicks(int everyTicks, Consumer<ClientPlayerEntity> code) {
    tasks.add(new Task(Math.max(1, everyTicks), code, () -> {}));
  }

  public void whenBecomesTrue(Predicate<ClientPlayerEntity> condition, Runnable action) {
    boolean[] wasTrue = {false};
    tasks.add(
        new Task(
            1,
            player -> {
              boolean isTrue = condition.test(player);
              boolean justBecameTrue = isTrue && !wasTrue[0];
              wasTrue[0] = isTrue;
              if (justBecameTrue) {
                action.run();
              }
            },
            () -> wasTrue[0] = false));
  }
}
