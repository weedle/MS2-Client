package com.mineshaftersquared.models;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.mineshaftersquared.models.OldAuth.Response;

public class NewAuth {

	public static Response login(String username, String password, String authserver) {
		String result = new String(new SimpleHTTPRequest("http://" + authserver + "/game/get_version")
				.addPost("user", username)
				.addPost("password", password).doPost());
		return parse(result);
	}
	
	private static Response parse(String raw) {
		if (raw == null) {
			return new Response(null, null, null);
		}
		String[] parts = raw.split(":");
		try {
			return new Response(raw, parts[0], parts[1]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			return new Response(raw, null, null);
		}
	}
	
	public static class Response {
		public final String rawResponse;
		public final String sessionId;
		public final String second;
		
		private Response(String raw, String sessionId, String second) {
			this.rawResponse = raw;
			this.sessionId = sessionId;
			this.second = second;
		}
	}
}
