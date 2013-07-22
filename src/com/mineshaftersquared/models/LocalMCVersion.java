package com.mineshaftersquared.models;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleUtils;

public class LocalMCVersion extends MCVersion {
	public final File installationRoot;
	public final File versionLocation;
	public final String name;
	public final File configFile;
	public final boolean isLocal;
	public LocalMCVersion(MCVersion version, File installation, File location, String name, boolean isLocal) {
		super(version.versionId, version.releaseTime, version.type, version.manager);
		this.installationRoot = installation;
		this.versionLocation = location;
		this.name = name;
		this.configFile = configFile(location, name);
		this.isLocal = isLocal;
	}

	public String[] checkComplete() {
		List<String> missing = new LinkedList<String>();
		SimpleOS os = SimpleOS.getOS();
		MCLibrary[] libs = this.getLibrariesForOS(os);
		File parent = new File(this.installationRoot, "libraries");
		for (int i = 0; i < libs.length; i++) {
			String uri = libs[i].getArtifactName(os);
			File f = new File(parent, uri);
			if (f.exists()) {
				continue;
			} else {
				missing.add(String.format("No lib %s", libs[i].name));
			}
		}
		return missing.toArray(new String[missing.size()]);
	}
	
	public static LocalMCVersion getLocalMCVersionFromLocation(File installation, File location, String name, boolean isLocal, MCVersionManager manager) {
		MCVersion version = manager.mcVersionFromData(SimpleUtils.fileGetContents(configFile(location, name)));
		return new LocalMCVersion(version, installation, location, name, isLocal);
	}
	private static File configFile(File location, String name) {
		return new File(location, name + ".json");
	}
	public static LocalMCVersion[] findInstallationsInRoot(File root, boolean isLocal, MCVersionManager manager) {
		List<LocalMCVersion> installations = new LinkedList<LocalMCVersion>();
		
		File versionsFolder = new File(root, "versions");
		versionsFolder.mkdir();
		for (File each : versionsFolder.listFiles()) {
			File config = new File(each, each.getName() + ".json");
			if (config.exists()) {
				installations.add(getLocalMCVersionFromLocation(root, each, each.getName(), isLocal, manager));
			}
		}
		
		return installations.toArray(new LocalMCVersion[installations.size()]);
	}
	
	public File[] getClassPath(SimpleOS os, File root) {
		MCLibrary[] libs = this.getLibrariesForOS(os);
		List<File> files = new LinkedList<File>();
		for (int i = 0; i < libs.length; i++) {
			if (libs[i].getNatives().get(os) == null) {
				files.add(new File(root, "libraries/" + libs[i].getArtifactName(os)));
			}
		}
		files.add(new File(root, "versions/" + this.name + "/" + this.versionId + ".jar"));
		return files.toArray(new File[files.size()]);
	}
	
	@Override
	public MCVersionDetails getDetails() {
		return this.getDetailsFromFile();
	}
	public MCVersionDetails getDetailsFromFile() {
		return this.manager.detailsFromData(SimpleUtils.fileGetContents(this.configFile));
	}
}