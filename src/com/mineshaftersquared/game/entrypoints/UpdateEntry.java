package com.mineshaftersquared.game.entrypoints;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import com.creatifcubed.simpleapi.SimpleException;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.resources.JarProcessBuilder;

public class UpdateEntry {
	public static final String COPY_NAME = "mineshaftersquared-new-copy.jar";
	public static final String FINAL_NAME = "mineshaftersquared.jar";
	public static final String DOWNLOAD_NAME = "mineshaftersquared-new.jar";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int step = -1;
		if (args.length < 1) {
			throw new RuntimeException("Invalid udpate option: no step given");
		}
		try {
			step = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		if (args.length < 2) {
			throw new RuntimeException("Invalid udpate option: no filename given");
		}
		
		switch (step) {
		case 0:
			copySelf();
			deleteFile(args[1]);
			renameNew();
			break;
		case 1:
			deleteFile(args[1]);
			JOptionPane.showMessageDialog(null, "Update installation done. You may now start the app");
			break;
		default:
			throw new RuntimeException("Invalid update step " + step);
		}
	}
	
	public static void copySelf() {
		File self = SimpleUtils.getJarPath();
		try {
			SimpleUtils.copyFile(self, new File(COPY_NAME));
		} catch (IOException ex) {
			throw new SimpleException(ex);
		}
		UniversalLauncher.log.info("Done duplicating update file");
	}
	
	public static void deleteFile(String filename) {
		while (true) {
			SimpleUtils.wait(1);
			File f = new File(filename);
			if (f.exists()) {
				if (f.delete()) {
					break;
				}
			} else {
				throw new RuntimeException(String.format("File {%s} does not exist", filename));
			}
		}
		UniversalLauncher.log.info("Deleted.");
	}
	
	public static void renameNew() {
		File copied = new File(COPY_NAME);
		if (!copied.exists()) {
			throw new RuntimeException(String.format("Copied new launcher {%s} does not exist", COPY_NAME));
		}
		if (copied.renameTo(new File(FINAL_NAME))) {
			try {
				System.out.println("Done renaming copy of new launcher. Beginning final step of update");
				List<String> commands = new LinkedList<String>();
				commands.add("java");
				commands.add("-jar");
				commands.add(FINAL_NAME);
				commands.add("update-step2");
				commands.add(SimpleUtils.getJarPath().getCanonicalPath());
				Process p = JarProcessBuilder.create(commands);
				if (p != null) {
					System.out.println("Done launching next");
					System.exit(0);
				} else {
					throw new RuntimeException("Creating next launcher process, is null");
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
