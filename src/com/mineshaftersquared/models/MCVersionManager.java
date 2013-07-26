package com.mineshaftersquared.models;

import java.nio.charset.Charset;
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
	
	private volatile MCVersion[] cachedVersions;
	private final Object lock;

	public MCVersionManager() {
		this.cachedVersions = null;
		this.lock = new Object();
	}
	
	public MCVersion[] getVersions() {
		return this.getVersions(VERSIONS_LIST_URL, false);
	}

	public MCVersion[] getVersions(String url, boolean flush) {
		synchronized (this.lock) {
			if (this.cachedVersions == null || flush) {
				this.cachedVersions = this.fetchVersionsFromURL(url);
			}
			return this.cachedVersions;
		}
	}
	
	public void refreshVersions() {
		synchronized (this.lock) {
			this.cachedVersions = null;
		}
	}

	private MCVersion[] fetchVersionsFromURL(String url) {
		try {
			String data = new String(new SimpleHTTPRequest(url).doGet(), Charset.forName("utf-8"));
			JsonElement root = new JsonParser().parse(data);
			JsonArray allVersions = root.getAsJsonObject().get("versions").getAsJsonArray();
			List<MCVersion> versions = new LinkedList<MCVersion>();
			for (JsonElement each : allVersions) {
				JsonObject version = each.getAsJsonObject();
				String versionId = version.get("id").getAsString();
				versions.add(new MCVersion(versionId));
			}
			return versions.toArray(new MCVersion[versions.size()]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}