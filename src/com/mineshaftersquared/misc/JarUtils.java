package com.mineshaftersquared.misc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.creatifcubed.simpleapi.SimpleStreams;
import com.mineshaftersquared.UniversalLauncher;

public class JarUtils {
	
	public static boolean patchJar(File in, File out, Map<String, InputStream> replacements) {
		ZipInputStream zis = null;
		ZipOutputStream zos = null;
		try {
			UniversalLauncher.log.info(String.format("Patching jar %s to %s ...", in.getCanonicalPath(), out.getCanonicalPath()));
			zis = new ZipInputStream(new FileInputStream(in));
			zos = new ZipOutputStream(new FileOutputStream(out));
			while (true) {
				ZipEntry entry = zis.getNextEntry();
				if (entry == null) {
					break;
				}
				String name = entry.getName();
				
				InputStream dataSource = zis;
				for (String regex : replacements.keySet()) {
					if (name.matches(regex)) {
						InputStream sub = replacements.get(regex);
						UniversalLauncher.log.info(String.format("Replacing %s which matched %s with %s ...", name, regex, String.valueOf(sub)));
						dataSource = sub;
						break;
					}
				}
				if (dataSource != null) {
					zos.putNextEntry(entry);
					SimpleStreams.pipeStreams(dataSource, zos);
					zos.flush();
				}
			}
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(zos);
		}
		return false;
	}
}
