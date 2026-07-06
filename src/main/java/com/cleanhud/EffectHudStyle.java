package com.cleanhud;

public enum EffectHudStyle {
	OFFHAND("Offhand"),
	OFF("Off");

	private final String label;

	EffectHudStyle(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
