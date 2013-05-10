/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared;

import java.util.Arrays;

import com.mineshaftersquared.game.entrypoints.FTBEntry;
import com.mineshaftersquared.game.entrypoints.ProxyEntry;
import com.mineshaftersquared.game.entrypoints.RegularEntry;
import com.mineshaftersquared.game.entrypoints.ServerEntry;
import com.mineshaftersquared.game.entrypoints.SpoutcraftEntry;
import com.mineshaftersquared.game.entrypoints.TechnicEntry;

/**
 * 
 * @author Adrian
 */
public class MS2Entry {

	public static void main(String[] args) {
		String entry = "universal";
		String[] shiftedArgs = new String[0];
		if (args.length > 0) {
			entry = args[0];
			try {
				shiftedArgs = Arrays.copyOfRange(args, 1, args.length);
			} catch (Exception ignore) {
				//
			}
		}
		for (int i = 0; i < shiftedArgs.length; i++) {
			System.out.println("Shifted args: " + shiftedArgs[i]);
		}
		if (entry.equals("regular")) {
			RegularEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("server-vanilla")) {
			ServerEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("ftb")) {
			FTBEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("technic")) {
			TechnicEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("server-bukkit")) {
			ServerEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("spoutcraft")) {
			SpoutcraftEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("proxy")) {
			ProxyEntry.main(shiftedArgs);
			return;
		}
		UniversalLauncher.main(shiftedArgs);
	}
}
