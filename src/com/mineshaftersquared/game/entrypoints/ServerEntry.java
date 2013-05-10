/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import java.util.Arrays;

import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.JarProcessBuilder;

/**
 * 
 * @author Adrian
 */
public class ServerEntry {

	public static void main(String[] args) {
		String jarname = "minecraft_server.jar";
		String authserver = UniversalLauncher.DEFAULT_AUTH_SERVER;

		if (args.length > 0) {
			jarname = args[0];
		}
		if (args.length > 1) {
			authserver = args[1];
		}

		MineProxy proxy = new MineProxy(authserver);
		proxy.start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
		System.setProperty("java.net.preferIPv4Stack", "true");

		String[] shiftedArgs = new String[0];
		try {
			shiftedArgs = Arrays.copyOfRange(args, 2, args.length);
		} catch (Exception ignore) {
			//
		}

		for (String str : shiftedArgs) {
			System.out.println("Shifted args: " + str);
		}

		JarProcessBuilder.wrapJar(jarname, shiftedArgs);
	}
}
