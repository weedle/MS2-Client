package com.mineshaftersquared;

import com.creatifcubed.simpleapi.SimpleConsole;
import com.mineshaftersquared.proxy.MS2Proxy;
import com.mineshaftersquared.proxy.MS2ProxyHandlerFactory;

public class MS2ProxyEntry {

	public static void main(String[] args) {
		MS2Proxy proxy = new MS2Proxy(new MS2Proxy.MS2RoutesDataSource("http://api.mineshaftersquared.com"), new MS2ProxyHandlerFactory());
		Thread t = proxy.startAsync();
		System.out.println("Type quit to exit");
		while (true) {
			String line = SimpleConsole.readLine();
			if (line.equals("quit")) {
				break;
			} else {
				System.out.println("Type quit to exit");
			}
		}
		System.out.println("MS2Proxy done.");
	}
}
