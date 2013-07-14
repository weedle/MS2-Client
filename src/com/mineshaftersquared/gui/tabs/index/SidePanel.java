package com.mineshaftersquared.gui.tabs.index;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.MCProfile;

public class SidePanel extends JPanel {
	
	private final UniversalLauncher app;
	private MCProfile[] profiles;
	
	public SidePanel(UniversalLauncher app) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.app = app;
		
		this.profiles = null;
		this.refreshProfiles();
		
		this.add(this.createProfileMenu());
		this.add(this.createStatusMenu());
		this.add(this.createPlayMenu());
		this.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, Integer.MAX_VALUE)));
	}
	
	private void refreshProfiles() {
		this.app.profileManager.refreshProfiles();
		this.profiles = this.app.profileManager.profilesAsArray();
	}
	
	private JPanel createProfileMenu() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Profile"));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		final JComboBox<MCProfile> profiles = new JComboBox<MCProfile>(new DefaultComboBoxModel<MCProfile>(this.profiles));
		JButton refresh = new JButton("Refresh");
		
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SidePanel.this.refreshProfiles();
				profiles.setModel(new DefaultComboBoxModel<MCProfile>(SidePanel.this.profiles));
			}
		});
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(profiles, c);
		c.gridx = 0;
		c.gridy++;
		panel.add(refresh, c);
		
		return panel;
	}
	
	private JPanel createPlayMenu() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Play"));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		JLabel usernameLabel = new JLabel("Username");
		JTextField usernameField = new JTextField(20);
		JLabel passwordLabel = new JLabel("Password");
		JPasswordField passwordField = new JPasswordField(20);
		JCheckBox rememberme = new JCheckBox("Remember me?");
		JButton login = new JButton("Login");
		JButton launch = new JButton("Play Offline");
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		panel.add(usernameLabel, c);
		c.gridx = 1;
		c.gridwidth = 2;
		panel.add(usernameField, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		panel.add(passwordLabel, c);
		c.gridx = 1;
		c.gridwidth = 2;
		panel.add(passwordField, c);
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 2;
		panel.add(rememberme, c);
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 1;
		panel.add(login, c);
		c.gridx = 2;
		panel.add(launch, c);
		
		return panel;
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
}
