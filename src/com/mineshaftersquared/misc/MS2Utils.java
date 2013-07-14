package com.mineshaftersquared.misc;

import java.io.File;

import com.creatifcubed.simpleapi.SimpleOS;
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

}
