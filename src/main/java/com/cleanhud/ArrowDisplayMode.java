package com.cleanhud;

import com.google.gson.annotations.SerializedName;

public enum ArrowDisplayMode {
	@SerializedName(value = "WITH_HOTBAR", alternate = {"ALWAYS", "ALWAYS_ALL"})
	WITH_HOTBAR("Include Hotbar"),
	@SerializedName(value = "NO_HOTBAR", alternate = {"ON"})
	NO_HOTBAR("Exclude Hotbar"),
	@SerializedName("OFF")
	OFF("Off");

	private final String label;

	ArrowDisplayMode(String label) {
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
