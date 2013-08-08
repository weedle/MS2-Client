package com.mineshaftersquared.misc;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;

public class MS2Utils {
	
	public static File getLocalDir() {
		return new File(System.getProperty("user.dir"));
	}
	
	public static File getMS2Dir() {
		return new File(getAppDataDir(), UniversalLauncher.MS2_DIR);
	}
	
	public static File getAppDataDir() {
		String userHome = System.getProperty("user.home", ".");
		switch (SimpleOS.getOS()) {
		case MAC:
			return new File(userHome, "Library/Application Support");
		case UNIX:
			return new File(userHome);
		case WINDOWS:
			String appdata = System.getenv("APPDATA");
			return new File(appdata == null ? userHome : appdata);
		default:
			return new File(userHome);
		}
	}
	
	public static File getDefaultMCDir() {
		String mcDir = null;
		switch (SimpleOS.getOS()) {
		case MAC:
			mcDir = "minecraft";
			break;
		case UNIX:
			mcDir = ".minecraft";
			break;
		case WINDOWS:
			mcDir = ".minecraft";
			break;
		default:
			mcDir = "minceraft";
			break;
		}
		return new File(getAppDataDir(), mcDir);
	}
	
	public static Process launchGame(File local, String authserver, boolean offline) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(local);
			List<String> args = new LinkedList<String>();
			
			args.add("java");
			args.add("-jar");
			args.add(SimpleUtils.getJarPath(UniversalLauncher.class).getCanonicalPath());
			args.add("-game");
			if (offline) {
				args.add("-offline");
			}
			pb.command(args);
			Process p = pb.start();
			return p;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static Process launchServer(File local, String server, String authserver, boolean isBukkit, String[] javaArgs, String[] mcArgs) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(local);
			// java [java args] -jar [mineshaftersquared.jar] -server=[server] -authserver=[authserver] [-guiconsole]? -mc [mc args]
			List<String> args = new ArrayList<String>(1 + javaArgs.length + 4 + (isBukkit ? 1 : 0) + 1 + mcArgs.length);
			args.add("java");
			args.addAll(Arrays.asList(javaArgs));
			args.add("-jar");
			args.add(SimpleUtils.getJarPath(UniversalLauncher.class).getCanonicalPath());
			args.add("-server=" + server);
			if (isBukkit) {
				args.add("-guiconsole");
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
	
	public static String getMD5(InputStream in) {
		DigestInputStream dis = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			dis = new DigestInputStream(in, md5);
			
			byte[] buffer = new byte[1024 * 8];
			while (dis.read(buffer) != -1);
			
			return StringUtils.leftPad(new BigInteger(1, md5.digest()).toString(16), 32, '0');
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(dis);
		}
		return null;
	}
	
	public static String readUntil(InputStream in, String endSequence) {
		return readUntil(in, endSequence.getBytes(Charset.forName("utf-8")));
	}
	
	public static String readUntil(InputStream in, byte[] endSequence) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			int i = 0;
			while (true) {
				byte b = 0;
				try {
					b = (byte) in.read();
				} catch (EOFException ex) {
					ex.printStackTrace();
					break;
				}
				out.write(b);
				if (b == endSequence[i]) {
					i++;
					if (i == endSequence.length) {
						break;
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return new String(out.toByteArray(), Charset.forName("utf-8"));
	}
	
	public static String getBukkitMinecraftServerClass(JarFile jar) {
		String match = null;
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			String name = jarEntry.getName();
			if (name.contains("MinecraftServer")) {
				UniversalLauncher.log.info("Possible server class " + name);
				if (name.matches("net/minecraft/server/(?:[a-zA-Z0-9_]+/)?MinecraftServer\\.class")) {
					UniversalLauncher.log.info("Matched server class " + name);
					match = name.substring(0, name.indexOf(".class")).replace('/', '.');
				}
			}
		}
		return match;
	}
}
