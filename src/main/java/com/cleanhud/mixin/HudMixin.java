package com.cleanhud.mixin;

import com.cleanhud.CleanHUDConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.Hud")
public class HudMixin {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true, require = 0)
    private void cleanHud$hideVanillaEffects(CallbackInfo callbackInfo) {
        if (CleanHUDConfig.INSTANCE.effectHudPosition.showsHud()) {
            callbackInfo.cancel();
        }
    }
}
