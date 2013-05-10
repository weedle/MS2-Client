/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import java.util.Arrays;

import com.mineshaftersquared.resources.JarProcessBuilder;
import com.mineshaftersquared.resources.ProcessOutputRedirector;

/**
 * 
 * @author Adrian
 */
public class SpoutcraftEntry {
	public static void main(String[] args) {
		String jarname = "ms2-spoutcraft.jar";
		int cutoff = 0;

		String[] shiftedArgs = new String[0];
		if (args.length > 0) {
			jarname = args[0];
			cutoff = 1;
		}

		shiftedArgs = Arrays.copyOfRange(args, cutoff, args.length);

		Process p = JarProcessBuilder.create(jarname, new String[] { "-noverify" }, shiftedArgs);

		Thread t = new Thread(new ProcessOutputRedirector(p, "[MS2-Spoutcraft]: "));
		t.setDaemon(true);
		t.start();
		try {
			p.waitFor();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
