package com.mineshaftersquared;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.creatifcubed.simpleapi.SimpleAggregateOutputStream;
import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleVersion;
import com.creatifcubed.simpleapi.swing.SimpleGUIConsole;
import com.mineshaftersquared.gui.MS2LauncherWindow;
import com.mineshaftersquared.misc.EventBus;
import com.mineshaftersquared.misc.MCLauncher;
import com.mineshaftersquared.misc.MS2Utils;
import com.mineshaftersquared.models.MCProfileManager;
import com.mineshaftersquared.models.MCVersionManager;
import com.mineshaftersquared.models.MCOneSixAuth;

public class UniversalLauncher implements Runnable {
	
	public final FileConfiguration prefs;
	public final MCProfileManager profilesManager;
	public final MCVersionManager versionsManager;
	public final EventBus eventBus;
	public final MCLauncher launcher;
	public final MCOneSixAuth auth;

	public static final SimpleVersion MS2_VERSION = new SimpleVersion("4.3.0");
	public static final String POLLING_SERVER = "http://ms2.creatifcubed.com/polling_scripts/";
	public static final String DEFAULT_AUTH_SERVER = "http://mineshaftersquared.com";
	public static final String MS2_RESOURCES_DIR = "ms2-resources";
	public static final String MS2_SETTINGS_NAME = "settings.xml";
	
	public static final Log log = LogFactory.getFactory().getInstance("[MS2]");
	public static final SimpleGUIConsole console = new SimpleGUIConsole();
	
	private MS2LauncherWindow mainWindow;
	private MCOneSixAuth.Response authResponse;

	static {
		console.init();
		System.setOut(new PrintStream(new SimpleAggregateOutputStream(System.out, console.getOut())));
		System.setErr(new PrintStream(new SimpleAggregateOutputStream(System.err, console.getErr())));
	}

	public UniversalLauncher() throws ConfigurationException, IOException {
		this.profilesManager = new MCProfileManager(MS2Utils.getLocalDir());
		this.versionsManager = new MCVersionManager();
		this.eventBus = new EventBus();
		this.launcher = new MCLauncher(this);
		this.auth = new MCOneSixAuth(this);
		this.authResponse = null;
		
		// Initialize resources
		File resources = new File(MS2_RESOURCES_DIR);
		resources.mkdirs();
		File defaultResources = new File(MS2Utils.getDefaultMCDir(), MS2_RESOURCES_DIR);
		defaultResources.mkdirs();
		this.prefs = new XMLConfiguration(new File(resources, MS2_SETTINGS_NAME));
		this.prefs.setAutoSave(true);
		
		this.prefs.setProperty("launcher.lastversion", MS2_VERSION.toString());
	}
	
	/**
	 * @param args
	 * @throws ConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ConfigurationException, IOException {

		new UniversalLauncher().run();
	}
	
	public MS2LauncherWindow mainWindow() {
		return this.mainWindow;
	}
	
	public MCOneSixAuth.Response authResponse() {
		return this.authResponse;
	}
	
	@Override
	public void run() {
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				UniversalLauncher.this.mainWindow = new MS2LauncherWindow(UniversalLauncher.this);
				UniversalLauncher.this.mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				UniversalLauncher.this.mainWindow.setPreferredSize(new Dimension(MS2LauncherWindow.PREFERRED_WIDTH, MS2LauncherWindow.PREFERRED_HEIGHT));
				UniversalLauncher.this.mainWindow.pack();
				UniversalLauncher.this.mainWindow.setLocationRelativeTo(null);
				UniversalLauncher.this.mainWindow.setVisible(true);
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						final String msg = UniversalLauncher.this.versionUpdates();
						if (msg != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									JOptionPane.showMessageDialog(UniversalLauncher.this.mainWindow(), msg);
								}
							});
						}
					}
				}).start();
			}
			
		});
	}

	private String versionUpdates() {
		try {
			String result = new String(new SimpleHTTPRequest(POLLING_SERVER + "latestversion.php")
					.addGet("currentversion", MS2_VERSION.toString()).doGet(Proxy.NO_PROXY)).trim();
			SimpleVersion latest = new SimpleVersion(result);
			UniversalLauncher.log.info("Latest version: " + latest.toString());
			this.eventBus.emit("latestversion", new EventBus.EventData(latest));
			if (MS2_VERSION.shouldUpdateTo(latest)) {
				return "There is an update at ms2.creatifcubed.com. Latest version: " + latest.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error checking updates. Please check manually at ms2.creatifcubed.com";
		}
		return null;
	}
}
