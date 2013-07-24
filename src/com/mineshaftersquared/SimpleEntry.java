package com.mineshaftersquared;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFrame;

import com.mineshaftersquared.misc.MS2Utils;

public class SimpleEntry extends JFrame {

	private static final Dimension DEFAULT_DIMENSIONS = new Dimension(854, 480);
	private static final int MC_BOOTSTRAP_VERSION = 4;
	
	public void run(String mc) throws Exception {
		File jar = new File(MS2Utils.getDefaultMCDir(), mc);
		@SuppressWarnings("resource")
		Class<?> clazz = new URLClassLoader(new URL[] { jar.toURI().toURL() }).loadClass("net.minecraft.launcher.Launcher");
		Constructor<?> ctor = clazz.getConstructor(new Class[] { JFrame.class, File.class, Proxy.class, PasswordAuthentication.class, String[].class, Integer.class });
		
		UniversalLauncher.log.info("Launching... ");
		ctor.newInstance(new Object[] { this, MS2Utils.getDefaultMCDir(), Proxy.NO_PROXY, null, new String[0], MC_BOOTSTRAP_VERSION });
		
		this.setSize(DEFAULT_DIMENSIONS);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		String mc = "ms2-launcher.jar";
		if (args.length > 1) {
			mc = args[1];
		}
		new SimpleEntry().run(mc);
	}
}
