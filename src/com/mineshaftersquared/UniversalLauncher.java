package com.mineshaftersquared;

import java.awt.Dimension;
import java.io.File;
import java.io.PrintStream;
import java.net.Proxy;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import com.creatifcubed.simpleapi.SimpleAggregateOutputStream;
import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleVersion;
import com.creatifcubed.simpleapi.swing.SimpleGUIConsole;
import com.mineshaftersquared.gui.MS2LauncherWindow;

public class UniversalLauncher implements Runnable {
	
	public final FileConfiguration prefs;

	public static final SimpleVersion MS2_VERSION = new SimpleVersion("4.3.0");
	public static final String POLLING_SERVER = "http://ms2.creatifcubed.com/polling_scripts/";
	public static final String DEFAULT_AUTH_SERVER = "http://mineshaftersquared.com";
	public static final String MS2_RESOURCES_DIR = "ms2-resources";
	public static final String MS2_SETTINGS_NAME = "settings.xml";
	
	public static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static final SimpleGUIConsole console = new SimpleGUIConsole();

	static {
		console.init();
		System.setOut(new PrintStream(new SimpleAggregateOutputStream(System.out, console.getOut())));
		System.setErr(new PrintStream(new SimpleAggregateOutputStream(System.err, console.getErr())));
	}

	public UniversalLauncher() throws ConfigurationException {
		// Initialize resources
		File resources = new File(MS2_RESOURCES_DIR);
		resources.mkdirs();
		this.prefs = new XMLConfiguration(new File(resources, MS2_SETTINGS_NAME));
		this.prefs.setAutoSave(true);
		
		this.prefs.setProperty("launcher.lastversion", MS2_VERSION.toString());
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String msg = UniversalLauncher.this.versionUpdates();
				if (msg != null) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, msg);
						}
					});
				}
			}
		}).start();
	}
	
	/**
	 * @param args
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws ConfigurationException {

		new UniversalLauncher().run();
	}

	@Override
	public void run() {
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame mainWindow = new MS2LauncherWindow(UniversalLauncher.this);
				mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				mainWindow.setPreferredSize(new Dimension(MS2LauncherWindow.PREFERRED_WIDTH, MS2LauncherWindow.PREFERRED_HEIGHT));
				mainWindow.pack();
				mainWindow.setLocationRelativeTo(null);
				mainWindow.setVisible(true);
			}
			
		});
	}

	private String versionUpdates() {
		try {
			String result = new String(new SimpleHTTPRequest(POLLING_SERVER + "latestversion.php")
					.addGet("currentversion", MS2_VERSION.toString()).doGet(Proxy.NO_PROXY)).trim();
			SimpleVersion latest = new SimpleVersion(result);
			UniversalLauncher.log.info("Latest version: " + latest.toString());
			if (MS2_VERSION.shouldUpdateTo(latest)) {
				return "There is an update at ms2.creatifcubed.com. Latest version: " + latest.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Error checking updates. Please try manually at ms2.creatifcubed.com";
		}
		return null;
	}
}
