package com.cleanhud;

import com.google.gson.annotations.SerializedName;

public enum ItemDisplayMode {
	@SerializedName(value = "WITH_HOTBAR", alternate = {"ALL_ITEMS", "ALL_ITEMS_ALWAYS"})
	WITH_HOTBAR("Include Hotbar"),
	@SerializedName(value = "NO_HOTBAR", alternate = {"OUTSIDE_HOTBAR"})
	NO_HOTBAR("Exclude Hotbar"),
	@SerializedName("OFF")
	OFF("Off");

	private final String label;

	ItemDisplayMode(String label) {
		this.label = label;
	}

	public boolean showsHud() {
		return this != OFF;
	}

	public boolean countsHotbar() {
		return this == WITH_HOTBAR;
	}

	@Override
	public String toString() {
		return label;
	}
}
