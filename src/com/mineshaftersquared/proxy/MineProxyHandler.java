package com.mineshaftersquared.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creatifcubed.simpleapi.SimpleStreams;
import com.mineshaftersquared.UniversalLauncher;

public class MineProxyHandler extends Thread {

	private DataInputStream fromClient;
	private DataOutputStream toClient;
	private Socket connection;
	private MineProxy proxy;
	private static String[] BLACKLISTED_HEADERS = new String[] { "Connection", "Proxy-Connection", "Transfer-Encoding" };
	public static Pattern HELP_TICKET_LOGIN_REGEX = Pattern
			.compile("\\b[0-9]{13}\\b:\\b\\w+\\b:\\S+:\\b[0-9|a-z]+\\b:\\b[0-9|a-z]+\\b");

	public MineProxyHandler(MineProxy proxy, Socket conn) throws IOException {
		this.setName("MineProxyHandler Thread");

		this.proxy = proxy;

		this.connection = conn;
		this.fromClient = new DataInputStream(conn.getInputStream());
		this.toClient = new DataOutputStream(conn.getOutputStream());
	}

	@Override
	public void run() {
		HashMap<String, String> headers = new HashMap<String, String>();

		// Read the incoming request
		String[] requestLine = readUntil(this.fromClient, '\n').split(" ");
		String method = requestLine[0].trim().toUpperCase();
		String url = requestLine[1].trim();
		UniversalLauncher.log.info("run beginning: request to: " + url);

		UniversalLauncher.log.info("Request: " + method + " " + url);

		// Read the incoming headers
		String header;
		do {
			header = readUntil(this.fromClient, '\n').trim();

			int splitPoint = header.indexOf(':');
			if (splitPoint != -1) {
				headers.put(header.substring(0, splitPoint).toLowerCase().trim(), header.substring(splitPoint + 1)
						.trim());
			}

		} while (header.length() > 0);

		// run matchers
		Matcher skinMatcher = MineProxy.SKIN_URL.matcher(url);
		Matcher cloakMatcher = MineProxy.CLOAK_URL.matcher(url);
		Matcher getversionMatcher = MineProxy.GETVERSION_URL.matcher(url);
		Matcher altLoginMatcher = MineProxy.ALTLOGIN_URL.matcher(url);
		Matcher joinserverMatcher = MineProxy.JOINSERVER_URL.matcher(url);
		Matcher checkserverMatcher = MineProxy.CHECKSERVER_URL.matcher(url);
		Matcher audiofix_url = MineProxy.AUDIOFIX_URL.matcher(url);
		Matcher dl_bukkit = MineProxy.DL_BUKKIT.matcher(url);
		Matcher client_snoop = MineProxy.CLIENT_SNOOP.matcher(url);
		Matcher server_snoop = MineProxy.SERVER_SNOOP.matcher(url);
		Matcher altSkinMatcher = MineProxy.ALTSKIN_URL.matcher(url);
		Matcher spoutcraftSkinMatcher = MineProxy.SPOUTCRAFT_SKIN_URL.matcher(url);
		Matcher technicSkinMatcher = MineProxy.TECHNIC_SKIN_URL.matcher(url);
		Matcher spoutcraftCloakMatcher = MineProxy.SPOUTCRAFT_CLOAK_URL.matcher(url);

		byte[] data = null;
		String contentType = null;
		String params;

		// If Skin Request
		if (skinMatcher.matches() || altSkinMatcher.matches() || spoutcraftSkinMatcher.matches()
				|| technicSkinMatcher.matches()) {
			UniversalLauncher.log.info("Skin");

			String username = null;
			if (skinMatcher.matches()) {
				username = skinMatcher.group(1);
			} else if (altSkinMatcher.matches()) {
				username = altSkinMatcher.group(1);
			} else if (spoutcraftSkinMatcher.matches()) {
				username = spoutcraftSkinMatcher.group(1);
			} else if (technicSkinMatcher.matches()) {
				username = technicSkinMatcher.group(1);
			}
			if (this.proxy.skinCache.containsKey(username)) { // Is the skin in
																// the cache?
				UniversalLauncher.log.info("Skin from cache");

				data = this.proxy.skinCache.get(username); // Then get it from
															// there
			} else {
				url = "http://" + MineProxy.authServer + "/game/get_skin/" + username;

				UniversalLauncher.log.info("To: " + url);

				data = getRequest(url); // Then get it...
				UniversalLauncher.log.info("Response length: " + data.length);

				this.proxy.skinCache.put(username, data); // And put it in there
			}

		} // If Cloak Request
		else if (cloakMatcher.matches() || spoutcraftCloakMatcher.matches()) {
			UniversalLauncher.log.info("Cloak");
			String username = null;
			if (cloakMatcher.matches()) {
				username = cloakMatcher.group(1);
			} else if (spoutcraftCloakMatcher.matches()) { // I know it's
															// pointless
				username = spoutcraftCloakMatcher.group(1);
			}
			if (this.proxy.cloakCache.containsKey(username)) {
				UniversalLauncher.log.info("Cloak from cache");
				data = this.proxy.cloakCache.get(username);
			} else {
				// url = "http://" + MineProxy.authServer + "/game/get_cloak/" +
				// username;
				if (MineProxy.authServer.equals(UniversalLauncher.DEFAULT_AUTH_SERVER)) {
					url = "http://ms2cloaks.creatifcubed.com/get_cloak.php?username=" + username;
				} else {
					url = "http://" + MineProxy.authServer + "/game/get_cloak/" + username;
				}

				UniversalLauncher.log.info("To: " + url);

				data = getRequest(url);
				UniversalLauncher.log.info("Response length: " + data.length);

				this.proxy.cloakCache.put(username, data);
			}

		} // If Version Request
		else if (getversionMatcher.matches() || altLoginMatcher.matches()) {
			UniversalLauncher.log.info("GetVersion");
			String oldUrl = url;
			url = "http://" + MineProxy.authServer + "/game/get_version/";
			UniversalLauncher.log.info("To: " + url);

			try {
				UniversalLauncher.log.info("old url " + oldUrl);
				char[] postdata = new char[0];
				if (getversionMatcher.matches()) {
					String contentLength = headers.get("content-length");
					int postlen = Integer.parseInt(contentLength);
					postdata = new char[postlen];
					InputStreamReader reader = new InputStreamReader(this.fromClient);
					reader.read(postdata);
				} else {
					String queryPart = oldUrl.split("\\?")[1];
					postdata = queryPart.toCharArray();
				}
				UniversalLauncher.log.info("POSTDATA: " + new String(postdata));

				String postString = new String();
				for (char c : postdata) {
					postString += c;
				}

				UniversalLauncher.log.info(postString);

				data = postRequest(url, new String(postdata), "application/x-www-form-urlencoded");

				String response = new String(data);
				UniversalLauncher.log.info("data: " + response);

				if (HELP_TICKET_LOGIN_REGEX.matcher(response).matches()) {
					// TODO: Flip Help Toggle
				}
			} catch (IOException ex) {
				UniversalLauncher.log.info("Unable to read POST data from getversion request: " + ex.getLocalizedMessage());
			}
		} // If JoinServer Request
		else if (joinserverMatcher.matches()) {
			UniversalLauncher.log.info("JoinServer");

			params = joinserverMatcher.group(1);
			url = "http://" + MineProxy.authServer + "/game/join_server" + params;
			UniversalLauncher.log.info("To: " + url);
			data = getRequest(url);
			contentType = "text/plain";
			// TODO There may be a bug here, keeps causing a hang in the MC
			// thread that tries to read the data from it
		} // If Check Server Request
		else if (checkserverMatcher.matches()) {
			UniversalLauncher.log.info("CheckServer");

			params = checkserverMatcher.group(1);
			url = "http://" + MineProxy.authServer + "/game/check_server" + params;
			UniversalLauncher.log.info("To: " + url);
			data = getRequest(url);

		} else if (audiofix_url.matches()) { // this is to fix the audio
												// problems
			UniversalLauncher.log.info("Audio Fix");
			url = "http://s3.amazonaws.com/MinecraftResources/";
			UniversalLauncher.log.info("To: " + url);
			data = getRequest(url);
		} else if (dl_bukkit.matches()) {
			UniversalLauncher.log.info("Bukkit Fix");
			data = getRequest(url);
		} else if (client_snoop.matches()) // tmp for now since else does not
											// seem to handle these dont have
											// time to look into it
		{
			params = client_snoop.group(1);
			url = "http://snoop\\.minecraft\\.net/client" + params;

			UniversalLauncher.log.info("To: " + url);

			try {
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(this.fromClient);
				reader.read(postdata);

				data = postRequest(url, new String(postdata), "application/x-www-form-urlencoded");

			} catch (IOException ex) {
				UniversalLauncher.log.info("Unable to read POST data from getversion request: " + ex.getLocalizedMessage());
			}
		} else if (server_snoop.matches()) {
			params = server_snoop.group(1);
			url = "http://snoop\\.minecraft\\.net/server" + params;

			UniversalLauncher.log.info("To: " + url);

			try {
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(this.fromClient);
				reader.read(postdata);

				data = postRequest(url, new String(postdata), "application/x-www-form-urlencoded");

			} catch (IOException ex) {
				UniversalLauncher.log.info("Unable to read POST data from getversion request: " + ex.getLocalizedMessage());
			}
		} // If Any other network request
		else {
			UniversalLauncher.log.info("No handler. Piping.");

			try {
				if (!url.startsWith("http://") && !url.startsWith("https://")) {
					url = "http://" + url;
				}
				URL u = new URL(url);
				if (method.equals("CONNECT")) {
					int port = u.getPort();
					if (port == -1) {
						port = 80;
					}
					Socket sock = new Socket(u.getHost(), port);

					SimpleStreams.pipeStreamsConcurrently(sock.getInputStream(), this.toClient);
					SimpleStreams.pipeStreamsConcurrently(this.connection.getInputStream(), sock.getOutputStream());
					// TODO Maybe put POST here instead, less to do, but would
					// it work?

					// to avoid a resource leak
					sock.close();

				} else if (method.equals("GET") || method.equals("POST")) {
					HttpURLConnection c = (HttpURLConnection) u.openConnection(Proxy.NO_PROXY);
					c.setRequestMethod(method);
					boolean post = method.equals("POST");

					for (String k : headers.keySet()) {
						c.setRequestProperty(k, headers.get(k)); // TODO Might
																	// need to
																	// blacklist
																	// these as
																	// well
																	// later
					}

					if (post) {
						c.setDoInput(true);
						c.setDoOutput(true);
						c.setUseCaches(false);
						c.connect();
						int postlen = Integer.parseInt(headers.get("content-length"));
						byte[] postdata = new byte[postlen];
						this.fromClient.read(postdata);
						DataOutputStream os = new DataOutputStream(c.getOutputStream());
						os.write(postdata);
					}

					int responseCode = c.getResponseCode();
					String res = "HTTP/1.0 " + responseCode + " " + c.getResponseMessage() + "\r\n";
					res += "Connection: close\r\nProxy-Connection: close\r\n";

					java.util.Map<String, java.util.List<String>> h = c.getHeaderFields();
					headerloop: for (String k : h.keySet()) {
						if (k == null) {
							continue;
						}

						k = k.trim();

						for (String forbiddenHeader : BLACKLISTED_HEADERS) {
							if (k.equalsIgnoreCase(forbiddenHeader)) {
								continue headerloop;
							}
						}

						java.util.List<String> vals = h.get(k);
						for (String v : vals) {
							res += k + ": " + v + "\r\n";
						}
					}
					res += "\r\n";

					int size = 0;
					if (responseCode / 100 != 5) {
						this.toClient.writeBytes(res);
						size = SimpleStreams.pipeStreams(c.getInputStream(), this.toClient);
					}

					this.toClient.close();
					this.connection.close();

					UniversalLauncher.log.info("Piping finished, data size: " + size);

				} else if (method.equals("HEAD")) {
					HttpURLConnection c = (HttpURLConnection) u.openConnection(Proxy.NO_PROXY);
					c.setRequestMethod("HEAD");

					for (String k : headers.keySet()) {
						c.setRequestProperty(k, headers.get(k));
					}

					String res = "HTTP/1.0 " + c.getResponseCode() + " " + c.getResponseMessage() + "\r\n";
					res += "Proxy-Connection: close\r\n";

					java.util.Map<String, java.util.List<String>> h = c.getHeaderFields();
					for (String k : h.keySet()) {
						if (k == null) {
							continue;
						}
						java.util.List<String> vals = h.get(k);
						for (String v : vals) {
							res += k + ": " + v + "\r\n";
						}
					}
					res += "\r\n";

					this.toClient.writeBytes(res); // TODO Occasional exception
													// socket write error
					this.toClient.close();
					this.connection.close();
				} else {
					UniversalLauncher.log.info("UNEXPECTED REQUEST TYPE: " + method);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}

		try {
			if (data != null) {
				this.toClient
						.writeBytes("HTTP/1.0 200 OK\r\nConnection: close\r\nProxy-Connection: close\r\nContent-Length: "
								+ data.length + "\r\n");

				if (contentType != null) {
					this.toClient.writeBytes("Content-Type: " + contentType + "\r\n");
				}

				this.toClient.writeBytes("\r\n");
				this.toClient.write(data);
				this.toClient.flush();
			}

			this.toClient.close();
			this.connection.close();
			this.fromClient.close();
			this.toClient.close();
			this.connection.close();
		} catch (IOException ex) {
			UniversalLauncher.log.info("Error: " + ex.getLocalizedMessage());
		}
	}

	public static byte[] getRequest(String url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
			conn.setInstanceFollowRedirects(false);
			Map<String, List<String>> requestHeaders = conn.getRequestProperties();
			int code = conn.getResponseCode();

			if (code == 301 || code == 302 || code == 303) {
				UniversalLauncher.log.info("Java didn't redirect automatically, going manual: " + Integer.toString(code));
				String l = conn.getHeaderField("location").trim();
				UniversalLauncher.log.info("Manual redirection to: " + l);
				return getRequest(l);
			}

			UniversalLauncher.log.info("Response: " + code);

			if (code == 403) {
				String s = "403 from req to " + url + "\nRequest headers:\n";

				for (String k : requestHeaders.keySet()) {
					if (k == null) {
						continue;
					}
					java.util.List<String> vals = requestHeaders.get(k);
					for (String v : vals) {
						s += k + ": " + v + "\n";
					}
				}

				s += "Response headers:\n";

				Map<String, List<String>> responseHeaders = conn.getHeaderFields();
				for (String k : responseHeaders.keySet()) {
					if (k == null) {
						continue;
					}
					java.util.List<String> vals = responseHeaders.get(k);
					for (String v : vals) {
						s += k + ": " + v + "\n";
					}
				}

				UniversalLauncher.log.info(s);
				UniversalLauncher.log.info("Contents:\n" + new String(grabData(conn.getErrorStream())));
			}

			if (code / 100 == 4) {
				return new byte[0];
			}

			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

			return grabData(in);

		} catch (MalformedURLException ex) {
			UniversalLauncher.log.info("Bad URL in getRequest: " + ex.getLocalizedMessage());
		} catch (IOException ex) {
			UniversalLauncher.log.info("IO error during a getRequest: " + ex.getLocalizedMessage());
		}

		return new byte[0];
	}

	public static byte[] postRequest(String url, String postdata, String contentType) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out);

