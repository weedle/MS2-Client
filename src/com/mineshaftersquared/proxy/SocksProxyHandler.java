package com.mineshaftersquared.proxy;

import java.net.Socket;

public class SocksProxyHandler extends MS2Proxy.Handler {

	public SocksProxyHandler(MS2Proxy ms2Proxy) {
		super(ms2Proxy);
	}

	@Override
	public void handle(Socket socket) {
		return;
	}
	
}
