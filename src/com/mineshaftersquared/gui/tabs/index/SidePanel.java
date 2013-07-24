package com.mineshaftersquared.gui.tabs.index;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

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
import javax.swing.SwingUtilities;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.profile.Profile;

public class SidePanel extends JPanel {
	
	private final UniversalLauncher app;
	private Profile[] profilesSource;
	private JComboBox<Profile> profile;
	
	public SidePanel(UniversalLauncher app) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.app = app;
		
		this.profilesSource = null;
		this.refreshProfiles();
		this.profile = null;
		
		this.add(this.createProfileMenu());
		this.add(this.createStatusMenu());
		this.add(this.createPlayMenu());
		this.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, Integer.MAX_VALUE)));
	}
	
	private void refreshProfiles() {
		try {
			this.app.profilesManager.loadProfiles();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Map<String, Profile> profilesMap = this.app.profilesManager.getProfiles();
		this.profilesSource = new Profile[profilesMap.size()];
		int i = 0;
		for (String key : profilesMap.keySet()) {
			this.profilesSource[i] = profilesMap.get(key);
			i++;
		}
	}
	
	private JPanel createProfileMenu() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Profile"));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		final JComboBox<Profile> profiles = new JComboBox<Profile>(new DefaultComboBoxModel<Profile>(this.profilesSource));
		JButton refresh = new JButton("Refresh");
		
		this.profile = profiles;
		
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SidePanel.this.refreshProfiles();
				profiles.setModel(new DefaultComboBoxModel<Profile>(SidePanel.this.profilesSource));
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
		
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Profile profile = (Profile) SidePanel.this.profile.getSelectedItem();
				if (profile != null) {
					SidePanel.this.app.launcher.launch(profile);
				}
			}
		});
		
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
