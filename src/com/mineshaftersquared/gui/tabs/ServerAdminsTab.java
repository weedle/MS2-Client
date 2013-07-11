package com.mineshaftersquared.gui.tabs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mineshaftersquared.UniversalLauncher;

public class ServerAdminsTab extends JPanel {
	
	private final UniversalLauncher app;
	
	public ServerAdminsTab(UniversalLauncher app) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.app = app;
		
		this.add(this.createInfoPanel());
		this.add(this.createLaunchPanel());
		this.add(this.createOptionsPanel());
		this.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, Integer.MAX_VALUE)));
	}
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Info"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 5, 5);
		
		JLabel info = new JLabel(
			"<html><ul>"
				+ "<li>Put server Jars in the same folder as this launcher</li>"
				+ "<li>You can start the server by command line:<ul>"
					+ "<li>java [java options, such as -Xmx2G for 2GB of RAM] -jar [mineshaftersquared.jar]</li>"
					+ "<li>[MS2 options: -server=&lt;&gt; -bukkit -authserver=&lt;&gt;]</li>"
					+ "<li>mc (this tells MS2 that the rest of the arguments are for Minecraft)</li>"
					+ "<li>[Minecraft options (usually only for a few server mods)]</li>"
				+ "</ul></li>"
				+ "<li>For more information go to ms2.creatifcubed.com/server_admins.php</li>"
			+ "</ul></html>");
		
		panel.add(info, c);
		
		return panel;
	}
	
	private JPanel createLaunchPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Launch"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 5, 5);
		c.ipadx = 10;
		
		JLabel downloadLabel = new JLabel("Download");
		JComboBox downloadableVersions = new JComboBox();
		JButton download = new JButton("Download");
		JButton openLocalDir = new JButton("Open local folder");
		JLabel serverLabel = new JLabel("Server");
		JComboBox server = new JComboBox();
		JButton launch = new JButton("Launch");
		JButton refresh = new JButton("Refresh");
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(downloadLabel, c);
		c.gridx = 1;
		panel.add(downloadableVersions, c);
		c.gridx = 2;
		panel.add(download, c);
		c.gridx = 3;
		panel.add(openLocalDir, c);
		c.gridx = 0;
		c.gridy++;
		panel.add(serverLabel, c);
		c.gridx = 1;
		panel.add(server, c);
		c.gridx = 2;
		panel.add(launch, c);
		c.gridx = 3;
		panel.add(refresh, c);
		
		return panel;
	}
	
	private JPanel createOptionsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Options"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 5, 5);
		c.ipadx = 10;
		
		JLabel info = new JLabel(
			"<html><ul>"
				+ "<li>Arguments are split by spaces. Escape spaces with a backslash (\"\\ \")."
				+ "<br />Escape backslashes with another backlash (\"\\\\\") (even if you use quotations, you must escape those characters)</li>"
				+ "<li>Pseudocode Example: -dir=\"/Library/Application\\ Support/\" -code=a\\\\b"
				+ "<br />if what you wanted was (1) -dir=\"/Library/Application Support/\" and (2) -code=a\\b</li>"
			+ "</ul></html>");
		JLabel javaOptionsLabel = new JLabel("Java Options");
		JTextField javaOptions = new JTextField(64);
		JLabel ms2OptionsLabel = new JLabel("Mineshafter Squared Options");
		JTextField ms2Options = new JTextField(64);
		JLabel minecraftOptionsLabel = new JLabel("Minecraft Options");
		JTextField minecraftOptions = new JTextField(64);
		JButton save = new JButton("Save");
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		panel.add(info, c);
		c.gridy++;
		c.gridwidth = 1;
		panel.add(javaOptionsLabel, c);
		c.gridy++;
		panel.add(javaOptions, c);
		c.gridy++;
		panel.add(ms2OptionsLabel, c);
		c.gridy++;
		panel.add(ms2Options, c);
		c.gridy++;
		panel.add(minecraftOptionsLabel, c);
		c.gridy++;
		panel.add(minecraftOptions, c);
		c.gridy++;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		panel.add(save, c);
		
		return panel;
	}
}
