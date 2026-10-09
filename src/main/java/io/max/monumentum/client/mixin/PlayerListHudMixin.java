package io.max.monumentum.client.mixin;

import io.max.monumentum.client.event.TablistHeaderCallback;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
  @Inject(method = "setHeader", at = @At("TAIL"))
  private void monumentum$onSetHeader(@Nullable Text header, CallbackInfo ci) {
    TablistHeaderCallback.EVENT.invoker().onHeader(header);
  }
}
