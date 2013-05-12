/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.game.entrypoints;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.creatifcubed.simpleapi.SimpleAggregateOutputStream;
import com.creatifcubed.simpleapi.swing.SimpleGUIConsole;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.JarProcessBuilder;

/**
 * 
 * @author Adrian
 */
public class BukkitEntry {
	public static void main(String[] args) {
		final SimpleGUIConsole console = new SimpleGUIConsole();
		console.setNewlinePrefix("");
		console.init();
		System.setOut(new PrintStream(new SimpleAggregateOutputStream(System.out, console.getOut())));
		System.setErr(new PrintStream(new SimpleAggregateOutputStream(System.err, console.getErr())));
		
		String jarname = "craftbukkit.jar";
		String authserver = UniversalLauncher.DEFAULT_AUTH_SERVER;

		if (args.length > 0) {
			jarname = args[0];
		}
		if (args.length > 1) {
			authserver = args[1];
		}

		MineProxy proxy = new MineProxy(authserver);
		proxy.start();

		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
		System.setProperty("java.net.preferIPv4Stack", "true");

		String[] shiftedArgs = new String[0];
		try {
			shiftedArgs = Arrays.copyOfRange(args, 2, args.length);
		} catch (Exception ignore) {
			//
		}
		final String[] finalShiftedArgs = shiftedArgs;
		final String finalJarname = jarname;

		for (String str : shiftedArgs) {
			UniversalLauncher.log.info("Shifted args: " + str);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Mineshafter Squared");
				SimpleSwingUtils.setSystemLookAndFeel();
				SimpleSwingUtils.setIcon(frame, "com/mineshaftersquared/resources/ms2.png");
				
				frame.setContentPane(console.getCompleteConsole());
				frame.setPreferredSize(new Dimension(600, 400));
				frame.pack();
				frame.setLocationRelativeTo(null);
				
				System.setIn(console.getIn());
				
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent event) {
						if (JOptionPane.showConfirmDialog(null, "<html>Are you sure you want to force exit?<br />You should use the \"stop\" command to stop the server gracefully</html>") == 0) {
							System.exit(-1);
						}
					}
				});
				
				SimpleSwingUtils.setAutoscroll(console.getOutputField(), true);
				
				frame.setVisible(true);
				
				JarProcessBuilder.wrapJar(finalJarname, finalShiftedArgs);
			}
		});
		
	}
}
