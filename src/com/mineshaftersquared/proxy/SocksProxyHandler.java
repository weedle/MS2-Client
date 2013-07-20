package com.mineshaftersquared.proxy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.io.IOUtils;

import com.creatifcubed.simpleapi.SimpleStreams;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public class SocksProxyHandler implements MS2Proxy.Handler {
	
	public static final int SOCKET_TIMEOUT = 1000 * 60 * 5; // 5 minutes
	public static final int BUFFER_SIZE = 1 << 16 - 1;
	
	private final Delegate delegate;
	
	public SocksProxyHandler(Delegate delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void handle(MS2Proxy ms2Proxy, Socket socket) {
		InputStream in = null;
		OutputStream out = null;
		try {
			socket.setSoTimeout(SOCKET_TIMEOUT);
			in = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
			out = socket.getOutputStream();
			
			in.mark(BUFFER_SIZE);
			int version = in.read();
			if (version == 5) {
				int numMethods = in.read();
				for (int i = 0; i < numMethods; i++) {
					if (in.read() == 0) {
						out.write(new byte[] { 5 });
					}
				}
			} else if (version == 4) {
				in.reset();
			} else {
				
			}
			SocksMessage msg = this.readMessage(in);
			this.handleRequest(ms2Proxy, msg, in, out);
			out.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(socket);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
	
	private void handleRequest(MS2Proxy ms2Proxy, SocksMessage msg, InputStream in, OutputStream out) throws IOException {
		if (msg.ip == null) {
			if (msg instanceof Socks5Message) {
				msg.ip = InetAddress.getByName(msg.host);
			} else {
				return; // TODO: look up
			}
		}
		switch (msg.command) {
		case 1:
			onConnect(ms2Proxy, msg, in, out);
			break;
		case 2:
			onBind(msg, in, out);
			break;
		case 3:
			// TODO: ERROR
			break;
		}
	}
	
	private void onConnect(MS2Proxy proxy, SocksMessage msg, InputStream in, OutputStream out) throws IOException {
		SocksMessage response = null;
		if (msg instanceof Socks5Message) {
			response = new Socks5Message(0, InetAddress.getLoopbackAddress(), 0);
		} else {
			response = new Socks4Message(90, InetAddress.getLocalHost(), 0, null, 0);
		}
		response.write(out);
		
		if (!this.delegate.onConnect(proxy, msg, in, out)) {
			Socket socket = new Socket(msg.ip, msg.port);
			SimpleStreams.pipeStreamsConcurrently(in, out);
			SimpleStreams.pipeStreams(socket.getInputStream(), out);
		}
	}
	
	private void onBind(SocksMessage msg, InputStream in, OutputStream out) throws IOException {
		ServerSocket server = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
		server.setSoTimeout(SOCKET_TIMEOUT);
		SocksMessage response;
		if (msg.version == 5) {
			response = new Socks5Message(0, server.getInetAddress(), server.getLocalPort());
		} else {
			response = new Socks4Message(90, server.getInetAddress(), server.getLocalPort(), null, 0);
		}
		response.write(out);
		Socket s = null;
		while (true) {
			s = null;
			try {
				s = server.accept();
				if (s.getInetAddress().equals(msg.ip)) {
					server.close();
					break;
				}
			} catch (SocketTimeoutException ex) {
				server.close();
				break;
			} finally {
				IOUtils.closeQuietly(s);
			}
		}
		if (s != null) {
			SimpleStreams.pipeStreamsConcurrently(in, s.getOutputStream());
			SimpleStreams.pipeStreams(s.getInputStream(), out);
		}
	}
	
	private SocksMessage readMessage(InputStream in) throws IOException {
		in.mark(5);
		int version = in.read();
		in.reset();
		SocksMessage msg = null;
		if (version == 5) {
			msg = new Socks5Message(in);
		} else if (version == 4) {
			msg = new Socks4Message(in);
		} else {
			throw new IOException("Invalid SOCKS version: " + version);
		}
		return msg;
	}
	
	public static interface Delegate {
		public boolean onConnect(MS2Proxy proxy, SocksMessage msg, InputStream in, OutputStream out);
	}
}
