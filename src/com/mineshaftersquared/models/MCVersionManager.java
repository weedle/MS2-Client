package com.mineshaftersquared.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleOS;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineshaftersquared.models.MCVersion.MCVersionDetails;
import com.mineshaftersquared.models.MCVersion.MCVersionType;

public class MCVersionManager {
	public static final String VERSION_JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String VERSIONS_LIST_URL = "https://s3.amazonaws.com/Minecraft.Download/versions/versions.json";
	public static final String VERSION_INFO_URL_TEMPLATE = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.json";
	public static final Map<String, SimpleOS> JSON_JAVA_OS_MAP = new HashMap<String, SimpleOS>() {
		{
			this.put("linux", SimpleOS.UNIX);
			this.put("windows", SimpleOS.WINDOWS);
			this.put("osx", SimpleOS.MAC);
		}
	};

	private String cachedVersionsListData;
	private MCVersion[] cachedVersions;
	private final Map<String, MCVersion> cachedVersionsMap;
	private final Map<String, String> cachedVersionsData;

	public MCVersionManager() {
		this.cachedVersionsListData = null;
		this.cachedVersions = null;
		this.cachedVersionsMap = new HashMap<String, MCVersion>();
		this.cachedVersionsData = new HashMap<String, String>();
	}

	public MCVersion find(String id) {
		return this.find(id, VERSION_INFO_URL_TEMPLATE, false);
	}

	public MCVersion find(String id, String url, boolean flush) {
		if (this.cachedVersionsMap.get(id) == null || flush) {
			this.cachedVersionsMap.put(id, this.fetchMCVersion(id, url, flush));
		}
		return this.cachedVersionsMap.get(id);
	}

	public MCVersion fetchMCVersion(String id, String urlTemplate, boolean flush) {
		String data = this.getMCVersionData(id, urlTemplate, flush);
		return mcVersionFromData(data);
	}

	public MCVersion mcVersionFromData(String data) {
		JsonObject root = new JsonParser().parse(data).getAsJsonObject();
		try {
			String id = root.get("id").getAsString();
			Date releaseTime = new SimpleDateFormat(VERSION_JSON_DATE_FORMAT).parse(root.get("releaseTime")
					.getAsString());
			MCVersionType type = MCVersionType.fromString(root.get("type").getAsString());
			return new MCVersion(id, releaseTime, type, this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public MCVersionDetails detailsFromData(String data) {
		// System.out.println("Got data: " + data);
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
						Logger.getLogger(MCVersion.class.getCanonicalName()).info(
								String.format("Parsing libs for %s, lib: %s, unknown natives os %s for value %s", id,
										lib.name, pair.getKey(), pair.getValue()));
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

		return new MCVersionDetails(libraries.toArray(new MCLibrary[libraries.size()]), processArguments,
				minecraftArguments, mainClass);
	}

	public String getMCVersionData(String id, String urlTemplate, boolean flush) {
		if (this.cachedVersionsData.get(id) == null || flush) {
			this.cachedVersionsData.put(id, new String(new SimpleHTTPRequest(String.format(urlTemplate, id)).doGet()));
		}
		return this.cachedVersionsData.get(id);
	}

	public MCVersion[] getVersions() {
		return this.getVersions(VERSIONS_LIST_URL, false);
	}

	public MCVersion[] getVersions(String url, boolean flush) {
		if (this.cachedVersions == null || flush) {
			this.cachedVersions = this.fetchVersionsFromURL(url, flush);
		}
		return this.cachedVersions;
	}

	public MCVersion[] fetchVersionsFromURL(String url, boolean flush) {
		try {
			String data = this.getVersionsListData(url, flush);
			JsonElement root = new JsonParser().parse(data);
			JsonArray allVersions = root.getAsJsonObject().get("versions").getAsJsonArray();
			List<MCVersion> versions = new LinkedList<MCVersion>();
			for (JsonElement each : allVersions) {
				JsonObject version = each.getAsJsonObject();
				String versionId = version.get("id").getAsString();
				try {
					Date releaseTime = new SimpleDateFormat(VERSION_JSON_DATE_FORMAT).parse(version.get("releaseTime")
							.getAsString());
					MCVersionType type = MCVersionType.fromString(version.get("type").getAsString());
					versions.add(new MCVersion(versionId, releaseTime, type, this));
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

	public MCVersion getLatestRelease() {
		return this.fetchLatestRelease(VERSIONS_LIST_URL, false);
	}

	public MCVersion fetchLatestRelease(String url, boolean flush) {
		String data = this.getVersionsListData(url, flush);
		String latestVersionId = new JsonParser().parse(data).getAsJsonObject().get("latest").getAsJsonObject()
				.get("release").getAsString();
		return this.find(latestVersionId);
	}

	public MCVersion getLatestSnapshot() {
		return this.fetchLatestSnapshot(VERSIONS_LIST_URL, false);
	}

	public MCVersion fetchLatestSnapshot(String url, boolean flush) {
		String data = this.getVersionsListData(url, flush);
		String latestVersionId = new JsonParser().parse(data).getAsJsonObject().get("latest").getAsJsonObject()
				.get("snapshot").getAsString();
		return this.find(latestVersionId);
	}

	private String getVersionsListData(String url, boolean flush) {
		if (this.cachedVersionsListData == null || flush) {
			this.cachedVersionsListData = new String(new SimpleHTTPRequest(url).doGet());
		}
		return this.cachedVersionsListData;
	}
}
