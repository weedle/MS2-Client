package com.mineshaftersquared.models;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import java.text.ParseException;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleOS;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MCVersion {
	public final String versionId;
	public final Date releaseTime;
	public final MCVersionType type;
	private MCVersionDetails details;

	public static final String VERSION_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String VERSIONS_LIST_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/versions.json";
	public static final String VERSION_INFO_URL_TEMPLATE = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.json";
	public static final Map<String, SimpleOS> JSON_JAVA_OS_MAP = new HashMap<String, SimpleOS>() {{
		put("linux", SimpleOS.UNIX);
		put("windows", SimpleOS.WINDOWS);
		put("osx", SimpleOS.MAC);
	}};

	private static String cachedVersionsListData = null;
	private static MCVersion[] cachedVersions = null;
	private static final Map<String, MCVersion> cachedVersionsMap = new HashMap<String, MCVersion>();
	private static final Map<String, String> cachedVersionsData = new HashMap<String, String>();

	public MCVersion(String versionId, Date releaseTime, MCVersionType type) {
		this.versionId = versionId;
		this.releaseTime = releaseTime;
		this.type = type;
		this.details = null;
	}

	public MCVersionDetails getDetails() {
		return getDetails(VERSION_INFO_URL_TEMPLATE, false);
	}
	public MCVersionDetails getDetails(String url, boolean flush) {
		if (this.details == null || flush) {
			this.details = fetchDetails(url, flush);
		}
		return this.details;
	}
	private MCVersionDetails fetchDetails(String url, boolean flush) {
		String data = getMCVersionData(this.versionId, url, flush);
		return detailsFromData(data);
	}

	public MCLibrary[] getLibrariesForOS(SimpleOS os) {
		List<MCLibrary> libs = new LinkedList<MCLibrary>();
		MCVersionDetails details = this.getDetails();
		for (int i = 0; i < details.libraries.length; i++) {
			if (details.libraries[i].getRestrictedOperatingSystems().contains(os)) {
				continue;
			}
			libs.add(details.libraries[i]);
		}
		return libs.toArray(new MCLibrary[libs.size()]);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MCVersion) {
			return this.versionId.equals(((MCVersion) o).versionId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.versionId.hashCode();
	}

	public static MCVersion find(String id) {
		return find(id, VERSION_INFO_URL_TEMPLATE, false);
	}
	public static MCVersion find(String id, String url, boolean flush) {
		if (cachedVersionsMap.get(id) == null || flush) {
			cachedVersionsMap.put(id, fetchMCVersion(id, url, flush));
		}
		return cachedVersionsMap.get(id);
	}
	public static MCVersion fetchMCVersion(String id, String urlTemplate, boolean flush) {
		String data = getMCVersionData(id, urlTemplate, flush);
		return mcVersionFromData(data);
	}
	public static MCVersion mcVersionFromData(String data) {
		JsonObject root = new JsonParser().parse(data).getAsJsonObject();
		try {
			String id = root.get("id").getAsString();
			Date releaseTime = new SimpleDateFormat(VERSION_JSON_DATE_FORMAT).parse(root.get("releaseTime").getAsString());
			MCVersionType type = MCVersionType.fromString(root.get("type").getAsString());
			return new MCVersion(id, releaseTime, type);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	public static MCVersionDetails detailsFromData(String data) {
		//System.out.println("Got data: " + data);
		JsonObject root = new JsonParser().parse(data).getAsJsonObject();
		
		String id = root.get("id").getAsString();
		String[] processArguments = new String[0];
		if (root.has("processArguments")) {
			root.get("processArguments").getAsString().split("_");
		}
		String[] minecraftArguments = root.get("minecraftArguments").getAsString().split(" ");
		String mainClass = root.get("mainClass").getAsString();

		JsonArray jsonLibraries = root.get("libraries").getAsJsonArray();
		List<MCLibrary> libraries = new LinkedList<MCLibrary>();
		for (JsonElement each : jsonLibraries) {
			JsonObject jsonLib = each.getAsJsonObject();
			MCLibrary lib = new MCLibrary(jsonLib.get("name").getAsString());
			if (jsonLib.has("natives")) {
				for (Map.Entry<String, JsonElement> pair : jsonLib.get("natives").getAsJsonObject().entrySet()) {
					SimpleOS os = JSON_JAVA_OS_MAP.get(pair.getKey());
					if (os == null) {
						os = SimpleOS.UNKNOWN;
						Logger.getLogger(MCVersion.class.getCanonicalName()).info(String.format("Parsing libs for %s, lib: %s, unknown natives os %s for value %s", id, lib.name, pair.getKey(), pair.getValue()));
					}
					lib.addNative(os, pair.getValue().getAsString());
				}
			}
			if (jsonLib.has("extract")) {
				JsonObject extract = jsonLib.get("extract").getAsJsonObject();
				if (extract.has("exclude")) {
					JsonArray jsonExcludes = extract.get("exclude").getAsJsonArray();
					for (JsonElement eachExclude : jsonExcludes) {
						lib.unpackingRules.addExclude(eachExclude.getAsString());
					}
				}
			}
			libraries.add(lib);
		}

		return new MCVersionDetails(libraries.toArray(new MCLibrary[libraries.size()]), processArguments, minecraftArguments, mainClass);
	}

	public static String getMCVersionData(String id, String urlTemplate, boolean flush) {
		if (cachedVersionsData.get(id) == null || flush) {
			cachedVersionsData.put(id, new String(new SimpleHTTPRequest(String.format(urlTemplate, id)).doGet()));
		}
		return cachedVersionsData.get(id);
	}

	public static MCVersion[] getVersions() {
		return getVersions(VERSIONS_LIST_URL, false);
	}
	public static MCVersion[] getVersions(String url, boolean flush) {
		if (cachedVersions == null || flush) {
			cachedVersions = fetchVersionsFromURL(url, flush);
		}
		return cachedVersions;
	}
	public static MCVersion[] fetchVersionsFromURL(String url, boolean flush) {
		try {
			String data = getVersionsListData(url, flush);
			JsonElement root = new JsonParser().parse(data);
			JsonArray allVersions = root.getAsJsonObject().get("versions").getAsJsonArray();
			List<MCVersion> versions = new LinkedList<MCVersion>();
			for (JsonElement each : allVersions) {
				JsonObject version = each.getAsJsonObject();
				String versionId = version.get("id").getAsString();
				try {
					Date releaseTime = new SimpleDateFormat(VERSION_JSON_DATE_FORMAT).parse(version.get("releaseTime").getAsString());
					MCVersionType type = MCVersionType.fromString(version.get("type").getAsString());
					versions.add(new MCVersion(versionId, releaseTime, type));
				} catch (ParseException ex) {
					ex.printStackTrace();
				}
			}
			return versions.toArray(new MCVersion[versions.size()]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static MCVersion getLatestRelease() {
		return fetchLatestRelease(VERSIONS_LIST_URL, false);
	}
	public static MCVersion fetchLatestRelease(String url, boolean flush) {
		String data = getVersionsListData(url, flush);
		String latestVersionId = new JsonParser().parse(data).getAsJsonObject().get("latest").getAsJsonObject().get("release").getAsString();
		return find(latestVersionId);
	}

	public static MCVersion getLatestSnapshot() {
		return fetchLatestSnapshot(VERSIONS_LIST_URL, false);
	}
	public static MCVersion fetchLatestSnapshot(String url, boolean flush) {
		String data = getVersionsListData(url, flush);
		String latestVersionId = new JsonParser().parse(data).getAsJsonObject().get("latest").getAsJsonObject().get("snapshot").getAsString();
		return find(latestVersionId);
	}

	private static String getVersionsListData(String url, boolean flush) {
		if (cachedVersionsListData == null || flush) {
			cachedVersionsListData = new String(new SimpleHTTPRequest(url).doGet());
		}
		return cachedVersionsListData;
	}

	public static enum MCVersionType {
		RELEASE, SNAPSHOT, UNKNOWN;

		public static MCVersionType fromString(String str) {
			if (str.equals("release")) {
				return RELEASE;
			} else if (str.equals("snapshot")) {
				return SNAPSHOT;
			}
			return UNKNOWN;
		}

		@Override
		public String toString() {
			switch (this) {
			case RELEASE:
				return "Release";
			case SNAPSHOT:
				return "Snapshot";
			case UNKNOWN:
				return "Unknown";
			}
			return null;
		}
	}

	public static class MCVersionDetails {
		public final MCLibrary[] libraries;
		public final String[] processArguments;
		public final String[] minecraftArguments;
		public final String mainClass;

		public MCVersionDetails(MCLibrary[] libraries, String[] processArguments, String[] minecraftArguments, String mainClass) {
			this.libraries = libraries;
			this.processArguments = processArguments;
			this.minecraftArguments = minecraftArguments;
			this.mainClass = mainClass;
		}
	}
}
/*
{
	"id": "13w25c",
	"time": "2013-06-20T17:23:37+02:00",
	"releaseTime": "2013-06-20T17:23:37+02:00",
	"type": "snapshot",
	"processArguments": "username_session_version",
	"minecraftArguments": "--username ${auth_player_name} --session ${auth_session} --version ${version_name} --gameDir ${game_directory} --assetsDir ${game_assets}",
	"minimumLauncherVersion": 2,
	"libraries": [{
		"name": "net.sf.jopt-simple:jopt-simple:4.5"
	}, {
		"name": "com.paulscode:codecjorbis:20101023"
	}, {
		"name": "com.paulscode:codecwav:20101023"
	}, {
		"name": "com.paulscode:libraryjavasound:20101123"
	}, {
		"name": "com.paulscode:librarylwjglopenal:20100824"
	}, {
		"name": "com.paulscode:soundsystem:20120107"
	}, {
		"name": "org.lwjgl.lwjgl:lwjgl:2.9.0"
	}, {
		"name": "org.lwjgl.lwjgl:lwjgl_util:2.9.0"
	}, {
		"name": "argo:argo:2.25_fixed"
	}, {
		"name": "org.bouncycastle:bcprov-jdk15on:1.47"
	}, {
		"name": "com.google.guava:guava:14.0"
	}, {
		"name": "org.apache.commons:commons-lang3:3.1"
	}, {
		"name": "commons-io:commons-io:2.4"
	}, {
		"name": "net.java.jinput:jinput:2.0.5"
	}, {
		"name": "net.java.jutils:jutils:1.0.0"
	}, {
		"name": "com.google.code.gson:gson:2.2.2"
	}, {
		"name": "org.lwjgl.lwjgl:lwjgl-platform:2.9.0",
		"natives": {
			"linux": "natives-linux",
			"windows": "natives-windows",
			"osx": "natives-osx"
		},
		"extract": {
			"exclude": [
				"META-INF/"
			]
		}
	}, {
		"name": "net.java.jinput:jinput-platform:2.0.5",
		"natives": {
			"linux": "natives-linux",
			"windows": "natives-windows",
			"osx": "natives-osx"
		},
		"extract": {
			"exclude": [
				"META-INF/"
			]
		}
	}],
	"mainClass": "net.minecraft.client.main.Main"
}
 */
/*
{
	"versions": [{
		"id": "13w16b",
		"time": "2013-04-24T11:51:24+02:00",
		"releaseTime": "2013-04-23T23:51:22+02:00",
		"type": "snapshot"
	}, {
		"id": "1.5.1",
		"time": "2013-06-13T16:17:11+02:00",
		"releaseTime": "2013-03-20T12:00:00+01:00",
		"type": "release"
	}, {
		"id": "1.5.2",
		"time": "2013-06-13T16:17:11+02:00",
		"releaseTime": "2013-04-25T17:45:00+02:00",
		"type": "release"
	}, {
		"id": "13w17a",
		"time": "2013-05-02T20:40:00+02:00",
		"releaseTime": "2013-04-25T17:50:00+02:00",
		"type": "snapshot"
	}, {
		"id": "13w18c",
		"time": "2013-05-03T11:19:35+02:00",
		"releaseTime": "2013-05-03T11:19:35+02:00",
		"type": "snapshot"
	}, {
		"id": "13w19a",
		"time": "2013-05-10T16:48:02+02:00",
		"releaseTime": "2013-05-10T16:48:02+02:00",
		"type": "snapshot"
	}, {
		"id": "13w21b",
		"time": "2013-05-27T10:50:42+02:00",
		"releaseTime": "2013-05-27T10:50:42+02:00",
		"type": "snapshot"
	}, {
		"id": "13w22a",
		"time": "2013-05-30T16:38:40+02:00",
		"releaseTime": "2013-05-30T16:38:40+02:00",
		"type": "snapshot"
	}, {
		"id": "13w23b",
		"time": "2013-06-08T02:32:01+02:00",
		"releaseTime": "2013-06-08T02:32:01+02:00",
		"type": "snapshot"
	}, {
		"id": "13w24b",
		"time": "2013-06-14T15:54:59+02:00",
		"releaseTime": "2013-06-14T14:19:13+02:00",
		"type": "snapshot"
	}, {
		"id": "13w25a",
		"time": "2013-06-17T16:08:06+02:00",
		"releaseTime": "2013-06-17T16:08:06+02:00",
		"type": "snapshot"
	}, {
		"id": "13w25b",
		"time": "2013-06-18T17:13:27+02:00",
		"releaseTime": "2013-06-18T17:13:27+02:00",
		"type": "snapshot"
	}, {
		"id": "13w25c",
		"time": "2013-06-20T17:23:37+02:00",
		"releaseTime": "2013-06-20T17:23:37+02:00",
		"type": "snapshot"
	}],
	"latest": {
		"snapshot": "13w25c",
		"release": "1.5.2"
	}
}
 */