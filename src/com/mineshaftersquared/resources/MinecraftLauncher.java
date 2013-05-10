/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.applet.Applet;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Adrian
 */
public class MinecraftLauncher implements Runnable {
	private String[] credentials;
	private String path;

	public MinecraftLauncher(String[] credentials, String path) {
		this.credentials = credentials;
		this.path = path;
	}

	@Override
	public void run() {
		try {
			ClassLoader cl = this.initClassLoader();
			Class mc = cl.loadClass("net.minecraft.client.Minecraft");
			this.fixMCPathField(mc);

			System.out.println("Launching as applet...");
			Class AppletWrapperClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
			Applet app = (Applet) AppletWrapperClass.newInstance();
			MS2Frame frame = new MS2Frame();
			Map<String, String> parameters = new HashMap<String, String>();

			parameters.put("username", this.credentials[0]);
			parameters.put("sessionid", this.credentials[1]);
			parameters.put("demo", "false");
			parameters.put("server", "false");
			parameters.put("stand-alone", "true");

			MS2Container container = new MS2Container(parameters, app);
			System.out.println("Starting frame/container/app...");

			frame.setVisible(true);
			frame.start(container);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void fixMCPathField(Class mc) throws IllegalArgumentException, IllegalAccessException {
		Field pathField = getMCPathField(mc);
		pathField.setAccessible(true);
		pathField.set(null, new File(this.path));

	}

	private static Field getMCPathField(Class mc) {
		Field[] fields = mc.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			if (f.getType() == File.class) {
				if (f.getModifiers() == (Modifier.STATIC + Modifier.PRIVATE)) {
					return f;
				}
			}
		}
		return null;
	}

	private ClassLoader initClassLoader() {
		try {
			System.out.println("Minecraft Launcher - init classloading...");
			String[] jarfiles = new String[] { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			URL[] urls = new URL[jarfiles.length];
			File binDir = new File(this.path, "bin");

			for (int i = 0; i < urls.length; i++) {
				File f = new File(binDir, jarfiles[i]);
				urls[i] = f.toURI().toURL();
				System.out.println("Loaded jar file: " + urls[i].toString());
			}

			String nativesDir = new File(binDir, "natives").toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.out.println("Set natives paths");

			URLClassLoader cl = new URLClassLoader(urls);

			System.setProperty("minecraft.applet.TargetDirectory", this.path);
			System.out.println("Created classloader and set TargetDirectory");
			System.out.println("Minecraft Launcher - done init classloader");

			return cl;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
