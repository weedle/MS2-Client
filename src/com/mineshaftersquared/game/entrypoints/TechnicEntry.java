/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import java.util.Arrays;

import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.resources.JarProcessBuilder;
import com.mineshaftersquared.resources.ProcessOutputRedirector;

/**
 * 
 * @author Adrian
 */
public class TechnicEntry {
	public static void main(String[] args) {
		String jarname = "TechnicLauncher.jar";
		String authserver = UniversalLauncher.DEFAULT_AUTH_SERVER;
		int minRam = 0;
		int maxRam = 0;
		String proxyPort = "9010";
		int cutoff = 0;

		if (args.length > 0) {
			jarname = args[0];
			cutoff = 1;
		}
		if (args.length > 1) {
			authserver = args[1];
			cutoff = 2;
		}
		if (args.length > 2) {
			try {
				minRam = Integer.parseInt(args[2]);
			} catch (NumberFormatException ignore) {
				//
			}
			cutoff = 3;
		}
		if (args.length > 3) {
			try {
				maxRam = Integer.parseInt(args[3]);
			} catch (Exception ignore) {
				//
			}
			cutoff = 4;
		}
		if (args.length > 4) {
			try {
				proxyPort = args[4];
			} catch (NumberFormatException ignore) {
				//
			}
			cutoff = 5;
		}
		//
		// MineProxy proxy = new MineProxy(authserver);
		// proxy.start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", proxyPort);
		System.setProperty("java.net.preferIPv4Stack", "true");

		String[] shiftedArgs = new String[0];
		try {
			shiftedArgs = Arrays.copyOfRange(args, cutoff, args.length);
		} catch (Exception ignore) {
			//
		}

		for (String str : shiftedArgs) {
			UniversalLauncher.log.info("Shifted args: " + str);
		}

		Process p = JarProcessBuilder.create(jarname, new String[] { "-Dhttp.proxyHost=127.0.0.1",
			"-Dhttp.proxyPort=" + proxyPort, "-Djava.net.preferIPv4Stack=true", "-noverify" }, shiftedArgs);

		Thread t = new Thread(new ProcessOutputRedirector(p, "[MS2-Technic]: %s"));
		t.setDaemon(true);
		t.start();
		try {
			p.waitFor();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
