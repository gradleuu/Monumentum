package io.max.monumentum.config;

import static io.max.monumentum.Monumentum.LOGGER;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public final class ConfigStore {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private final Path file;
  private volatile MonumentumConfig config = new MonumentumConfig();
  private String lastJson = "";

  private ConfigStore(Path file) {
    this.file = file;
  }

  public static ConfigStore createAndLoad(Path file) {
    ConfigStore store = new ConfigStore(file);
    store.reload();
    Thread watcher = new Thread(store::watch, "monumentum-config-watcher");
    watcher.setDaemon(true);
    watcher.start();
    return store;
  }

  public MonumentumConfig get() {
    return config;
  }

  public synchronized void edit(Runnable change) {
    change.run();
    write();
  }

  private synchronized void reload() {
    String json;
    try {
      if (Files.notExists(file)) {
        write();
        return;
      }
      json = Files.readString(file);
    } catch (IOException e) {
      LOGGER.warn("Can't read {}", file, e);
      return;
    }
    if (json.equals(lastJson)) {
      return;
    }
    lastJson = json;
    try {
      MonumentumConfig loaded = GSON.fromJson(json, MonumentumConfig.class);
      if (loaded == null) {
        throw new JsonParseException("empty file");
      }
      config = loaded;
      LOGGER.info("Reloaded {}", file.getFileName());
    } catch (JsonParseException e) {
      LOGGER.warn(
          "Invalid {}, keeping the previous config: {}", file.getFileName(), e.getMessage());
    }
  }

  private void write() {
    String json = GSON.toJson(config);
    try {
      Path temp = file.resolveSibling(file.getFileName() + ".tmp");
      Files.writeString(temp, json);
      Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      lastJson = json;
    } catch (IOException e) {
      LOGGER.error("Can't save {}", file, e);
    }
  }

  private void watch() {
    try (WatchService service = file.getFileSystem().newWatchService()) {
      file.getParent().register(service, ENTRY_CREATE, ENTRY_MODIFY);
      while (true) {
        WatchKey key = service.take();
        boolean changed = false;
        for (WatchEvent<?> event : key.pollEvents()) {
          changed |= event.kind() == OVERFLOW || file.getFileName().equals(event.context());
        }
        if (!key.reset()) {
          return;
        }
        if (changed) {
          Thread.sleep(100);
          reload();
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.error("Turning off ConfigStore watcher! It's a bug! Restart to fix - if it is not fixed PLEASE report!", e);
    }
  }
}
