package com.mineshaftersquared.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Proxy;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.mineshaftersquared.UniversalLauncher;

public class HttpProxyHandler extends MS2Proxy.Handler {
	
	private Map<String, byte[]> skinCache = new HashMap<String, byte[]>();
	private Map<String, byte[]> cloakCache = new HashMap<String, byte[]>();
	
	public HttpProxyHandler(MS2Proxy ms2Proxy) {
		super(ms2Proxy);
	}
	
	public void handle(Socket socket) {
		try {
			
		} finally {
			IOUtils.closeQuietly(socket);
		}
	}
	
	public void on(String method, String url, Map<String, String> headers, InputStream in, OutputStream out) {
		switch (method.toLowerCase()) {
		case "get":
			this.onGet(url, headers, in, out);
			break;
		case "post":
			this.onPost(url, headers, in, out);
			break;
		case "head":
			this.onHead(url, headers, in, out);
			break;
		case "connect":
			this.onConnect(url, headers, in, out);
			break;
		default:
			throw new IllegalArgumentException("Unknown method " + method);
		}
	}

	public void onGet(String url, Map<String, String> headers, InputStream in, OutputStream out) {
		UniversalLauncher.log.info("Proxy - get - " + url);
		Matcher skinMatcher = MS2Proxy.SKIN_URL.matcher(url);
		Matcher cloakMatcher = MS2Proxy.CLOAK_URL.matcher(url);
		if (skinMatcher.matches()) {
			String username = skinMatcher.group(1);
			UniversalLauncher.log.info("Proxy - skin - " + username);
			byte[] data = this.skinCache.get(username);
			if (data == null) {
				String proxiedURL = this.ms2Proxy.authserver + "/mcapi/skin/" + username + ".png";
				data = this.getRequest(proxiedURL);
			}
			this.skinCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return;
		} else if (cloakMatcher.matches()) {
			String username = cloakMatcher.group(1);
			UniversalLauncher.log.info("Proxy - cloak - " + username);
			byte[] data = this.cloakCache.get(username);
			if (data == null) {
				String proxiedURL = this.ms2Proxy.authserver + "/mcapi/cloak/" + username + ".png";
				data = this.getRequest(proxiedURL);
			}
			this.cloakCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return;
		} else {
			UniversalLauncher.log.info("Proxy - no handler for - " + url);
		}
	}
	public void onPost(String url, Map<String, String> headers, InputStream in, OutputStream out) {
		
	}
	public void onHead(String url, Map<String, String> headers, InputStream in, OutputStream out) {
		
	}
	public void onConnect(String url, Map<String, String> headers, InputStream in, OutputStream out) {
		
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
}
