package com.mineshaftersquared.models;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.creatifcubed.simpleapi.SimpleUtils;

public class LocalMCVersion extends MCVersion {
	public final File installation;
	public final File location;
	public final String name;
	public final File configFile;
	public final boolean isLocal;
	public LocalMCVersion(MCVersion version, File installation, File location, String name, boolean isLocal) {
		super(version.versionId, version.releaseTime, version.type);
		this.installation = installation;
		this.location = location;
		this.name = name;
		this.configFile = configFile(location, name);
		this.isLocal = isLocal;
	}

	public String[] checkComplete() {
		List<String> missing = new LinkedList<String>();
		return missing.toArray(new String[missing.size()]);
	}
	
	public static LocalMCVersion getLocalMCVersionFromLocation(File installation, File location, String name, boolean isLocal) {
		MCVersion version = MCVersion.mcVersionFromData(SimpleUtils.fileGetContents(configFile(location, name)));
		return new LocalMCVersion(version, installation, location, name, isLocal);
	}
	private static File configFile(File location, String name) {
		return new File(location, name + ".json");
	}
	public static LocalMCVersion[] findInstallationsInRoot(File root, boolean isLocal) {
		List<LocalMCVersion> installations = new LinkedList<LocalMCVersion>();
		
		File versionsFolder = new File(root, "versions");
		versionsFolder.mkdir();
		for (File each : versionsFolder.listFiles()) {
			File config = new File(each, each.getName() + ".json");
			if (config.exists()) {
				installations.add(getLocalMCVersionFromLocation(root, each, each.getName(), isLocal));
			}
		}
		
		return installations.toArray(new LocalMCVersion[installations.size()]);
	}
	
	@Override
	public MCVersionDetails getDetails() {
		return this.getDetailsFromFile();
	}
	public MCVersionDetails getDetailsFromFile() {
		return detailsFromData(SimpleUtils.fileGetContents(this.configFile));
	}
}
