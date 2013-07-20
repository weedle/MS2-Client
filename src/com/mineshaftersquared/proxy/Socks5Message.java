package com.mineshaftersquared.proxy;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Credits to download13 of Mineshafter
 */
class Socks5Message extends SocksMessage {
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

	public static final int CMD_CONNECT = 0x1;
	public static final int CMD_BIND = 0x2;
	public static final int CMD_UDP_ASSOCIATE = 0x3;

	public static final int ATYP_IPV4 = 0x1; // Where is 2??
	public static final int ATYP_DOMAINNAME = 0x3; // !!!!rfc1928
	public static final int ATYP_IPV6 = 0x4;

	public static final int SOCKS_IPV6_LENGTH = 16;
	
	public int addrType;

	private byte[] data;
	private boolean doResolveIP = true;

	public Socks5Message(int cmd, InetAddress ip, int port) {
		super(cmd, ip, port);
		this.host = ip == null ? "0.0.0.0" : ip.getHostName();
		this.version = SOCKS_VERSION;

		byte[] addr;

		if (ip == null) {
			addr = new byte[4];
		} else {
			addr = ip.getAddress();
		}

		this.addrType = addr.length == 4 ? ATYP_IPV4 : ATYP_IPV6;

		this.data = new byte[6 + addr.length];
		this.data[0] = (byte) SOCKS_VERSION; // Version
		this.data[1] = (byte) this.command; // Command
		this.data[2] = (byte) 0; // Reserved byte
		this.data[3] = (byte) this.addrType; // Address type

		// Put Address
		System.arraycopy(addr, 0, this.data, 4, addr.length);
		// Put port
		this.data[this.data.length - 2] = (byte) (port >> 8);
		this.data[this.data.length - 1] = (byte) (port);
	}

	public Socks5Message(int cmd, String hostName, int port) {
		super(cmd, null, port);
		this.host = hostName;
		this.version = SOCKS_VERSION;

		// System.out.println("Doing ATYP_DOMAINNAME");

		this.addrType = ATYP_DOMAINNAME;
		byte addr[] = hostName.getBytes();

		this.data = new byte[7 + addr.length];
		this.data[0] = (byte) SOCKS_VERSION; // Version
		this.data[1] = (byte) this.command; // Command
		this.data[2] = (byte) 0; // Reserved byte
		this.data[3] = (byte) ATYP_DOMAINNAME; // Address type
		this.data[4] = (byte) addr.length; // Length of the address

		// Put Address
		System.arraycopy(addr, 0, this.data, 5, addr.length);
		// Put port
		this.data[this.data.length - 2] = (byte) (port >> 8);
		this.data[this.data.length - 1] = (byte) (port);
	}

	public Socks5Message(InputStream in) throws IOException {
		this.read(in);
	}

	@Override
	public void read(InputStream in) throws IOException {
		this.data = null;
		this.ip = null;

		DataInputStream dis = new DataInputStream(in);

		this.version = dis.readUnsignedByte();
		this.command = dis.readUnsignedByte();
		dis.readUnsignedByte(); // Reserved
		this.addrType = dis.readUnsignedByte();

		byte addr[];

		switch (this.addrType) {
		case ATYP_IPV4:
			addr = new byte[4];
			dis.readFully(addr);
			this.host = InetAddress.getByAddress(addr).getHostName();
			break;
		case ATYP_IPV6:
			addr = new byte[SOCKS_IPV6_LENGTH];
			dis.readFully(addr);
			this.host = InetAddress.getByAddress(addr).getHostAddress();
			break;
		case ATYP_DOMAINNAME:
			addr = new byte[dis.readUnsignedByte()];
			dis.readFully(addr);
			this.host = new String(addr);
			break;
		default:
			return;
		}

		this.port = dis.readUnsignedShort();

		if (this.addrType != ATYP_DOMAINNAME && this.doResolveIP) {
			try {
				this.ip = InetAddress.getByName(this.host);
			} catch (UnknownHostException uh_ex) {
			}
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		if (this.data == null) {
			Socks5Message msg;

			if (this.addrType == ATYP_DOMAINNAME) {
				msg = new Socks5Message(this.command, this.host, this.port);
			} else {
				if (this.ip == null) {
					try {
						this.ip = InetAddress.getByName(this.host);
					} catch (UnknownHostException e) {
						return; // XXX What do here?
					}
				}
				msg = new Socks5Message(this.command, this.ip, this.port);
			}
			this.data = msg.data;
		}
		out.write(this.data);
	}

	@Override
	public InetAddress getInetAddress() throws UnknownHostException {
		if (this.ip != null) {
			return this.ip;
		}
		return (this.ip = InetAddress.getByName(this.host));
	}
}