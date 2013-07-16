package com.mineshaftersquared.proxy;

import java.net.InetAddress;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public abstract class SocksMessage {
	
	public final int command;
	public final InetAddress ip;
	public final int port;
	
	public int version;
	public String host;
	public String user;
	
	public SocksMessage(int command, InetAddress ip, int port) {
		this.command = command;
		this.ip = ip;
		this.port = port;
	}
}
