package com.mineshaftersquared.models;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;

public class OldAuth {
	
	public static Response login(String username, String password, String authserver) {
		String result = new String(new SimpleHTTPRequest("http://" + authserver + "/game/get_version")
				.addPost("user", username)
				.addPost("password", password).doPost());
		return parse(result);
	}
	
	private static Response parse(String raw) {
		if (raw == null) {
			return new Response(null, null, null, null);
		}
		String[] parts = raw.split(":");
		try {
			return new Response(raw, parts[2], parts[3], parts[2]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			return new Response(raw, null, null, parts[0]);
		}
	}
	
	public static class Response {
		public final String rawResponse;
		public final String sessionId;
		public final String message;
		public final String username;
		
		private Response(String raw, String username, String sessionId, String message) {
			this.rawResponse = raw;
			this.username = username;
			this.sessionId = sessionId;
			this.message = message;
		}
	}
}
