package com.mineshaftersquared.models.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

import sun.misc.Launcher;

import com.creatifcubed.simpleapi.SimpleOS;
import com.google.gson.JsonSyntaxException;

public class LocalVersionList extends FileBasedVersionList {
	private final File baseDirectory;
	private final File baseVersionsDir;

	public LocalVersionList(File baseDirectory) {
		if ((baseDirectory == null) || (!baseDirectory.isDirectory())) {
			throw new IllegalArgumentException("Base directory is not a folder!");
		}

		this.baseDirectory = baseDirectory;
		this.baseVersionsDir = new File(this.baseDirectory, "versions");
		if (!this.baseVersionsDir.isDirectory()) {
			this.baseVersionsDir.mkdirs();
		}
	}

	@Override
	protected InputStream getFileInputStream(String uri) throws FileNotFoundException {
		return new FileInputStream(new File(this.baseDirectory, uri));
	}

	@Override
	public void refreshVersions() throws IOException {
		this.clearCache();

		File[] files = this.baseVersionsDir.listFiles();
		if (files == null) {
			return;
		}

		for (File directory : files) {
			String id = directory.getName();
			File jsonFile = new File(directory, id + ".json");

			if ((directory.isDirectory()) && (jsonFile.exists())) {
				try {
					CompleteVersion version = this.gson.fromJson(this.getUrl("versions/" + id + "/" + id + ".json"),
							CompleteVersion.class);
					this.addVersion(version);
				} catch (JsonSyntaxException ex) {
					ex.printStackTrace();
				}
			}
		}

		for (Version version : this.getVersions()) {
			ReleaseType type = version.getType();

			if ((this.getLatestVersion(type) == null)
					|| (this.getLatestVersion(type).getUpdatedTime().before(version.getUpdatedTime()))) {
				this.setLatestVersion(version);
			}
		}
	}

	public void saveVersionList() throws IOException {
		String text = this.serializeVersionList();
		PrintWriter writer = new PrintWriter(new File(this.baseVersionsDir, "versions.json"));
		writer.print(text);
		writer.close();
	}

	public void saveVersion(CompleteVersion version) throws IOException {
		String text = this.serializeVersion(version);
		File target = new File(this.baseVersionsDir, version.getId() + "/" + version.getId() + ".json");
		if (target.getParentFile() != null) {
			target.getParentFile().mkdirs();
		}
		PrintWriter writer = new PrintWriter(target);
		writer.print(text);
		writer.close();
	}

	public File getBaseDirectory() {
		return this.baseDirectory;
	}

	public boolean hasAllFiles(CompleteVersion version, SimpleOS os) {
		Set<String> files = version.getRequiredFiles(os);

		for (String file : files) {
			if (!new File(this.baseDirectory, file).isFile()) {
				return false;
			}
		}

		return true;
	}
}
