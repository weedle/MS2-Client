/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.swing.SimpleSwingWaiter;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.SimpleWaiter;
import com.creatifcubed.simpleapi.SimpleXMLSettings;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.game.entrypoints.UpdateEntry;
import com.mineshaftersquared.resources.JarProcessBuilder;

/**
 * 
 * @author Raidriar
 */
public class NewsTabPane extends AbstractTabPane {
	private final SimpleISettings prefs;
	public NewsTabPane(SimpleISettings prefs) {
		this.prefs = prefs;
		JTabbedPane tabs = new JTabbedPane();

		tabs.add("Mojang Minecraft", this.createMojangNews());
		tabs.add("Mineshafter Squared", this.createMS2News());
		tabs.add("Update Launcher", this.createUpdatePanel());

		this.add(tabs);
	}

	public JPanel createMojangNews() {
		JPanel panel = new JPanel(new BorderLayout());

		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
		//		browser.setBackground(Color.DARK_GRAY);
		browser.setContentType("text/html");

		browser.setText("<html><body><h1>Loading Mojang Minecraft news..</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					browser.setPage(new URL("http://mcupdate.tumblr.com/"));
				} catch (Exception ex) {
					ex.printStackTrace();
					browser.setText("<html><body><h1>Failed to fetch Minecraft news</h1><br>Error: " + ex.toString()
							+ "</body></html>");
				}
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);

		return panel;
	}

	public JPanel createMS2News() {
		JPanel panel = new JPanel(new BorderLayout());

		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
		//		browser.setBackground(Color.DARK_GRAY);
		browser.setContentType("text/html");

		browser.setText("<html><body><h1>Loading Mineshafter Squared news..</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					browser.setPage(new URL("http://mineshaftersquared.tumblr.com/mobile"));
				} catch (Exception ex) {
					ex.printStackTrace();
					browser.setText("<html><body><h1>Failed to fetch Mineshafter Squared news</h1><br>Error: " + ex.toString()
							+ "</body></html>");
				}
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);

		return panel;
	}

	public JPanel createUpdatePanel() {
		JPanel fullPanel = new JPanel(new GridLayout(0, 1));
		JPanel updatePanel = new JPanel(new GridBagLayout());
		updatePanel.setBorder(SimpleSwingUtils.createLineBorder("Update"));

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		updatePanel.add(new JLabel("In-app update! (if it doesn't work, download the latest version manually at ms2.creatifcubed.com)"), c);
		c.gridy++;
		JPanel info = new JPanel();
		info.add(new JLabel("Current version: "));
		info.add(new JLabel(UniversalLauncher.MS2_VERSION.toString()));
		info.add(Box.createHorizontalStrut(20));
		info.add(new JLabel("Latest version: "));
		JLabel latestVersion = new JLabel(this.prefs.tmpGetString("latestversion.string", "unknown"));
		this.prefs.tmpPut("latestversion.label", latestVersion);
		info.add(latestVersion);

		updatePanel.add(info, c);

		JButton update = new JButton("Update");
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "After downloading, this app will close. Wait for the 'update done' message");
				SimpleSwingWaiter waiter = new SimpleSwingWaiter("Downloading update...");
				waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
					@Override
					public Void doInBackground() {
						try {
							SimpleUtils.downloadFile(new URL("http://" + UniversalLauncher.POLLING_SERVER + "latestdownload.php?jar=yes"), UpdateEntry.DOWNLOAD_NAME, 1 << 24);
							List<String> commands = new LinkedList<String>();
							
							commands.add("java");
							commands.add("-jar");
							commands.add(UpdateEntry.DOWNLOAD_NAME);
							commands.add("update-step1");
							commands.add(SimpleUtils.getJarPath().getCanonicalPath());
							
							Process p = JarProcessBuilder.create(commands);
							if (p != null) {
								UniversalLauncher.log.info("Downloaded launcher update. Beginning update installation");
								System.exit(0);
							} else {
								JOptionPane.showMessageDialog(null, "<html>Error starting update installation.<br />Please try again or download manually at ms2.creatifcubed.com</html>");
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, "<html>Error downloading update.<br />Check the dev console or download manually at ms2.creatifcubed.com</html>");
							e.printStackTrace();
						}
						return null;
					}
				};
				waiter.run();
			}
		});

		c.gridy++;
		updatePanel.add(update, c);

		fullPanel.add(updatePanel);

		JPanel updateInfo = this.createUpdatePanelInfoSubpanel();
		updateInfo.setBorder(SimpleSwingUtils.createLineBorder("Update Info"));
		fullPanel.add(updateInfo);

		return fullPanel;
	}

	public JPanel createUpdatePanelInfoSubpanel() {
		JPanel panel = new JPanel(new BorderLayout());

		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
		//browser.setBackground(Color.DARK_GRAY);
		browser.setContentType("text/html");

		browser.addHyperlinkListener(SimpleSwingUtils.createHyperlinkListenerOpen("Unable to open link \"%s\" (reason: {%s})"));

		browser.setText("<html><body><h1>Loading update info...</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					browser.setPage(new URL("http://" + UniversalLauncher.POLLING_SERVER  + "update_info.php"));
				} catch (Exception ex) {
					ex.printStackTrace();
					browser.setText("<html><body><h1>Failed to fetch update info</h1><br>Error: " + ex.toString()
							+ "</body></html>");
				}
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);

		return panel;
	}
}
