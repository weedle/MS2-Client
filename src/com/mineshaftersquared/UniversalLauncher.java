/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.creatifcubed.simpleapi.SimpleAggregateOutputStream;
import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.SimpleVersion;
import com.creatifcubed.simpleapi.SimpleXMLSettings;
import com.creatifcubed.simpleapi.swing.SimpleGUIConsole;
import com.mineshaftersquared.gui.LauncherWindow;
import com.mineshaftersquared.proxy.MineProxy;

/*
 * Settings:
 * 
 * - proxy.authserver
 * - launcher.remembercredentials
 * - launcher.username
 * - launcher.password
 * - launcher.pathfind
 * - ftb.launchername
 * - technic.launchername
 * - serveradmins.vanilla.name
 * - serveradmins.bukkit.name
 */

/**
 * 
 * @author Adrian
 */
public class UniversalLauncher {

	public static final SimpleVersion MS2_VERSION = new SimpleVersion("4.2.12");
	public static final String POLLING_SERVER = "ms2.creatifcubed.com/polling_scripts/";
	public static final String DEFAULT_AUTH_SERVER = "mineshaftersquared.com";
	public static final String BETA_AUTH_SERVER = "ms2auth.creatifcubed.com";
	public static final String MS2_RESOURCES_DIR = "ms2-resources";
	public static final String MS2_SETTINGS_NAME = "settings.xml";
	
	public static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static final SimpleGUIConsole console = new SimpleGUIConsole();
	
	static {
		console.init();
	}

	private SimpleISettings prefs;
	private String[] args;
	private LauncherWindow mainWindow;
	public MineProxy proxy;

	public UniversalLauncher(String[] args) {
		this.args = args;
	}

	public void run() {
		System.setOut(new PrintStream(new SimpleAggregateOutputStream(System.out, console.getOut())));
		System.setErr(new PrintStream(new SimpleAggregateOutputStream(System.err, console.getErr())));
		
		this.initializeResources();
		this.proxy = null;
		LauncherWindow mainWindow = new LauncherWindow(getWindowTitle(), this.prefs);
		if (this.args.length > 0) {
			if (this.args[0].toLowerCase().equals("server")) {
				mainWindow.setActiveTab(5);
			}
		}
		mainWindow.setVisible(true);
		this.mainWindow = mainWindow;
		this.mainWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (UniversalLauncher.this.proxy != null && !UniversalLauncher.this.proxy.isEnded) {
					if (JOptionPane.showConfirmDialog(null,
							"The proxy is still running. Are you sure you want to close?") == 0) {

					} else {
						return;
					}
				}
				e.getWindow().dispose();
			}
		});

		UniversalLauncher.checkVersionUpdates();

		this.checkLastVersion();
		this.logCurrentVersion();
		this.pingAuthServer(UniversalLauncher.this.prefs.getString("proxy.authserver", DEFAULT_AUTH_SERVER));

	}

	public static String getWindowTitle() {
		return "Mineshafter Squared - Universal Launcher " + MS2_VERSION.toString();
	}

	public LauncherWindow getMainWindow() {
		return this.mainWindow;
	}

	public static void main(String[] args) {
		new UniversalLauncher(args).run();
	}

	public SimpleISettings getPrefs() {
		return this.prefs;
	}

	private void checkLastVersion() {
		SimpleVersion lastVersion = null;
		try {
			String stored = this.prefs.getString("launcher.lastversion");
			lastVersion = new SimpleVersion(stored);
		} catch (Exception ignore) {
			System.out
					.println("Couldn't get last version. If this isn't the first launch, check updates at ms2.creatifcubed.com");
		}

		if (lastVersion == null) {
			new File("ms2-resources").delete();
			this.initializeResources();
		}
	}

	private void logCurrentVersion() {
		this.prefs.put("launcher.lastversion", MS2_VERSION.toString());
		this.prefs.save();
	}

	public void pingAuthServer(final String server) {
		// System.out.println("Pinging " +
		// UniversalLauncher.this.prefs.getString("proxy.authserver",
		// DEFAULT_AUTH_SERVER));
		// System.out.println(server);
		this.mainWindow.updateServerStatusMessage("Pinging " + server + " ...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (SimpleUtils.httpPing(server)) {
					UniversalLauncher.this.mainWindow.updateServerStatusMessage("Server " + server + " OK");
				} else {
					UniversalLauncher.this.mainWindow.updateServerStatusMessage("Server " + server + " Offline");
				}
			}
		}).start();

	}

	private void initializeResources() {
		File resources = new File(UniversalLauncher.MS2_RESOURCES_DIR);
		if (!resources.exists()) {
			resources.mkdir();
		}
		this.prefs = new SimpleXMLSettings(UniversalLauncher.MS2_RESOURCES_DIR + "/"
				+ UniversalLauncher.MS2_SETTINGS_NAME);
		this.prefs.tmpPut("instance", this);
	}

	public static void checkVersionUpdates() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String result = new String(new SimpleHTTPRequest("http://" + POLLING_SERVER + "latestversion.php")
							.addGet("currentversion", MS2_VERSION.toString()).doGet(SimpleHTTPRequest.NO_PROXY)).trim();
					SimpleVersion latest = new SimpleVersion(result);
					System.out.println("Latest version: " + latest.toString());
					if (MS2_VERSION.shouldUpdateTo(latest)) {
						JOptionPane.showMessageDialog(null,
								"There is an update at ms2.creatifcubed.com. Latest version: " + latest.toString());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"Error checking updates. Please try manually at ms2.creatifcubed.com");
				}
			}
		}).start();
	}
}
