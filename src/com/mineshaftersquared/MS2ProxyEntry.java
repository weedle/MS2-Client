package com.mineshaftersquared;

import com.mineshaftersquared.proxy.MS2Proxy;
import com.mineshaftersquared.proxy.MS2ProxyHandlerFactory;

public class MS2ProxyEntry {

	public static void main(String[] args) {
		MS2Proxy proxy = new MS2Proxy("http://mineshaftersquared.com", new MS2ProxyHandlerFactory());
		proxy.startAsync();
	}
}
