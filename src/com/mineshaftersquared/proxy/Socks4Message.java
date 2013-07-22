package com.mineshaftersquared.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * Credits to download13 of Mineshafter
 */
class Socks4Message extends SocksMessage {

	public static final int SOCKS_VERSION = 4;

	public final static int REQUEST_CONNECT = 1;
	public final static int REQUEST_BIND = 2;

	public final static int REPLY_OK = 90;
	public final static int REPLY_REJECTED = 91;
	public final static int REPLY_NO_CONNECT = 92;
	public final static int REPLY_BAD_IDENTD = 93;

	private byte[] msgBytes;
	private int msgLength;

	/**
	 * Server failed reply, cmd command for failed request
	 */
	public Socks4Message(int cmd) {
		super(cmd, null, 0);
		this.user = null;

		this.msgLength = 2;
		this.msgBytes = new byte[2];

		this.msgBytes[0] = (byte) 0;
		this.msgBytes[1] = (byte) this.command;
	}

	// Server successfull reply
	public Socks4Message(int cmd, InetAddress ip, int port) {
		this(0, cmd, ip, port, null);
	}

	public Socks4Message(int version, int cmd, InetAddress ip, int port, String user) {
		super(cmd, ip, port);
		this.user = user;
		this.version = version;

		this.msgLength = user == null ? 8 : 9 + user.length();
		this.msgBytes = new byte[this.msgLength];

		this.msgBytes[0] = (byte) version;
		this.msgBytes[1] = (byte) this.command;
		this.msgBytes[2] = (byte) (port >> 8);
		this.msgBytes[3] = (byte) port;

		byte[] addr;

		if (ip != null) {
			addr = ip.getAddress();
		} else {
			addr = new byte[4];
			addr[0] = addr[1] = addr[2] = addr[3] = 0;
		}
		System.arraycopy(addr, 0, this.msgBytes, 4, 4);

		if (user != null) {
			byte[] buf = user.getBytes();
			System.arraycopy(buf, 0, this.msgBytes, 8, buf.length);
			this.msgBytes[this.msgBytes.length - 1] = 0;
		}
	}

	public Socks4Message(InputStream in) throws IOException {
		this.msgBytes = null;
		this.read(in);
	}

	@Override
	public void read(InputStream in) throws IOException {
		DataInputStream dis = new DataInputStream(in);
		this.version = dis.readUnsignedByte();
		this.command = dis.readUnsignedByte();
		this.port = dis.readUnsignedShort();
		byte[] addr = new byte[4];
		dis.readFully(addr);
		this.ip = InetAddress.getByAddress(addr);
		this.host = this.ip.getHostName();
		int b = in.read();
		// Hope there are no idiots with user name bigger than this
		byte[] userBytes = new byte[256];
		int i = 0;
		for (i = 0; i < userBytes.length && b > 0; ++i) {
			userBytes[i] = (byte) b;
			b = in.read();
		}
		this.user = new String(userBytes, 0, i);
	}

	@Override
	public void write(OutputStream out) throws IOException {
		if (this.msgBytes == null) {
			Socks4Message msg = new Socks4Message(this.version, this.command, this.ip, this.port, this.user);
			this.msgBytes = msg.msgBytes;
			this.msgLength = msg.msgLength;
		}
		out.write(this.msgBytes);
	}

}