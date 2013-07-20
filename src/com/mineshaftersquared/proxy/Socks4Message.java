package com.mineshaftersquared.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public class Socks4Message extends SocksMessage {

	public static final int SOCKS_VERSION = 4;
	public static final int REQUEST_CONNECT = 1;
	public static final int REQUEST_BIND = 2;
	public static final int REPLY_OK = 90;
	public static final int REPLY_REJECTED = 91;
	public static final int REPLY_NO_CONNECT = 92;
	public static final int REPLY_BAD_IDENTD = 93;

	public Socks4Message(int command, InetAddress ip, int port, String user, int version) {
		super(command, ip, port, version);
		this.user = user;
		this.version = version;
	}
	
	public Socks4Message(InputStream in) throws IOException {
		this.read(in);
	}

	@Override
	public void read(InputStream in) throws IOException {
		DataInputStream dis = new DataInputStream(in);
		this.version = dis.readUnsignedByte();
		this.command = dis.readUnsignedByte();
		this.port = dis.readUnsignedShort();
		byte[] address = new byte[4];
		dis.readFully(address);
		this.ip = InetAddress.getByAddress(address);
		this.host = this.ip.getHostName();
		
		byte[] userBytes = new byte[256];
		int i = 0;
		while (i < userBytes.length) {
			int b = in.read();
			if (b < 0) {
				break;
			}
			userBytes[i] = (byte) b;
			i++;
		}
		this.user = new String(userBytes, 0, i, Charset.forName("utf-8"));
	}

	@Override
	public void write(OutputStream out) throws IOException {
		out.write(this.data());
	}
	
	@Override
	public byte[] data() {
		byte[] data = new byte[this.user == null ? 8 : 9 + this.user.length()];

		data[0] = (byte) this.version;
		data[1] = (byte) this.command;
		data[2] = (byte) (this.port >>> 8);
		data[3] = (byte) this.port;

		byte[] address = null;
		if (this.ip == null) {
			address = new byte[4];
		} else {
			address = this.ip.getAddress();
		}
		
		System.arraycopy(address, 0, data, 4, 4);
		
		if (this.user != null) {
			byte[] userBytes = this.user.getBytes(Charset.forName("utf-8"));
			System.arraycopy(userBytes, 0, data, 8, userBytes.length);
			data[data.length - 1] = 0;
		}
		return data;
	}

}
