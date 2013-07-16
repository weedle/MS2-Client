package com.mineshaftersquared.proxy;

import java.net.InetAddress;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public class Socks4Message extends SocksMessage {

	private final byte[] msgBytes;

	public static final int SOCKS_VERSION = 4;
	public static final int REQUEST_CONNECT = 1;
	public static final int REQUEST_BIND = 2;
	public static final int REPLY_OK = 90;
	public static final int REPLY_REJECTED = 91;
	public static final int REPLY_NO_CONNECT = 92;
	public static final int REPLY_BAD_IDENTD = 93;

	public Socks4Message(int command, InetAddress ip, int port, String user, int version) {
		super(command, ip, port);
		this.user = user;
		this.version = version;
		
		this.msgBytes = new byte[user == null ? 8 : 9 + user.length()];
		
		this.msgBytes[0] = (byte) version;
		this.msgBytes[1] = (byte) this.command;
		this.msgBytes[2] = (byte) (port >>> 8);
		this.msgBytes[3] = (byte) port;
		
		byte[] addressBytes = null;
		if (ip == null) {
			addressBytes = new byte[4];
		} else {
			addressBytes = ip.getAddress();
		}
	}

}