		try {
			writer.write(postdata);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] rd = postRequest(url, out.toByteArray(), contentType);

		return rd;
	}

	public static byte[] postRequest(String url, byte[] postdata, String contentType) {
		try {
			URL u = new URL(url);

			HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
			c.setDoOutput(true);
			c.setRequestMethod("POST");

			c.setRequestProperty("Host", u.getHost());
			c.setRequestProperty("Content-Length", Integer.toString(postdata.length));
			c.setRequestProperty("Content-Type", contentType);

			BufferedOutputStream out = new BufferedOutputStream(c.getOutputStream());
			out.write(postdata);
			out.flush();
			out.close();

			byte[] data = grabData(new BufferedInputStream(c.getInputStream()));
			return data;

		} catch (java.net.UnknownHostException ex) {
			UniversalLauncher.log.info("Unable to resolve remote host, returning null: " + ex.getLocalizedMessage());
		} catch (MalformedURLException ex) {
			UniversalLauncher.log.info("Bad URL when doing postRequest: " + ex.getLocalizedMessage());
		} catch (IOException ex) {
			UniversalLauncher.log.info("Error: " + ex.getLocalizedMessage());
		}

		return null;
	}

	public static byte[] grabData(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		while (true) {
			int len;
			try {
				len = in.read(buffer);
				if (len == -1) {
					break;
				}
			} catch (IOException e) {
				break;
			}
			out.write(buffer, 0, len);
		}

		return out.toByteArray();
	}

	public static String readUntil(DataInputStream is, String endSequence) {
		return readUntil(is, endSequence.getBytes());
	}

	public static String readUntil(DataInputStream is, char endSequence) {
		return readUntil(is, new byte[] { (byte) endSequence });
	}

	public static String readUntil(DataInputStream is, byte endSequence) {
		return readUntil(is, new byte[] { endSequence });
	}

	public static String readUntil(DataInputStream is, byte[] endSequence) { // If
																				// there
																				// is
																				// an
																				// edge
																				// case,
																				// make
																				// sure
																				// we
																				// can
																				// see
																				// it
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String r = null;

		try {
			int i = 0;

			while (true) {
				boolean end = false;
				byte b = is.readByte(); // Read a byte
				if (b == endSequence[i]) { // If equal to current byte of
											// endSequence
					if (i == endSequence.length - 1) {
						end = true; // If we hit the end of endSequence, we're
									// done
					}

					i++; // Increment for next round
				} else {
					i = 0; // Reset
				}

				out.write(b);
				if (end) {
					break;
				}
			}
		} catch (IOException ex) {
			UniversalLauncher.log.info("readUntil unable to read from InputStream, endSeq: " + new String(endSequence));
			UniversalLauncher.log.info("Error: " + ex.getLocalizedMessage());
		}

		try {
			r = out.toString("UTF-8");
		} catch (java.io.UnsupportedEncodingException ex) {
			UniversalLauncher.log.info("readUntil unable to encode data: " + out.toString());
			UniversalLauncher.log.info("Error: " + ex.getLocalizedMessage());
		}

		return r;
	}
}