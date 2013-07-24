package com.mineshaftersquared.gui.tabs.index;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FileUtils;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.gui.tabs.ServerAdminsTab;
import com.mineshaftersquared.misc.JavaProcessOutputRedirector;
import com.mineshaftersquared.misc.MS2Utils;

public class SidePanel extends JPanel {
	
	private final UniversalLauncher app;
	
	public SidePanel(UniversalLauncher app) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.app = app;
		
		this.add(this.createStatusMenu());
		this.add(this.createLaunchMenu());
		this.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, Integer.MAX_VALUE)));
	}
	
	private JPanel createStatusMenu() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Status"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 5, 5);
		c.ipadx = 10;
		
		JLabel serverStatusLabel = new JLabel("Server Status");
		final JLabel serverStatus = new JLabel("Loading...");
		JButton refresh = new JButton("Refresh");
		
		final Runnable ping = new Runnable() {
			@Override
			public void run() {
				serverStatus.setText("Loading...");
				final boolean reachable = SimpleUtils.httpPing(SidePanel.this.app.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						serverStatus.setText(reachable ? "No errors detected" : "Unreachable");
					}
				});
			}
		};
		
		new Thread(ping).start();
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new Thread(ping).start();
			}
		});
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(serverStatusLabel, c);
		c.gridx = 1;
		panel.add(serverStatus, c);
		c.gridx = 1;
		c.gridy++;
		panel.add(refresh, c);
		
		return panel;
	}
	
	private JPanel createLaunchMenu() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Launch"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 5, 5);
		c.ipadx = 10;
		
		final File startAutomaticallyFlag = new File(UniversalLauncher.MC_START_AUTOMATICALLY);
		final JCheckBox startAutomatically = new JCheckBox("Start Automatically", startAutomaticallyFlag.exists());
		JButton launch = new JButton("Launch");
		JLabel info = new JLabel("<html>Checking 'start automatically'<br />will create a file '" + UniversalLauncher.MC_START_AUTOMATICALLY
				+ "<br />Delete this file to disable auto-launching<br /></html>");
		startAutomatically.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (startAutomatically.isSelected()) {
					try {
						FileUtils.write(startAutomaticallyFlag, "Delete this file to disable automatically starting the Minecraft launcher", Charset.forName("utf-8"));
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				} else {
					startAutomaticallyFlag.delete();
				}
			}
		});
		
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Process p = MS2Utils.launchGame(MS2Utils.getLocalDir(), SidePanel.this.app.prefs.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER));
				if (p == null) {
					JOptionPane.showMessageDialog(SidePanel.this, "Unable to start game. See debug tab");
				} else {
					new Thread(new JavaProcessOutputRedirector(p, "[MS2Game] %s")).start();
				}
			}
		});
		
		c.gridx = 0;
		c.gridy= 0;
		c.gridwidth = 1;
		panel.add(startAutomatically, c);
		c.gridx = 1;
		panel.add(launch, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		panel.add(info, c);
		
		return panel;
	}
}
