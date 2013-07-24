package com.mineshaftersquared;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.misc.ExtendedGnuParser;

public class MS2LauncherUpdate {
	
	public static final Options options = new Options();
	public static final String JAR_NAME = "mineshaftersquared.jar";
	public static final String NEW_JAR_NAME = "mineshaftersquared-new.jar";
	public static final String OLD_JAR_RENAME = "mineshaftersquared-old.jar";
	
	static {
		options.addOption(MS2Entry.updateOption);
		options.addOption(MS2Entry.argOption);
	}
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		CommandLineParser cmdParser = new ExtendedGnuParser();
		CommandLine cmd = cmdParser.parse(options, args);
		
		String stepStr = cmd.getOptionValue("update");
		int step = 0;
		if (stepStr != null) {
			try {
				step = Integer.parseInt(stepStr);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				return;
			}
		}
		String arg = cmd.getOptionValue("arg");
		
		switch (step) {
		case 0: // old: download new jar
			step0();
			break;
		case 1: // new: rename old jar
			if (arg == null) {
				UniversalLauncher.log.info("Must give arg for update (old jar name)");
				System.exit(1);
			}
			step1(arg);
			break;
		case 2: // old: rename new jar, delete self
			if (arg == null) {
				UniversalLauncher.log.info("Must give arg for update (new jar name)");
				System.exit(1);
			}
			step2(arg);
			break;
		}
	}
	
	public static void step0() throws IOException {
		File self = SimpleUtils.getJarPath(UniversalLauncher.class);
		File update = new File(NEW_JAR_NAME);
		
		FileUtils.copyURLToFile(new URL(UniversalLauncher.POLLING_SERVER + "latestdownload.php?jar=yes"), update);
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", update.getCanonicalPath(), "-update=1", "-arg=" + self.getName());
		pb.start();
	}
	
	public static void step1(String oldJar) throws IOException {
		File self = SimpleUtils.getJarPath(UniversalLauncher.class);
		File oldFile = new File(oldJar);
		File oldRename = new File(OLD_JAR_RENAME);
		
		oldFile.renameTo(oldRename);
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", oldRename.getCanonicalPath(), "-update=2", "-arg=" + self.getName());
		pb.start();
	}
	
	public static void step2(String newJar) throws IOException {
		File self = SimpleUtils.getJarPath(UniversalLauncher.class);
		File newFile = new File(newJar);
		File newRename = new File(JAR_NAME);
		
		newFile.renameTo(newRename);
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", newRename.getCanonicalPath());
		pb.start();
		
		self.deleteOnExit();
	}
}
