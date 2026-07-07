package com.cleanhud;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.StateManager;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CleanHUDConfigScreen {
	public static Screen create(Screen parent) {
		final CleanHUDPreset[] selectedPreset = {CleanHUDConfig.INSTANCE.preset};
		PresetState presetState = new PresetState(selectedPreset);

		Option<ArmorHudPosition> armorPositionOption = armorPositionOption(
				() -> CleanHUDConfig.INSTANCE.armorHudPosition,
				value -> {
					CleanHUDConfig.INSTANCE.armorHudPosition = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().armorHudPosition
		);

		Option<ArmorHudStyle> armorStyleOption = armorStyleOption(
				() -> CleanHUDConfig.INSTANCE.armorHudStyle,
				value -> {
					CleanHUDConfig.INSTANCE.armorHudStyle = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().armorHudStyle
		);

		Option<EffectHudPosition> effectPositionOption = effectPositionOption(
				() -> CleanHUDConfig.INSTANCE.effectHudPosition,
				value -> {
					CleanHUDConfig.INSTANCE.effectHudPosition = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().effectHudPosition
		);

		Option<EffectHudStyle> effectStyleOption = effectStyleOption(
				() -> CleanHUDConfig.INSTANCE.effectHudStyle,
				value -> {
					CleanHUDConfig.INSTANCE.effectHudStyle = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().effectHudStyle
		);

		Option<ArrowDisplayMode> arrowDisplayOption = arrowDisplayModeOption(
				() -> CleanHUDConfig.INSTANCE.arrowDisplayMode,
				value -> {
					CleanHUDConfig.INSTANCE.arrowDisplayMode = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().arrowDisplayMode
		);

		Option<Boolean> arrowRequireBowOption = toggleOption(
				"Require Bow/Crossbow",
				Component.literal("Only shows the arrow display while holding a bow or crossbow."),
				() -> CleanHUDConfig.INSTANCE.arrowRequireBow,
				value -> {
					CleanHUDConfig.INSTANCE.arrowRequireBow = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().arrowRequireBow
		);

		Option<Boolean> separateArrowTypesOption = toggleOption(
				"Separate Arrow Types",
				Component.literal("Shows each arrow type separately instead of showing all arrows combined."),
				() -> CleanHUDConfig.INSTANCE.separateArrowTypes,
				value -> {
					CleanHUDConfig.INSTANCE.separateArrowTypes = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().separateArrowTypes
		);

		Option<ItemDisplayMode> itemDisplayModeOption = itemDisplayModeOption(
				() -> CleanHUDConfig.INSTANCE.itemDisplayMode,
				value -> {
					CleanHUDConfig.INSTANCE.itemDisplayMode = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().itemDisplayMode
		);

		Option<Boolean> itemRequireHeldOption = toggleOption(
				"Require Held Item",
				Component.literal("Only shows the custom item display while holding that item."),
				() -> CleanHUDConfig.INSTANCE.itemRequireHeld,
				value -> {
					CleanHUDConfig.INSTANCE.itemRequireHeld = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().itemRequireHeld
		);

		Option<String> customItemDisplaysOption = customItemDisplaysOption(
				() -> CleanHUDConfig.itemIdsToText(CleanHUDConfig.INSTANCE.customItemDisplays),
				value -> {
					CleanHUDConfig.INSTANCE.customItemDisplays = CleanHUDConfig.parseItemIds(value);
					presetState.markCustom();
				},
				CleanHUDConfig.itemIdsToText(CleanHUDConfig.defaults().customItemDisplays)
		);

		Option<ClockPosition> clockOption = clockPositionOption(
				() -> CleanHUDConfig.INSTANCE.clockPosition,
				value -> {
					CleanHUDConfig.INSTANCE.clockPosition = value;
					presetState.markCustom();
				},
				CleanHUDConfig.defaults().clockPosition
		);

		Option<CleanHUDPreset> presetOption = presetOption(
				selectedPreset,
				presetState,
				armorPositionOption,
				armorStyleOption,
				effectPositionOption,
				effectStyleOption
		);

		presetState.presetOption = presetOption;

		return YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Clean HUD"))
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("HUD"))
						.option(presetOption)
						.option(armorPositionOption)
						.option(armorStyleOption)
						.option(effectPositionOption)
						.option(effectStyleOption)
						.option(arrowDisplayOption)
						.option(arrowRequireBowOption)
						.option(separateArrowTypesOption)
						.option(itemDisplayModeOption)
						.option(itemRequireHeldOption)
						.option(customItemDisplaysOption)
						.option(clockOption)
						.build())
				.save(() -> saveConfig(selectedPreset))
				.build()
				.generateScreen(parent);
	}

	private static Option<CleanHUDPreset> presetOption(
			CleanHUDPreset[] selectedPreset,
			PresetState presetState,
			Option<ArmorHudPosition> armorPositionOption,
			Option<ArmorHudStyle> armorStyleOption,
			Option<EffectHudPosition> effectPositionOption,
			Option<EffectHudStyle> effectStyleOption
	) {
		return Option.<CleanHUDPreset>createBuilder()
				.name(Component.literal("Preset"))
				.description(OptionDescription.of(presetDescription()))
				.stateManager(StateManager.createSimple(CleanHUDConfig.defaults().preset, () -> selectedPreset[0], value -> {

					selectedPreset[0] = value;

					if (value == CleanHUDPreset.CUSTOM) {
						CleanHUDConfig.markCustomPreset();
						return;
					}

					presetState.applyingPreset = true;
					try {
						CleanHUDConfig.applyPreset(value);
						armorPositionOption.requestSet(CleanHUDConfig.INSTANCE.armorHudPosition);
						armorStyleOption.requestSet(CleanHUDConfig.INSTANCE.armorHudStyle);
						effectPositionOption.requestSet(CleanHUDConfig.INSTANCE.effectHudPosition);
						effectStyleOption.requestSet(CleanHUDConfig.INSTANCE.effectHudStyle);
					} finally {
						presetState.applyingPreset = false;
					}
				}))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(CleanHUDPreset.class).formatValue(CleanHUDConfigScreen::presetComponent))
				.build();
	}

	private static Option<Boolean> toggleOption(String name, Component description, Supplier<Boolean> getter, Consumer<Boolean> setter, boolean defaultValue) {
		return Option.<Boolean>createBuilder()
				.name(Component.literal(name))
				.description(OptionDescription.of(description))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(TickBoxControllerBuilder::create)
				.build();
	}

	private static Option<String> customItemDisplaysOption(Supplier<String> getter, Consumer<String> setter, String defaultValue) {
		return Option.<String>createBuilder()
				.name(Component.literal("Custom Item Display"))
				.description(OptionDescription.of(customItemDisplaysDescription()))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(StringControllerBuilder::create)
				.build();
	}

	private static Option<ArmorHudPosition> armorPositionOption(Supplier<ArmorHudPosition> getter, Consumer<ArmorHudPosition> setter, ArmorHudPosition defaultValue) {
		return Option.<ArmorHudPosition>createBuilder()
				.name(Component.literal("Armor HUD Position"))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(ArmorHudPosition.class).formatValue(CleanHUDConfigScreen::armorPositionComponent))
				.build();
	}

	private static Option<ArmorHudStyle> armorStyleOption(Supplier<ArmorHudStyle> getter, Consumer<ArmorHudStyle> setter, ArmorHudStyle defaultValue) {
		return Option.<ArmorHudStyle>createBuilder()
				.name(Component.literal("Armor HUD Background"))
				.description(OptionDescription.of(armorStyleDescription()))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(ArmorHudStyle.class).formatValue(CleanHUDConfigScreen::armorStyleComponent))
				.build();
	}

	private static Option<EffectHudPosition> effectPositionOption(Supplier<EffectHudPosition> getter, Consumer<EffectHudPosition> setter, EffectHudPosition defaultValue) {
		return Option.<EffectHudPosition>createBuilder()
				.name(Component.literal("Effect HUD Position"))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(EffectHudPosition.class).formatValue(CleanHUDConfigScreen::effectPositionComponent))
				.build();
	}

	private static Option<EffectHudStyle> effectStyleOption(Supplier<EffectHudStyle> getter, Consumer<EffectHudStyle> setter, EffectHudStyle defaultValue) {
		return Option.<EffectHudStyle>createBuilder()
				.name(Component.literal("Effect HUD Background"))
				.description(OptionDescription.of(effectStyleDescription()))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(EffectHudStyle.class).formatValue(CleanHUDConfigScreen::effectStyleComponent))
				.build();
	}

	private static Option<ArrowDisplayMode> arrowDisplayModeOption(Supplier<ArrowDisplayMode> getter, Consumer<ArrowDisplayMode> setter, ArrowDisplayMode defaultValue) {
		return Option.<ArrowDisplayMode>createBuilder()
				.name(Component.literal("Arrow Display"))
				.description(OptionDescription.of(hotbarModeDescription("arrows", "arrow display")))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(ArrowDisplayMode.class).formatValue(CleanHUDConfigScreen::arrowDisplayModeComponent))
				.build();
	}

	private static Option<ItemDisplayMode> itemDisplayModeOption(Supplier<ItemDisplayMode> getter, Consumer<ItemDisplayMode> setter, ItemDisplayMode defaultValue) {
		return Option.<ItemDisplayMode>createBuilder()
				.name(Component.literal("Item Display Mode"))
				.description(OptionDescription.of(hotbarModeDescription("matching items", "custom item display")))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(ItemDisplayMode.class).formatValue(CleanHUDConfigScreen::itemDisplayModeComponent))
				.build();
	}

	private static Option<ClockPosition> clockPositionOption(Supplier<ClockPosition> getter, Consumer<ClockPosition> setter, ClockPosition defaultValue) {
		return Option.<ClockPosition>createBuilder()
				.name(Component.literal("Clock Position"))
				.description(OptionDescription.of(Component.literal("Controls where the system time is shown.")))
				.stateManager(StateManager.createSimple(defaultValue, getter, setter))
				.controller(option -> EnumControllerBuilder.create(option).enumClass(ClockPosition.class).formatValue(CleanHUDConfigScreen::clockPositionComponent))
				.build();
	}

	private static Component armorStyleDescription() {
		return Component.literal("")
				.append(yellowName("Hotbar"))
				.append(Component.literal(" uses the hotbar texture.\n"))
				.append(yellowName("Offhand"))
				.append(Component.literal(" uses the offhand slot texture.\n"))
				.append(offName())
				.append(Component.literal(" hides the armor background."));
	}

	private static Component effectStyleDescription() {
		return Component.literal("")
				.append(yellowName("Offhand"))
				.append(Component.literal(" uses the offhand slot texture.\n"))
				.append(offName())
				.append(Component.literal(" hides the effect background."));
	}

	private static Component presetDescription() {
		return Component.literal("")
				.append(bold("Hotbar"))
				.append(Component.literal(" shows armor and effects beside the hotbar.\n"))
				.append(bold("Dynamic"))
				.append(Component.literal(" moves armor to the left and effects to the top."));
	}

	private static Component hotbarModeDescription(String itemName, String displayName) {
		return Component.literal("")
				.append(optionName("Include Hotbar"))
				.append(Component.literal(" counts all " + itemName + ".\n"))
				.append(optionName("Exclude Hotbar"))
				.append(Component.literal(" only counts " + itemName + " outside the hotbar.\n"))
				.append(offName())
				.append(Component.literal(" hides the " + displayName + "."));
	}

	private static Component customItemDisplaysDescription() {
		return Component.literal("Items to show beside the hotbar.\n\n")
				.append(bold("Example"))
				.append(Component.literal(": minecraft:totem_of_undying, cobblestone"));
	}

	private static Component coloredName(String text, ChatFormatting color) {
		return Component.literal(text).withStyle(color).withStyle(ChatFormatting.BOLD);
	}

	private static Component optionName(String text) {
		return coloredName(text, ChatFormatting.GREEN);
	}

	private static Component yellowName(String text) {
		return coloredName(text, ChatFormatting.YELLOW);
	}

	private static Component offName() {
		return coloredName("Off", ChatFormatting.RED);
	}

	private static Component bold(String text) {
		return Component.literal(text).withStyle(ChatFormatting.BOLD);
	}

	private static void saveConfig(CleanHUDPreset[] selectedPreset) {
		if (selectedPreset[0] == null) {
			selectedPreset[0] = CleanHUDConfig.INSTANCE.preset == null ? CleanHUDPreset.CUSTOM : CleanHUDConfig.INSTANCE.preset;
		}

		if (selectedPreset[0] == CleanHUDPreset.CUSTOM) {
			CleanHUDConfig.markCustomPreset();
		} else {
			CleanHUDConfig.applyPreset(selectedPreset[0]);
		}

		CleanHUDConfig.save();
	}

	private static Component presetComponent(CleanHUDPreset value) {
		return switch (value) {
			case HOTBAR -> Component.literal("Hotbar").withStyle(ChatFormatting.WHITE);
			case DYNAMIC -> Component.literal("Dynamic").withStyle(ChatFormatting.WHITE);
			case CUSTOM -> Component.literal("Custom").withStyle(ChatFormatting.WHITE);
		};
	}

	private static Component armorStyleComponent(ArmorHudStyle value) {
		return switch (value) {
			case HOTBAR -> Component.literal("Hotbar").withStyle(ChatFormatting.YELLOW);
			case OFFHAND -> Component.literal("Offhand").withStyle(ChatFormatting.YELLOW);
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
		};
	}

	private static Component effectStyleComponent(EffectHudStyle value) {
		return switch (value) {
			case OFFHAND -> Component.literal("Offhand").withStyle(ChatFormatting.YELLOW);
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
		};
	}

	private static Component arrowDisplayModeComponent(ArrowDisplayMode value) {
		return switch (value) {
			case WITH_HOTBAR -> Component.literal("Include Hotbar").withStyle(ChatFormatting.GREEN);
			case NO_HOTBAR -> Component.literal("Exclude Hotbar").withStyle(ChatFormatting.GREEN);
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
		};
	}

	private static Component itemDisplayModeComponent(ItemDisplayMode value) {
		return switch (value) {
			case WITH_HOTBAR -> Component.literal("Include Hotbar").withStyle(ChatFormatting.GREEN);
			case NO_HOTBAR -> Component.literal("Exclude Hotbar").withStyle(ChatFormatting.GREEN);
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
		};
	}

	private static Component armorPositionComponent(ArmorHudPosition value) {
		return switch (value) {
			case BOTTOM -> Component.literal("Bottom").withStyle(ChatFormatting.WHITE);
			case LEFT -> Component.literal("Left").withStyle(ChatFormatting.WHITE);
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
		};
	}

	private static Component effectPositionComponent(EffectHudPosition value) {
		return switch (value) {
			case BOTTOM -> Component.literal("Bottom").withStyle(ChatFormatting.WHITE);
			case TOP -> Component.literal("Top").withStyle(ChatFormatting.WHITE);
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
		};
	}

	private static Component clockPositionComponent(ClockPosition value) {
		return switch (value) {
			case OFF -> Component.literal("Off").withStyle(ChatFormatting.RED);
			case TOP_LEFT -> Component.literal("Top Left").withStyle(ChatFormatting.WHITE);
			case TOP_RIGHT -> Component.literal("Top Right").withStyle(ChatFormatting.WHITE);
			case BOTTOM_LEFT -> Component.literal("Bottom Left").withStyle(ChatFormatting.WHITE);
			case BOTTOM_RIGHT -> Component.literal("Bottom Right").withStyle(ChatFormatting.WHITE);
		};
	}

	private static class PresetState {
		private final CleanHUDPreset[] selectedPreset;
		private boolean applyingPreset;
		private Option<CleanHUDPreset> presetOption;

		private PresetState(CleanHUDPreset[] selectedPreset) {
			this.selectedPreset = selectedPreset;
		}

		private void markCustom() {
			if (applyingPreset) {
				return;
			}

			CleanHUDConfig.markCustomPreset();
			selectedPreset[0] = CleanHUDPreset.CUSTOM;

			if (presetOption != null) {
				presetOption.requestSet(CleanHUDPreset.CUSTOM);
			}
		}
	}
}
