package com.cleanhud;

public enum ClockPosition {
	OFF("Off"),
	TOP_LEFT("Top Left"),
	TOP_RIGHT("Top Right"),
	BOTTOM_LEFT("Bottom Left"),
	BOTTOM_RIGHT("Bottom Right");

	private final String label;

	ClockPosition(String label) {
		this.label = label;
	}

	public boolean showsClock() {
		return this != OFF;
	}

	@Override
	public String toString() {
		return label;
	}
}
