package com.cleanhud;

public enum EffectHudPosition {
	BOTTOM("Bottom"),
	TOP("Top"),
	OFF("Off");

	private final String label;

	EffectHudPosition(String label) {
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
