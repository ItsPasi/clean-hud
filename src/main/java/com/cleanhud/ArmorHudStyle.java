package com.cleanhud;

public enum ArmorHudStyle {
	HOTBAR("Hotbar"),
	OFFHAND("Offhand"),
	OFF("Off");

	private final String label;

	ArmorHudStyle(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
