package com.mineshaftersquared.models.version;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleOS;

public class RemoteVersionList extends VersionList {
	private final Proxy proxy;

	public RemoteVersionList(Proxy proxy) {
		this.proxy = proxy;
	}

	public boolean hasAllFiles(CompleteVersion version, SimpleOS os) {
		return true;
	}

	@Override
	protected String getUrl(String uri) throws IOException {
		return new String(new SimpleHTTPRequest("https://s3.amazonaws.com/Minecraft.Download/" + uri).doGet(this.proxy));
	}

	public Proxy getProxy() {
		return this.proxy;
	}
}
