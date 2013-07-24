package com.mineshaftersquared.gui.tabs.index;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.mineshaftersquared.UniversalLauncher;

public class SidePanel extends JPanel {
	
	private final UniversalLauncher app;
	
	public SidePanel(UniversalLauncher app) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.app = app;
		
		this.add(this.createStatusMenu());
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
}
