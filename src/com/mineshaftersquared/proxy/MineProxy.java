package com.mineshaftersquared.proxy;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.regex.Pattern;

import com.mineshaftersquared.UniversalLauncher;


public class MineProxy extends Thread {

	public static String authServer;
	private int port;
	public volatile boolean shouldEnd;
	public boolean isEnded;
	// Patterns
	public static final Pattern SKIN_URL = Pattern.compile("http://skins\\.minecraft\\.net/MinecraftSkins/(.+?)\\.png");
	public static final Pattern CLOAK_URL = Pattern
			.compile("http://skins\\.minecraft\\.net/MinecraftCloaks/(.+?)\\.png");
	public static final Pattern GETVERSION_URL = Pattern
			.compile("http://session\\.minecraft\\.net/game/getversion\\.jsp");
	public static final Pattern ALTLOGIN_URL = Pattern.compile("(http://)?login\\.minecraft\\.net(.*)");
	public static final Pattern JOINSERVER_URL = Pattern
			.compile("http://session\\.minecraft\\.net/game/joinserver\\.jsp(.*)");
	public static final Pattern CHECKSERVER_URL = Pattern
			.compile("http://session\\.minecraft\\.net/game/checkserver\\.jsp(.*)");
	public static final Pattern AUDIOFIX_URL = Pattern.compile("http://s3\\.amazonaws\\.com/MinecraftResources/");
	public static Pattern CLIENT_SNOOP = Pattern.compile("http://snoop\\.minecraft\\.net/client(.*)");
	public static final Pattern SERVER_SNOOP = Pattern.compile("http://snoop\\.minecraft\\.net/server(.*)");
	public static final Pattern DL_BUKKIT = Pattern.compile("http://dl.bukkit.org/(.+?)");
	public static final Pattern ALTSKIN_URL = Pattern.compile("http://s3.amazonaws.com/MinecraftSkins/(.+?)\\.png");
	public static final Pattern SPOUTCRAFT_SKIN_URL = Pattern
			.compile("http://cdn.spout.org/game/vanilla/skin/(.+?)\\.png");
	public static final Pattern TECHNIC_SKIN_URL = Pattern
			.compile("http://cdn.spout.org/game/vanilla/skin/(.+?)\\.png");
	public static final Pattern SPOUTCRAFT_CLOAK_URL = Pattern
			.compile("http://cdn.spout.org/game/vanilla/cape/(.+?)\\.png");

	/* NTS: See if this is still needed */
	public Hashtable<String, byte[]> skinCache;
	public Hashtable<String, byte[]> cloakCache;

	public MineProxy(String currentAuthServer) {
		this.setName("MineProxy Thread");
		this.setDaemon(true);

		MineProxy.authServer = currentAuthServer; // TODO maybe change this
													// leave it for now

		this.skinCache = new Hashtable<String, byte[]>();
		this.cloakCache = new Hashtable<String, byte[]>();
		this.shouldEnd = false;
		this.port = -1;
		this.isEnded = true;
	}

	// @SuppressWarnings("resource")
	@Override
	public void run() {
		ServerSocket server = null;
		try {
			int port = 9010; // A lot of other applications use the 80xx range,
			// let's try for some less crowded real-estate
			while (port < 12000) { // That should be enough
				try {
					UniversalLauncher.log.info("Trying to proxy on port " + port);
					byte[] loopback = { 127, 0, 0, 1 };
					server = new ServerSocket(port, 16, InetAddress.getByAddress(loopback));
					this.port = port;
					UniversalLauncher.log.info("Proxying successful");
					break;
				} catch (BindException ex) {
					port++;
				}

			}
			this.isEnded = false;
			server.setSoTimeout(1000 * 5);
			while (true) {
				try {
					if (this.shouldEnd) {
						System.out.println("Proxy on port " + this.port + " ending");
						break;
					}
					Socket connection = server.accept();

					MineProxyHandler handler = new MineProxyHandler(this, connection);
					handler.start();
				} catch (Exception ignore) {
					// System.out.println("{MineProxy timed out (normal)}");
				}
			}
		} catch (IOException ex) {
			UniversalLauncher.log.info("Error in server accept loop: " + ex.getLocalizedMessage());
		} finally {
			try {
				// System.out.println("Closing proxy server");
				server.close();
			} catch (Exception ignore) {
				//
			} finally {
				// System.out.println("Setting isEnded to true");
				this.isEnded = true;
				System.out.println("This.isended: " + this.isEnded);
			}
		}
	}

	public int getPort() {
		while (this.port < 0) {
			try {
				sleep(50);
			} catch (InterruptedException ex) {
				UniversalLauncher.log.info("Interrupted while waiting for port: " + ex.getLocalizedMessage());
			}
		}

		return this.port;
	}
}