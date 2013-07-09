package com.mineshaftersquared;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class MS2Entry {
	
	static Options cmdOptions = new Options() {{
		addOption("help", false, "About the Mineshafter Squared Universal Launcher");
		
		addOption("server", true, "Server jar to start");
		addOption("bukkit", false, "server: Use this flag for Bukkit and derivatives");
		addOption("authserver", true, "server: Domain to poll for authentication, skins, etc.");
		addOption("authport", true, "server: Proxy port");
	}};

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws  
	 */
	public static void main(String[] args) throws Exception {
		CommandLineParser cmdParser = new GnuParser();
		CommandLine cmd = cmdParser.parse(cmdOptions, args);
		
		if (cmd.hasOption("server")) {
			
		} else if (cmd.hasOption("help")) {
			showHelp();
		} else if (args.length > 0) {
			if (args[0].startsWith("update")) {
				MS2LauncherUpdate.main(args);
			} else {
				showHelp();
			}
		} else {
			UniversalLauncher.main(args);
		}
	}
	
	public static void showHelp() {
		System.out.println("Mineshafter Squared, Universal Launcher v" + UniversalLauncher.MS2_VERSION.toString());
		System.out.println("See ms2.creatifcubed.com for more details");
		new HelpFormatter().printHelp("java <Java options> -jar mineshaftersquared.jar <MS2 options> <mc <Minecraft options>>", cmdOptions);
	}

}
