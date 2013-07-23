package com.mineshaftersquared.models.version;

import java.io.IOException;

public class RemoteVersionList extends VersionList {
	private final Proxy proxy;

	public RemoteVersionList(Proxy proxy) {
		this.proxy = proxy;
	}

	public boolean hasAllFiles(CompleteVersion version, OperatingSystem os) {
		return true;
	}

	@Override
	protected String getUrl(String uri) throws IOException {
		return Http.performGet(new URL("https://s3.amazonaws.com/Minecraft.Download/" + uri), this.proxy);
	}

	public Proxy getProxy() {
		return this.proxy;
	}
}
