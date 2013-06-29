package com.mineshaftersquared.game.entrypoints;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.OldAuth;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.MinecraftLauncher;
import com.mineshaftersquared.resources.Utils;

public class OneSixRegularEntry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Args length: " + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println("Args " + i + ": " + args[i]);
			System.out.println();
		}
		if (args.length < 1) {
			System.out.println("No proxy server given");
		}
		if (args.length < 2) {
			System.out.println("No minecraft main class given");
			System.exit(-1);
		}
		String[] shiftedArgs = new String[args.length - 1];
		System.arraycopy(args, 1, shiftedArgs, 0, shiftedArgs.length);

		MineProxy proxy = new MineProxy(args[0]);
		proxy.start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
		System.setProperty("java.net.preferIPv4Stack", "true");

		System.setProperty("minecraft.applet.WrapperClass", "com.mineshaftersquared.resources.MS2Container");
		try {
			Class<?> c = Class.forName(args[1]);
			Method main = c.getDeclaredMethod("main", String[].class);
			main.invoke(null, new Object[] { shiftedArgs });
		} catch (ClassNotFoundException ex) {
			System.out.println("Unable to get Minecraft main class " + args[1]);
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
