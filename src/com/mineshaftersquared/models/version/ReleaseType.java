package com.mineshaftersquared.models.version;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType {
	SNAPSHOT("snapshot"),
	RELEASE("release");

	private static final Map<String, ReleaseType> lookup;
	private final String name;

	private ReleaseType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static ReleaseType getByName(String name) {
		return lookup.get(name);
	}

	static {
		lookup = new HashMap();

		for (ReleaseType type : values()) {
			lookup.put(type.getName(), type);
		}
	}
}