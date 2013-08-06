package com.mineshaftersquared;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.creatifcubed.simpleapi.SimpleConsole;
import com.mineshaftersquared.misc.ExtendedGnuParser;
import com.mineshaftersquared.proxy.MS2Proxy;
import com.mineshaftersquared.proxy.MS2HttpProxyHandlerFactory;

public class MS2ProxyEntry {
	
	public static final Options options = new Options() {{
		addOption(MS2Entry.authOfflineOption);
		addOption(MS2Entry.authserverOption);
		addOption(MS2Entry.mcSeparatorOption);
	}};

	public static void main(String[] args) throws ParseException {
		CommandLineParser cmdParser = new ExtendedGnuParser();
		CommandLine cmd = cmdParser.parse(options, args);
		
		MS2Proxy proxy = new MS2Proxy(new MS2Proxy.MS2RoutesDataSource(cmd.getOptionValue("authserver", UniversalLauncher.DEFAULT_AUTH_SERVER)), new MS2HttpProxyHandlerFactory());
		proxy.offline = cmd.hasOption("offline");
		Thread t = proxy.startAsync();
		int port = proxy.getProxyPort();
		System.out.println("Type 'quit' to exit, 'port' to get port");
		while (true) {
			String line = SimpleConsole.readLine();
			if (line.equals("quit")) {
				break;
			} else if (line.equals("port")) {
				System.out.println("Port: " + port);
			} else {
				System.out.println("Type 'quit' to exit, 'port' to get port");
			}
		}
		System.out.println("MS2Proxy done.");
	}
}
