package com.mineshaftersquared.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Abstract class which describes SOCKS4/5 response/request. Credits to
 * download13 of Mineshafter
 */
public abstract class SocksMessage {
	/** Host as an IP address */
	public InetAddress ip;
	/** SOCKS version, or version of the response for SOCKS4 */
	public int version;
	/** Port field of the request/response */
	public int port;
	/** Request/response code as an int */
	public int command;
	/** Host as string. */
	public String host;
	/** User field for SOCKS4 request messages */
	public String user;

	public SocksMessage() {
		this.ip = null;
		this.version = 0;
		this.port = 0;
		this.command = 0;
		this.host = null;
		this.user = null;
	}

	public SocksMessage(int command, InetAddress ip, int port) {
		this.command = command;
		this.ip = ip;
		this.port = port;
	}

	/**
	 * Initializes Message from the stream. Reads server response from given
	 * stream.
	 * 
	 * @param in
	 *            Input stream to read response from.
	 * @throws SocksException
	 *             If server response code is not SOCKS_SUCCESS(0), or if any
	 *             error with protocol occurs.
	 * @throws IOException
	 *             If any error happens with I/O.
	 */
	public abstract void read(InputStream in) throws IOException;

	/**
	 * Writes the message to the stream.
	 * 
	 * @param out
	 *            Output stream to which message should be written.
	 */
	public abstract void write(OutputStream out) throws IOException;

	/**
	 * Get the Address field of this message as InetAddress object.
	 * 
	 * @return Host address or null, if one can't be determined.
	 */
	public InetAddress getInetAddress() throws UnknownHostException {
		return this.ip;
	}

	/**
	 * Get string representaion of this message.
	 * 
	 * @return string representation of this message.
	 */
	public String debug() {
		return "{SocksMessage: {"
				+ "Version: " + this.version
				+ ", Command: " + this.command
				+ ", IP: " + this.ip
				+ ", Port: " + this.port
				+ ", User: " + this.user
				+ "}}";
	}

}