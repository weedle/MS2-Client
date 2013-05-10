/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.swing.SimpleLinkableLabel;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.JarProcessBuilder;
import com.mineshaftersquared.resources.ProcessOutputRedirector;
import com.mineshaftersquared.resources.Utils;

/**
 * 
 * @author Adrian
 */
public class ModsTabPane extends AbstractTabPane {
	private SimpleISettings prefs;

	public ModsTabPane(SimpleISettings prefs) {
		this.prefs = prefs;
		this.add(this.createForgePanel());
		this.add(this.createFeedtheBeastPanel());
		this.add(this.createTechnicPanel());
		this.add(this.createSpoutcraftPanel());
	}

	private JPanel createForgePanel() {
		JPanel forgePanel = new JPanel();
		forgePanel.setBorder(SimpleSwingUtils.createLineBorder("Forge"));

		forgePanel
		.add(new SimpleLinkableLabel(
				"<ul>"
						+ "<li>No special instructions for Forge - regular install and launch client!</li>"
						+ "<li>Read more at <a href=\"http://ms2.creatifcubed.com/forge.php\">ms2.creatifcubed.com/forge.php</a></li>"
						+ "<li>Forge official instructions: <a href=\"http://www.minecraftforge.net/wiki/Installation/Universal\">minecraftforge.net/wiki/Installation/Universal</a></li>"
						+ "</ul>", 550));

		return forgePanel;
	}

