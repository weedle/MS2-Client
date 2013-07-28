package com.mineshaftersquared.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.mineshaftersquared.misc.MS2Utils;

public class MS2ProxyHandler implements MS2Proxy.Handler, SocksProxyHandler.Delegate {
	
	private final MS2HttpProxyHandler httpHandler;
	private final SocksProxyHandler socksHandler;
	
	public MS2ProxyHandler() {
		this.httpHandler = new MS2HttpProxyHandler();
		this.socksHandler = new SocksProxyHandler(this);
	}
	
	@Override
	public void handle(MS2Proxy ms2Proxy, Socket socket) {
		this.socksHandler.handle(ms2Proxy, socket);
	}
	
	public boolean onConnect(MS2Proxy ms2Proxy, SocksMessage msg, InputStream in, OutputStream out) {
		in.mark(65535);
		// Can't use buffered reader/input stream reader because it reads ahead
		String firstLine = MS2Utils.readUntil(in, "\n");
		String[] request = firstLine.split(" ");
		if (request.length != 3) {
			try {
				in.reset();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return false;
		}
		String method = request[0].toLowerCase();
		String path = request[1];
		
		Map<String, String> headers = new HashMap<String, String>();
		while (true) {
			String line = MS2Utils.readUntil(in, "\n").trim();
			if (line.isEmpty()) {
				break;
			}
			String[] keyValue = line.split(":");
			if (keyValue.length >= 2) {
				headers.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim().toLowerCase());
			}
		}
		String url = "http://" + headers.get("host") + path;
		MS2Proxy.log.info("Proxy - onConnect - " + url);
		if (this.httpHandler.respondsTo(method) && this.httpHandler.on(method, url, headers, in, out, ms2Proxy)) {
			return true;
		}
		try {
			in.reset();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
