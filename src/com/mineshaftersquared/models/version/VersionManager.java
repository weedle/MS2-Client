package com.mineshaftersquared.models.version;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.misc.Launcher;

import com.creatifcubed.simpleapi.SimpleOS;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.updater.Downloadable;

public class VersionManager {
	public final VersionList localVersionList;
	public final VersionList remoteVersionList;
	private final Object refreshLock = new Object();
	private boolean isRefreshing;

	public VersionManager(VersionList localVersionList, VersionList remoteVersionList) {
		this.localVersionList = localVersionList;
		this.remoteVersionList = remoteVersionList;
	}

	public void refreshVersions() throws IOException {
		synchronized (this.refreshLock) {
			this.isRefreshing = true;
		}
		try {
			this.localVersionList.refreshVersions();
			this.remoteVersionList.refreshVersions();
		} catch (IOException ex) {
			synchronized (this.refreshLock) {
				this.isRefreshing = false;
			}
			throw ex;
		}

		if ((this.localVersionList instanceof LocalVersionList)) {
			for (Version version : this.remoteVersionList.getVersions()) {
				String id = version.getId();
				if (this.localVersionList.getVersion(id) != null) {
					this.localVersionList.removeVersion(id);
					this.localVersionList.addVersion(this.remoteVersionList.getCompleteVersion(id));
					try {
						((LocalVersionList) this.localVersionList).saveVersion(this.localVersionList
								.getCompleteVersion(id));
					} catch (IOException ex) {
						synchronized (this.refreshLock) {
							this.isRefreshing = false;
						}
						throw ex;
					}
				}
			}
		}

		synchronized (this.refreshLock) {
			this.isRefreshing = false;
		}
	}

	public List<VersionSyncInfo> getVersions() {
		return this.getVersions(null);
	}

	public List<VersionSyncInfo> getVersions(VersionFilter filter) {
		synchronized (this.refreshLock) {
			if (this.isRefreshing) {
				return new ArrayList();
			}
		}

		List result = new ArrayList();
		Object lookup = new HashMap();
		Map counts = new EnumMap(ReleaseType.class);

		for (ReleaseType type : ReleaseType.values()) {
			counts.put(type, Integer.valueOf(0));
		}

		for (Version version : this.localVersionList.getVersions()) {
			if ((version.getType() != null)
					&& (version.getUpdatedTime() != null)
					&& ((filter == null) || ((filter.getTypes().contains(version.getType())) && (((Integer) counts
							.get(version.getType())).intValue() < filter.getMaxCount())))) {
				VersionSyncInfo syncInfo = this.getVersionSyncInfo(version,
						this.remoteVersionList.getVersion(version.getId()));
				((Map) lookup).put(version.getId(), syncInfo);
				result.add(syncInfo);
			}
		}
		for (Version version : this.remoteVersionList.getVersions()) {
			if ((version.getType() != null)
					&& (version.getUpdatedTime() != null)
					&& (!((Map) lookup).containsKey(version.getId()))
					&& ((filter == null) || ((filter.getTypes().contains(version.getType())) && (((Integer) counts
							.get(version.getType())).intValue() < filter.getMaxCount())))) {
				VersionSyncInfo syncInfo = this.getVersionSyncInfo(this.localVersionList.getVersion(version.getId()),
						version);
				((Map) lookup).put(version.getId(), syncInfo);
				result.add(syncInfo);

				if (filter != null) {
					counts.put(version.getType(),
							Integer.valueOf(((Integer) counts.get(version.getType())).intValue() + 1));
				}
			}
		}
		if (result.isEmpty()) {
			for (Version version : this.localVersionList.getVersions()) {
				if ((version.getType() != null) && (version.getUpdatedTime() != null)) {
					VersionSyncInfo syncInfo = this.getVersionSyncInfo(version,
							this.remoteVersionList.getVersion(version.getId()));
					((Map) lookup).put(version.getId(), syncInfo);
					result.add(syncInfo);
				}
			}
		}

		Collections.sort(result, new Comparator<VersionSyncInfo>() {
			public int compare(VersionSyncInfo a, VersionSyncInfo b) {
				Version aVer = a.getLatestVersion();
				Version bVer = b.getLatestVersion();

				if ((aVer.getReleaseTime() != null) && (bVer.getReleaseTime() != null)) {
					return bVer.getReleaseTime().compareTo(aVer.getReleaseTime());
				}
				return bVer.getUpdatedTime().compareTo(aVer.getUpdatedTime());
			}
		});
		return result;
	}

	public VersionSyncInfo getVersionSyncInfo(Version version) {
		return this.getVersionSyncInfo(version.getId());
	}

	public VersionSyncInfo getVersionSyncInfo(String name) {
		return this.getVersionSyncInfo(this.localVersionList.getVersion(name), this.remoteVersionList.getVersion(name));
	}

