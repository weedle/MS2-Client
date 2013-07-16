package com.mineshaftersquared.proxy;

import com.mineshaftersquared.proxy.MS2Proxy.Handler;

public class MS2ProxyHandlerFactory implements MS2Proxy.HandlerFactory {

	@Override
	public Handler createHandler() {
		return new MS2ProxyHandler();
	}

}
