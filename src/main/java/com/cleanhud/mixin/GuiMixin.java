package com.cleanhud.mixin;

import com.cleanhud.CleanHUDConfig;

import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = {"extractEffects", "extractStatusEffects", "extractMobEffects"}, at = @At("HEAD"), cancellable = true, require = 0)
	private void cleanHud$hideVanillaEffects(CallbackInfo callbackInfo) {
		if (CleanHUDConfig.INSTANCE.effectHudPosition.showsHud()) {
			callbackInfo.cancel();
		}
	}
}
