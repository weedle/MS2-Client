package com.mineshaftersquared;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import SevenZip.Compression.LZMA.Decoder;

import com.creatifcubed.simpleapi.SimpleResources;
import com.creatifcubed.simpleapi.SimpleStreams;
import com.creatifcubed.simpleapi.swing.SimpleSwingWaiter;
import com.mineshaftersquared.misc.ExtendedGnuParser;
import com.mineshaftersquared.misc.JarUtils;
import com.mineshaftersquared.misc.MS2Utils;
import com.mineshaftersquared.proxy.MS2Proxy;
import com.mineshaftersquared.proxy.MS2ProxyHandlerFactory;

/**
 * Credits to download13 of Mineshafter
 * Modified by Adrian
 */
public class MCEntry extends JFrame implements Runnable {
	
	private static final Dimension DEFAULT_DIMENSIONS = new Dimension(854, 480);
	private static final int MC_BOOTSTRAP_VERSION = 4;
	
	public static final Options options = new Options() {{
		addOption(MS2Entry.authOfflineOption);
		addOption(MS2Entry.authserverOption);
		addOption(MS2Entry.mcSeparatorOption);
	}};
	
	private final File mcDir;
	private final File launcherJar;
	private final File packedLauncherJar;
	private final File packedLauncherJarNew;
	private final File patchedLauncherJar;
	private final String authserver;
	private final boolean offline;
	
	public MCEntry(File mcDir, String authserver, boolean offline) {
		super("Minecraft Launcher");
		this.mcDir = mcDir;
		this.launcherJar  = new File(mcDir, "launcher.jar");
		this.packedLauncherJar = new File(mcDir, "launcher.pack.lzma");
		this.packedLauncherJarNew = new File(mcDir, "launcher.pack.lzma.new");
		this.patchedLauncherJar = new File(mcDir, "ms2-launcher.jar");
		this.authserver = authserver;
		this.offline = offline;
	}
	
	public void run() {
		// Clean resources
		UniversalLauncher.log.info("Cleaning resources...");
		this.packedLauncherJarNew.delete();
		this.patchedLauncherJar.delete();
		
		// Download resources
		String md5 = null;
		if (this.packedLauncherJar.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(this.packedLauncherJar);
				md5 = MS2Utils.getMD5(fis);
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} finally {
				IOUtils.closeQuietly(fis);
			}
		}
		UniversalLauncher.log.info("launcher.pack.lzma MD5 is " + md5);
		if (!downloadMCLauncher(this.packedLauncherJarNew, md5)) {
			UniversalLauncher.log.info("Unable to download Minecraft launcher");
			return;
		}
		
		// Prepare resources
		UniversalLauncher.log.info("Preparing resources...");
		if (this.packedLauncherJarNew.exists()) {
			this.packedLauncherJar.delete();
			this.packedLauncherJarNew.renameTo(packedLauncherJar);
		}
		
		// Unpack launcher
		UniversalLauncher.log.info("Unpacking launcher...");
		if (!this.unpackLauncher()) {
			UniversalLauncher.log.info("Unable to unpack Minecraft launcher");
			return;
		}
		
		// Patch launcher
		UniversalLauncher.log.info("Patching launcher...");
		if (!this.patchLauncher()) {
			UniversalLauncher.log.info("Unable to patch Minecraft launcher");
			return;
		}
		