	public VersionSyncInfo getVersionSyncInfo(Version localVersion, Version remoteVersion) {
		boolean installed = localVersion != null;
		boolean upToDate = installed;

		if ((installed) && (remoteVersion != null)) {
			upToDate = !remoteVersion.getUpdatedTime().after(localVersion.getUpdatedTime());
		}
		if ((localVersion instanceof CompleteVersion)) {
			upToDate &= this.localVersionList.hasAllFiles((CompleteVersion) localVersion,
					SimpleOS.getOS());
		}

		return new VersionSyncInfo(localVersion, remoteVersion, installed, upToDate);
	}

	public List<VersionSyncInfo> getInstalledVersions() {
		List result = new ArrayList();

		for (Version version : this.localVersionList.getVersions()) {
			if ((version.getType() != null) && (version.getUpdatedTime() != null)) {
				VersionSyncInfo syncInfo = this.getVersionSyncInfo(version,
						this.remoteVersionList.getVersion(version.getId()));
				result.add(syncInfo);
			}
		}
		return result;
	}

	public VersionList getRemoteVersionList() {
		return this.remoteVersionList;
	}

	public VersionList getLocalVersionList() {
		return this.localVersionList;
	}

	public CompleteVersion getLatestCompleteVersion(VersionSyncInfo syncInfo) throws IOException {
		if (syncInfo.getLatestSource() == VersionSyncInfo.VersionSource.REMOTE) {
			CompleteVersion result = null;
			IOException exception = null;
			try {
				result = this.remoteVersionList.getCompleteVersion(syncInfo.getLatestVersion());
			} catch (IOException e) {
				exception = e;
				try {
					result = this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
				} catch (IOException localIOException1) {
				}
			}
			if (result != null) {
				return result;
			}
			throw exception;
		}

		return this.localVersionList.getCompleteVersion(syncInfo.getLatestVersion());
	}

	public Set<Downloadable> downloadVersion(VersionSyncInfo syncInfo, Set<Downloadable> downloads) throws IOException {
		if (!(this.localVersionList instanceof LocalVersionList)) {
			throw new IllegalArgumentException("Cannot download if local repo isn't a LocalVersionList");
		}
		if (!(this.remoteVersionList instanceof RemoteVersionList)) {
			throw new IllegalArgumentException("Cannot download if local repo isn't a RemoteVersionList");
		}
		CompleteVersion version = this.getLatestCompleteVersion(syncInfo);
		File baseDirectory = ((LocalVersionList) this.localVersionList).getBaseDirectory();
		Proxy proxy = ((RemoteVersionList) this.remoteVersionList).getProxy();

		downloads.addAll(version.getRequiredDownloadables(SimpleOS.getOS(), proxy,
				baseDirectory, false));

		String jarFile = "versions/" + version.getId() + "/" + version.getId() + ".jar";
		downloads.add(new Downloadable(proxy, new URL(
				"https://s3.amazonaws.com/Minecraft.Download/" + jarFile), new File(baseDirectory, jarFile), false));

		return downloads;
	}

	public Set<Downloadable> downloadResources(Set<Downloadable> downloads) throws IOException {
		File baseDirectory = ((LocalVersionList) this.localVersionList).getBaseDirectory();

		downloads.addAll(this.getResourceFiles(((RemoteVersionList) this.remoteVersionList).getProxy(),
				baseDirectory));

		return downloads;
	}

	private Set<Downloadable> getResourceFiles(Proxy proxy, File baseDirectory) {
		Set result = new HashSet();
		try {
			URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(resourceUrl.openStream());
			NodeList nodeLst = doc.getElementsByTagName("Contents");

			long start = System.nanoTime();
			for (int i = 0; i < nodeLst.getLength(); i++) {
				Node node = nodeLst.item(i);

				if (node.getNodeType() == 1) {
					Element element = (Element) node;
					String key = element.getElementsByTagName("Key").item(0).getChildNodes().item(0).getNodeValue();
					String etag = element.getElementsByTagName("ETag") != null ? element.getElementsByTagName("ETag")
							.item(0).getChildNodes().item(0).getNodeValue() : "-";
					long size = Long.parseLong(element.getElementsByTagName("Size").item(0).getChildNodes().item(0)
							.getNodeValue());

					if (size > 0L) {
						File file = new File(baseDirectory, "assets/" + key);
						if (etag.length() > 1) {
							etag = Downloadable.getEtag(etag);
							if ((file.isFile()) && (file.length() == size)) {
								String localMd5 = Downloadable.getMD5(file);
								if (localMd5.equals(etag)) {
									continue;
								}
							}
						}
						Downloadable downloadable = new Downloadable(proxy, new URL(
								"https://s3.amazonaws.com/Minecraft.Resources/" + key), file, false);
						downloadable.setExpectedSize(size);
						result.add(downloadable);
					}
				}
			}
			long end = System.nanoTime();
			long delta = end - start;
		} catch (Exception ex) {
			ex.printStackTrace();
			UniversalLauncher.log.info("Couldn't download resources");
		}

		return result;
	}

}