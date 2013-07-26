package com.mineshaftersquared.proxy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.mineshaftersquared.proxy.MCYggdrasilOffline.ProfilesJSON.OuterProfile;

/**
 * Credits to download13 of Mineshafter
 * 
 */
public class MCYggdrasilOffline {

	private final List<Profile> profiles;
	private final Gson gson;

	public MCYggdrasilOffline(File profilesFile) {
		this.profiles = new LinkedList<Profile>();
		this.gson = new Gson();

		try {
			ProfilesJSON profiles = gson.fromJson(new FileReader(profilesFile), ProfilesJSON.class);
			Map<String, OuterProfile> map = profiles.profiles;
			for (String key : map.keySet()) {
				OuterProfile op = map.get(key);
				Profile p = op.auth;
				if (p == null) {
					continue;
				}

				if (p.displayName == null || p.displayName.trim().isEmpty()) {
					p.displayName = p.username;
				}
				this.profiles.add(p);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized Profile profileByUsername(String username) {
		for (Profile each : this.profiles) {
			if (each.username.equals(username)) {
				return each;
			}
		}
		return null;
	}

	public synchronized Profile profileByAccessToken(String token) {
		for (Profile each : this.profiles) {
			if (each.accessToken.equals(token)) {
				return each;
			}
		}
		return null;
	}

	public synchronized String authenticate(MCYggdrasilRequest req) {
		String accessToken = randomToken();
		Profile p = this.profileByUsername(req.username);
		if (p == null) {
			String clientId = randomToken();
			p = new Profile(req.username, accessToken, clientId, req.username);
			this.profiles.add(p);
		}
		p.accessToken = accessToken;

		ProfileResponse pr = new ProfileResponse(p.uuid, p.displayName);
		AuthResponse ar = new AuthResponse(req.clientToken, p.accessToken, pr);
		ar.availableProfiles.add(pr);
		return gson.toJson(ar);
	}

	public synchronized String refresh(MCYggdrasilRequest req) {
		Profile p = profileByAccessToken(req.accessToken);
		if (p == null) {
			return "{\"error\": \"ForbiddenOperationException\", \"errorMessage\": \"Invalid token.\"}";
		}
		p.accessToken = randomToken();
		AuthResponse ar = new AuthResponse(req.clientToken, p.accessToken, new ProfileResponse(p.uuid, p.displayName));
		return gson.toJson(ar);
	}

	public synchronized String invalidate(MCYggdrasilRequest req) {
		Profile p = this.profileByAccessToken(req.accessToken);
		if (p == null) {
			return "{\"error\": \"ForbiddenOperationException\", \"errorMessage\": \"Invalid token.\"}";
		}
		p.accessToken = null;
		return "";
	}

	public static String randomToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static class Profile {
		public final String username;
		public String accessToken;
		public final String uuid;
		public String displayName;

		public Profile(String username, String accessToken, String uuid, String displayName) {
			this.username = username;
			this.accessToken = accessToken;
			this.uuid = uuid;
			this.displayName = displayName;
		}
	}

	public static class ProfilesJSON {
		public final Map<String, OuterProfile> profiles;
		public final String selectedProfile;
		public final String clientToken;

		public ProfilesJSON(String selectedProfile, String clientToken) {
			this.profiles = new HashMap<String, OuterProfile>();
			this.selectedProfile = selectedProfile;
			this.clientToken = clientToken;
		}


		public static class OuterProfile {
			public final String name;
			public final Profile auth;

			public OuterProfile(String name, Profile auth) {
				this.name = name;
				this.auth = auth;
			}
		}
	}
	public static class ProfileResponse {
		public final String id;
		public final String name;
		public ProfileResponse(String id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	public static class AuthResponse {
		public final String accessToken;
		public final String clientToken;
		public final ProfileResponse selectedProfile;
		public final List<ProfileResponse> availableProfiles;

		public AuthResponse(String clientToken, String accessToken, ProfileResponse selected) {
			this.clientToken = clientToken;
			this.accessToken = accessToken;
			this.selectedProfile = selected;
			this.availableProfiles = new LinkedList<ProfileResponse>();
		}
	}
}