		// Start launcher
		UniversalLauncher.log.info("Starting launcher...");
		if (!this.startLauncher()) {
			UniversalLauncher.log.info("Unable to start Minecraft Launcher");
			return;
		}
	}
	
	public boolean unpackLauncher() {
		try {
			String path = this.packedLauncherJar.getCanonicalPath();
			File unpacked = new File(path.substring(0, path.lastIndexOf(".")));
			InputStream in = null;
			OutputStream out = null;
			JarOutputStream jos = null;
			try {
				in = new FileInputStream(this.packedLauncherJar);
				out = new FileOutputStream(unpacked);
				jos = new JarOutputStream(new FileOutputStream(this.launcherJar));
				
				byte[] properties = new byte[5];
				in.read(properties, 0, 5);
				Decoder decoder = new Decoder();
				decoder.SetDecoderProperties(properties);
				long outSize = 0;
				for (int i = 0; i < 8; i++) {
					long b = in.read();
					outSize |= b << (8 * i);
				}
				decoder.Code(in, out, outSize);
				
				Pack200.newUnpacker().unpack(unpacked, jos);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(jos);
				unpacked.delete();
			}
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean patchLauncher() {
		Map<String, InputStream> replacements = new HashMap<String, InputStream>();
		replacements.put("META-INF/.*\\.(?:DSA|RSA|SF)", null);
		replacements.put("META-INF/MANIFEST\\.MF", new ByteArrayInputStream("Manifest-Version: 1.0\n".getBytes(Charset.forName("utf-8"))));
//		replacements.put("net/minecraft/launcher/Http\\.class", SimpleResources.loadAsStream("net/minecraft/launcher/Http.class"));
//		replacements.put("net/minecraft/launcher/updater/download/Downloadable\\.class", SimpleResources.loadAsStream("net/minecraft/launcher/updater/download/Downloadable.class"));
//		replacements.put("net/minecraft/hopper/Util\\.class", SimpleResources.loadAsStream("net/minecraft/hopper/Util.class"));
		replacements.put("net/minecraft/launcher/Http\\.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/Http.class"));
		replacements.put("net/minecraft/launcher/updater/download/Downloadable\\.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/Downloadable.class"));
		replacements.put("net/minecraft/hopper/Util\\.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/Util.class"));
		replacements.put("com/mojang/authlib/HttpAuthenticationService\\.class", SimpleResources.loadAsStream("com/mineshaftersquared/resources/HttpAuthenticationService.class"));
		return JarUtils.patchJar(this.launcherJar, this.patchedLauncherJar, replacements, null);
	}
	
	public boolean startLauncher() {
		MS2Proxy ms2Proxy = new MS2Proxy(new MS2Proxy.MS2RoutesDataSource(this.authserver), new MS2ProxyHandlerFactory());
		ms2Proxy.offline = this.offline;
		Thread t = ms2Proxy.startAsync();
		
		Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(InetAddress.getLoopbackAddress(), ms2Proxy.getProxyPort()));
		
		try {
			@SuppressWarnings("resource")
			ClassLoader cl = new URLClassLoader(new URL[] { this.patchedLauncherJar.toURI().toURL() });
			Class<?> clazz = cl.loadClass("net.minecraft.launcher.Launcher");
			Constructor<?> ctor = clazz.getConstructor(new Class[] { JFrame.class, File.class, Proxy.class, PasswordAuthentication.class, String[].class, Integer.class });
			
			UniversalLauncher.log.info("Launching... ");
			ctor.newInstance(new Object[] { this, this.mcDir, proxy, null, new String[0], MC_BOOTSTRAP_VERSION });
			
			this.setSize(DEFAULT_DIMENSIONS);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args) throws ParseException {
		CommandLineParser cmdParser = new ExtendedGnuParser();
		CommandLine cmd = cmdParser.parse(options, args);
		
		File mcDir = MS2Utils.getDefaultMCDir();
		mcDir.mkdirs();
		new MCEntry(mcDir, cmd.getOptionValue("authserver", UniversalLauncher.DEFAULT_AUTH_SERVER), cmd.hasOption("offline")).run();
	}
	
	public static boolean downloadMCLauncher(final File file, final String md5) {
		UniversalLauncher.log.info("Downloading Minecraft launcher...");
		final boolean[] rt = new boolean[1];
		rt[0] = false;
		SimpleSwingWaiter waiter = new SimpleSwingWaiter("Downloading Minecraft Launcher");
		waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					URL url = new URL("http://s3.amazonaws.com/Minecraft.Download/launcher/launcher.pack.lzma");
					HttpURLConnection con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
					
					con.setUseCaches(false);
					con.setDefaultUseCaches(false);
					con.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
					con.setRequestProperty("Expires", "0");
					con.setRequestProperty("Pragma", "no-cache");
					if (md5 != null) {
						con.setRequestProperty("If-None-Match", md5.toLowerCase());
					}
					int code = con.getResponseCode();
					if (code / 100 == 2) {
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(file);
							SimpleStreams.pipeStreams(con.getInputStream(), fos);
						} finally {
							IOUtils.closeQuietly(fos);
						}
						rt[0] = true;
					}
					if (code == 304) {
						UniversalLauncher.log.info("Server MD5 matched local MD5 (no update needed)");
						rt[0] = true;
					}
					UniversalLauncher.log.info("HTTP status code was " + code);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				return null;
			}
		};
		try {
			SwingUtilities.invokeAndWait(waiter);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
		return rt[0];
	}
}
