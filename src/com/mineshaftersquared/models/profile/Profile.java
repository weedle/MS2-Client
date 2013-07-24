package com.mineshaftersquared.models.profile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.File;

import com.mineshaftersquared.models.LauncherVisibilityRule;
import com.mineshaftersquared.models.version.ReleaseType;
import com.mineshaftersquared.models.version.VersionFilter;

public class Profile {
	public static final String DEFAULT_JRE_ARGUMENTS_64BIT = "-Xmx1G";
	public static final String DEFAULT_JRE_ARGUMENTS_32BIT = "-Xmx512M";
	public static final Resolution DEFAULT_RESOLUTION = new Resolution(854, 480);
	public static final LauncherVisibilityRule DEFAULT_LAUNCHER_VISIBILITY = LauncherVisibilityRule.CLOSE_LAUNCHER;
	public static final Set<ReleaseType> DEFAULT_RELEASE_TYPES = new HashSet(
			Arrays.asList(new ReleaseType[] { ReleaseType.RELEASE }));
	private String name;
	private File gameDir;
	private String lastVersionId;
	private String javaDir;
	private String javaArgs;
	private Resolution resolution;
	private Set<ReleaseType> allowedReleaseTypes;
	private String playerUUID;
	private Boolean useHopperCrashService;
	private LauncherVisibilityRule launcherVisibilityOnGameClose;

	public Profile() {
	}

	public Profile(Profile copy) {
		this.name = copy.name;
		this.gameDir = copy.gameDir;
		this.playerUUID = copy.playerUUID;
		this.lastVersionId = copy.lastVersionId;
		this.javaDir = copy.javaDir;
		this.javaArgs = copy.javaArgs;
		this.resolution = (copy.resolution == null ? null : new Resolution(copy.resolution));
		this.allowedReleaseTypes = (copy.allowedReleaseTypes == null ? null : new HashSet(copy.allowedReleaseTypes));
		this.useHopperCrashService = copy.useHopperCrashService;
		this.launcherVisibilityOnGameClose = copy.launcherVisibilityOnGameClose;
	}

	public Profile(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getGameDir() {
		return this.gameDir;
	}

	public void setGameDir(File gameDir) {
		this.gameDir = gameDir;
	}

	public void setLastVersionId(String lastVersionId) {
		this.lastVersionId = lastVersionId;
	}

	public void setJavaDir(String javaDir) {
		this.javaDir = javaDir;
	}

	public void setJavaArgs(String javaArgs) {
		this.javaArgs = javaArgs;
	}

	public String getLastVersionId() {
		return this.lastVersionId;
	}

	public String getJavaArgs() {
		return this.javaArgs;
	}

	public String getJavaPath() {
		return this.javaDir;
	}

	public Resolution getResolution() {
		return this.resolution;
	}

	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}

	public String getPlayerUUID() {
		return this.playerUUID;
	}

	public void setPlayerUUID(String playerUUID) {
		this.playerUUID = playerUUID;
	}

	public Set<ReleaseType> getAllowedReleaseTypes() {
		return this.allowedReleaseTypes;
	}

	public void setAllowedReleaseTypes(Set<ReleaseType> allowedReleaseTypes) {
		this.allowedReleaseTypes = allowedReleaseTypes;
	}

	public boolean getUseHopperCrashService() {
		return this.useHopperCrashService == null;
	}

	public void setUseHopperCrashService(boolean useHopperCrashService) {
		this.useHopperCrashService = (useHopperCrashService ? null : Boolean.valueOf(false));
	}

	public VersionFilter getVersionFilter() {
		VersionFilter filter = new VersionFilter().setMaxCount(2147483647);

		if (this.allowedReleaseTypes == null) {
			filter.onlyForTypes((ReleaseType[]) DEFAULT_RELEASE_TYPES.toArray(new ReleaseType[DEFAULT_RELEASE_TYPES
					.size()]));
		} else {
			filter.onlyForTypes((ReleaseType[]) this.allowedReleaseTypes
					.toArray(new ReleaseType[this.allowedReleaseTypes.size()]));
		}

		return filter;
	}

	public LauncherVisibilityRule getLauncherVisibilityOnGameClose() {
		return this.launcherVisibilityOnGameClose;
	}

	public void setLauncherVisibilityOnGameClose(LauncherVisibilityRule launcherVisibilityOnGameClose) {
		this.launcherVisibilityOnGameClose = launcherVisibilityOnGameClose;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	public static class Resolution {
		private int width;
		private int height;

		public Resolution() {
		}

		public Resolution(Resolution resolution) {
			this(resolution.getWidth(), resolution.getHeight());
		}

		public Resolution(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}
	}
}