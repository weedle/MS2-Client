/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleUtils;

/**
 * 
 * @author Adrian
 */
public class JarProcessBuilder {
	private JarProcessBuilder() {
		//
	}

	public static Process create(String jarpath, int minRam, int maxRam, String[] flags, String[] params) {
		return create(getCommand(jarpath, minRam, maxRam, flags, params));
	}

	public static Process create(List<String> commands) {
		return create(commands.toArray(new String[commands.size()]));
	}

	public static Process create(String[] commands) {
		System.out.println("Creating command...");
		for (String str : commands) {
			System.out.print(str + " ");
		}
		System.out.println();
		ProcessBuilder pb = new ProcessBuilder(Arrays.asList(commands));
		try {
			Process p = pb.start();

			return p;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static List<String> getCommand(String jarpath, int minRam, int maxRam, String[] flags, String[] params) {
		List<String> newFlags = new LinkedList<String>();
		String max = maxRam == 0 ? null : ("-Xmx" + maxRam + "m");
		String min = minRam == 0 ? null : ("-Xms" + minRam + "m");
		if (max != null) {
			newFlags.add(max);
		}
		if (min != null) {
			newFlags.add(min);
		}
		if (flags != null) {
			for (String str : flags) {
				newFlags.add(str);
			}
		}
		return getCommand(jarpath, newFlags.toArray(new String[newFlags.size()]), params);
	}

	public static List<String> getCommand(String jarpath, String[] flags, String[] params) {
		List<String> args = new LinkedList<String>();

		args.add("java");
		if (flags != null) {
			for (String str : flags) {
				args.add(str);
			}
		}
		args.add("-jar");
		args.add(jarpath);
		if (params != null) {
			for (String str : params) {
				args.add(str);
			}
		}

		return args;
	}

	public static Process create(String jarpath, String[] flags, String[] params) {
		return create(getCommand(jarpath, flags, params));
	}

	public static String[] getConsoleCommand(List<String> command) {
		return getConsoleCommand(command.toArray(new String[command.size()]));
	}

	public static String[] getConsoleCommand(String[] command) {
		switch (SimpleOS.getOS()) {
		case WINDOWS:
			return SimpleUtils.appendArrays(new String[] { "cmd", "/c", "start" }, command);
		case MAC:
			String commandString = "";
			for (String str : command) {
				if (str.startsWith("'") || str.startsWith("\"")) {
					commandString += str;
				} else {
					if (str.indexOf(" ") == -1) {
						commandString += str;
					} else {
						commandString += "\"" + str.replace("\"", "\\\"") + "\"";
					}
				}
				commandString += " ";
			}
			if (command.length > 0) {
				commandString = commandString.substring(0, commandString.length() - 1);
			}

			return new String[] { "osascript", "-e",
				String.format("tell application \"Terminal\" to do script \"%s\"", commandString.replace("'", "\\'")) };
		case UNIX:
			return SimpleUtils.appendArrays(new String[] { "gnome-terminal", "-x" }, command);
		}
		return null;
	}

	public static void wrapJar(String load, String[] args) {
		try {
			JarFile jar = new JarFile(load);
			Attributes attributes = jar.getManifest().getMainAttributes();
			String mainClass = attributes.getValue("Main-Class");

			URL u = new File(load).toURI().toURL();

			System.out.println("{JarProcessBuilder: wrapping jar {" + load + "}, main class {" + mainClass + "}");

			URLClassLoader cl = new URLClassLoader(new URL[] { u });

			Class main = cl.loadClass(mainClass);
			Method mainMethod = main.getDeclaredMethod("main", String[].class);

			for (String str : args) {
				System.out.println("Wrapping with args: " + str);
			}

			// TODO
			mainMethod.invoke(null, new Object[] { args }); // what
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
