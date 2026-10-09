package io.max.monumentum.client.mixin;

import io.max.monumentum.client.event.WorldChangeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.max.monumentum.Monumentum.LOGGER;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
  @Inject(method = "setWorld", at = @At("TAIL"))
  private void monumentum$onSetWorld(@Nullable ClientWorld world, CallbackInfo ci) {
    WorldChangeCallback.EVENT.invoker().onWorldChange(world);
  }
}
