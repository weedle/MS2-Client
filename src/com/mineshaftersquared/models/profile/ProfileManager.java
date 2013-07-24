package com.mineshaftersquared.models.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import sun.misc.Launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.misc.DateTypeAdapter;
import com.mineshaftersquared.misc.GsonFileSerializer;
import com.mineshaftersquared.misc.LowerCaseEnumTypeAdapterFactory;

public class ProfileManager {
	public static final String DEFAULT_PROFILE_NAME = "(Default)";
	private final Gson gson;
	private final UniversalLauncher app;
	private final Map<String, Profile> profiles = new HashMap();
	private final File profileFile;
	private String selectedProfile;

	public ProfileManager(UniversalLauncher app, File base) {
		this.profileFile = new File(base, "ms2-launcher_profiles.json");

		this.app = app;
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
		builder.registerTypeAdapter(File.class, new GsonFileSerializer());
		builder.setPrettyPrinting();
		this.gson = builder.create();
	}

	public boolean saveProfiles() throws IOException {
			RawProfileList rawProfileList = new RawProfileList();
			rawProfileList.profiles = this.profiles;
			rawProfileList.selectedProfile = this.getSelectedProfile().getName();
			rawProfileList.clientToken = this.app.getClientToken();

			FileUtils.writeStringToFile(this.profileFile, this.gson.toJson(rawProfileList));
			return true;
	}
	
	public String[] validateProfile(Profile p) {
		List<String> errors = new LinkedList<String>();
		if (this.profiles.containsKey(p.getName())) {
			errors.add("This profile name already exists");
		}
		if (p.getName().isEmpty()) {
			errors.add("Profile name is empty");
		}
		if (!p.getGameDir().exists()) {
			errors.add("Invlaid profile folder");
		}
		
		return errors.toArray(new String[errors.size()]);
	}

	public boolean loadProfiles() throws IOException {
		this.profiles.clear();
		this.selectedProfile = null;
			if (this.profileFile.isFile()) {
				RawProfileList rawProfileList = (RawProfileList) this.gson.fromJson(
						FileUtils.readFileToString(this.profileFile), RawProfileList.class);

				this.profiles.putAll(rawProfileList.profiles);
				this.selectedProfile = rawProfileList.selectedProfile;
				this.app.setClientToken(rawProfileList.clientToken);

				return true;
			}
		return false;
	}
	
	public boolean addProfile(Profile p) throws IOException {
		this.profiles.put(p.getName(), p);
		return this.saveProfiles();
	}

	public boolean deleteProfile(Profile p) throws IOException {
		this.profiles.remove(p.getName());
		return this.saveProfiles();
	}

	public Profile getSelectedProfile() {
		if ((this.selectedProfile == null) || (!this.profiles.containsKey(this.selectedProfile))) {
			if (this.profiles.get("(Default)") != null) {
				this.selectedProfile = "(Default)";
			} else if (this.profiles.size() > 0) {
				this.selectedProfile = ((Profile) this.profiles.values().iterator().next()).getName();
			} else {
				this.selectedProfile = "(Default)";
				this.profiles.put("(Default)", new Profile(this.selectedProfile));
			}
		}

		return (Profile) this.profiles.get(this.selectedProfile);
	}

	public Map<String, Profile> getProfiles() {
		return this.profiles;
	}


	public void setSelectedProfile(String selectedProfile) {
		boolean update = !this.selectedProfile.equals(selectedProfile);
		this.selectedProfile = selectedProfile;
	}


	private static class RawProfileList {
		public Map<String, Profile> profiles = new HashMap();
		public String selectedProfile;
		public UUID clientToken = UUID.randomUUID();
	}
}