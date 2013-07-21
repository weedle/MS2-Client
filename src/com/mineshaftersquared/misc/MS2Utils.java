package com.mineshaftersquared.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;

public class MS2Utils {
	
	public static File getLocalDir() {
		return new File(System.getProperty("user.dir"));
	}
	
	public static File getDefaultMCDir() {
		String userHome = System.getProperty("user.home", ".");
		switch (SimpleOS.getOS()) {
		case MAC:
			return new File(userHome, "Library/Application Support/minecraft");
		case UNIX:
			return new File(userHome, ".minecraft");
		case WINDOWS:
			String appdata = System.getenv("APPDATA");
			return new File(appdata == null ? userHome : appdata, ".minecraft");
		default:
			return new File(userHome, "minecraft");
		}
	}
	
	public static Process launchServer(File local, String server, String authserver, boolean isBukkit, String[] javaArgs, String[] mcArgs) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(local);
			// java [java args] -jar [mineshaftersquared.jar] -server=[server] -authserver=[authserver] [-bukkit]? -mc [mc args]
			List<String> args = new ArrayList<String>(1 + javaArgs.length + 4 + (isBukkit ? 1 : 0) + 1 + mcArgs.length);
			args.add("java");
			args.addAll(Arrays.asList(javaArgs));
			args.add("-jar");
			args.add(SimpleUtils.getJarPath(UniversalLauncher.class).getCanonicalPath());
			args.add("-server=" + server);
			if (isBukkit) {
				args.add("-bukkit");
			}
			args.add("-mc");
			args.addAll(Arrays.asList(mcArgs));
			
			pb.command(args);
			Process p = pb.start();
			return p;
		} catch (IOException ex){ 
			ex.printStackTrace();
		}
		return null;
	}
}
