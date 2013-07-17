package com.mineshaftersquared.proxy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Credits to download13 of Mineshafter
 * 
 */
public class MCYggdrasilImpersonator {
	
	private final List<Profile> profiles;
	
	public MCYggdrasilImpersonator() {
		this.profiles = new LinkedList<Profile>();
	}
	
	public static class Profile {
		public final String username;
		public final String accessToken;
		public final String uuid;
		public final String displayName;
		
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
	}
	public static class ProfileResponse {
		public final String id;
		public final String name;
		public ProfileResponse(String id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	public static class OuterProfile {
		public final String name;
		public final Profile auth;
		
		public OuterProfile(String name, Profile auth) {
			this.name = name;
			this.auth = auth;
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
