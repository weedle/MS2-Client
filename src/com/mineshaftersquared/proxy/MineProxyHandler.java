package com.mineshaftersquared.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleStreams;
import com.mineshaftersquared.UniversalLauncher;

public class MineProxyHandler extends Thread {

	private final DataInputStream fromClient;
	private final DataOutputStream toClient;
	private final Socket connection;
	private final MineProxy proxy;
	private static final String[] BLACKLISTED_HEADERS = new String[] { "Connection", "Proxy-Connection",
	"Transfer-Encoding" };
	public static final Pattern HELP_TICKET_LOGIN_REGEX = Pattern
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
		BufferedReader rd = new BufferedReader(new InputStreamReader(this.fromClient));
		// Read the incoming request
		String[] requestLine = null;
		System.out.println("Starting proxy run");
		while (true) {
			try {
				String str = rd.readLine();
				if (str == null) {
					System.out.println("String null, done");
					break;
				}
				System.out.println("Read string: " + str);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		if (true) return;
		try {
			requestLine = rd.readLine().split(" ");
		} catch (IOException ex) {
			ex.printStackTrace();
			writeToClientAndFinish(new byte[0]);
			return;
		}
		String method = requestLine[0].trim().toUpperCase();
		System.out.println("Method is: " + method);
		String url = requestLine[1].trim();
		UniversalLauncher.log.info("run beginning: request to: " + url);

		UniversalLauncher.log.info("Request: " + method + " " + url);

		// Read the incoming headers
		while (true) {
			String header = null;
			try {
				header = rd.readLine().trim();
			} catch (IOException ex) {
				ex.printStackTrace();
				writeToClientAndFinish(new byte[0]);
				return;
			}
			if (header.isEmpty()) {
				break;
			}
			int splitPoint = header.indexOf(':');
			if (splitPoint != -1) {
				headers.put(header.substring(0, splitPoint).toLowerCase().trim(), header.substring(splitPoint + 1)
						.trim());
			}

		}

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
			if (this.proxy.skinCache.containsKey(username)) {
				UniversalLauncher.log.info("Skin from cache");
				data = this.proxy.skinCache.get(username);
			} else {
				url = "http://" + this.proxy.authServer + "/game/get_skin/" + username;

				UniversalLauncher.log.info("To: " + url);

				data = getRequest(url);
				UniversalLauncher.log.info("Response length: " + data.length);

				this.proxy.skinCache.put(username, data);
			}
		} else if (cloakMatcher.matches() || spoutcraftCloakMatcher.matches()) {
			UniversalLauncher.log.info("Cloak");
			String username = null;
			if (cloakMatcher.matches()) {
				username = cloakMatcher.group(1);
			} else if (spoutcraftCloakMatcher.matches()) {
				// pointless for now
				username = spoutcraftCloakMatcher.group(1);
			}
			if (this.proxy.cloakCache.containsKey(username)) {
				UniversalLauncher.log.info("Cloak from cache");
				data = this.proxy.cloakCache.get(username);
			} else {
				if (this.proxy.authServer.equals(UniversalLauncher.DEFAULT_AUTH_SERVER)) {
					url = "http://ms2cloaks.creatifcubed.com/get_cloak.php?username=" + username;
				} else {
					url = "http://" + this.proxy.authServer + "/game/get_cloak/" + username;
				}
				UniversalLauncher.log.info("To: " + url);
				data = getRequest(url);
				UniversalLauncher.log.info("Response length: " + data.length);
				this.proxy.cloakCache.put(username, data);
			}
		} else if (getversionMatcher.matches() || altLoginMatcher.matches()) {
			UniversalLauncher.log.info("GetVersion");
			String oldUrl = url;
			url = "http://" + this.proxy.authServer + "/game/get_version/";
			UniversalLauncher.log.info("To: " + url);

			UniversalLauncher.log.info("old url " + oldUrl);
			char[] postdata = new char[0];
			if (getversionMatcher.matches()) {
				String contentLength = headers.get("content-length");
				int postlen = Integer.parseInt(contentLength);
				postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(this.fromClient);
				try {
					reader.read(postdata);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
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

			data = postRequest(url, new String(postdata));

			String response = new String(data);
			UniversalLauncher.log.info("data: " + response);

			if (HELP_TICKET_LOGIN_REGEX.matcher(response).matches()) {
				// TODO: Flip Help Toggle
			}
			
		} else if (joinserverMatcher.matches()) {
			UniversalLauncher.log.info("JoinServer");

			String params = joinserverMatcher.group(1);
			url = "http://" + this.proxy.authServer + "/game/join_server" + params;
			UniversalLauncher.log.info("To: " + url);
			data = getRequest(url);
			// TODO There may be a bug here, keeps causing a hang in the MC
			// thread that tries to read the data from it
			
		} else if (checkserverMatcher.matches()) {
			UniversalLauncher.log.info("CheckServer");

			String params = checkserverMatcher.group(1);
			url = "http://" + this.proxy.authServer + "/game/check_server" + params;
			UniversalLauncher.log.info("To: " + url);
			data = getRequest(url);
		} else if (audiofix_url.matches()) {
			UniversalLauncher.log.info("Audio Fix");
			url = "http://s3.amazonaws.com/MinecraftResources/";
			UniversalLauncher.log.info("To: " + url);
			data = getRequest(url);
		} else if (dl_bukkit.matches()) {
			UniversalLauncher.log.info("Bukkit Fix");
			data = getRequest(url);
		} else if (client_snoop.matches()) {
			// tmp for now, else doesn't seem to handle these
			String params = client_snoop.group(1);
			url = "http://snoop\\.minecraft\\.net/client" + params;

			UniversalLauncher.log.info("To: " + url);

			try {
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(this.fromClient);
				reader.read(postdata);

				data = postRequest(url, new String(postdata));

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else if (server_snoop.matches()) {
			String params = server_snoop.group(1);
			url = "http://snoop\\.minecraft\\.net/server" + params;

			UniversalLauncher.log.info("To: " + url);

			try {
				int postlen = Integer.parseInt(headers.get("content-length"));
				char[] postdata = new char[postlen];
				InputStreamReader reader = new InputStreamReader(this.fromClient);
				reader.read(postdata);

				data = postRequest(url, new String(postdata));

			} catch (IOException ex) {
				UniversalLauncher.log.info("Unable to read POST data from getversion request: "
						+ ex.getLocalizedMessage());
			}
		} else {
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
					final Socket sock = new Socket(u.getHost(), port);
					final Semaphore socketLock = new Semaphore(2);
					
					SimpleStreams.PipeStreamDoneListener onDone = new SimpleStreams.PipeStreamDoneListener() {
						@Override
						public void onDone(int bytes) {
							synchronized (socketLock) {
								System.out.println("Aquiring: " + socketLock.availablePermits());
								System.out.println("Is closed: " + sock.isClosed());
								socketLock.acquireUninterruptibly();
								if (socketLock.availablePermits() == 0) {
									//try {
										//sock.close();
										//toClient.close();
										//connection.close();
										System.out.println("Closed");
									//} catch (IOException e) {
										//e.printStackTrace();
									//}
								}
							}
						}
					};
//					SimpleStreams.pipeStreams(sock.getInputStream(), this.toClient);
//					SimpleStreams.pipeStreams(this.connection.getInputStream(), sock.getOutputStream());
//					sock.close();
					//String msg = "CONNECT google.com:443 HTTP/1.0\r\n"
					//		+ "User-Agent: sun.net.www.protocol.http.HttpURLConnection.userAgent\r\n\r\n";
					//this.toClient.write(msg.getBytes("ascii7"));
					//SimpleStreams.pipestre(this.fromClient, System.out);
					//SimpleStreams.pipeStreamsConcurrently(sock.getInputStream(), this.toClient, null);
					//SimpleStreams.pipeStreamsConcurrently(this.fromClient, sock.getOutputStream(), null);
					//sock.close();
					return;
				} else if (method.equals("GET") || method.equals("POST")) {
					HttpURLConnection c = (HttpURLConnection) u.openConnection(Proxy.NO_PROXY);
					c.setRequestMethod(method);
					boolean post = method.equals("POST");

					for (String k : headers.keySet()) {
						c.setRequestProperty(k, headers.get(k));
						// TODO blacklist these
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
					String res = formatBeginningOfHeader(responseCode, c.getResponseMessage());

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

					String res = formatBeginningOfHeader(c.getResponseCode(), c.getResponseMessage());

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
				System.out.println("hmm");
				e.printStackTrace();
			}
			
			writeToClientAndFinish(data);
		}
	}

	private void writeToClientAndFinish(byte[] data) {
		try {
			if (data != null) {
				this.toClient
				.writeBytes(formatBeginningOfHeader(200, "OK") + "\r\nContent-Length: "
						+ data.length + "\r\n");

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
			ex.printStackTrace();
		}
	}

	public static byte[] postRequest(String urlStr, String data) {
		try {
			return createRequest(urlStr).addPost(data).doPost(Proxy.NO_PROXY);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		return new byte[0];
	}

	public static byte[] getRequest(String urlStr) {
		try {
			return createRequest(urlStr).doGet(Proxy.NO_PROXY);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		}
		return new byte[0];
	}

	public static SimpleHTTPRequest createRequest(String urlStr) throws MalformedURLException {
		URL url = new URL(urlStr);
		return new SimpleHTTPRequest(url.getProtocol() + "://" + url.getAuthority() + url.getPath()).addGet(url
				.getQuery());
	}

	public static byte[] grabData(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			SimpleStreams.pipeStreams(in, out);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return out.toByteArray();
	}
	
	private static String formatBeginningOfHeader(int responseCode, String responseMessage) {
		return String.format("HTTP/1.0 %d %s\r\n"
				+ "Connection: close\r\n"
				+ "Proxy-Connection: close\r\n", responseCode, responseMessage);
	}
}