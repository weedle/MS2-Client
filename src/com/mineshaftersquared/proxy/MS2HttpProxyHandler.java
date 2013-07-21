package com.mineshaftersquared.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.google.gson.Gson;
import com.mineshaftersquared.UniversalLauncher;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public class MS2HttpProxyHandler implements MS2Proxy.Handler {
	
	private final Map<String, byte[]> skinCache;
	private final Map<String, byte[]> cloakCache;
	
	public MS2HttpProxyHandler() {
		this.skinCache = new HashMap<String, byte[]>();
		this.cloakCache = new HashMap<String, byte[]>();
	}
	
	public void handle(MS2Proxy proxy, Socket socket) {
		try {
			
		} finally {
			IOUtils.closeQuietly(socket);
		}
	}
	
	public boolean respondsTo(String method) {
		return Arrays.asList("get", "post", "head", "connect").contains(method);
	}
	
	public boolean on(String method, String url, Map<String, String> headers, InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		switch (method.toLowerCase()) {
		case "get":
			return this.onGet(url, headers, in, out, ms2Proxy);
		case "post":
			return this.onPost(url, headers, in, out, ms2Proxy);
		case "head":
			return this.onHead(url, headers, in, out, ms2Proxy);
		case "connect":
			return this.onConnect(url, headers, in, out, ms2Proxy);
		default:
			throw new IllegalArgumentException("Unknown method " + method);
		}
	}

	public boolean onGet(String url, Map<String, String> headers, InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		UniversalLauncher.log.info("Proxy - get - " + url);
		Matcher skinMatcher = MS2Proxy.SKIN_URL.matcher(url);
		Matcher cloakMatcher = MS2Proxy.CLOAK_URL.matcher(url);
		if (skinMatcher.matches()) {
			String username = skinMatcher.group(1);
			UniversalLauncher.log.info("Proxy - skin - " + username);
			byte[] data = this.skinCache.get(username);
			if (data == null) {
//				String proxiedURL = ms2Proxy.authserver + "/mcapi/skin/" + username + ".png";
//				data = this.getRequest(proxiedURL);
				data = new byte[0];
			}
			this.skinCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return true;
		} else if (cloakMatcher.matches()) {
			String username = cloakMatcher.group(1);
			UniversalLauncher.log.info("Proxy - cloak - " + username);
			byte[] data = this.cloakCache.get(username);
			if (data == null) {
//				String proxiedURL = ms2Proxy.authserver + "/mcapi/cloak/" + username + ".png";
//				data = this.getRequest(proxiedURL);
				data = new byte[0];
			}
			this.cloakCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return true;
		} else {
			return false;
		}
	}
	public boolean onPost(String url, Map<String, String> headers, InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		UniversalLauncher.log.info("Proxy - post - " + url);
		int contentLength = Integer.parseInt(headers.get("content-length"));
		Matcher authServerMatcher = MS2Proxy.AUTH_URL.matcher(url);
		
		if (authServerMatcher.matches()) {
			UniversalLauncher.log.info("Proxy - auth");
			
			String endpoint = authServerMatcher.group(1);
			try {
				char[] body = new char[contentLength];
				InputStreamReader reader = new InputStreamReader(in, Charset.forName("utf-8"));
				reader.read(body);
				String postedJSON = new String(body);
				String response = this.authServerAction(endpoint, postedJSON, ms2Proxy);
				this.sendResponse(out, "application/json", response.getBytes(Charset.forName("utf-8")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return true;
		}
		return false;
	}
	public boolean onHead(String url, Map<String, String> headers, InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		return false;
	}
	public boolean onConnect(String url, Map<String, String> headers, InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		return false;
	}
	
	private void sendResponse(OutputStream out, String contentType, byte[] data) {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(out);
			writer.append("HTTP/1.0 200 OK\r\nConnection: close\r\nProxy-Connection: close\r\n");
			writer.append("Content-Length:" + data.length + "\r\n");
			if (contentType != null) {
				writer.append("Content-Type: " + contentType + "\r\n\r\n");
			}
			writer.flush();
			out.write(data);
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(writer);
		}
	}
	
	private byte[] getRequest(String url) {
		return new SimpleHTTPRequest(url).doGet(Proxy.NO_PROXY);
	}
	
	private byte[] postRequest(String url, String postdata) {
		return new SimpleHTTPRequest(url).addPost(postdata).doPost(Proxy.NO_PROXY);
	}
	
	private String authServerAction(String action, String postedJSON, MS2Proxy ms2Proxy) {
		UniversalLauncher.log.info("Proxy - auth - endpoint: " + action + ", postedJSON - " + postedJSON);
		Gson gson = new Gson();
		MCYggdrasilRequest data = gson.fromJson(postedJSON, MCYggdrasilRequest.class);
		
		SimpleHTTPRequest request;
		if (action.equalsIgnoreCase("authenticate")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getAuthenticateURL());
			request.addPost("username", data.username);
			request.addPost("password", data.password);
			request.addPost("clientToken", data.clientToken);
		} else if (action.equalsIgnoreCase("refresh")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getRefreshURL());
			request.addPost("clientToken", data.clientToken);
			request.addPost("accessToken", data.accessToken);
		} else if (action.equalsIgnoreCase("invalidate")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getInvalidateURL());
			request.addPost("clientToken", data.clientToken);
			request.addPost("accessToken", data.accessToken);
		} else {
			throw new IllegalArgumentException("Unknown action " + action);
		}
		
		return new String(request.doPost(Proxy.NO_PROXY), Charset.forName("utf-8"));
	}
}
