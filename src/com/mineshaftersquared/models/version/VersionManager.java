package com.mineshaftersquared.models.version;

import java.io.File;
import java.net.Proxy;

public class VersionManager {
	
	public final LocalVersionList localVersionList;
	public final RemoteVersionList remoteVersionList;
	
	public VersionManager(File localBase) {
		this.localVersionList = new LocalVersionList(localBase);
		this.remoteVersionList = new RemoteVersionList(Proxy.NO_PROXY);
	}
}
