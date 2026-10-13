package io.max.monumentum.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.max.monumentum.net.ServerMessage.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

final class ServerMessageDecoder {
  private ServerMessageDecoder() {}

  /**
   * @throws RuntimeException on malformed JSON or wrong value types
   */
  static ServerMessage decode(String json) {
    JsonObject o = JsonParser.parseString(json).getAsJsonObject();
    String type = str(o, "_type");
    if (type == null) {
      throw new IllegalArgumentException("missing _type");
    }
    return switch (type) {
      // absent = false
      case "ServerInfoPacket" -> new ServerInfo(bool(o, "allowPublicizeContent"));
      case "LocationUpdatedPacket" -> new Location(req(o, "shard"), str(o, "content"));
      case "ClassUpdatePacket" ->
          new ClassUpdate(list(o, "abilities", ServerMessageDecoder::abilityState));
      case "AbilityUpdatePacket" ->
          new AbilityUpdate(
              name(o),
              integer(o, "remainingCooldown"),
              integer(o, "remainingCharges"),
              str(o, "mode"),
              boxed(o, "remainingDuration"),
              boxed(o, "initialDuration"));
      case "PlayerStatusPacket" -> new PlayerStatus(integer(o, "silenceDuration"));
      case "StrikeChestUpdatePacket" -> new StrikeChest(integer(o, "newLimit"), boxed(o, "count"));
      case "MassEffectUpdatePacket" ->
          new MassEffectUpdate(list(o, "effects", ServerMessageDecoder::effect));
      case "EffectUpdatePacket" -> new EffectUpdate(effect(o.getAsJsonObject("effect")));
      default -> new Unknown(type);
    };
  }

  private static AbilityState abilityState(JsonObject o) {
    return new AbilityState(
        name(o),
        str(o, "className"),
        integer(o, "remainingCooldown"),
        integer(o, "initialCooldown"),
        integer(o, "remainingCharges"),
        integer(o, "maxCharges"),
        str(o, "mode"),
        boxed(o, "remainingDuration"),
        boxed(o, "initialDuration"));
  }

  private static Effect effect(JsonObject o) {
    return new Effect(
        req(o, "UUID"),
        integer(o, "displayPriority"),
        Objects.requireNonNullElse(str(o, "name"), ""),
        integer(o, "duration"),
        dbl(o, "power"),
        bool(o, "positive"),
        bool(o, "percentage"));
  }

  private static <T> List<T> list(JsonObject o, String key, Function<JsonObject, T> mapper) {
    JsonElement e = o.get(key);
    if (e == null || e.isJsonNull()) {
      // Gson omits null arrays
      return List.of();
    }
    List<T> out = new ArrayList<>();
    for (JsonElement item : e.getAsJsonArray()) {
      out.add(mapper.apply(item.getAsJsonObject()));
    }
    return List.copyOf(out);
  }

  /** The server marks ability names @Nullable; keep the packet and let consumers filter blanks */
  private static String name(JsonObject o) {
    return Objects.requireNonNullElse(str(o, "name"), "");
  }

  private static @Nullable String str(JsonObject o, String key) {
    JsonElement e = o.get(key);
    return e == null || e.isJsonNull() ? null : e.getAsString();
  }

  private static String req(JsonObject o, String key) {
    String v = str(o, key);
    if (v == null) {
      throw new IllegalArgumentException("missing " + key);
    }
    return v;
  }

  private static int integer(JsonObject o, String key) {
    JsonElement e = o.get(key);
    return e == null || e.isJsonNull() ? 0 : e.getAsInt();
  }

  private static @Nullable Integer boxed(JsonObject o, String key) {
    JsonElement e = o.get(key);
    return e == null || e.isJsonNull() ? null : e.getAsInt();
  }

  private static double dbl(JsonObject o, String key) {
    JsonElement e = o.get(key);
    return e == null || e.isJsonNull() ? 0 : e.getAsDouble();
  }

  private static boolean bool(JsonObject o, String key) {
    JsonElement e = o.get(key);
    return e != null && !e.isJsonNull() && e.getAsBoolean();
  }
}
