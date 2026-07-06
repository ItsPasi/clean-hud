package com.cleanhud;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class CleanHUDConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(CleanHUD.MOD_ID + ".json");
	private static final CleanHUDConfig DEFAULTS = new CleanHUDConfig();
	private static final Pattern ITEM_ID_PATTERN = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

	public static CleanHUDConfig INSTANCE = new CleanHUDConfig();

	public CleanHUDPreset preset = CleanHUDPreset.HOTBAR;
	public ArmorHudPosition armorHudPosition = ArmorHudPosition.BOTTOM;
	public ArmorHudStyle armorHudStyle = ArmorHudStyle.HOTBAR;
	public EffectHudPosition effectHudPosition = EffectHudPosition.BOTTOM;
	public EffectHudStyle effectHudStyle = EffectHudStyle.OFFHAND;
	public ArrowDisplayMode arrowDisplayMode = ArrowDisplayMode.WITH_HOTBAR;
	public boolean arrowRequireBow = true;
	public boolean separateArrowTypes = false;
	public ItemDisplayMode itemDisplayMode = ItemDisplayMode.WITH_HOTBAR;
	public boolean itemRequireHeld = false;
	public List<String> customItemDisplays = new ArrayList<>();
	public ClockPosition clockPosition = ClockPosition.OFF;

	public static CleanHUDConfig defaults() {
		return DEFAULTS;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			INSTANCE = new CleanHUDConfig();
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			CleanHUDConfig loaded = GSON.fromJson(reader, CleanHUDConfig.class);
			INSTANCE = loaded != null ? loaded : new CleanHUDConfig();
			sanitize();
		} catch (Exception exception) {
			CleanHUD.LOGGER.warn("Failed to load Clean HUD config, using defaults", exception);
			INSTANCE = new CleanHUDConfig();
		}
	}

	private static void sanitize() {
		if (INSTANCE.preset == null) {
			INSTANCE.preset = CleanHUDPreset.HOTBAR;
		}

		if (INSTANCE.armorHudPosition == null) {
			INSTANCE.armorHudPosition = ArmorHudPosition.BOTTOM;
		}

		if (INSTANCE.armorHudStyle == null) {
			INSTANCE.armorHudStyle = ArmorHudStyle.HOTBAR;
		}

		if (INSTANCE.effectHudPosition == null) {
			INSTANCE.effectHudPosition = EffectHudPosition.BOTTOM;
		}

		if (INSTANCE.effectHudStyle == null) {
			INSTANCE.effectHudStyle = EffectHudStyle.OFFHAND;
		}

		if (INSTANCE.customItemDisplays == null) {
			INSTANCE.customItemDisplays = new ArrayList<>();
		} else {
			INSTANCE.customItemDisplays = parseItemIds(itemIdsToText(INSTANCE.customItemDisplays));
		}

		if (INSTANCE.arrowDisplayMode == null) {
			INSTANCE.arrowDisplayMode = ArrowDisplayMode.WITH_HOTBAR;
		}

		if (INSTANCE.itemDisplayMode == null) {
			INSTANCE.itemDisplayMode = ItemDisplayMode.WITH_HOTBAR;
		}

		if (INSTANCE.clockPosition == null) {
			INSTANCE.clockPosition = ClockPosition.OFF;
		}
	}

	public static void applyPreset(CleanHUDPreset preset) {
		if (preset == null) {
			return;
		}

		INSTANCE.preset = preset;

		switch (preset) {
			case HOTBAR -> {
				INSTANCE.armorHudPosition = ArmorHudPosition.BOTTOM;
				INSTANCE.armorHudStyle = ArmorHudStyle.HOTBAR;
				INSTANCE.effectHudPosition = EffectHudPosition.BOTTOM;
				INSTANCE.effectHudStyle = EffectHudStyle.OFFHAND;
			}
			case DYNAMIC -> {
				INSTANCE.armorHudPosition = ArmorHudPosition.LEFT;
				INSTANCE.armorHudStyle = ArmorHudStyle.OFFHAND;
				INSTANCE.effectHudPosition = EffectHudPosition.TOP;
				INSTANCE.effectHudStyle = EffectHudStyle.OFF;
			}
			case CUSTOM -> {
			}
		}
	}

	public static void markCustomPreset() {
		INSTANCE.preset = CleanHUDPreset.CUSTOM;
	}

	public static List<String> parseItemIds(String text) {
		Set<String> itemIds = new LinkedHashSet<>();

		if (text == null || text.isBlank()) {
			return new ArrayList<>();
		}

		for (String rawId : text.split("[,;\\s]+")) {
			String itemId = normalizeItemId(rawId);

			if (itemId != null) {
				itemIds.add(itemId);
			}
		}

		return new ArrayList<>(itemIds);
	}

	public static String itemIdsToText(List<String> itemIds) {
		if (itemIds == null || itemIds.isEmpty()) {
			return "";
		}

		List<String> normalizedIds = new ArrayList<>();

		for (String itemId : itemIds) {
			String normalizedId = normalizeItemId(itemId);

			if (normalizedId != null) {
				normalizedIds.add(normalizedId);
			}
		}

		return String.join(", ", normalizedIds);
	}

	public static String normalizeItemId(String value) {
		if (value == null) {
			return null;
		}

		String itemId = value.trim().toLowerCase(Locale.ROOT);

		if (itemId.isEmpty()) {
			return null;
		}

		if (!itemId.contains(":")) {
			itemId = "minecraft:" + itemId;
		}

		return ITEM_ID_PATTERN.matcher(itemId).matches() ? itemId : null;
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(INSTANCE, writer);
			}
		} catch (IOException exception) {
			CleanHUD.LOGGER.warn("Failed to save Clean HUD config", exception);
		}
	}
}
