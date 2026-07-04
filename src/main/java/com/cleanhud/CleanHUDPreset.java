package com.cleanhud;

public enum CleanHUDPreset {
	HOTBAR("Hotbar"),
	DYNAMIC("Dynamic"),
	CUSTOM("Custom");

	private final String label;

	CleanHUDPreset(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
