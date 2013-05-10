/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import java.util.Arrays;

import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.JarProcessBuilder;
import com.mineshaftersquared.resources.ProcessOutputRedirector;

/**
 * 
 * @author Adrian
 */
public class FTBEntry {
	public static void main(String[] args) {
		String jarname = "FTB_Launcher.jar";
		String authserver = UniversalLauncher.DEFAULT_AUTH_SERVER;
		int minRam = 0;
		int maxRam = 0;

		if (args.length > 0) {
			jarname = args[0];
		}
		if (args.length > 1) {
			authserver = args[1];
		}
		if (args.length > 2) {
			try {
				minRam = Integer.parseInt(args[2]);
			} catch (NumberFormatException ignore) {
				//
			}
		}
		if (args.length > 3) {
			try {
				maxRam = Integer.parseInt(args[3]);
			} catch (Exception ignore) {
				//
			}
		}

		MineProxy proxy = new MineProxy(authserver);
		proxy.start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
		System.setProperty("java.net.preferIPv4Stack", "true");

		String[] shiftedArgs = new String[0];
		try {
			shiftedArgs = Arrays.copyOfRange(args, 4, args.length);
		} catch (Exception ignore) {
			//
		}

		for (String str : shiftedArgs) {
			System.out.println("Shifted args: " + str);
		}

		Process p = JarProcessBuilder.create(jarname, new String[] { "-Dhttp.proxyHost=127.0.0.1",
			"-Dhttp.proxyPort=" + String.valueOf(proxy.getPort()), "-Djava.net.preferIPv4Stack=true" }, shiftedArgs);

		Thread t = new Thread(new ProcessOutputRedirector(p, "[MS2-FTB]: "));
		t.setDaemon(true);
		t.start();
		try {
			System.out.println("Waiting...");
			p.waitFor();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.out.println("Done");
		proxy.shouldEnd = true;
	}
}
