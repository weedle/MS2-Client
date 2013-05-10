/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import java.net.URL;
import java.net.URLEncoder;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.OldAuth;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.MinecraftLauncher;
import com.mineshaftersquared.resources.Utils;

/**
 * 
 * @author Adrian
 */
public class RegularEntry {

	public static void main(String[] args) {
		String username = "Player" + System.currentTimeMillis() % 1000;
		String sessionId = "-1";
		int pathfind = Utils.PATH_LOCAL;
		String authserver = UniversalLauncher.DEFAULT_AUTH_SERVER;
		if (args.length > 0) {
			username = args[0];
		}
		if (args.length > 1) {
			sessionId = args[1];
		}
		if (args.length > 2) {
			try {
				pathfind = Integer.parseInt(args[2]);
			} catch (NumberFormatException ignore) {
				//
			}
		}
		if (args.length > 3) {
			authserver = args[3];
		}
		if (args.length > 4) {
			if (args[4].equals("login")) {
				OldAuth.Response result = OldAuth.login(username, sessionId, authserver);
				if (result.sessionId == null) {
					System.out.println("Could not log in: " + result.message);
				} else {
					System.out.println(String.format("Logged in with username {%s} and sessionId {%s}", result.message, sessionId));
					sessionId = result.sessionId;
				}
			}
		}

		MineProxy proxy = new MineProxy(authserver);
		proxy.start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
		System.setProperty("java.net.preferIPv4Stack", "true");

		System.setProperty("minecraft.applet.WrapperClass", "com.mineshaftersquared.resources.MS2Container");
		MinecraftLauncher m = new MinecraftLauncher(new String[] { username, sessionId }, Utils.getMCPath(pathfind));
		m.run();
	}

}
