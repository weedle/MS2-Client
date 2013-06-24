package com.mineshaftersquared.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.models.MCLibrary;
import com.mineshaftersquared.models.MCVersion;

import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MCDownloader {

	public static final String LIBS_BASE_URL = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
	public static final String JAR_URL_TEMPLATE = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.jar";
	public static final String RESOURCES_URL = "https://s3.amazonaws.com/Minecraft.Resources/";
	private static final Logger log = Logger.getLogger(MCDownloader.class.getCanonicalName());

	/**
	 * Downloads:
	 * - version json file
	 * - version jar
	 * - version natives
	 * 
	 * - required libraries
	 * - resources if needed
	 * @param version
	 */
	public static void downloadVersion(MCVersion version, File base) {
		downloadBasics(version, base);
		downloadGenerics(version, base);
	}
	private static void downloadBasics(MCVersion version, File base) {
		// Download json, jar
		File parent = new File(base, "versions/" + version.versionId);
		parent.mkdirs();
		try {
			File infoFile = new File(parent, version.versionId + ".json");
			log.info("Downloading version " + version.versionId + " ... Info at " + infoFile.toURI().toURL().toString());
			SimpleUtils.filePutContents(infoFile, MCVersion.getMCVersionData(version.versionId, MCVersion.VERSION_INFO_URL_TEMPLATE, false));
			SimpleUtils.downloadFile(new URL(String.format(JAR_URL_TEMPLATE, version.versionId)), new File(parent, version.versionId + ".jar").getCanonicalPath(), 1 << 24);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	private static void downloadGenerics(MCVersion version, File base) {
		try {
			SimpleOS os = SimpleOS.getOS();
			MCLibrary[] libs = version.getLibrariesForOS(os);
			File parent = new File(base, "libraries");
			parent.mkdirs();
			for (int i = 0; i < libs.length; i++) {
				String uri = libs[i].getDownloadName(os);
				File f = new File(parent, uri);
				f.getParentFile().mkdirs();
				SimpleUtils.downloadFile(new URL(LIBS_BASE_URL + uri), f.getCanonicalPath(), 1 << 24);
			}
			unpackNatives(version, base);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		File resourcesBase = new File(base, "assets");
		if (!resourcesBase.exists()) {
			downloadResources(version, resourcesBase);
		}
	}
	private static void unpackNatives(MCVersion version, File base) {
		try {
			SimpleOS os = SimpleOS.getOS();
			MCLibrary[] libs = version.getLibrariesForOS(os);
			File parent = new File(base, "versions/" + version.versionId);
			File nativesDir = new File(parent, version.versionId + "-natives"); // add System.nanoTime()?
			nativesDir.mkdirs();
			for (int i = 0; i < libs.length; i++) {
				String nativesName = libs[i].getNatives().get(os);
				if (nativesName == null) {
					continue;
				}
				String uri = libs[i].getDownloadName(os);
				File nativesLib = new File(base, "libraries/" + uri);
				if (!nativesLib.exists()) {
					throw new FileNotFoundException(String.format("Native lib %s not found", uri));
				}
				ZipFile zip = new ZipFile(nativesLib);
				try {
					Enumeration<? extends ZipEntry> entries = zip.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (libs[i].unpackingRules.shouldExtract(entry.getName())) {
							if (entry.isDirectory()) {
								continue;
							}
							byte[] buffer = new byte[1024 * 4];
							BufferedInputStream inputStream = null;
							FileOutputStream outputStream = null;
							try {
								inputStream = new BufferedInputStream(zip.getInputStream(entry));
								outputStream = new FileOutputStream(new File(nativesDir, entry.getName()));
								while (true) {
									int length = inputStream.read(buffer, 0, buffer.length);
									if (length == -1) {
										break;
									}
									outputStream.write(buffer, 0, length);
								}
							} finally {
								SimpleUtils.closeSilently(inputStream);
								SimpleUtils.closeSilently(outputStream);
							}
						}
					}
				} finally {
					zip.close();
				}
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		/*
              File targetFile = new File(targetDir, entry.getName());
              if (targetFile.getParentFile() != null) targetFile.getParentFile().mkdirs();

              if (!entry.isDirectory()) {
                BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));

                byte[] buffer = new byte[2048];
                FileOutputStream outputStream = new FileOutputStream(targetFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                try
                {
                  int length;
                  while ((length = inputStream.read(buffer, 0, buffer.length)) != -1)
                    bufferedOutputStream.write(buffer, 0, length);
                }
                finally {
                  Downloadable.closeSilently(bufferedOutputStream);
                  Downloadable.closeSilently(outputStream);
                  Downloadable.closeSilently(inputStream);
                }
              }
            }
          }
        } finally { zip.close(); }
      }
    }
  }
		 */
	}
	private static void downloadResources(MCVersion version, File resourcesBase) {
		try {
			resourcesBase.mkdirs();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new URL(RESOURCES_URL).openStream());
			NodeList nodes = doc.getElementsByTagName("Contents");

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
					String etag = element.getElementsByTagName("ETag") != null ? element.getElementsByTagName("ETag").item(0).getChildNodes().item(0).getNodeValue() : "-";
					long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0).getNodeValue());

					if (size > 0) {
						File file = new File(resourcesBase, key);
						// check etag length
						file.getParentFile().mkdirs();
						try {
							SimpleUtils.downloadFile(new URL(RESOURCES_URL + key), file.getCanonicalPath(), 1 << 24);
							if (file.length() != size) {
								throw new IOException(String.format("Error downloading asset %s, downloaded size %d does not match expected size %d", key, file.length(), size));
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
	}
}