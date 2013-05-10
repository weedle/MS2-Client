package com.mineshaftersquared.resources;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.creatifcubed.simpleapi.SimpleUtils;

public class GameUpdaterProxy {
	private String dir;
	public boolean forceUpdate;

	public GameUpdaterProxy(String dir) {
		this.dir = dir;
		this.forceUpdate = false;
	}

	public boolean update() {
		try {
			if (!hasOfficialLauncher(this.dir)) {
				downloadMojangOfficialLauncher(this.dir);
			}

			File bin = new File(this.dir, "bin");
			if (!bin.exists()) {
				bin.mkdir();
			}

			if (this.forceUpdate) {
				new File(bin, "minecraft.jar").delete();
			}

			String separator = System.getProperty("file.separator");

			URLClassLoader cl = new URLClassLoader(new URL[] { new File(this.dir, "minecraft.jar").toURI().toURL() });
			Class gameupdaterClass = cl.loadClass("net.minecraft.GameUpdater");
			Constructor constructor = gameupdaterClass.getConstructor(String.class, String.class, Boolean.TYPE);

			String oldVersion = "-1";
			File oldMc = new File(new File(this.dir, "bin"), "minecraft.jar");
			if (oldMc.exists()) {
				oldVersion = Utils.getMCVersion(oldMc);
			}
			Runnable gameUpdater = (Runnable) constructor.newInstance(oldVersion, "minecraft.jar", false);
			String path = this.dir + separator + "bin" + separator;

			Field forceUpdate = gameupdaterClass.getField("forceUpdate");
			forceUpdate.setBoolean(gameUpdater, this.forceUpdate);

			Method loadJarURLs = gameupdaterClass.getDeclaredMethod("loadJarURLs");
			Method downloadJars = gameupdaterClass.getDeclaredMethod("downloadJars", String.class);
			Method extractJars = gameupdaterClass.getDeclaredMethod("extractJars", String.class);
			Method extractNatives = gameupdaterClass.getDeclaredMethod("extractNatives", String.class);

			loadJarURLs.setAccessible(true);
			downloadJars.setAccessible(true);
			extractJars.setAccessible(true);
			extractNatives.setAccessible(true);

			loadJarURLs.invoke(gameUpdater);
			downloadJars.invoke(gameUpdater, path);
			extractJars.invoke(gameUpdater, path);
			extractNatives.invoke(gameUpdater, path);

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static boolean hasOfficialLauncher(String dir) {
		return new File(dir, "minecraft.jar").exists();
	}

	public static boolean downloadMojangOfficialLauncher(String dir) {
		try {
			SimpleUtils.downloadFile(new URL("https://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar"),
					new File(dir, "minecraft.jar").getCanonicalPath(), 1 << 24);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
