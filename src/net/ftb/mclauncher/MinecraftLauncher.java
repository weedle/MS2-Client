package net.ftb.mclauncher;

import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.log.LogLevel;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;

public class MinecraftLauncher {
	public static Process launchMinecraft(String workingDir, String username, String password, String forgename,
			String rmax) throws IOException {
		String[] jarFiles = { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
		StringBuilder cpb = new StringBuilder("");
		File tempDir = new File(new File(workingDir).getParentFile(), "instMods/");
		if (tempDir.isDirectory()) {
			for (String name : tempDir.list()) {
				if ((name.toLowerCase().contains("forge")) && (name.toLowerCase().endsWith(".zip"))
						&& (!name.toLowerCase().equalsIgnoreCase(forgename))) {
					if (new File(tempDir, forgename).exists()) {
						new File(tempDir, name).delete();
					} else {
						new File(tempDir, name).renameTo(new File(tempDir, forgename));
					}
				}

				if ((!name.equalsIgnoreCase(forgename))
						&& ((name.toLowerCase().endsWith(".zip")) || (name.toLowerCase().endsWith(".jar")))) {
					cpb.append(OSUtils.getJavaDelimiter());
					cpb.append(new File(tempDir, name).getAbsolutePath());
				}
			}
		} else {
			Logger.logInfo("Not a directory.");
		}

		cpb.append(OSUtils.getJavaDelimiter());
		cpb.append(new File(tempDir, forgename).getAbsolutePath());

		for (String jarFile : jarFiles) {
			cpb.append(OSUtils.getJavaDelimiter());
			cpb.append(new File(new File(workingDir, "bin"), jarFile).getAbsolutePath());
		}

		List arguments = new ArrayList();

		String separator = System.getProperty("file.separator");
		String path = OSUtils.getCurrentOS() == OSUtils.OS.WINDOWS ? new StringBuilder()
				.append(System.getProperty("java.home")).append(separator).append("bin").append(separator)
				.append("javaw").toString() : new StringBuilder().append(System.getProperty("java.home"))
				.append(separator).append("bin").append(separator).append("java").toString();
		arguments.add(path);

		setMemory(arguments, rmax);

		arguments.add("-XX:+UseConcMarkSweepGC");
		arguments.add("-XX:+CMSIncrementalMode");
		arguments.add("-XX:+AggressiveOpts");

		arguments.add("-Dhttp.proxyHost=127.0.0.1");
		System.out.println("PROXY PORT: " + System.getProperty("http.proxyPort"));
		arguments.add("-Dhttp.proxyPort=" + System.getProperty("http.proxyPort", "9010"));

		arguments.add("-cp");
		arguments.add(new StringBuilder().append(System.getProperty("java.class.path")).append(cpb.toString())
				.toString());

		String additionalOptions = Settings.getSettings().getAdditionalJavaOptions();
		if (!additionalOptions.isEmpty()) {
			Collections.addAll(arguments, additionalOptions.split("\\s+"));
		}

		arguments.add(MinecraftLauncher.class.getCanonicalName());
		arguments.add(workingDir);
		arguments.add(!ModPack.getSelectedPack().getAnimation().equalsIgnoreCase("empty") ? new StringBuilder()
				.append(OSUtils.getDynamicStorageLocation()).append("ModPacks").append(separator)
				.append(ModPack.getSelectedPack().getDir()).append(separator)
				.append(ModPack.getSelectedPack().getAnimation()).toString() : "empty");
		arguments.add(forgename);
		arguments.add(username);
		arguments.add(password);
		arguments.add(new StringBuilder()
				.append(ModPack.getSelectedPack().getName())
				.append(" v")
				.append(Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? ModPack
						.getSelectedPack().getVersion() : Settings.getSettings().getPackVer()).toString());
		arguments.add(new StringBuilder().append(OSUtils.getDynamicStorageLocation()).append("ModPacks")
				.append(separator).append(ModPack.getSelectedPack().getDir()).append(separator)
				.append(ModPack.getSelectedPack().getLogoName()).toString());

		ProcessBuilder processBuilder = new ProcessBuilder(arguments);
		processBuilder.redirectErrorStream(true);
		return processBuilder.start();
	}

	private static void setMemory(List<String> arguments, String rmax) {
		boolean memorySet = false;
		try {
			int min = 256;
			if ((rmax != null) && (Integer.parseInt(rmax) > 0)) {
				arguments.add(new StringBuilder().append("-Xms").append(min).append("M").toString());
				Logger.logInfo(new StringBuilder().append("Setting MinMemory to ").append(min).toString());
				arguments.add(new StringBuilder().append("-Xmx").append(rmax).append("M").toString());
				Logger.logInfo(new StringBuilder().append("Setting MaxMemory to ").append(rmax).toString());
				memorySet = true;
			}
		} catch (Exception e) {
			Logger.logError("Error parsing memory settings", e);
		}
		if (!memorySet) {
			arguments.add("-Xms256M");
			Logger.logInfo("Defaulting MinMemory to 256");
			arguments.add("-Xmx1024M");
			Logger.logInfo("Defaulting MaxMemory to 1024");
		}
	}

	public static void main(String[] args) {
		String basepath = args[0];
		String animationname = args[1];
		String forgename = args[2];
		String username = args[3];
		String password = args[4];
		String modPackName = args[5];
		String modPackImageName = args[6];
		Settings.getSettings().save();
		try {
			System.out.println("Loading jars...");
			String[] jarFiles = { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };
			HashMap map = new HashMap();
			int counter = 0;
			File tempDir = new File(new File(basepath).getParentFile(), "instMods/");
			if (tempDir.isDirectory()) {
				for (String name : tempDir.list()) {
					if ((!name.equalsIgnoreCase(forgename))
							&& ((name.toLowerCase().endsWith(".zip")) || (name.toLowerCase().endsWith(".jar")))) {
						map.put(Integer.valueOf(counter), new File(tempDir, name));
						counter++;
					}
				}

			}

			map.put(Integer.valueOf(counter), new File(tempDir, forgename));
			counter++;
			for (String jarFile : jarFiles) {
				map.put(Integer.valueOf(counter), new File(new File(basepath, "bin"), jarFile));
				counter++;
			}

			URL[] urls = new URL[map.size()];
			for (int i = 0; i < counter; i++) {
				try {
					urls[i] = ((File) map.get(Integer.valueOf(i))).toURI().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				System.out.println(new StringBuilder().append("Loading URL: ").append(urls[i].toString()).toString());
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(basepath, "bin"), "natives").toString();
			System.out.println("Natives loaded...");

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(basepath).getParent());

			URLClassLoader cl = new URLClassLoader(urls, MinecraftLauncher.class.getClassLoader());

			System.out.println("Loading minecraft class");
			Class mc = cl.loadClass("net.minecraft.client.Minecraft");
			System.out.println(new StringBuilder().append("mc = ").append(mc).toString());
			Field[] fields = mc.getDeclaredFields();
			System.out.println(new StringBuilder().append("field amount: ").append(fields.length).toString());
			String[] mcArgs;
			String mcDir;
			Class MCAppletClass;
			Applet mcappl;
			MinecraftFrame mcWindow;
			for (Field f : fields) {

				if (f.getType() == File.class) {
					if (0 != (f.getModifiers() & 0xA)) {
						f.setAccessible(true);
						f.set(null, new File(basepath));
						System.out.println(new StringBuilder().append("Fixed Minecraft Path: Field was ")
								.append(f.toString()).toString());
						break;
					}
				}
			}
			mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			mcDir = mc.getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "minecraft" })
					.toString();

			System.out.println(new StringBuilder().append("MCDIR: ").append(mcDir).toString());

			System.out.println("Launching with applet wrapper...");
			try {
				MCAppletClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
				mcappl = (Applet) MCAppletClass.newInstance();
				mcWindow = new MinecraftFrame(modPackName, modPackImageName, animationname);
				mcWindow.start(mcappl, mcArgs[0], mcArgs[1]);
			} catch (InstantiationException e) {
				Logger.log("Applet wrapper failed! Falling back to compatibility mode.", LogLevel.WARN, e);
				mc.getMethod("main", new Class[] { java.lang.String.class }).invoke(null, new Object[] { mcArgs });
			}
		} catch (Exception e) {
		}
	}
}