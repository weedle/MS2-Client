package com.mineshaftersquared.models;

import java.io.File;

import com.mineshaftersquared.misc.MS2Utils;

public class MCProfile {
	
	public static final String[] DEFAULT_JAVA_ARGS = {"-Xms1G", "-Xmx1G"};
	
	private String name;
	private String versionId;
	private File gameDir;
	private String[] javaArgs;
	private boolean isLocal;
	
	public MCProfile(String name) {
		this(name, null, null, null, false);
	}
	
	public MCProfile(String name, String versionId, File gameDir, String[] javaArgs, boolean isLocal) {
		this.setName(name);
		this.setGameDir(gameDir);
		this.setVersionId(versionId);
		this.setJavaArgs(javaArgs);
		this.setIsLocal(isLocal);
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getGameDir() {
		if (this.getIsLocal()) {
			return MS2Utils.getLocalDir();
		}
		if (this.gameDir == null) {
			return MS2Utils.getDefaultMCDir();
		}
		return this.gameDir;
	}
	public void setGameDir(File gameDir) {
		this.gameDir = gameDir;
	}
	public String getVersionId() {
		if (this.versionId == null) {
			return null;
		}
		return this.versionId;
	}
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
	public String[] getJavaArgs() {
		String[] javaArgs = this.javaArgs == null ? DEFAULT_JAVA_ARGS : this.javaArgs;
		return javaArgs.clone();
	}
	public void setJavaArgs(String[] javaArgs) {
		this.javaArgs = javaArgs == null ? null : javaArgs.clone();
	}
	public boolean getIsLocal() {
		return this.isLocal;
	}
	public void setIsLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof MCProfile) {
			MCProfile that = (MCProfile) other;
			return this.getName().equalsIgnoreCase(that.getName()) && (this.getIsLocal() ? this.getIsLocal() == that.getIsLocal() : this.getGameDir().equals(that.getGameDir()));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
}
