package com.mineshaftersquared.proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleStreams;
import com.google.gson.Gson;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.misc.MS2Utils;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 * 
 * TODO: closing resources...
 */
public class MS2HttpProxyHandler implements MS2Proxy.Handler {

	private final Map<String, byte[]> skinCache;
	private final Map<String, byte[]> cloakCache;

	private final MCYggdrasilOffline yggdrasilOffline;
	private static final String[] BLACKLISTED_HEADERS = new String[]{"Connection", "Proxy-Connection", "Transfer-Encoding"};

	public MS2HttpProxyHandler() {
		this.skinCache = new HashMap<String, byte[]>();
		this.cloakCache = new HashMap<String, byte[]>();
		this.yggdrasilOffline = new MCYggdrasilOffline(new File(MS2Utils.getDefaultMCDir(), "launcher_profiles.json"));
	}

	public void handle(MS2Proxy ms2Proxy, Socket socket) {
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());

			String[] requestLine = MS2Utils.readUntil(dis, "\n").split(" ");

			String method = requestLine[0].trim().toLowerCase();
			String path = requestLine[1].trim();

			Map<String, String> headers = new HashMap<String, String>();
			while (true) {
				String line = MS2Utils.readUntil(dis, "\n").trim().toLowerCase();
				if (line.isEmpty()) {
					break;
				}
				String[] keyValue = line.split(":");
				if (keyValue.length >= 2) {
					String key = keyValue[0].trim();
					String val = keyValue[1].trim();
					headers.put(key, val);
				}
			}

			String url = path;
			MS2Proxy.log.info("Proxy - onHttp - " + Arrays.asList(requestLine));
			if (this.respondsTo(method)) {
				if (!this.on(method, url, headers, dis, dos, ms2Proxy)) {
					this.noProxy(method, url, headers, dis, dos);
				}
			} else {
				MS2Proxy.log.info("Unknown method " + method);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(socket);
			IOUtils.closeQuietly(dis);
			IOUtils.closeQuietly(dos);
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
			writer = new OutputStreamWriter(out, Charset.forName("utf-8"));
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

	public void noProxy(String method, String url, Map<String, String> headers, DataInputStream in, DataOutputStream out) throws IOException {
		URL urlObject = new URL(url);
		MS2Proxy.log.info("Piping for " + urlObject.toString());
		if (method.equals("get") || method.equals("post")) {
			HttpURLConnection con = (HttpURLConnection) urlObject.openConnection(Proxy.NO_PROXY);
			con.setRequestMethod(method.toUpperCase());
			boolean post = method.equals("post");

			for (String key : headers.keySet()) {
				con.setRequestProperty(key, headers.get(key)); // TODO Might need to blacklist these as well later
			}

			if (post) {
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.connect();
				int postlen = Integer.parseInt(headers.get("content-length"));
				byte[] postdata = new byte[postlen];
				in.read(postdata);

				DataOutputStream os = new DataOutputStream(con.getOutputStream());
				os.write(postdata);
			}

			int responseCode = con.getResponseCode();
			String res = "HTTP/1.0 " + responseCode + " " + con.getResponseMessage() + "\r\n";
			res += "Connection: close\r\nProxy-Connection: close\r\n";

			Map<String, List<String>> headerFields = con.getHeaderFields();

			for(String key : headerFields.keySet()) {
				if (key == null) {
					continue;
				}

				boolean skip = false;
				for (String each : BLACKLISTED_HEADERS) {
					if (key.equalsIgnoreCase(each)) {
						skip = true;
						break;
					}
				}
				if (!skip) {
					List<String> vals = headerFields.get(key);
					for(String each : vals) {
						res += key + ": " + each + "\r\n";
					}
				}
			}
			res += "\r\n";

			int size = -1;
			if (responseCode / 100 != 5) {
				out.writeBytes(res);
				size = SimpleStreams.pipeStreams(con.getInputStream(), out);
			}
			
			out.write(res.getBytes(Charset.forName("utf-8")));

			MS2Proxy.log.info("Piping finished, data size: " + size);
		} else if (method.equals("connect")) {
			int port = urlObject.getPort();
			if (port == -1) {
				port = 80;
			}
			Socket socket = new Socket(urlObject.getHost(), port); // TODO: close
			SimpleStreams.pipeStreamsConcurrently(socket.getInputStream(), out);
			SimpleStreams.pipeStreamsConcurrently(in, socket.getOutputStream());
		} else if (method.equals("head")) {
			HttpURLConnection con = (HttpURLConnection) urlObject.openConnection(Proxy.NO_PROXY);
			con.setRequestMethod("HEAD");

			for (String key : headers.keySet()) {
				con.setRequestProperty(key, headers.get(key));
			}

			String res = "HTTP/1.0 " + con.getResponseCode() + " " + con.getResponseMessage() + "\r\n";
			res += "Proxy-Connection: close\r\n";
			Map<String, List<String>> headerFields = con.getHeaderFields();

			for (String key : headerFields.keySet()) {
				if (key == null) {
					continue;
				}
				List<String> vals = headerFields.get(key);
				for(String each : vals) {
					res += key + ": " + each + "\r\n";
				}
			}
			res += "\r\n";

			out.write(res.getBytes(Charset.forName("utf-8")));
		} else {
			throw new IllegalArgumentException("Unknown method " + method);
		}
	}
}
