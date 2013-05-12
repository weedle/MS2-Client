/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared;

import java.util.Arrays;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.game.entrypoints.BukkitEntry;
import com.mineshaftersquared.game.entrypoints.FTBEntry;
import com.mineshaftersquared.game.entrypoints.ProxyEntry;
import com.mineshaftersquared.game.entrypoints.RegularEntry;
import com.mineshaftersquared.game.entrypoints.ServerEntry;
import com.mineshaftersquared.game.entrypoints.SpoutcraftEntry;
import com.mineshaftersquared.game.entrypoints.TechnicEntry;
import com.mineshaftersquared.game.entrypoints.UpdateEntry;

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
			UniversalLauncher.log.info("Shifted args: " + shiftedArgs[i]);
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
			BukkitEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("spoutcraft")) {
			SpoutcraftEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("proxy")) {
			ProxyEntry.main(shiftedArgs);
			return;
		} else if (entry.equals("update-step1")) { // called from new version, rename old
			UpdateEntry.main(SimpleUtils.appendArrays(new String[] { "0" }, shiftedArgs));
			return;
		} else if (entry.equals("update-step2")) { // called from old version, rename new and start new
			UpdateEntry.main(SimpleUtils.appendArrays(new String[] { "1" }, shiftedArgs));
			return;
		}
		UniversalLauncher.main(shiftedArgs);
	}
}
