package com.mineshaftersquared.gui.tabs.index;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.mineshaftersquared.UniversalLauncher;

public class SidePanel extends JPanel {
	
	private final UniversalLauncher app;
	
	public SidePanel(UniversalLauncher app) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.app = app;
		
		this.add(this.createProfileMenu());
		this.add(this.createStatusMenu());
		this.add(this.createPlayMenu());
		this.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, Integer.MAX_VALUE)));
	}
	
	private JPanel createProfileMenu() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Profile"));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		
		JComboBox profiles = new JComboBox();
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(profiles, c);
		
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
		JLabel serverStatus = new JLabel("Seems OK");
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(serverStatusLabel, c);
		c.gridx = 1;
		panel.add(serverStatus, c);
		
		return panel;
	}
}
