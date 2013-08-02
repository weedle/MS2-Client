package com.mineshaftersquared;

import java.io.File;
import java.net.Proxy;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleVersion;
import com.mineshaftersquared.misc.ExtendedGnuParser;
import com.mineshaftersquared.misc.MS2Utils;
import com.mineshaftersquared.misc.UpdateMessage;

public class MS2Entry {
	
	public static final Option serverOption = new Option("server", true, "Server jar to start");
	public static final Option bukkitOption = new Option("bukkit", false, "server: Use this flag for Bukkit and derivatives if you want a GUI console");
	public static final Option authserverOption = new Option("authserver", true, "server: Domain to poll for authentication, skins, etc. Defaults to " + UniversalLauncher.DEFAULT_AUTH_SERVER);
	public static final Option mcSeparatorOption = new Option("mc", false, "Everything after this is an argument for Minecraft");
	
	public static final Options cmdOptions = new Options() {{
		addOption("help", false, "About the Mineshafter Squared Universal Launcher");
		
		addOption(serverOption);
		addOption(bukkitOption);
		addOption(authserverOption);
		addOption("game", false, "Start the Minecraft launcher directly");
		addOption("gui", false, "Start the MS2 launcher (ignores " + UniversalLauncher.MC_START_AUTOMATICALLY + ")");
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
		}  else if (cmd.hasOption("game")) {
			MCEntry.main(args);
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
