package com.mineshaftersquared.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
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
import com.creatifcubed.simpleapi.SimpleStreams;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.updater.Downloadable;
import com.mineshaftersquared.models.version.CompleteVersion;
import com.mineshaftersquared.models.version.ExtractRules;
import com.mineshaftersquared.models.version.Library;
import com.mineshaftersquared.models.version.Version;

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

	public void downloadVersion(Version v, File baseDir) {
		try {
			CompleteVersion version = this.app.versionManager.remoteVersionList.getCompleteVersion(v.getId());
			
			Set<Downloadable> downloads = version.getRequiredDownloadables(SimpleOS.getOS(), Proxy.NO_PROXY, baseDir, true);
			String jar = "versions/" + version.getId() + "/" + version.getId() + ".jar";
			downloads.add(new Downloadable(Proxy.NO_PROXY, new URL("https://s3.amazonaws.com/Minecraft.Download/" + jar), new File(baseDir, jar), false));
			
			for (Downloadable each : downloads) {
				this.out.println("Downloading " + each.getUrl().toString() + " ...");
				each.download();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public File unpackNatives(CompleteVersion version, File baseDir) throws IOException {
		SimpleOS os = SimpleOS.getOS();
		Collection<Library> libraries = version.getRelevantLibraries();
		File natives = new File(baseDir, "versions/" + version.getId() + "/natives-" + System.nanoTime());
		for (Library each : libraries) {
			Map<SimpleOS, String> nativesPerOs = each.getNatives();
			
			if (nativesPerOs != null && nativesPerOs.get(os) != null) {
				File f = new File(baseDir, "libraries/" + each.getArtifactPath(nativesPerOs.get(os)));
				ExtractRules extractRules = each.getExtractRules();
				ZipFile zip = new ZipFile(f);
				try {
					Enumeration<? extends ZipEntry> entries = zip.entries();
					while (entries.hasMoreElements()) {
						ZipEntry entry = entries.nextElement();
						if (extractRules == null || extractRules.shouldExtract(entry.getName())) {
							File target = new File(natives, entry.getName());
							target.getParentFile().mkdirs();
							if (!entry.isDirectory()) {
								SimpleStreams.pipeStreams(zip.getInputStream(entry), new FileOutputStream(target));
							}
						}
					}
				} finally {
					zip.close();
				}
			}
		}
		return natives;
	}
}