	private JPanel createSpoutcraftPanel() {
		JPanel spoutcraftPanel = new JPanel(new GridBagLayout());
		spoutcraftPanel.setBorder(SimpleSwingUtils.createLineBorder("Spoutcraft"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;

		spoutcraftPanel
		.add(new SimpleLinkableLabel(
				"<ul>"
						+ "<li>Read more at <a href=\"http://ms2.creatifcubed.com/spoutcraft.php\">ms2.creatifcubed.com/spoutcraft.php</a></li>"
						+ "<li>As Spoutcraft has its own launcher, you must use it to configure settings"
						+ " (eg: RAM, minecraft location)</li>" + "</ul>", 550), c);

		c.gridx = 0;
		c.gridy = 1;
		final JTextField nameField = new JTextField(20);
		nameField.setText(this.prefs.getString("spoutcraft.launchername", "ms2-spoutcraft"));
		final JCheckBox remember = new JCheckBox("Remember?", true);
		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String jarName = nameField.getText().trim();
				if (jarName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Invalid file name");
					return;
				}
				if (!(new File(jarName + ".jar").exists())) {
					JOptionPane
					.showMessageDialog(
							null,
							"File {"
									+ jarName
									+ "} not found"
									+ "\nDid you download the custom Spoutcraft launcher at ms2.creatifcubed.com/spoutcraft.php");
					return;
				}
				if (remember.isSelected()) {
					ModsTabPane.this.prefs.put("spoutcraft.launchername", jarName);
					ModsTabPane.this.prefs.save();
				}
				MineProxy proxy = ((UniversalLauncher) ModsTabPane.this.prefs.tmpGetObject("instance")).proxy;
				if (proxy == null) {
					JOptionPane.showMessageDialog(null, "Proxy has not been started. Start it in the settings tab");
					return;
				} else {
					System.out.println("Spoutcraft using proxy: " + proxy.getPort());
				}
				jarName += ".jar";
				System.out.println("Modding Spoutcraft Launcher {" + jarName + "}...");
				File original = new File(jarName);
				File modded = Utils.getModdedFile(original);
				modded.delete();
				Map<String, Integer> changes = new HashMap<String, Integer>();
				// changes.put("org/spoutcraft/launcher/util/Utils.class",
				// Utils.MOD_REPLACE);
				Utils.editJar(original, modded, changes);
				System.out.println("Done modding Spoutcraft launcher {" + modded.getName() + "}");
				try {
					String path = SimpleUtils.getJarPath().getCanonicalPath();
					List<String> commands = JarProcessBuilder.getCommand(path, null, new String[] { "proxy",
						ModsTabPane.this.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER) });

					Process p = JarProcessBuilder.create(path, null,
							new String[] { "spoutcraft", modded.getName(), });

					new Thread(new ProcessOutputRedirector(p, "[MS2-Spoutcraft]: ")).start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		spoutcraftPanel.add(AbstractTabPane.createGenericLaunchPanel("Launcher name (def ms2-spoutcraft)", nameField,
				remember, launch), c);

		return spoutcraftPanel;
	}

	private JPanel createFeedtheBeastPanel() {
		JPanel ftbPanel = new JPanel(new GridBagLayout());
		ftbPanel.setBorder(SimpleSwingUtils.createLineBorder("Feed the Beast"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;

		ftbPanel.add(
				new SimpleLinkableLabel(
						"<ul>"
								+ "<li>Read more at <a href=\"http://ms2.creatifcubed.com/feed_the_beast.php\">ms2.creatifcubed.com/feed_the_beast.php</a></li>"
								+ "<li>As FTB has its own launcher, you must use it to configure settings"
								+ " (eg: RAM, minecraft location)</li>" + "</ul>", 550), c);

		c.gridx = 0;
		c.gridy = 1;
		final JTextField nameField = new JTextField(20);
		nameField.setText(this.prefs.getString("ftb.launchername", "FTB_Launcher"));
		final JCheckBox remember = new JCheckBox("Remember?", true);
		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String jarName = nameField.getText().trim();
				if (jarName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Invalid file name");
					return;
				}
				if (!(new File(jarName + ".jar").exists())) {
					JOptionPane.showMessageDialog(null, "File {" + jarName + "} not found"
							+ "\nDid you download the Feed the Beast launcher at feed-the-beast.com?");
					return;
				}
				if (remember.isSelected()) {
					ModsTabPane.this.prefs.put("ftb.launchername", jarName);
					ModsTabPane.this.prefs.save();
				}
				jarName += ".jar";
				System.out.println("Modding FTB Launcher {" + jarName + "}...");
				File original = new File(jarName);
				File modded = Utils.getModdedFile(original);
				modded.delete();
				Map<String, Integer> changes = new HashMap<String, Integer>();
				changes.put("net/ftb/mclauncher/MinecraftLauncher.class", Utils.MOD_REPLACE);
				changes.put("net/ftb/workers/LoginWorker.class", Utils.MOD_REPLACE);
				Utils.editJar(original, modded, changes);
				System.out.println("Done modding FTB launcher {" + modded.getName() + "}");

				int minRam = ModsTabPane.this.prefs.getInt("runtime.ram.min", 0);
				int maxRam = ModsTabPane.this.prefs.getInt("runtime.ram.max", 0);
				try {
					Process p = JarProcessBuilder.create(
							SimpleUtils.getJarPath().getCanonicalPath(),
							null,
							new String[] {
								"ftb",
								modded.getName(),
								ModsTabPane.this.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER),
								String.valueOf(minRam), String.valueOf(maxRam) });

					new Thread(new ProcessOutputRedirector(p, "[MS2-FTBProxy]: ")).start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		ftbPanel.add(AbstractTabPane.createGenericLaunchPanel("Launcher name (def FTB_Launcher)", nameField, remember,
				launch), c);

		return ftbPanel;
	}

	private JPanel createTechnicPanel() {
		JPanel technicPanel = new JPanel(new GridBagLayout());
		technicPanel.setBorder(SimpleSwingUtils.createLineBorder("Technic"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;

		technicPanel
		.add(new SimpleLinkableLabel(
				"<ul>"
						+ "<li>Read more at <a href=\"http://ms2.creatifcubed.com/technic.php\">ms2.creatifcubed.com/technic.php</a></li>"
						+ "<li>As Technic has its own launcher, you must use it to configure settings"
						+ " (eg: RAM, minecraft location)</li>" + "</ul>", 550), c);

		c.gridx = 0;
		c.gridy = 1;

		final JTextField nameField = new JTextField(20);
		nameField.setText(this.prefs.getString("technic.launchername", "ms2-techniclauncher"));
		final JCheckBox remember = new JCheckBox("Remember?", true);

		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String jarName = nameField.getText().trim();
				if (jarName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Invalid file name");
					return;
				}
				if (!(new File(jarName + ".jar").exists())) {
					JOptionPane.showMessageDialog(null, "File {" + jarName + "} not found"
							+ "\nDid you download the custom Technic launcher at ms2.creatifcubed.com/technic.php");
					return;
				}
				if (remember.isSelected()) {
					ModsTabPane.this.prefs.put("technic.launchername", jarName);
					ModsTabPane.this.prefs.save();
				}
				jarName += ".jar";
				System.out.println("Modding Technic Launcher {" + jarName + "}...");
				File original = new File(jarName);
				File modded = Utils.getModdedFile(original);
				modded.delete();
				Map<String, Integer> changes = new HashMap<String, Integer>();
				// changes.put("org/spoutcraft/launcher/util/Utils.class",
				// Utils.MOD_REPLACE);
				Utils.editJar(original, modded, changes);
				System.out.println("Done modding Technic launcher {" + modded.getName() + "}");

				int minRam = ModsTabPane.this.prefs.getInt("runtime.ram.min", 0);
				int maxRam = ModsTabPane.this.prefs.getInt("runtime.ram.max", 0);
				try {
					String path = SimpleUtils.getJarPath().getCanonicalPath();
					List<String> commands = JarProcessBuilder.getCommand(path, null, new String[] { "proxy",
							ModsTabPane.this.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER) });

					MineProxy proxy = ((UniversalLauncher) ModsTabPane.this.prefs.tmpGetObject("instance")).proxy;
					String proxyPort = null;
					if (proxy == null) {
						JOptionPane.showMessageDialog(null, "Proxy has not been started. Start it in the settings tab");
						return;
					} else {
						proxyPort = String.valueOf(proxy.getPort());
						System.out.println("Technic using proxy: " + proxyPort);
					}
					Process p = JarProcessBuilder.create(
							path,
							null,
							new String[] {
									"technic",
									modded.getName(),
									ModsTabPane.this.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER),
									String.valueOf(minRam), String.valueOf(maxRam), proxyPort });

					new Thread(new ProcessOutputRedirector(p, "[MS2-TechnicProxy]: ")).start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}
		});

		technicPanel.add(AbstractTabPane.createGenericLaunchPanel("Launcher name (def ms2-techniclauncher)", nameField,
				remember, launch), c);

		return technicPanel;
	}
}
