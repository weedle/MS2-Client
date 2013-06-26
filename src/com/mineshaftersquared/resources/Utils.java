/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleResources;
import com.creatifcubed.simpleapi.SimpleStreams;
import com.mineshaftersquared.models.LocalMCVersion;

/**
 * 
 * @author Adrian
 */
public class Utils {

	public static final int PATH_LOCAL = 1;
	public static final int PATH_DEFAULTMC = 2;

	public static final int MOD_IGNORE = 1;
	public static final int MOD_REPLACE = 2;

	private Utils() {
		return;
	}

	public static String getMCPath(int pathfind) {
		switch (pathfind) {
		case PATH_LOCAL:
			return System.getProperty("user.dir", ".");
		case PATH_DEFAULTMC:
			return getDefaultMCPath();
		}
		return System.getProperty("user.dir", ".");
	}

	public static String getAppDataPath() {
		String path = System.getProperty("user.home", ".");
		switch (SimpleOS.getOS()) {
		case WINDOWS:
			String appdata = System.getenv("APPDATA");
			path = (appdata == null ? path : appdata);
			break;
		case MAC:
			path += "/Library/Application Support";
			break;
		case UNIX:
			//
			break;
		default:
			//
		}
		return path;
	}

	private static String getDefaultMCPath() {
		String base = getAppDataPath();
		switch (SimpleOS.getOS()) {
		case WINDOWS:
			base += "\\.minecraft";
			break;
		case MAC:
			base += "/minecraft";
			break;
		case UNIX:
			base += "/.minecraft";
			break;
		default:
			base += "/minecraft";
		}
		return base;
	}

	public static boolean existsInstallationIn(int pathfind) {
		String separator = System.getProperty("file.separator");
		return new File(getMCPath(pathfind) + separator + "bin" + separator + "minecraft.jar").exists();
	}

	public static List<String> listMCInstallations() {
		List<String> places = new LinkedList<String>();
		places.add((existsInstallationIn(PATH_LOCAL) ? "Found" : "Did not find")
				+ " bin/minecraft.jar in current (local) folder");
		places.add((existsInstallationIn(PATH_DEFAULTMC) ? "Found" : "Did not find")
				+ " bin/minecraft.jar at default location");

		return places;
	}

	public static String editJar(File originalJar, File moddedJar, Map<String, Integer> changes) {
		ZipInputStream in = null;
		ZipOutputStream out = null;
		try {
			in = new ZipInputStream(new FileInputStream(originalJar));
			out = new ZipOutputStream(new FileOutputStream(moddedJar));

			while (true) {
				ZipEntry entry = in.getNextEntry();
				if (entry == null) {
					break;
				}
				String entryName = entry.getName();

				InputStream source = in;
				if (changes.containsKey(entryName)) {
					int command = changes.get(entryName);
					System.out
							.println("Modding {" + originalJar.getName() + "} - {" + entryName + ", " + command + "}");
					if (command == MOD_IGNORE) {
						continue;
					} else if (command == MOD_REPLACE) {
						source = SimpleResources.loadAsStream(entryName);
					}
				}
				out.putNextEntry(new ZipEntry(entryName));
				SimpleStreams.pipeStreams(source, out);
				out.flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (Exception ignore) {
				//
			}
		}
		return null;
	}

	public static File getModdedFile(File original) {
		String name = original.getName();
		return new File(original.getParentFile(), "ms2-modded-" + name);

	}

	public static String getMCVersion(File file) {
		String prefix = "Minecraft Minecraft ";
		Pattern magic = Pattern.compile("(" + prefix + "(\\w|\\.)+(?=\01\00))");
		String version = "Unknown";
		JarFile jar = null;
		try {
			jar = new JarFile(file);
			ZipEntry entry = jar.getEntry("net/minecraft/client/Minecraft.class");
			if (entry != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
				String line;
				while ((line = br.readLine()) != null) {
					Matcher matcher = magic.matcher(line);
					if (matcher.find()) {
						version = matcher.group().substring(prefix.length());
						break;
					}
				}
				br.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				jar.close();
			} catch (Exception ignore) {
				// do nothing
			}
		}
		return version;
	}
	
	public static LocalMCVersion[] getLocalLocationVersions() {
		return LocalMCVersion.findInstallationsInRoot(new File(Utils.getMCPath(Utils.PATH_LOCAL)), true);
	}
	public static LocalMCVersion[] getDefaultLocationVersions() {
		return LocalMCVersion.findInstallationsInRoot(new File(Utils.getMCPath(Utils.PATH_DEFAULTMC)), false);
	}
	
}
