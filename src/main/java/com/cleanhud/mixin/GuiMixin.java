package com.cleanhud.mixin;

import com.cleanhud.CleanHUDConfig;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
	private void cleanHud$hideVanillaEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		if (CleanHUDConfig.INSTANCE.effectHudPosition.showsHud()) {
			callbackInfo.cancel();
		}
	}
}
