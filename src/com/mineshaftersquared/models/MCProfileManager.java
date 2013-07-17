package com.mineshaftersquared.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.misc.GsonFileDeserializer;
import com.mineshaftersquared.misc.GsonFileSerializer;
import com.mineshaftersquared.misc.MS2Utils;

public class MCProfileManager {
	private final File location;
	public static final String PROFILES_JSON_NAME = "ms2-launcher_profiles.json";
	private final Set<MCProfile> profiles;
	
	public MCProfileManager(File location) {
		this.location = location;
		this.profiles = new HashSet<MCProfile>();
		this.refreshProfiles();
	}
	
	public synchronized String[] validateProfile(MCProfile profile) {
		Set<String> errors = new HashSet<String>();
		if (this.profiles.contains(profile)) {
			errors.add("This profile already exists");
		}
		if (profile.getName().isEmpty()) {
			errors.add("Profile name is empty");
		}
		if (!profile.getName().matches("[a-zA-Z0-9_-]+")) {
			errors.add("Profile name can only contain letters, numbers, underscores, and dashes");
		}
		if (!profile.getGameDir().exists()) {
			errors.add("Invalid profile folder");
		}
		return errors.toArray(new String[errors.size()]);
	}
	
	public synchronized boolean addProfile(MCProfile profile) {
		return this.profiles.add(profile);
	}
	
	public synchronized boolean deleteProfile(MCProfile profile) {
		return this.profiles.remove(profile);
	}
	
	public synchronized boolean saveProfiles() throws IOException {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(File.class, new GsonFileSerializer())
				.registerTypeAdapter(File.class, new GsonFileDeserializer())
				.create();
		Set<MCProfile> locals = new HashSet<MCProfile>();
		Set<MCProfile> globals = new HashSet<MCProfile>();
		for (MCProfile each : this.profiles) {
			(each.getIsLocal() ? locals : globals).add(each);
		}
		FileUtils.write(this.getLocalProfilesFile(), gson.toJson(locals), Charset.forName("utf-8"));
		FileUtils.write(this.getGlobalProfilesFile(), gson.toJson(globals), Charset.forName("utf-8"));
		return true;
	}
	
	public synchronized void refreshProfiles() {
		this.profiles.clear();
		this.profiles.addAll(this.refresh(this.getLocalProfilesFile()));
		this.profiles.addAll(this.refresh(this.getGlobalProfilesFile()));
	}
	
	public synchronized Set<MCProfile> getProfiles() {
		return Collections.unmodifiableSet(this.profiles);
	}
	
	public synchronized MCProfile[] profilesAsArray() {
		return this.profiles.toArray(new MCProfile[this.profiles.size()]);
	}
	
	private Set<MCProfile> refresh(File json) {
		try {
			Set<MCProfile> profiles = new Gson().fromJson(new FileReader(json), new TypeToken<Set<MCProfile>>(){}.getType());
			if (profiles != null) {
				return profiles;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IllegalStateException ex) {
			ex.printStackTrace();
		}
		return new HashSet<MCProfile>();
	}
	
	private File getLocalProfilesFile() {
		return new File(this.location, UniversalLauncher.MS2_RESOURCES_DIR + "/" + PROFILES_JSON_NAME);
	}
	
	private File getGlobalProfilesFile() {
		return new File(MS2Utils.getDefaultMCDir(), UniversalLauncher.MS2_RESOURCES_DIR + "/" + PROFILES_JSON_NAME);
	}
}
