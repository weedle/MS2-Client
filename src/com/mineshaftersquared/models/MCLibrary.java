package com.mineshaftersquared.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.creatifcubed.simpleapi.SimpleOS;

public class MCLibrary {
	public final String name;
	private final Set<SimpleOS> restrictedOperatingSystems;
	private final Map<SimpleOS, String> natives;
	public final MCLibraryUnpackingRules unpackingRules;

	public MCLibrary(String name) {
		this.name = name;
		this.restrictedOperatingSystems = new HashSet<SimpleOS>();
		this.natives = new HashMap<SimpleOS, String>();
		this.unpackingRules = new MCLibraryUnpackingRules();
	}

	public void addRestrictedOS(SimpleOS os) {
		this.restrictedOperatingSystems.add(os);
	}

	public void addNative(SimpleOS os, String nativeName) {
		this.natives.put(os, nativeName);
	}

	public class MCLibraryUnpackingRules {
		public final List<String> excludes;
		public MCLibraryUnpackingRules() {
			this.excludes = new LinkedList<String>();
		}

		public void addExclude(String exclude) {
			this.excludes.add(exclude);
		}

		public boolean shouldExtract(String path) {
			for (String each : this.excludes) {
				if (path.startsWith(each)) {
					return false;
				}
			}
			return true;
		}
	}
	
	public Set<SimpleOS> getRestrictedOperatingSystems() {
		return Collections.unmodifiableSet(this.restrictedOperatingSystems);
	}
	public Map<SimpleOS, String> getNatives() {
		return Collections.unmodifiableMap(this.natives);
	}
	
	/**
	 * https://s3.amazonaws.com/Minecraft.Download/libraries/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar
	 * Minecraft.Download.net/libraries/[package periods => slashes]/[jar name]/[jar version]/[jar name]-[jar-version].jar
	 * org.lwjgl.lwjgl:lwjgl:2.9.0
	 * @param base
	 * @return
	 */
	public String getArtifactName(SimpleOS os) {
		String[] parts = this.name.split(":");
		String nativesName = this.natives.get(os);
		return parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + (nativesName == null ? "" : "-" + nativesName) + ".jar";
	}
}