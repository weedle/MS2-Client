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
import com.mineshaftersquared.misc.MS2Utils;

/**
 * Credits to download13 of Mineshafter Modified by Adrian
 * 
 * TODO: closing resources...
 */
public class MS2HttpProxyHandler implements MS2Proxy.Handler {

	private final Map<String, byte[]> skinCache;
	private final Map<String, byte[]> cloakCache;
	
	public static final String MOJANG_JOINSERVER = "http://session.minecraft.net/game/joinserver.jsp";
	public static final String MOJANG_CHECKSERVER = "http://session.minecraft.net/game/checkserver.jsp";

	private static final String[] BLACKLISTED_HEADERS = new String[] {
		"Connection", "Proxy-Connection", "Transfer-Encoding" };

	public MS2HttpProxyHandler() {
		this.skinCache = new HashMap<String, byte[]>();
		this.cloakCache = new HashMap<String, byte[]>();
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
				String line = MS2Utils.readUntil(dis, "\n").trim()
						.toLowerCase();
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

	public boolean on(String method, String url, Map<String, String> headers,
			InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
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

	public boolean onGet(String url, Map<String, String> headers,
			InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		MS2Proxy.log.info("Proxy - get - " + url);
		Matcher skinMatcher = MS2Proxy.SKIN_URL.matcher(url);
		Matcher cloakMatcher = MS2Proxy.CLOAK_URL.matcher(url);
		Matcher checkserverMatcher = MS2Proxy.CHECKSERVER_URL.matcher(url);
		Matcher joinserverMatcher = MS2Proxy.JOINSERVER_URL.matcher(url);

		if (skinMatcher.matches()) {
			String username = skinMatcher.group(1);
			MS2Proxy.log.info("Proxy - skin - " + username);
			byte[] data = this.skinCache.get(username);

			if (data == null) {
				data = new SimpleHTTPRequest(
						ms2Proxy.routes.getSkinURL(username)).doGet();
			}

			this.skinCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return true;
		} else if (cloakMatcher.matches()) {
			String username = cloakMatcher.group(1);
			MS2Proxy.log.info("Proxy - cloak - " + username);
			byte[] data = this.cloakCache.get(username);

			if (data == null) {
				data = new SimpleHTTPRequest(
						ms2Proxy.routes.getCloakURL(username)).doGet();
			}

			this.cloakCache.put(username, data);
			this.sendResponse(out, "image/png", data);
			return true;
		} else if (checkserverMatcher.matches()) {
			MS2Proxy.log.info("Proxy - checkserver");
			String[] request = url.split("[?]");
			String response = this.authMultiplayerAction("checkserver",
					request[1], ms2Proxy);

			this.sendResponse(out, "text/plain",
					response.getBytes(Charset.forName("utf-8")));

			return true;
		} else if (joinserverMatcher.matches()) {
			MS2Proxy.log.info("Proxy - joinserver");
			String[] request = url.split("[?]");
			String response = this.authMultiplayerAction("joinserver",
					request[1], ms2Proxy);

			this.sendResponse(out, "text/plain",
					response.getBytes(Charset.forName("utf-8")));
			return true;
		} else {
			return false;
		}
	}

	public boolean onPost(String url, Map<String, String> headers,
			InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		MS2Proxy.log.info("Proxy - post - " + url);
		int contentLength = Integer.parseInt(headers.get("content-length"));
		Matcher authServerMatcher = MS2Proxy.AUTH_URL.matcher(url);

		if (authServerMatcher.matches()) {
			MS2Proxy.log.info("Proxy - auth");

			String action = authServerMatcher.group(1).toLowerCase();
			try {
				char[] body = new char[contentLength];
				InputStreamReader reader = new InputStreamReader(in, Charset.forName("utf-8"));
				int x = reader.read(body);
				String postedJSON = new String(body);
//				for (String key : headers.keySet()) {
//					MS2Proxy.log.info(key + ": " + headers.get(key));
//				}
				String response = this.authServerAction(action, postedJSON,
						ms2Proxy);
				this.sendResponse(out, "application/json",
						response.getBytes(Charset.forName("utf-8")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public boolean onHead(String url, Map<String, String> headers,
			InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
		return false;
	}

	public boolean onConnect(String url, Map<String, String> headers,
			InputStream in, OutputStream out, MS2Proxy ms2Proxy) {
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

	private String authServerAction(String action, String postedJSON,
			MS2Proxy ms2Proxy) {
		MCYggdrasilOffline yggdrasil = null;
		if (ms2Proxy.offline) {
			MS2Proxy.log.info("Using offline authentication");
			yggdrasil = new MCYggdrasilOffline(new File(MS2Utils.getDefaultMCDir(), MCYggdrasilOffline.LAUNCHER_PROFILES));
		}
		MS2Proxy.log.info("Proxy - auth - action: " + action
				+ ", postedJSON - " + postedJSON);
		Gson gson = new Gson();
		MCYggdrasilRequest data = gson.fromJson(postedJSON,
				MCYggdrasilRequest.class);

		/**
		 * The 3 return statements here make the difference between online and
		 * offline authentication. A few if statements and a flag can give users
		 * the option to pick between the two. Lets leave this in here for now
		 * but turn it to online mode by default.
		 */
		SimpleHTTPRequest request;
		if (action.equalsIgnoreCase("authenticate")) {
			if (ms2Proxy.offline) {
				String response = yggdrasil.authenticate(data);
				MS2Proxy.log.info("Proxy - auth - returnedJSON - " + response);
				return response;
			}
			request = new SimpleHTTPRequest(
					ms2Proxy.routes.getAuthenticateURL());
			request.addPost("username", data.username);
			request.addPost("password", data.password);
			request.addPost("clientToken", data.clientToken);
		} else if (action.equalsIgnoreCase("refresh")) {
			if (ms2Proxy.offline) {
				String response = yggdrasil.refresh(data);
				MS2Proxy.log.info("Proxy - auth - returnedJSON - " + response);
				return response;
			}
			request = new SimpleHTTPRequest(ms2Proxy.routes.getRefreshURL());
			request.addPost("clientToken", data.clientToken);
			request.addPost("accessToken", data.accessToken);
		} else if (action.equalsIgnoreCase("invalidate")) {
			if (ms2Proxy.offline) {
				String response = yggdrasil.invalidate(data);
				MS2Proxy.log.info("Proxy - auth - returnedJSON (offline) - " + response);
				return response;
			}
			request = new SimpleHTTPRequest(ms2Proxy.routes.getInvalidateURL());
			request.addPost("clientToken", data.clientToken);
			request.addPost("accessToken", data.accessToken);
		} else {
			throw new IllegalArgumentException("Unknown action " + action);
		}

		String response = new String(request.doPost(Proxy.NO_PROXY),
				Charset.forName("utf-8"));
		MS2Proxy.log.info("Proxy - auth - returnedJSON - " + response);
		return response;
	}

	private String authMultiplayerAction(String action, String data,
			MS2Proxy ms2Proxy) {
		MS2Proxy.log.info("Proxy - auth - action: " + action
				+ ", data - " + data);

		SimpleHTTPRequest request = null;
		if (action.equals("joinserver")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getJoinServerURL());
			// return "OK";
		} else if (action.equals("checkserver")) {
			request = new SimpleHTTPRequest(ms2Proxy.routes.getCheckServerURL());
			// return "YES";
		} else {
			throw new IllegalArgumentException("Unknown action " + action);
		}
		request.addGet(data);
		String ms2Response = new String(request.doGet(Proxy.NO_PROXY),
				Charset.forName("utf-8"));
		
		if (action.equals("joinserver")) {
			if (ms2Response.equals("OK")) {
				return "OK";
			}
		} else if (action.equals("checkserver")) {
			if (ms2Response.equals("YES")) {
				return "YES";
			}
		}
		
//		if (ms2Proxy.isPlayerMarked(player)) {
//			return ms2Response;
//		}
		
		MS2Proxy.log.info("Player not authenticated with Mineshafter Squared. Trying Mojang (passthrough)");
		
		SimpleHTTPRequest request2 = null;
		if (action.equals("joinserver")) {
			request2 = new SimpleHTTPRequest(MOJANG_JOINSERVER);
		} else if (action.equals("checkserver")) {
			request2 = new SimpleHTTPRequest(MOJANG_CHECKSERVER);
		}
		request2.addGet(data);
		String mojangResponse = new String(request2.doGet(Proxy.NO_PROXY), Charset.forName("utf-8"));
		
		if (action.equals("joinserver")) {
			if (mojangResponse.equals("OK")) {
				return "OK";
			}
		} else if (action.equals("checkserver")) {
			if (mojangResponse.equals("YES")) {
				return "YES";
			}
		}
		
//		ms2Proxy.markPlayer(player);
		
		return mojangResponse;
	}

	public void noProxy(String method, String url, Map<String, String> headers,
			DataInputStream in, DataOutputStream out) throws IOException {
		URL urlObject = new URL(url);
		MS2Proxy.log.info("Piping for " + urlObject.toString());
		if (method.equals("get") || method.equals("post")) {
			HttpURLConnection con = (HttpURLConnection) urlObject
					.openConnection(Proxy.NO_PROXY);
			con.setRequestMethod(method.toUpperCase());
			boolean post = method.equals("post");

			for (String key : headers.keySet()) {
				con.setRequestProperty(key, headers.get(key)); // TODO Might
				// need to
				// blacklist
				// these as well
				// later
			}

			if (post) {
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.connect();
				int postlen = Integer.parseInt(headers.get("content-length"));
				byte[] postdata = new byte[postlen];
				in.read(postdata);

				DataOutputStream os = new DataOutputStream(
						con.getOutputStream());
				os.write(postdata);
			}

			int responseCode = con.getResponseCode();
			String res = "HTTP/1.0 " + responseCode + " "
					+ con.getResponseMessage() + "\r\n";
			res += "Connection: close\r\nProxy-Connection: close\r\n";

			Map<String, List<String>> headerFields = con.getHeaderFields();

			for (String key : headerFields.keySet()) {
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
					for (String each : vals) {
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
			final Socket socket = new Socket(urlObject.getHost(), port);

			final boolean[] lock = new boolean[0];

			SimpleStreams.PipeStreamDoneListener listener = new SimpleStreams.PipeStreamDoneListener() {
				@Override
				public void onDone(int bytesPiped) {
					synchronized (lock) {
						if (lock[0] == true) {
							IOUtils.closeQuietly(socket);
						} else {
							lock[0] = true;
						}
					}
				}
			};
			SimpleStreams.pipeStreamsConcurrently(socket.getInputStream(), out,
					listener);
			SimpleStreams.pipeStreamsConcurrently(in, socket.getOutputStream(),
					listener);
		} else if (method.equals("head")) {
			HttpURLConnection con = (HttpURLConnection) urlObject
					.openConnection(Proxy.NO_PROXY);
			con.setRequestMethod("HEAD");

			for (String key : headers.keySet()) {
				con.setRequestProperty(key, headers.get(key));
			}

			String res = "HTTP/1.0 " + con.getResponseCode() + " "
					+ con.getResponseMessage() + "\r\n";
			res += "Proxy-Connection: close\r\n";
			Map<String, List<String>> headerFields = con.getHeaderFields();

			for (String key : headerFields.keySet()) {
				if (key == null) {
					continue;
				}
				List<String> vals = headerFields.get(key);
				for (String each : vals) {
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
