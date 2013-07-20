package com.mineshaftersquared.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Credits to download13 of Mineshafter Modified by Adrian
 */
public class Socks5Message extends SocksMessage {
	
	public int addressType;
	public final boolean doResolveIP;

	public static final int SOCKS_VERSION = 5;
	public static final int REPLY_SUCCESS = 0;
	public static final int REPLY_FAILURE = 1;
	public static final int REPLY_BADCONNECT = 2;
	public static final int REPLY_BADNETWORK = 3;
	public static final int REPLY_HOST_UNREACHABLE = 4;
	public static final int REPLY_CONNECTION_REFUSED = 5;
	public static final int REPLY_TTL_EXPIRE = 6;
	public static final int REPLY_CMD_NOT_SUPPORTED = 7;
	public static final int REPLY_ADDR_NOT_SUPPORTED = 8;
	public static final int CMD_CONNECT = 1;
	public static final int CMD_BIND = 2;
	public static final int CMD_UDP_ASSOCIATE = 3;
	public static final int ATYP_IPV4 = 1;
	public static final int ATYP_DOMAINNAME = 3;
	public static final int ATYP_IPV6 = 4;
	public static final int SOCKS_IPV6_LENGTH = 16;

	public Socks5Message(int command, InetAddress ip, int port) {
		super(command, ip, port, SOCKS_VERSION);
		this.host = (ip == null ? "0.0.0.0" : ip.getHostName());
		this.doResolveIP = true;

		byte[] address = null;
		if (ip == null) {
			address = new byte[4];
		} else {
			address = ip.getAddress();
		}

		this.addressType = address.length == 4 ? 1 : 4;
	}

	public Socks5Message(int command, String host, int port) {
		super(command, null, port, SOCKS_VERSION);
		this.host = host;
		this.version = SOCKS_VERSION;
		this.doResolveIP = true;

		this.addressType = 3;
		byte[] address = host.getBytes(Charset.forName("utf-8"));
	}
	
	public Socks5Message(InputStream in) throws IOException {
		this.read(in);
		this.doResolveIP = true;
	}

	@Override
	public void read(InputStream in) throws IOException {
		this.ip = null;

		DataInputStream dis = new DataInputStream(in);

		this.version = dis.readUnsignedByte();
		this.command = dis.readUnsignedByte();
		dis.readUnsignedByte();
		this.addressType = dis.readUnsignedByte();

		byte[] buffer = null;
		switch (this.addressType) {
		case 1:
			buffer = new byte[4];
			break;
		case 4:
			buffer = new byte[16];
			break;
		case 3:
			buffer = new byte[dis.readUnsignedByte()];
			break;
		default:
			return; // TODO: ERROR
		}
		dis.readFully(buffer);
		this.host = InetAddress.getByAddress(buffer).getHostName();
		this.port = dis.readUnsignedShort();

		if (this.addressType != 3 && this.doResolveIP) {
			this.loadIPFromHost();
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		out.write(this.data());
	}

	@Override
	public byte[] data() {
		byte[] data = null;
		if (this.addressType == 3) {
			byte[] address = host.getBytes(Charset.forName("utf-8"));
			data = new byte[6 + address.length];
			data[0] = (byte) this.version;
			data[1] = (byte) this.command;
			data[2] = 0;
			data[3] = (byte) this.addressType;
			System.arraycopy(address, 0, data, 4, address.length);
			data[data.length - 2] = (byte) (port >>> 8);
			data[data.length - 1] = (byte) port;
		} else {
			byte[] address = null;
			if (this.ip == null) {
				address = new byte[4];
			} else {
				address = this.ip.getAddress();
			}
			data = new byte[6 + address.length];
			data[0] = (byte) this.version;
			data[1] = (byte) this.command;
			data[2] = 0;
			data[3] = (byte) this.addressType;
			System.arraycopy(address, 0, data, 4, address.length);
			data[data.length - 2] = (byte) (port >>> 8);
			data[data.length - 1] = (byte) port;
		}
		return data;
	}
	
	private void loadIPFromHost() {
		this.ip = null;
		try {
			this.ip = InetAddress.getByName(this.host);
		} catch (UnknownHostException ignore) {
			ignore.printStackTrace();
		}
	}
}
