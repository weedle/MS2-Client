package com.mineshaftersquared;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import com.creatifcubed.simpleapi.SimpleAggregateOutputStream;
import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.swing.SimpleGUIConsole;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.misc.ExtendedGnuParser;
import com.mineshaftersquared.misc.MS2Utils;
import com.mineshaftersquared.proxy.MS2HttpProxyHandlerFactory;
import com.mineshaftersquared.proxy.MS2Proxy;
import com.mineshaftersquared.proxy.MS2ProxyHandlerFactory;

public class ServerEntry {
	
	public static final Dimension SERVER_CONSOLE_DIMENSIONS = new Dimension(854, 480);

	public static final Options options = new Options() {{
		addOption(MS2Entry.serverOption);
		addOption(MS2Entry.bukkitOption);
		addOption(MS2Entry.authserverOption);
		addOption(MS2Entry.mcSeparatorOption);
	}};

	public static void main(String[] args) throws ParseException {
		CommandLineParser cmdParser = new ExtendedGnuParser();
		CommandLine cmd = cmdParser.parse(options, args);

		String server = cmd.getOptionValue("server");
		boolean isBukkit = cmd.hasOption("bukkit");
		String authserver = cmd.getOptionValue("authserver", UniversalLauncher.DEFAULT_AUTH_SERVER);

		List<String> mcArgs = new LinkedList<String>();
		boolean mcArgsFlag = false;
		for (String each : args) {
			if (mcArgsFlag) {
				mcArgs.add(each);
			} else {
				if (each.equals("-mc")) {
					mcArgsFlag = true;
				}
			}
		}
		
		if (isBukkit) {
			final SimpleGUIConsole console = new SimpleGUIConsole();
			console.init();
			System.setOut(new PrintStream(new SimpleAggregateOutputStream(System.out, console.getOut())));
			System.setErr(new PrintStream(new SimpleAggregateOutputStream(System.err, console.getErr())));
			System.setIn(console.getIn());
			SimpleSwingUtils.setAutoscroll(console.getOutputField(), true);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					final JFrame frame = new JFrame("MS2 Server Console");
					frame.setContentPane(console.getCompleteConsole());
					frame.setPreferredSize(SERVER_CONSOLE_DIMENSIONS);
					frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent event) {
							if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to force quit?") == JOptionPane.YES_OPTION) {
								System.exit(0);
							}
						}
					});
					frame.pack();
					frame.setVisible(true);
				}
			});
			mcArgs.add("--nojline");
		}
		launchServer(server, authserver, mcArgs.toArray(new String[mcArgs.size()]));
	}
	
	private static void launchServer(String server, String authserver, String[] mcArgs) {
		MS2Proxy ms2Proxy = new MS2Proxy(new MS2Proxy.MS2RoutesDataSource(authserver), new MS2HttpProxyHandlerFactory());
		ms2Proxy.startAsync();

		System.setProperty("http.proxyHost", InetAddress.getLoopbackAddress().getHostAddress());
		System.setProperty("http.proxyPort", "" + ms2Proxy.getProxyPort());
		
//		System.out.println(new String(new SimpleHTTPRequest("http://ms2.creatifcubed.com/polling_scripts/test.php?a=b").doGet()));
//		if (true) return;
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getLoopbackAddress(), ms2Proxy.getProxyPort()));
		
		try {
			Attributes attributes = null;
			String mainClassName = null;
			JarFile jar = new JarFile(server);
			Class<?> mainClazz = null;
			String serverClazzName = MS2Utils.getBukkitMinecraftServerClass(jar);
			if (serverClazzName == null) {
				UniversalLauncher.log.info("Unable to get Minecraft Server class!");
			} else {
				UniversalLauncher.log.info("Found Minecraft Server class " + serverClazzName);
			}
			URLClassLoader cl = new URLClassLoader(new URL[] { new File(server).toURI().toURL() });
			try {
				attributes = jar.getManifest().getMainAttributes();
				mainClassName = attributes.getValue("Main-Class");
			} finally {
				IOUtils.closeQuietly(jar);
			}
			mainClazz = cl.loadClass(mainClassName);
			
			UniversalLauncher.log.info("Starting class " + mainClassName + " ... Passing args " + Arrays.asList(mcArgs) + " ...");
			Method main = mainClazz.getDeclaredMethod("main", new Class[] { String[].class });
			main.invoke(mainClazz, new Object[] { mcArgs });
			
			if (serverClazzName != null) {
				Class<?> serverClazz = cl.loadClass(serverClazzName);
				
				boolean foundProxy = false;
				outerloop:
				for (Field each : serverClazz.getDeclaredFields()) {
					UniversalLauncher.log.info("Found class field " + each.getName() + ", is type " + each.getType().getName());
					if (serverClazz.isAssignableFrom(each.getType())) {
						each.setAccessible(true);
						Object instance = each.get(serverClazz);
						UniversalLauncher.log.info("Found instance");
						for (Field property : each.getType().getDeclaredFields()) {
							UniversalLauncher.log.info("Found object field " + property.getName() + ", is type " + property.getType().getName());
							if (Proxy.class.isAssignableFrom(property.getType())) {
								property.setAccessible(true);
								property.set(instance, proxy);
								foundProxy = true;
								break outerloop;
							}
						}
					}
				}
				if (foundProxy) {
					UniversalLauncher.log.info("Found proxy field");
				} else {
					UniversalLauncher.log.info("Unable to find proxy field!");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
	}
}
