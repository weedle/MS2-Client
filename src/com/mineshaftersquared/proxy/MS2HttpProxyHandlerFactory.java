package com.mineshaftersquared.proxy;

import com.mineshaftersquared.proxy.MS2Proxy.Handler;

public class MS2HttpProxyHandlerFactory implements MS2Proxy.HandlerFactory {

	@Override
	public Handler createHandler() {
		return new MS2HttpProxyHandler();
	}

}
