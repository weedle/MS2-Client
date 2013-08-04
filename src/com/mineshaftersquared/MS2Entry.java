package com.mineshaftersquared;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleResources;
import com.creatifcubed.simpleapi.SimpleVersion;
import com.mineshaftersquared.misc.ExtendedGnuParser;
import com.mineshaftersquared.misc.JarUtils;
import com.mineshaftersquared.misc.MS2Utils;
import com.mineshaftersquared.misc.UpdateMessage;

public class MS2Entry {
	
	public static final Option serverOption = new Option("server", true, "Server jar to start");
	public static final Option bukkitOption = new Option("guiconsole", false, "server: Use this flag for a GUI console (beta)");
	public static final Option authserverOption = new Option("authserver", true, "server: Domain to poll for authentication, skins, etc. Defaults to " + UniversalLauncher.DEFAULT_AUTH_SERVER);
	public static final Option mcSeparatorOption = new Option("mc", false, "Everything after this is an argument for Minecraft");
	public static final Option authOfflineOption = new Option("offline", false, "Don't use auth server; authenticate everything");
	
	public static final Options cmdOptions = new Options() {{
		addOption("help", false, "About the Mineshafter Squared Universal Launcher");
		
		addOption(serverOption);
		addOption(bukkitOption);
		addOption(authserverOption);
		addOption("game", false, "Start the Minecraft launcher directly");
		addOption("gui", false, "Start the MS2 launcher (ignores " + UniversalLauncher.MC_START_AUTOMATICALLY + ")");
		addOption("proxy", false, "Start the proxy");
		addOption(authOfflineOption);
		addOption("patchbungee", true, "Patch BungeeCord jar");
		addOption(mcSeparatorOption);
	}};
	

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws  
	 */
	public static void main(String[] args) throws Exception {
		CommandLineParser cmdParser = new ExtendedGnuParser();
		CommandLine cmd = cmdParser.parse(cmdOptions, args);
		
		if (cmd.hasOption("server")) {
			ServerEntry.main(args);
		} else if (cmd.hasOption("game")) {
			MCEntry.main(args);
		} else if (cmd.hasOption("proxy")) {
			MS2ProxyEntry.main(args);
		} else if (cmd.hasOption("patchbungee")) {
			String jarFile = cmd.getOptionValue("patchbungee");
			File file = new File(jarFile);
			File patched = new File("ms2-" + jarFile);
			if (!file.exists()) {
				System.out.println("Bungee jar does not exist. Is it in the same folder as this jar?");
				return;
			}
			UniversalLauncher.log.info("Patching bungee...");
			Map<String, InputStream> replacements = new HashMap<String, InputStream>();
			replacements.put("net/md_5/bungee/connection/InitialHandler.*\\.class", null);
			Map<String, InputStream> additions = new HashMap<String, InputStream>();
			InputStream in = SimpleResources.loadAsStream("net/mineshaftersquared/InitialHandler.class");
			System.out.println("Is null: " + (in == null));
			in = SimpleResources.loadAsStream("net/mineshaftersquared/InitialHandler$1.class");
			System.out.println("Is nulll: " + (in == null));
			additions.put("net/md_5/bungee/connection/InitialHandler.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$1.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$1.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$2.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$2.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$3.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$3.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$3$1.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$3$1.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$4.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$4.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$4$1.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$4$1.class"));
			additions.put("net/md_5/bungee/connection/InitialHandler$State.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/InitialHandler$State.class"));
			
			if (JarUtils.patchJar(file, patched, replacements, additions)) {
				UniversalLauncher.log.info("Done patching.");
			} else {
				UniversalLauncher.log.info("Error patching");
			}
		} else if (cmd.hasOption("help")) {
			showHelp();
		} else if (args.length > 0) {
			showHelp();
		} else {
			if (new File(MS2Utils.getMS2Dir(), UniversalLauncher.MC_START_AUTOMATICALLY).exists()) {
				final UniversalLauncher app = new UniversalLauncher();
				final String msg = app.versionUpdates();
				final UpdateMessage[] updatesMessages = app.updatesMessages();
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						if (app.showUpdatesMessages(null, msg, updatesMessages)) {
							System.exit(0);
						}
					}
				});
				boolean offline = app.prefs.getBoolean("launcher.offline", false);
				ArrayList<String> allArgs = new ArrayList<String>(args.length + (offline ? 1 : 0));
				if (offline) {
					allArgs.add("-offline");
				}
				MCEntry.main(args);
			} else {
				UniversalLauncher.main(args);
			}
		}
	}
	
	public static void showHelp() {
		System.out.println("Mineshafter Squared, Universal Launcher v" + UniversalLauncher.MS2_VERSION.toString());
		System.out.println("See ms2.creatifcubed.com for more details");
		new HelpFormatter().printHelp("java <Java options> -jar mineshaftersquared.jar <MS2 options> <-mc <Minecraft options>>", cmdOptions);
	}

}
