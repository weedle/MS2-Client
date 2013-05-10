/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.resources.JarProcessBuilder;

/**
 * 
 * @author Raidriar
 */
public class MS2LauncherUpdater {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Must give updater target file");
			System.exit(-1);
		}
		String launchername = args[0];
		System.out.println("Removing old launcher...");
		while (true) {
			File f = new File(launchername);
			f.delete();
			if (!f.exists()) {
				break;
			}
			System.out.println("Not deleted yet, waiting 3 seconds... ");
			try {
				Thread.sleep(10000 * 3);
			} catch (InterruptedException ignore) {
				ignore.printStackTrace();
			}
		}
		System.out.println("Old launcher gone.");
		System.out.println("Downloading file...");
		try {
			SimpleUtils.downloadFile(new URL("http://ms2.creatifcubed.com/latestdownload.php?jar=yes"), new File(
					launchername).getCanonicalPath(), 1 << 24);
			System.out.println("Downloaded new launcher.");

			System.out.println("Relaunching...");
			Process p = JarProcessBuilder.create(launchername, null, null);
			System.out.println("Done. Process is null (should be false): " + (p == null));
			return;
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Something went wrong.");
	}
}
