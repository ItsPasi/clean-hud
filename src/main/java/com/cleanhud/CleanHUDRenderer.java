package com.cleanhud;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CleanHUDRenderer {
	private static final int HOTBAR_WIDTH = 182;
	private static final int HOTBAR_HEIGHT = 22;
	private static final int HOTBAR_SIZE = 9;
	private static final int ITEM_SIZE = 16;
	private static final int EFFECT_ICON_SIZE = 18;
	private static final int SIDE_SLOT_ADVANCE = 29;
	private static final int GROUP_SLOT_ADVANCE = 23;
	private static final int OFFHAND_BACKGROUND_WIDTH = 29;
	private static final int OFFHAND_BACKGROUND_HEIGHT = 24;
	private static final int LEFT_OFFHAND_ITEM_OFFSET = 3;
	private static final int RIGHT_OFFHAND_ITEM_OFFSET = 10;
	private static final int EFFECT_ICON_X_OFFSET = RIGHT_OFFHAND_ITEM_OFFSET - 1;
	private static final int EFFECT_ICON_Y_OFFSET = 3;
	private static final int OFFHAND_ITEM_Y_OFFSET = 4;
	private static final int CLOCK_RIGHT_EXTRA_PADDING = 2;
	private static final int CLOCK_PADDING = 2;
	private static final int EFFECT_ROW_ADVANCE = GROUP_SLOT_ADVANCE;
	private static final int TOP_EFFECT_ROW_ADVANCE = OFFHAND_BACKGROUND_HEIGHT + 11;
	private static final int SCREEN_EDGE_PADDING = 2;

	private static final Identifier HOTBAR_OFFHAND_LEFT_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
	private static final Identifier HOTBAR_OFFHAND_RIGHT_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_offhand_right");

	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int LOW_TEXT_COLOR = 0xFFFF5555;
	private static final int SHADOW_COLOR = 0xAA000000;
	private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private static final Map<String, Integer> EFFECT_MAX_DURATIONS = new HashMap<>();

	public static void render(GuiGraphicsExtractor graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;

		if (isGuiHidden(minecraft) || player == null) {
			return;
		}

		int guiWidth = graphics.guiWidth();
		int guiHeight = graphics.guiHeight();
		int hotbarLeft = guiWidth / 2 - HOTBAR_WIDTH / 2;
		int hotbarRight = hotbarLeft + HOTBAR_WIDTH;
		int hotbarY = guiHeight - HOTBAR_HEIGHT - 1;

		if (CleanHUDConfig.INSTANCE.clockPosition.showsClock() && !isDebugScreenOpen(minecraft)) {
			renderClock(graphics, minecraft.font, guiWidth, guiHeight);
		}

		if (player.isSpectator()) {
			return;
		}

		boolean hasOffhandItem = !player.getOffhandItem().isEmpty();
		boolean offhandOnLeft = hasOffhandItem && player.getMainArm() == HumanoidArm.RIGHT;
		boolean offhandOnRight = hasOffhandItem && player.getMainArm() == HumanoidArm.LEFT;

		if (CleanHUDConfig.INSTANCE.armorHudPosition.showsHud()) {
			renderArmor(graphics, minecraft.font, player, hotbarLeft, hotbarY, offhandOnLeft, guiHeight);
		}

		int rightHudOffset = renderArrowHud(graphics, minecraft.font, player, hotbarRight, hotbarY, offhandOnRight);

		rightHudOffset += renderCustomItemDisplays(graphics, minecraft.font, player, hotbarRight, hotbarY, offhandOnRight, rightHudOffset);

		int effectRightHudOffset = rightHudOffset > 0
				? rightHudOffset + SIDE_SLOT_ADVANCE - GROUP_SLOT_ADVANCE
				: 0;

		if (CleanHUDConfig.INSTANCE.effectHudPosition.showsHud()) {
			renderEffects(graphics, minecraft, player, hotbarRight, hotbarY, offhandOnRight, guiWidth, guiHeight, effectRightHudOffset);
		}
	}

	private static void renderArmor(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, int hotbarLeft, int hotbarY, boolean offhandOnLeft, int guiHeight) {
		List<ItemStack> armorStacks = getArmorStacks(player);

		if (CleanHUDConfig.INSTANCE.armorHudPosition == ArmorHudPosition.LEFT) {
			renderArmorLeft(graphics, font, armorStacks, guiHeight);
		} else {
			renderArmorBottom(graphics, font, armorStacks, hotbarLeft, hotbarY, offhandOnLeft);
		}
	}

	private static List<ItemStack> getArmorStacks(LocalPlayer player) {
		List<ItemStack> armorStacks = new ArrayList<>();
		addIfPresent(armorStacks, player.getItemBySlot(EquipmentSlot.HEAD));
		addIfPresent(armorStacks, player.getItemBySlot(EquipmentSlot.CHEST));
		addIfPresent(armorStacks, player.getItemBySlot(EquipmentSlot.LEGS));
		addIfPresent(armorStacks, player.getItemBySlot(EquipmentSlot.FEET));
		return armorStacks;
	}

	private static void renderArmorBottom(GuiGraphicsExtractor graphics, Font font, List<ItemStack> armorStacks, int hotbarLeft, int hotbarY, boolean offhandOnLeft) {
		int firstSlotX = hotbarLeft - OFFHAND_BACKGROUND_WIDTH - (offhandOnLeft ? SIDE_SLOT_ADVANCE : 0);
		int startSlotX = firstSlotX - (armorStacks.size() - 1) * GROUP_SLOT_ADVANCE;

		for (int i = 0; i < armorStacks.size(); i++) {
			drawArmorSlot(graphics, font, armorStacks.get(i), startSlotX + i * GROUP_SLOT_ADVANCE, hotbarY);
		}
	}

	private static void renderArmorLeft(GuiGraphicsExtractor graphics, Font font, List<ItemStack> armorStacks, int guiHeight) {
		int columnHeight = OFFHAND_BACKGROUND_HEIGHT + Math.max(0, armorStacks.size() - 1) * GROUP_SLOT_ADVANCE;
		int startSlotY = guiHeight / 2 - columnHeight / 2;

		for (int i = 0; i < armorStacks.size(); i++) {
			drawArmorSlot(graphics, font, armorStacks.get(i), SCREEN_EDGE_PADDING, startSlotY + i * GROUP_SLOT_ADVANCE);
		}
	}

	private static void drawArmorSlot(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int slotX, int slotY) {
		int itemX = slotX + LEFT_OFFHAND_ITEM_OFFSET;
		int itemY = slotY + OFFHAND_ITEM_Y_OFFSET;

		if (CleanHUDConfig.INSTANCE.armorHudBackground) {
			drawOffhandBackground(graphics, slotX, slotY, true);
		}

		graphics.item(stack, itemX, itemY);

		if (!drawLowDurabilityText(graphics, font, stack, itemX, itemY)) {
			graphics.itemDecorations(font, stack, itemX, itemY);
		}
	}

	private static boolean drawLowDurabilityText(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int itemX, int itemY) {
		if (!stack.isDamageableItem()) {
			return false;
		}

		int maxDamage = stack.getMaxDamage();

		if (maxDamage <= 0) {
			return false;
		}

		int durabilityLeft = Math.max(0, maxDamage - stack.getDamageValue());
		int barWidth = Math.clamp(Math.round(13.0F * durabilityLeft / maxDamage), 0, 13);

		if (barWidth > 2) {
			return false;
		}

		String text = Integer.toString(durabilityLeft);
        int x = itemX + ITEM_SIZE - font.width(text) + 1;
		int y = itemY + ITEM_SIZE - font.lineHeight + 2;

		drawTextWithShadow(graphics, font, text, x, y, LOW_TEXT_COLOR);
		return true;
	}

	private static void drawItemCount(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int itemX, int itemY) {
		if (stack.isEmpty() || stack.getCount() <= 1) {
			return;
		}

		String text = formatItemCount(stack.getCount());
		int x = itemX + ITEM_SIZE - font.width(text) + 1;
		int y = itemY + ITEM_SIZE - font.lineHeight + 2;

		drawTextWithShadow(graphics, font, text, x, y, TEXT_COLOR);
	}

	private static String formatItemCount(int count) {
		if (count < 1000) {
			return Integer.toString(count);
		}

		if (count < 10000) {
			int whole = count / 1000;
			int decimal = (count % 1000) / 100;
			return decimal == 0 ? whole + "k" : whole + "." + decimal + "k";
		}

		if (count < 1000000) {
			return (count / 1000) + "k";
		}

		if (count < 10000000) {
			int whole = count / 1000000;
			int decimal = (count % 1000000) / 100000;
			return decimal == 0 ? whole + "m" : whole + "." + decimal + "m";
		}

		return (count / 1000000) + "m";
	}

	private static void addIfPresent(List<ItemStack> stacks, ItemStack stack) {
		if (!stack.isEmpty()) {
			stacks.add(stack);
		}
	}

	private static int renderArrowHud(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, int hotbarRight, int hotbarY, boolean offhandOnRight) {
		ArrowDisplayMode displayMode = CleanHUDConfig.INSTANCE.arrowDisplayMode;

		if (!displayMode.showsHud()) {
			return 0;
		}

		if (CleanHUDConfig.INSTANCE.arrowRequireBow && !isHoldingBowOrCrossbow(player)) {
			return 0;
		}

		List<ItemStack> arrowStacks = findArrowStacks(player);

		if (arrowStacks.isEmpty()) {
			return 0;
		}

		int startSlotX = hotbarRight + (offhandOnRight ? SIDE_SLOT_ADVANCE : 0);
		int itemY = hotbarY + OFFHAND_ITEM_Y_OFFSET;

		for (int i = 0; i < arrowStacks.size(); i++) {
			ItemStack arrowStack = arrowStacks.get(i);
			int slotX = startSlotX + i * GROUP_SLOT_ADVANCE;
			int itemX = slotX + RIGHT_OFFHAND_ITEM_OFFSET;

			drawOffhandBackground(graphics, slotX, hotbarY, false);
			graphics.item(arrowStack, itemX, itemY);
			drawItemCount(graphics, font, arrowStack, itemX, itemY);
		}

		return arrowStacks.size() * GROUP_SLOT_ADVANCE;
	}

	private static List<ItemStack> findArrowStacks(LocalPlayer player) {
		ArrowDisplayMode displayMode = CleanHUDConfig.INSTANCE.arrowDisplayMode;
		List<ItemStack> arrowStacks = findSeparateArrowStacks(player, displayMode);

		if (CleanHUDConfig.INSTANCE.separateArrowTypes) {
			return arrowStacks;
		}

		if (arrowStacks.isEmpty()) {
			return arrowStacks;
		}

		int totalArrows = 0;

		for (ItemStack arrowStack : arrowStacks) {
			totalArrows += arrowStack.getCount();
		}

		ItemStack displayStack = arrowStacks.size() == 1 ? arrowStacks.getFirst().copy() : new ItemStack(Items.ARROW);
		displayStack.setCount(totalArrows);

		List<ItemStack> displayStacks = new ArrayList<>();
		displayStacks.add(displayStack);
		return displayStacks;
	}

	private static List<ItemStack> findSeparateArrowStacks(LocalPlayer player, ArrowDisplayMode displayMode) {
		List<ItemStack> displayStacks = new ArrayList<>();

		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);

			if (stack.isEmpty() || !stack.is(ItemTags.ARROWS) || !shouldCountSlot(i, displayMode.countsHotbar())) {
				continue;
			}

			addArrowDisplayStack(displayStacks, stack);
		}

		displayStacks.sort(Comparator.comparingInt(CleanHUDRenderer::arrowSortIndex));
		return displayStacks;
	}

	private static void addArrowDisplayStack(List<ItemStack> displayStacks, ItemStack stack) {
		for (ItemStack displayStack : displayStacks) {
			if (ItemStack.isSameItemSameComponents(displayStack, stack)) {
				displayStack.setCount(displayStack.getCount() + stack.getCount());
				return;
			}
		}

		displayStacks.add(stack.copy());
	}

	private static int arrowSortIndex(ItemStack stack) {
		if (stack.is(Items.ARROW)) {
			return 0;
		}

		if (stack.is(Items.SPECTRAL_ARROW)) {
			return 1;
		}

		if (stack.is(Items.TIPPED_ARROW)) {
			return 2;
		}

		return 3;
	}

	private static boolean shouldCountSlot(int slot, boolean countsHotbar) {
		return slot >= HOTBAR_SIZE || countsHotbar;
	}

	private static boolean isHoldingBowOrCrossbow(LocalPlayer player) {
		return isBowOrCrossbow(player.getMainHandItem()) || isBowOrCrossbow(player.getOffhandItem());
	}

	private static boolean isBowOrCrossbow(ItemStack stack) {
		return stack.is(Items.BOW) || stack.is(Items.CROSSBOW);
	}

	private static int renderCustomItemDisplays(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, int hotbarRight, int hotbarY, boolean offhandOnRight, int rightHudOffset) {
		ItemDisplayMode displayMode = CleanHUDConfig.INSTANCE.itemDisplayMode;

		if (!displayMode.showsHud() || CleanHUDConfig.INSTANCE.customItemDisplays.isEmpty()) {
			return 0;
		}

		int startSlotX = hotbarRight + (offhandOnRight ? SIDE_SLOT_ADVANCE : 0) + rightHudOffset;
		int renderedSlots = 0;

		for (String itemId : CleanHUDConfig.INSTANCE.customItemDisplays) {
			if (CleanHUDConfig.INSTANCE.itemRequireHeld && !isHoldingItemId(player, itemId)) {
				continue;
			}

			ItemStack displayStack = findCustomItemStack(player, itemId, displayMode);

			if (displayStack.isEmpty()) {
				continue;
			}

			int slotX = startSlotX + renderedSlots * GROUP_SLOT_ADVANCE;
			int itemX = slotX + RIGHT_OFFHAND_ITEM_OFFSET;
			int itemY = hotbarY + OFFHAND_ITEM_Y_OFFSET;

			drawOffhandBackground(graphics, slotX, hotbarY, false);
			graphics.item(displayStack, itemX, itemY);
			drawItemCount(graphics, font, displayStack, itemX, itemY);
			renderedSlots++;
		}

		return renderedSlots * GROUP_SLOT_ADVANCE;
	}

	private static ItemStack findCustomItemStack(LocalPlayer player, String itemId, ItemDisplayMode displayMode) {
		ItemStack displayStack = ItemStack.EMPTY;
		int totalItems = 0;

		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);

			if (stack.isEmpty() || !matchesItemId(stack, itemId)) {
				continue;
			}

			boolean outsideHotbar = i >= HOTBAR_SIZE;

			if (!outsideHotbar && !displayMode.countsHotbar()) {
				continue;
			}

			totalItems += stack.getCount();

			if (displayStack.isEmpty()) {
				displayStack = stack.copy();
			}
		}

		if (displayStack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		displayStack.setCount(totalItems);
		return displayStack;
	}

	private static boolean isHoldingItemId(LocalPlayer player, String itemId) {
		return matchesItemId(player.getMainHandItem(), itemId) || matchesItemId(player.getOffhandItem(), itemId);
	}

	private static boolean matchesItemId(ItemStack stack, String itemId) {
		if (stack.isEmpty()) {
			return false;
		}

		Identifier stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return stackItemId.toString().equals(itemId);
	}

	private static void renderEffects(GuiGraphicsExtractor graphics, Minecraft minecraft, LocalPlayer player, int hotbarRight, int hotbarY, boolean offhandOnRight, int guiWidth, int guiHeight, int arrowOffset) {
		List<MobEffectInstance> effects = player.getActiveEffects().stream()
				.sorted(Comparator.comparingInt(CleanHUDRenderer::effectDurationForSorting))
				.toList();

		if (CleanHUDConfig.INSTANCE.effectHudPosition == EffectHudPosition.TOP) {
			renderEffectsTop(graphics, minecraft, effects, guiWidth, guiHeight);
		} else {
			renderEffectsBottom(graphics, minecraft.font, effects, hotbarRight, hotbarY, offhandOnRight, guiWidth, arrowOffset);
		}
	}

	private static void renderEffectsBottom(GuiGraphicsExtractor graphics, Font font, List<MobEffectInstance> effects, int hotbarRight, int hotbarY, boolean offhandOnRight, int guiWidth, int arrowOffset) {
		int startSlotX = hotbarRight + (offhandOnRight ? SIDE_SLOT_ADVANCE : 0) + arrowOffset;
		int slotX = startSlotX;
		int row = 0;

		EffectRenderState renderState = new EffectRenderState();

		for (MobEffectInstance effect : effects) {
			if (slotX + OFFHAND_BACKGROUND_WIDTH > guiWidth && slotX != startSlotX) {
				slotX = startSlotX;
				row++;
			}

			drawEffectSlot(graphics, font, effect, slotX, hotbarY - row * EFFECT_ROW_ADVANCE, false, renderState);
			slotX += GROUP_SLOT_ADVANCE;
		}

		finishEffectRendering(graphics, font, renderState);
	}

	private static void renderEffectsTop(GuiGraphicsExtractor graphics, Minecraft minecraft, List<MobEffectInstance> effects, int guiWidth, int guiHeight) {
		if (effects.isEmpty()) {
			return;
		}

		int maxSlotsPerRow = Math.max(1, (guiWidth - SCREEN_EDGE_PADDING * 2 - OFFHAND_BACKGROUND_WIDTH) / GROUP_SLOT_ADVANCE + 1);
		int topSlotY = topEffectY(minecraft, guiHeight);
		int renderedSlots = 0;

		EffectRenderState renderState = new EffectRenderState();

		for (int row = 0; renderedSlots < effects.size(); row++) {
			int rowSlots = Math.min(maxSlotsPerRow, effects.size() - renderedSlots);
			int startSlotX = centeredRowStart(guiWidth, rowSlots);
			int slotY = topSlotY + row * TOP_EFFECT_ROW_ADVANCE;

			for (int i = 0; i < rowSlots; i++) {
				drawEffectSlot(graphics, minecraft.font, effects.get(renderedSlots), startSlotX + i * GROUP_SLOT_ADVANCE, slotY, true, renderState);
				renderedSlots++;
			}
		}

		finishEffectRendering(graphics, minecraft.font, renderState);
	}

	private static void drawEffectSlot(GuiGraphicsExtractor graphics, Font font, MobEffectInstance effect, int slotX, int slotY, boolean topLayout, EffectRenderState renderState) {
		int itemX = slotX + EFFECT_ICON_X_OFFSET;
		int itemY = slotY + EFFECT_ICON_Y_OFFSET;
		String effectKey = effectBarKey(effect);

		renderState.activeEffectKeys.add(effectKey);

		if (CleanHUDConfig.INSTANCE.effectHudBackground) {
			drawOffhandBackground(graphics, slotX, slotY, false);
		}

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, effectIcon(effect), itemX, itemY, EFFECT_ICON_SIZE, EFFECT_ICON_SIZE);
		drawEffectDurationBar(graphics, effect, effectKey, itemX, itemY);
		addEffectAmplifierText(font, effect, itemX, itemY, renderState.amplifierTexts);
		addEffectWarningText(font, effect, itemX, itemY, topLayout, renderState.warningTexts);
	}

	private static void finishEffectRendering(GuiGraphicsExtractor graphics, Font font, EffectRenderState renderState) {
		for (EffectText amplifierText : renderState.amplifierTexts) {
			drawTextWithShadow(graphics, font, amplifierText.text, amplifierText.x, amplifierText.y, amplifierText.color);
		}

		for (EffectText warningText : renderState.warningTexts) {
			drawTextWithShadow(graphics, font, warningText.text, warningText.x, warningText.y, warningText.color);
		}

		EFFECT_MAX_DURATIONS.keySet().removeIf(key -> !renderState.activeEffectKeys.contains(key));
	}

	private static void addEffectAmplifierText(Font font, MobEffectInstance effect, int itemX, int itemY, List<EffectText> amplifierTexts) {
		if (effect.getAmplifier() <= 0) {
			return;
		}

		String text = Integer.toString(effect.getAmplifier() + 1);
		int x = itemX + EFFECT_ICON_SIZE - font.width(text) + 1;
		int y = itemY + EFFECT_ICON_SIZE - font.lineHeight + 2;

		amplifierTexts.add(new EffectText(text, x, y, TEXT_COLOR));
	}

	private static void addEffectWarningText(Font font, MobEffectInstance effect, int itemX, int itemY, boolean topLayout, List<EffectText> warningTexts) {
		if (effect.isInfiniteDuration()) {
			return;
		}

		int seconds = effectSeconds(effect);

		if (seconds > 10) {
			return;
		}

		String text = seconds + "s";
		int x = itemX + EFFECT_ICON_SIZE / 2 - font.width(text) / 2;
		int y = topLayout ? itemY + EFFECT_ICON_SIZE + 1 : Math.max(SCREEN_EDGE_PADDING, itemY - 12);
		int color = seconds <= 3 ? LOW_TEXT_COLOR : TEXT_COLOR;

		warningTexts.add(new EffectText(text, x, y, color));
	}

	private static void drawEffectDurationBar(GuiGraphicsExtractor graphics, MobEffectInstance effect, String effectKey, int itemX, int itemY) {
		if (effect.isInfiniteDuration()) {
			return;
		}

		int duration = Math.max(0, effect.getDuration());
		int maxDuration = Math.max(duration, EFFECT_MAX_DURATIONS.getOrDefault(effectKey, duration));

		EFFECT_MAX_DURATIONS.put(effectKey, maxDuration);

		if (maxDuration <= 0) {
			return;
		}

		int barX = itemX + 2;
		int barY = itemY + EFFECT_ICON_SIZE - 3;
		int barMaxWidth = EFFECT_ICON_SIZE - 3;
		int barWidth = Math.clamp(Math.round((float) barMaxWidth * duration / maxDuration), 0, barMaxWidth);
		int red = Math.min(255, 255 - Math.round(255.0F * barWidth / barMaxWidth));
		int green = Math.min(255, Math.round(255.0F * barWidth / barMaxWidth));
		int color = 0xFF000000 | red << 16 | green << 8;

		graphics.fill(barX, barY, barX + barMaxWidth, barY + 2, 0xFF000000);
		graphics.fill(barX, barY, barX + barWidth, barY + 1, color);
	}

	private static Identifier effectIcon(MobEffectInstance effect) {
		return effect.getEffect().unwrapKey()
				.map(key -> key.identifier().withPrefix("mob_effect/"))
				.orElse(Identifier.withDefaultNamespace("missingno"));
	}

	private static int effectDurationForSorting(MobEffectInstance effect) {
		return effect.isInfiniteDuration() ? Integer.MAX_VALUE : effect.getDuration();
	}

	private static String effectBarKey(MobEffectInstance effect) {
		return effectIcon(effect) + ":" + effect.getAmplifier();
	}

	private static int effectSeconds(MobEffectInstance effect) {
		return Math.max(0, (effect.getDuration() + 19) / 20);
	}

	private static int centeredRowStart(int guiWidth, int rowSlots) {
		int rowWidth = OFFHAND_BACKGROUND_WIDTH + (rowSlots - 1) * GROUP_SLOT_ADVANCE;
		return guiWidth / 2 - rowWidth / 2;
	}

	private static int topEffectY(Minecraft minecraft, int guiHeight) {
		int bossBars = bossBarCount(minecraft);
		int y = 4;

		if (bossBars <= 0) {
			return y;
		}

		y = 12;

		for (int i = 0; i < bossBars; i++) {
			y += 19;

			if (y >= guiHeight / 3) {
				break;
			}
		}

		return y + 3;
	}

	private static int bossBarCount(Minecraft minecraft) {
		Object bossOverlay = findBossOverlay(minecraft, 0);

		if (bossOverlay == null) {
			return 0;
		}

		for (Field field : bossOverlay.getClass().getDeclaredFields()) {
			if (!Map.class.isAssignableFrom(field.getType())) {
				continue;
			}

			try {
				field.setAccessible(true);
				Object value = field.get(bossOverlay);

				if (value instanceof Map<?, ?> map) {
					return map.size();
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		return 0;
	}

	private static Object findBossOverlay(Object object, int depth) {
		if (object == null || depth > 2) {
			return null;
		}

		if (object.getClass().getSimpleName().toLowerCase().contains("boss")) {
			return object;
		}

		for (Field field : object.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				Object value = field.get(object);

				if (value == null || value == object) {
					continue;
				}

				Object bossOverlay = findBossOverlay(value, depth + 1);

				if (bossOverlay != null) {
					return bossOverlay;
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		return null;
	}

	private static boolean isGuiHidden(Minecraft minecraft) {
		Object options = minecraft.options;

		for (String methodName : List.of("isGuiHidden", "isHudHidden", "hideGui", "hideHud", "shouldHideGui", "shouldHideHud")) {
			try {
				Method method = options.getClass().getDeclaredMethod(methodName);
				method.setAccessible(true);
				Object value = method.invoke(options);

				if (value instanceof Boolean hidden) {
					return hidden;
				}
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		for (String fieldName : List.of("hideGui", "hideHud", "hideHUD", "guiHidden", "hudHidden")) {
			try {
				Field field = options.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.getBoolean(options);
			} catch (ReflectiveOperationException | RuntimeException ignored) {
			}
		}

		return false;
	}

	private static boolean isDebugScreenOpen(Minecraft minecraft) {
		return minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.GAME_VERSION);
	}

	private static void renderClock(GuiGraphicsExtractor graphics, Font font, int guiWidth, int guiHeight) {
		String time = LocalTime.now().format(CLOCK_FORMAT);
		ClockPosition position = CleanHUDConfig.INSTANCE.clockPosition;

		int x = switch (position) {
			case TOP_LEFT, BOTTOM_LEFT -> CLOCK_PADDING;
			case TOP_RIGHT, BOTTOM_RIGHT -> guiWidth - font.width(time) - CLOCK_PADDING - CLOCK_RIGHT_EXTRA_PADDING;
			case OFF -> 0;
		};
		int y = switch (position) {
			case TOP_LEFT, TOP_RIGHT -> CLOCK_PADDING;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> guiHeight - font.lineHeight - CLOCK_PADDING;
			case OFF -> 0;
		};

		drawTextWithShadow(graphics, font, time, x, y, TEXT_COLOR);
	}

	private static void drawTextWithShadow(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
		graphics.text(font, text, x + 1, y + 1, SHADOW_COLOR, false);
		graphics.text(font, text, x, y, color, false);
	}

	private static void drawOffhandBackground(GuiGraphicsExtractor graphics, int x, int y, boolean leftSide) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, leftSide ? HOTBAR_OFFHAND_LEFT_SPRITE : HOTBAR_OFFHAND_RIGHT_SPRITE, x, y, OFFHAND_BACKGROUND_WIDTH, OFFHAND_BACKGROUND_HEIGHT);
	}

	private static class EffectRenderState {
		private final Set<String> activeEffectKeys = new HashSet<>();
		private final List<EffectText> amplifierTexts = new ArrayList<>();
		private final List<EffectText> warningTexts = new ArrayList<>();
	}

	private static class EffectText {
		private final String text;
		private final int x;
		private final int y;
		private final int color;

		private EffectText(String text, int x, int y, int color) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.color = color;
		}
	}
}
