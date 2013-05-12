/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.swing.SimpleLinkableLabel;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.resources.JarProcessBuilder;
import com.mineshaftersquared.resources.ProcessOutputRedirector;
import com.mineshaftersquared.resources.Utils;

/**
 * 
 * @author Adrian
 */
public class ServerAdminsTabPane extends AbstractTabPane {

	private SimpleISettings prefs;

	public ServerAdminsTabPane(SimpleISettings prefs) {
		this.prefs = prefs;
		this.add(this.createInfoPanel());
		this.add(this.createVanillaPanel());
		// this.add(this.createFeedtheBeastPanel());
		this.add(this.createBukkit());
	}

	private JPanel createInfoPanel() {
		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBorder(SimpleSwingUtils.createLineBorder("Info"));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		infoPanel.add(new SimpleLinkableLabel("<html><ul>" + "<li>Use the RAM config on the Settings tab</li>"
				+ "<li>Put Mineshafter Squared in your servers folder (server fold is MS2 folder)</li>"
				+ "<li><a href=\"http://ms2.creatifcubed.com/server_admins.php\">Read about special server admin options at ms2.creatifcubed.com</a></li>"
				+ "<li>You can also find server-specific instructions at <a href=\"http://ms2.creatifcubed.com\">ms2.creatifcubed.com</a></li>"
				+ "<li>Example: Instructions for Bukkit servers are under the <a href=\"http://ms2.creatifcubed.com/bukkit.php\">Bukkit page</a></li>"
				+ "</ul></html>"), c);

		JButton openLocal = new JButton("Open servers folder");
		openLocal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = Utils.getMCPath(Utils.PATH_LOCAL);
				String fileExplorer = SimpleUtils.getOSFileExplorer();
				if (fileExplorer == null) {
					fileExplorer = JOptionPane.showInputDialog("Could not find file browser."
							+ "\nSpecify your file browser, or manually go to " + path, "xterm");
				}
				if (fileExplorer != null) {
					SimpleUtils.openFolder(fileExplorer, path);
				}
			}
		});

		c.gridy = 1;
		c.insets = new Insets(0, 30, 0, 0);
		infoPanel.add(openLocal, c);

		return infoPanel;
	}

	private JPanel createVanillaPanel() {
		JPanel vanillaPanel = new JPanel(new GridBagLayout());
		vanillaPanel.setBorder(SimpleSwingUtils.createLineBorder("Servers with GUIs (Vanilla, Forge, FTB, Voltz)"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;

		final JTextField serverNameField = new JTextField(20);
		serverNameField.setText(this.prefs.getString("serveradmins.vanilla.name", "minecraft_server"));

		final JCheckBox remember = new JCheckBox("Remember?", true);

		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerAdminsTabPane.this.showMS2AlphaPrompt();
				String serverName = serverNameField.getText().trim();
				if (serverName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Invalid file name");
					return;
				}
				if (!(new File(serverName + ".jar").exists())) {
					JOptionPane.showMessageDialog(null, "File {" + serverName + "} not found in local folder");
					return;
				}
				if (remember.isSelected()) {
					ServerAdminsTabPane.this.prefs.put("serveradmins.vanilla.name", serverName);
					ServerAdminsTabPane.this.prefs.save();
				}
				serverName += ".jar";

				int minRam = ServerAdminsTabPane.this.prefs.getInt("runtime.ram.min", 0);
				int maxRam = ServerAdminsTabPane.this.prefs.getInt("runtime.ram.max", 0);
				try {
					Process p = JarProcessBuilder.create(
							SimpleUtils.getJarPath().getCanonicalPath(),
							minRam,
							maxRam,
							null,
							new String[] {
								"server-vanilla",
								serverName,
								ServerAdminsTabPane.this.prefs.getString("proxy.authserver",
										UniversalLauncher.DEFAULT_AUTH_SERVER) });

					new Thread(new ProcessOutputRedirector(p, "[MS2-Server]: %s")).start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		vanillaPanel
		.add(AbstractTabPane.createGenericLaunchPanel("Server Jar name", serverNameField, remember, launch));

		return vanillaPanel;
	}

	private JPanel createFeedtheBeastPanel() {
		JPanel ftbPanel = new JPanel();
		ftbPanel.setBorder(SimpleSwingUtils.createLineBorder("Feed the Beast"));

		return ftbPanel;
	}

	private JPanel createBukkit() {
		JPanel bukkitPanel = new JPanel(new GridBagLayout());
		bukkitPanel.setBorder(SimpleSwingUtils
				.createLineBorder("Command line servers (Bukkit and most derivatives, MCPC, Spoutcraft)"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;

		final JTextField serverNameField = new JTextField(20);
		serverNameField.setText(this.prefs.getString("serveradmins.bukkit.name", "craftbukkit"));

		final JCheckBox remember = new JCheckBox("Remember?", true);

		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerAdminsTabPane.this.showMS2AlphaPrompt();
				String serverName = serverNameField.getText().trim();
				if (serverName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Invalid file name");
					return;
				}
				if (!(new File(serverName + ".jar").exists())) {
					JOptionPane.showMessageDialog(null, "File {" + serverName + "} not found in local folder");
					return;
				}

				if (remember.isSelected()) {
					ServerAdminsTabPane.this.prefs.put("serveradmins.bukkit.name", serverName);
					ServerAdminsTabPane.this.prefs.save();
				}
				serverName += ".jar";

				int minRam = ServerAdminsTabPane.this.prefs.getInt("runtime.ram.min", 0);
				int maxRam = ServerAdminsTabPane.this.prefs.getInt("runtime.ram.max", 0);
				try {
					List<String> commands = JarProcessBuilder.getCommand(
							SimpleUtils.getJarPath().getCanonicalPath(),
							minRam,
							maxRam,
							null,
							new String[] {
								"server-bukkit",
								serverName,
								ServerAdminsTabPane.this.prefs.getString("proxy.authserver",
										UniversalLauncher.DEFAULT_AUTH_SERVER), });
					Process p = JarProcessBuilder.create(commands);
					new Thread(new ProcessOutputRedirector(p, "[MS2-Bukkit]: %s")).start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		bukkitPanel.add(AbstractTabPane.createGenericLaunchPanel("Server Jar name", serverNameField, remember, launch));

		return bukkitPanel;
	}

	private void showMS2AlphaPrompt() {
		if (!this.prefs.getBool("messages.serverswitchalphaproxy", false)) {
			JCheckBox dontShowAgain = new JCheckBox("Don't show again");
			if (!this.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER).equals(
					UniversalLauncher.BETA_AUTH_SERVER)) {
				int result = JOptionPane.showConfirmDialog(null,
						new Object[] {
						"It looks like you're using the regular proxy"
								+ "\nDo you want to switch to the alpha server so new MS2 users can play?",
								dontShowAgain }, "Switch to ms2auth.creatifcubed.com for new users!",
								JOptionPane.YES_NO_OPTION);
				if (result != -1 && dontShowAgain.isSelected()) {
					this.prefs.put("messages.serverswitchalphaproxy", true);
					this.prefs.save();
				}
				if (result == 0) {
					this.prefs.put("proxy.authserver", UniversalLauncher.BETA_AUTH_SERVER);
					this.prefs.save();
					JOptionPane.showMessageDialog(null, "Your proxy has been updated (see settings tab)"
							+ "\nNew users can join your server");
					((SettingsTabPane) this.prefs.tmpGetObject("tabs.settings"))
					.pingAuthServer();
					((JTextField) this.prefs.tmpGetObject("proxy.serverfield"))
					.setText(UniversalLauncher.BETA_AUTH_SERVER);
				} else if (result == 1) {
					JOptionPane.showMessageDialog(null, "New users will not be able to join your server"
							+ "\nYou can change this in the settings tab (proxy => alpha server)");
				}
			}
		}
	}

	// private JPanel createForgePanel() {
	// JPanel forgePanel = new JPanel(new GridBagLayout());
	// forgePanel.setBorder(AbstractTabPane.createLineBorder("Forge"));
	// GridBagConstraints c = new GridBagConstraints();
	//
	// c.gridx = 0;
	// c.gridy = 0;
	// c.anchor = GridBagConstraints.WEST;
	//
	// forgePanel.add(new JLabel("Server Name"), c);
	//
	// JTextField forgeServerField = new JTextField(20);
	//
	// c.gridy = 1;
	// forgePanel.add(forgeServerField, c);
	//
	// c.gridx++;
	// forgePanel.add(new JLabel(".jar"), c);
	// c.gridx++;
	// forgePanel.add(Box.createHorizontalStrut(10), c);
	//
	// c.gridy = 0;
	// c.gridx++;
	// forgePanel.add(new JLabel("Minecraft Server Name"), c);
	//
	// final JTextField minecraftServerField = new JTextField(20);
	//
	// c.gridy = 1;
	// forgePanel.add(minecraftServerField, c);
	// c.gridx++;
	// forgePanel.add(new JLabel(".jar"), c);
	//
	// JPanel launchPanel = new JPanel();
	//
	// JCheckBox remember = new JCheckBox("Remember names?");
	//
	// JButton launch = new JButton("Launch");
	//
	// launchPanel.add(remember);
	// launchPanel.add(launch);
	//
	// c.gridy = 2;
	// c.gridx = 0;
	// forgePanel.add(launchPanel, c);
	//
	// c.gridx = 3;
	//
	// JButton defaultMCServer = new JButton("Default MC Server");
	// defaultMCServer.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// minecraftServerField.setText("minecraft_server");
	// }
	// });
	//
	// forgePanel.add(defaultMCServer, c);
	//
	// return forgePanel;
	// }
}
