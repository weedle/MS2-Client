package com.mineshaftersquared.models;

import com.mineshaftersquared.UniversalLauncher;

public class MCOneSixAuth {
	
	private final UniversalLauncher app;
	
	public MCOneSixAuth(UniversalLauncher app) {
		this.app = app;
	}
	
	public Response login(String username, String password) {
		return null;
	}

	public static class Response {
		public final String username;
		public final String accessToken;
		public final String clientToken;
		
		public Response(String username, String accessToken, String clientToken) {
			this.username = username;
			this.accessToken = accessToken;
			this.clientToken = clientToken;
		}
	}
}
