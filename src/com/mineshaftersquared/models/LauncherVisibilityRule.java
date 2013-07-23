package com.mineshaftersquared.models;

public enum LauncherVisibilityRule {
	HIDE_LAUNCHER("Hide launcher and re-open when game closes"),
	CLOSE_LAUNCHER("Close launcher when game starts"),
	DO_NOTHING("Keep the launcher open");

	private final String description;

	private LauncherVisibilityRule(String name) {
		this.description = name;
	}

	public String getName() {
		return this.description;
	}

	@Override
	public String toString() {
		return this.description;
	}
}