package com.cleanhud;

public enum ArmorHudPosition {
	BOTTOM("Bottom"),
	LEFT("Left"),
	OFF("Off");

	private final String label;

	ArmorHudPosition(String label) {
		this.label = label;
	}

	public boolean showsHud() {
		return this != OFF;
	}

	@Override
	public String toString() {
		return label;
	}
}
