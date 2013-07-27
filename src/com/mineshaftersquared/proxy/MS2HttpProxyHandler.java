package com.mineshaftersquared.proxy;

import java.io.File;
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
import com.mineshaftersquared.misc.MS2Utils;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public class MS2HttpProxyHandler implements MS2Proxy.Handler {
	
	private final Map<String, byte[]> skinCache;
	private final Map<String, byte[]> cloakCache;
	
	private final MCYggdrasilOffline yggdrasilOffline;
	
	public MS2HttpProxyHandler() {
		this.skinCache = new HashMap<String, byte[]>();
		this.cloakCache = new HashMap<String, byte[]>();
		this.yggdrasilOffline = new MCYggdrasilOffline(new File(MS2Utils.getDefaultMCDir(), "launcher_profiles.json"));
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
		method = method.toLowerCase();
		if (method.equals("get")) {
			return this.onGet(url, headers, in, out, ms2Proxy);
		} else if (method.equals("post")) {
			return this.onPost(url, headers, in, out, ms2Proxy);
		} else if (method.equals("head")) {
			return this.onHead(url, headers, in, out, ms2Proxy);
		} else if (method.equals("connect")) {
			return this.onConnect(url, headers, in, out, ms2Proxy);
		} else {
			throw new IllegalArgumentException("Unknown method " + method);
		}
	}

	public boolean onGet(String url, Map<String, String> headers, InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		UniversalLauncher.log.info("Proxy - get - " + url);
		Matcher skinMatcher = MS2Proxy.SKIN_URL.matcher(url);
		Matcher cloakMatcher = MS2Proxy.CLOAK_URL.matcher(url);
		Matcher checkserverMatcher = MS2Proxy.CHECKSERVER_URL.matcher(url);
		Matcher joinserverMatcher = MS2Proxy.JOINSERVER_URL.matcher(url);
		if (skinMatcher.matches()) {
			String username = skinMatcher.group(1);
			UniversalLauncher.log.info("Proxy - skin - " + username);
			byte[] data = this.skinCache.get(username);
			if (data == null) {
				data = new SimpleHTTPRequest(ms2Proxy.routes.getSkinURL()).addGet("username", username).doGet();
			}
			this.skinCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return true;
		} else if (cloakMatcher.matches()) {
			String username = cloakMatcher.group(1);
			UniversalLauncher.log.info("Proxy - cloak - " + username);
			byte[] data = this.cloakCache.get(username);
			if (data == null) {
				data = new SimpleHTTPRequest(ms2Proxy.routes.getCloakURL()).addGet("username", username).doGet();
			}
			this.cloakCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return true;
		} else if (checkserverMatcher.matches()) {
			this.sendResponse(out, "text/plain", "yes".getBytes(Charset.forName("utf-8")));
			return true;
		} else if (joinserverMatcher.matches()) {
			this.sendResponse(out, "text/plain", "ok".getBytes(Charset.forName("utf-8")));
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
			
			String action = authServerMatcher.group(1);
			try {
				char[] body = new char[contentLength];
				InputStreamReader reader = new InputStreamReader(in/*, Charset.forName("utf-8")*/);
				int x = reader.read(body);
				String postedJSON = new String(body);
				for (String key : headers.keySet()) {
					System.out.println(key + ": " + headers.get(key));
				}
				System.out.println("Content length was : " + contentLength);
				System.out.println("Read: " + x);
				System.out.println("String length: " + postedJSON.length());
				System.out.println("Posted json: " + postedJSON);
				System.out.flush();
				String response = this.authServerAction(action, postedJSON, ms2Proxy);
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
		UniversalLauncher.log.info("Proxy - auth - action: " + action + ", postedJSON - " + postedJSON);
		Gson gson = new Gson();
		MCYggdrasilRequest data = gson.fromJson(postedJSON, MCYggdrasilRequest.class);

        /**
         * The 3 return statements here make the difference between online and offline
         * authentication.  A few if statements and a flag can give users the option
         * to pick between the two.  Lets leave this in here for now but turn it to
         * online mode by default.
         */
        SimpleHTTPRequest request;
		if (action.equalsIgnoreCase("authenticate")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getAuthenticateURL());
			request.addPost("username", data.username);
			request.addPost("password", data.password);
			request.addPost("clientToken", data.clientToken);
			//return this.yggdrasilOffline.authenticate(data);
		} else if (action.equalsIgnoreCase("refresh")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getRefreshURL());
			request.addPost("clientToken", data.clientToken);
			request.addPost("accessToken", data.accessToken);
			//return this.yggdrasilOffline.refresh(data);
		} else if (action.equalsIgnoreCase("invalidate")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getInvalidateURL());
			request.addPost("clientToken", data.clientToken);
			request.addPost("accessToken", data.accessToken);
			//return this.yggdrasilOffline.invalidate(data);
		} else {
			throw new IllegalArgumentException("Unknown action " + action);
		}
		
		return new String(request.doPost(Proxy.NO_PROXY), Charset.forName("utf-8"));
	}
}
