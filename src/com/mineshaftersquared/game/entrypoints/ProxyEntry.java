/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import com.creatifcubed.simpleapi.SimpleConsole;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.proxy.MineProxy;

/**
 * 
 * @author Adrian
 */
public class ProxyEntry {

	public static void main(String[] args) {
		String authserver = "mineshaftersquared.com";
		if (args.length > 0) {
			authserver = args[0];
		}
		MineProxy proxy = new MineProxy(authserver);
		proxy.start();
		UniversalLauncher.log.info("Proxying on port: " + proxy.getPort());
		while (true) {
			String in = SimpleConsole.readLine("Commands: stop\n");
			if (in.equals("stop")) {
				break;
			}
		}
	}
}
