package com.cleanhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.resources.Identifier;

public class CleanHUDClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CleanHUDConfig.load();
		CleanHUD.LOGGER.info("Clean HUD initialized");

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(CleanHUD.MOD_ID, "hud"), (graphics, tickCounter) -> CleanHUDRenderer.render(graphics));
	}
}
