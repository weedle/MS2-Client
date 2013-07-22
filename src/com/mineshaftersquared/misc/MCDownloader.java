package com.mineshaftersquared.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.creatifcubed.simpleapi.SimpleAggregateOutputStream;
import com.creatifcubed.simpleapi.SimpleOS;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.MCLibrary;
import com.mineshaftersquared.models.MCVersion;
import com.mineshaftersquared.models.MCVersionManager;

public class MCDownloader {

	public static final String LIBS_BASE_URL = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
	public static final String JAR_URL_TEMPLATE = "https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.jar";
	public static final String RESOURCES_URL = "https://s3.amazonaws.com/Minecraft.Resources/";
	public final SimpleAggregateOutputStream aggregate;
	private final PrintStream out;
	private final UniversalLauncher app;

	public MCDownloader(UniversalLauncher app) {
		this.aggregate = new SimpleAggregateOutputStream(System.out);
		this.out = new PrintStream(this.aggregate);
		this.app = app;
	}

	/**
	 * Downloads: - version json file - version jar - version natives
	 * 
	 * - required libraries - resources if needed
	 * 
	 * @param version
	 */
	public boolean downloadVersion(MCVersion version, File base, String name) {
		try {
			this.out.println("There are 4 steps to a download: the version-specific files, the libraries, unpacking the natives, resources/assets");
			this.out.println("Downloading in: " + base.getCanonicalPath());
			return this.downloadBasics(version, base, name) && this.downloadGenerics(version, base, name);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean downloadBasics(MCVersion version, File base, String name) {
		// Download json, jar
		File parent = new File(base, "versions/" + name);
		parent.mkdirs();
		try {
			File infoFile = new File(parent, name + ".json");
			this.out.println("Downloading version " + version.versionId + " ... Info at "
					+ infoFile.toURI().toURL().toString());
			SimpleUtils.filePutContents(infoFile,
					this.app.versionsManager.getMCVersionData(version.versionId, MCVersionManager.VERSION_INFO_URL_TEMPLATE, false));
			this.out.println("Done downloading config file. Downloading jar...");
			SimpleUtils.downloadFile(new URL(String.format(JAR_URL_TEMPLATE, version.versionId)), new File(parent,
					version.versionId + ".jar").getCanonicalPath(), 1 << 24);
			this.out.println("Done downloading version specific files.");
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		this.out.println("Failed to download version specific files. You should delete this installation and try again.");
		return false;
	}

	public boolean downloadGenerics(MCVersion version, File base, String name) {
		try {
			this.out.println("Downloading libraries...");
			SimpleOS os = SimpleOS.getOS();
			MCLibrary[] libs = version.getLibrariesForOS(os);
			File parent = new File(base, "libraries");
			parent.mkdirs();
			for (int i = 0; i < libs.length; i++) {
				String uri = libs[i].getArtifactName(os);
				File f = new File(parent, uri);
				if (f.exists() && f.length() > 0) {
					this.out.println(String.format("Library at %s already exists, skipping", f.getCanonicalPath()));
				} else {
					this.out.println(String.format("Downloading library %s ...", libs[i].name));
					f.getParentFile().mkdirs();
					SimpleUtils.downloadFile(new URL(LIBS_BASE_URL + uri), f.getCanonicalPath(), 1 << 24);
				}
			}
			this.out.println("Done downloading libraries.");
		} catch (IOException ex) {
			ex.printStackTrace();
			this.out.println("Something went wrong downloading the libraries. You should delete this installation and try again");
			return false;
		}
		File resourcesBase = new File(base, "assets");
		return this.downloadResources(version, resourcesBase);
	}

	public File unpackNatives(MCVersion version, File base, String name) {
		try {
			this.out.println("Unpacking natives...");
			SimpleOS os = SimpleOS.getOS();
			MCLibrary[] libs = version.getLibrariesForOS(os);
			File parent = new File(base, "versions/" + name);
			File nativesDir = new File(parent, version.versionId + "-natives-" + System.nanoTime());
			nativesDir.mkdirs();
			for (int i = 0; i < libs.length; i++) {
				String nativesName = libs[i].getNatives().get(os);
				if (nativesName == null) {
					continue;
				}
				String uri = libs[i].getArtifactName(os);
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
			this.out.println("Done unpacking natives.");
			return nativesDir;
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		this.out.println("Something went wrong unpacking the natives. You should delete this installation and try again.");
		return null;
	}

	public boolean downloadResources(MCVersion version, File resourcesBase) {
		try {
			this.out.println("Downloading resources...");
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
					// String etag = element.getElementsByTagName("ETag") !=
					// null ?
					// element.getElementsByTagName("ETag").item(0).getChildNodes().item(0).getNodeValue()
					// : "-";
					long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0)
							.getNodeValue());

					if (size > 0) {
						File file = new File(resourcesBase, key);
						if (file.exists()) {
							if (file.length() == size) {
								this.out.println(String.format("Resource %s already exists and has the same size (skipping)", key));
								continue;
							} else {
								this.out.println(String.format("Resource %s exists, but does not have the same size %d. Redownloading...", key, size));
							}
						} else {
							this.out.println(String.format("Downloading resource %s ...", key));
						}
						// check etag length
						file.getParentFile().mkdirs();
						try {
							SimpleUtils.downloadFile(new URL(RESOURCES_URL + key), file.getCanonicalPath(), 1 << 24);
							if (file.length() != size) {
								throw new IOException(
										String.format(
												"Error downloading asset %s, downloaded size %d does not match expected size %d",
												key, file.length(), size));
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
			this.out.println("Done downloading resources.");
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		this.out.println("There was an error downloading resources. You should delete this installation and try again");
		return false;
	}
}