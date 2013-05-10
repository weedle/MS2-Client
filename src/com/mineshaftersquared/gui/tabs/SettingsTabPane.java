/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.SimpleWaiter;
import com.creatifcubed.simpleapi.swing.SimpleLinkableLabel;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.creatifcubed.simpleapi.swing.SimpleWrappedLabel;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.proxy.MineProxy;
import com.mineshaftersquared.resources.Utils;

/**
 * 
 * @author Adrian
 */
public class SettingsTabPane extends AbstractTabPane {

	private SimpleISettings prefs;

	public SettingsTabPane(SimpleISettings prefs) {
		this.prefs = prefs;
		this.add(this.createSystemInfoPanel());
		this.add(this.createRamPanel());
		this.add(this.createProxyPane());
	}

	public JPanel createRamPanel() {
		JPanel ramPanel = new JPanel(new GridBagLayout());
		ramPanel.setBorder(SimpleSwingUtils.createLineBorder("RAM"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 10;
		c.anchor = GridBagConstraints.NORTHWEST;
		int physicalRam = (int) (Math.floor((double) SimpleUtils.getRam() / 1024 / 1024 / 512) * 512); // MB

		int minRam = SimpleUtils.constrain(this.prefs.getInt("runtime.ram.min", 0), 0, physicalRam);
		int maxRam = SimpleUtils.constrain(this.prefs.getInt("runtime.ram.max", 0), 0, physicalRam);

		c.gridwidth = 2;
		ramPanel.add(
				new SimpleWrappedLabel(
						String.format(
								"I detected %d MB (%.2f GB) of physical Ram. Set to 0 ram to ignore setting."
										+ " I detected default %d MB of ram for the Java VM. Minecraft recommends a minimum 2GB (2048 MB) of RAM.",
								physicalRam, Math.round((double) physicalRam / 1024 * 100) / 100.0, (Runtime
										.getRuntime().maxMemory() / 1024 / 1024)), 550), c);

		final JLabel minRamLabel = new JLabel("Initial Memory (MB)");
		final JLabel maxRamLabel = new JLabel("Max Memory (MB");
		updateMemoryLabels(minRamLabel, minRam, maxRamLabel, maxRam);

		final SpinnerNumberModel minMemorySpinnerModel = new SpinnerNumberModel(minRam, 0, physicalRam, 256);
		final SpinnerNumberModel maxMemorySpinnerModel = new SpinnerNumberModel(maxRam, 0, physicalRam, 256);

		JSpinner minMemorySpinner = new JSpinner(minMemorySpinnerModel);
		JSpinner maxMemorySpinner = new JSpinner(maxMemorySpinnerModel);

		JButton saveRam = new JButton("Save");
		saveRam.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int minRam = minMemorySpinnerModel.getNumber().intValue();
				int maxRam = maxMemorySpinnerModel.getNumber().intValue();

				if (minRam != 0 && minRam > maxRam) {
					JOptionPane.showMessageDialog(null, "Initial ram cannot be greater than max ram");
					return;
				}

				SettingsTabPane.this.prefs.put("runtime.ram.min", minRam);
				SettingsTabPane.this.prefs.put("runtime.ram.max", maxRam);
				SettingsTabPane.this.prefs.save();
				SettingsTabPane.updateMemoryLabels(minRamLabel, minRam, maxRamLabel, maxRam);
			}
		});

		c.gridwidth = 1;
		c.ipadx = 0;
		c.gridy++;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		ramPanel.add(minRamLabel, c);
		c.gridx = 1;
		c.ipadx = 50;
		c.anchor = GridBagConstraints.NORTHEAST;
		ramPanel.add(minMemorySpinner, c);
		c.gridy++;
		c.ipadx = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		ramPanel.add(maxRamLabel, c);
		c.gridx = 1;
		c.ipadx = 50;
		c.anchor = GridBagConstraints.NORTHEAST;
		ramPanel.add(maxMemorySpinner, c);
		c.gridy++;
		c.gridx = 1;
		c.ipady = 5;
		// c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHEAST;
		ramPanel.add(saveRam, c);

		return ramPanel;
	}

	private static void updateMemoryLabels(JLabel minMemoryLabel, int min, JLabel maxMemoryLabel, int max) {
		maxMemoryLabel.setText("Max Memory (MB): " + (max == 0 ? "(unused)" : ("-Xmx" + max + "m")));
		minMemoryLabel.setText("Initial Memory (MB): " + (min == 0 ? "(unused)" : ("-Xms" + min + "m")));
	}

	public JPanel createSystemInfoPanel() {
		JPanel systemInfoPanel = new JPanel(new GridBagLayout());
		systemInfoPanel.setBorder(SimpleSwingUtils.createLineBorder("System"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;

		List<String> infos = new LinkedList<String>();

		infos.add("Java version: " + System.getProperty("java.version"));
		infos.add("Local folder at: " + Utils.getMCPath(Utils.PATH_LOCAL) + " (found: "
				+ Utils.existsInstallationIn(Utils.PATH_LOCAL) + ")");
		infos.add("Default MC folder at: " + Utils.getMCPath(Utils.PATH_DEFAULTMC) + " (found: "
				+ Utils.existsInstallationIn(Utils.PATH_DEFAULTMC) + ")");

		String bin = "<ul>";
		for (String str : infos) {
			bin += "<li>" + str + "</li>";
		}
		bin += "</ul>";

		c.gridx = 0;
		c.gridy = 0;
		systemInfoPanel.add(new SimpleWrappedLabel(bin, 550), c);

		// JPanel openDirsPanel = new JPanel();

		// openDirsPanel.add(openLocal);
		// openDirsPanel.add(openDefaultMC);
		// c.gridy++;
		// systemInfoPanel.add(openDirsPanel, c);

		return systemInfoPanel;
	}

	public JPanel createProxyPane() {
		JPanel proxyPanel = new JPanel(new GridLayout(0, 1));
		JPanel serverPanel = new JPanel();
		proxyPanel.setBorder(SimpleSwingUtils.createLineBorder("Proxy"));

		final JTextField serverField = new JTextField(20);
		serverField.setText(this.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER));
		this.prefs.tmpPut("proxy.serverfield", serverField);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsTabPane.this.prefs.put("proxy.authserver", serverField.getText().trim());
				SettingsTabPane.this.prefs.save();
				JOptionPane.showMessageDialog(null, "Saved auth server");
				((UniversalLauncher) SettingsTabPane.this.prefs.tmpGetObject("instance")).pingAuthServer(serverField
						.getText().trim());
			}
		});

		JButton defaultServerButton = new JButton("Default");
		defaultServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverField.setText(UniversalLauncher.DEFAULT_AUTH_SERVER);
				SettingsTabPane.this.prefs.put("proxy.authserver", serverField.getText());
				SettingsTabPane.this.prefs.save();
				JOptionPane.showMessageDialog(null, "Saved auth server (MS2 stable server)");
				((UniversalLauncher) SettingsTabPane.this.prefs.tmpGetObject("instance"))
						.pingAuthServer(UniversalLauncher.DEFAULT_AUTH_SERVER);
			}
		});

		JButton altServerButton = new JButton("Alpha (new accounts!)");
		altServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverField.setText(UniversalLauncher.BETA_AUTH_SERVER);
				SettingsTabPane.this.prefs.put("proxy.authserver", serverField.getText());
				SettingsTabPane.this.prefs.save();
				JOptionPane.showMessageDialog(null, "Saved auth server (MS2 alpha server)");
				((UniversalLauncher) SettingsTabPane.this.prefs.tmpGetObject("instance"))
						.pingAuthServer(UniversalLauncher.BETA_AUTH_SERVER);
			}
		});

		serverPanel.add(new JLabel("Auth server: "));
		serverPanel.add(new JLabel("http://"));
		serverPanel.add(serverField);
		serverPanel.add(saveButton);
		serverPanel.add(defaultServerButton);
		serverPanel.add(altServerButton);

		JPanel embeddedProxyPanel = new JPanel();

		final JLabel proxyLabel = new JLabel("Not proxying");

		final JButton startstopProxyButton = new JButton("Start");

		startstopProxyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final UniversalLauncher app = (UniversalLauncher) SettingsTabPane.this.prefs.tmpGetObject("instance");
				if (app.proxy == null) {
					app.proxy = new MineProxy(SettingsTabPane.this.prefs.getString("proxy.authserver",
							UniversalLauncher.DEFAULT_AUTH_SERVER));
					app.proxy.start();
					startstopProxyButton.setText("Stop");
					proxyLabel.setText("Proxying on port " + app.proxy.getPort());
				} else {
					new SimpleWaiter("Stopping proxy...", new Runnable() {
						@Override
						public void run() {
							app.proxy.shouldEnd = true;
							while (!app.proxy.isEnded) {
								// if (System.currentTimeMillis() / 1000 % 5 ==
								// 0)
								// System.out.println("Server isEnded: " +
								// app.proxy.isEnded);
								SimpleUtils.wait(1);
							}
							app.proxy = null;
							startstopProxyButton.setText("Start");
							proxyLabel.setText("Not proxying");
						}
					}, null).run();
				}
			}
		});

		embeddedProxyPanel.add(startstopProxyButton);
		embeddedProxyPanel.add(proxyLabel);

		JPanel infoPanel = new JPanel();
		infoPanel
				.add(new SimpleLinkableLabel(
						"For debugging (the proxy and in general), use the run-scripts."
								+ "For more information, go to <a href='http://ms2.creatifcubed.com/more.php'>ms2.creatifcubed.com</a>"));

		proxyPanel.add(serverPanel);
		proxyPanel.add(embeddedProxyPanel);
		return proxyPanel;
	}
}
