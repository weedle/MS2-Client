/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.creatifcubed.simpleapi.swing.SimpleLinkedLabel;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.creatifcubed.simpleapi.swing.SimpleWrappedLabel;
import com.mineshaftersquared.UniversalLauncher;

/**
 * 
 * @author Adrian
 */
public class AboutTabPane extends AbstractTabPane {
	public AboutTabPane() {
		//this.add(this.createTeamPanel());
		//this.add(this.createSupportPanel());
		this.add(this.createAboutInfoPanel());
	}
	
	private JPanel createAboutInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
		browser.setContentType("text/html");
		
		browser.addHyperlinkListener(SimpleSwingUtils.createHyperlinkListenerOpen("Unable to open link %s"));

		browser.setText("<html><body><h1>Loading about info...</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					browser.setPage(new URL("http://" + UniversalLauncher.POLLING_SERVER  + "about_info.php"));
				} catch (Exception ex) {
					ex.printStackTrace();
					browser.setText("<html><body><h1>Failed to fetch feedback info</h1><br>Error: " + ex.toString()
							+ "</body></html>");
				}
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);

		return panel;
	}

	private JPanel createTeamPanel() {
		JPanel teamPanel = new JPanel(new GridBagLayout());
		teamPanel.setBorder(SimpleSwingUtils.createLineBorder("Team"));

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipadx = 40;
		c.ipady = 20;

		c.gridx = 0;

		c.gridy = 0;
		teamPanel.add(new JLabel("Website"), c);
		c.gridy++;
		teamPanel.add(new JLabel("MS2 Universal Launcher Site"), c);
		c.gridy++;
		teamPanel.add(new JLabel("Main dude"), c);
		c.gridy++;
		teamPanel.add(new JLabel("Launcher"), c);

		c.gridx = 1;

		c.gridy = 0;
		teamPanel.add(new SimpleLinkedLabel("mineshaftersquared.com", "http://mineshaftersquared.com"), c);
		c.gridy++;
		teamPanel.add(new SimpleLinkedLabel("ms2.creatifcubed.com", "http://ms2.creatifcubed.com"), c);
		c.gridy++;
		teamPanel.add(new SimpleLinkedLabel("Ryan", "http://kayoticlabs.com"), c);
		c.gridy++;
		teamPanel.add(new SimpleLinkedLabel("Adrian", "http://creatifcubed.com"), c);
		c.gridy++;

		return teamPanel;
	}

	private JPanel createSupportPanel() {
		JPanel supportPanel = new JPanel(new GridBagLayout());
		supportPanel.setBorder(SimpleSwingUtils.createLineBorder("Support"));

		GridBagConstraints c = new GridBagConstraints();

		// c.anchor = GridBagConstraints.NORTHWEST;
		c.ipadx = 40;
		c.ipady = 20;

		c.gridx = 0;
		c.gridy = 0;
		supportPanel.add(new JLabel("Read full:"), c);
		c.gridx = 1;
		supportPanel.add(new SimpleLinkedLabel("ms2.creatifcubed.com/about.php",
				"http://ms2.creatifcubed.com/about.php"), c);

		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy++;
		JLabel minecraftText = new SimpleWrappedLabel("If you enjoyed Minecraft, please consider buying the game."
				+ " Mojang is a great company, with real people who worked hard at making it.", 500);
		supportPanel.add(minecraftText, c);

		JLabel ms2Text = new SimpleWrappedLabel("If you have bought Minecraft and enjoyed our service,"
				+ " please consider supporting us. We are also real people spending our free time"
				+ " making and improving this service for you.", 500);

		c.gridy++;
		supportPanel.add(ms2Text, c);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;

		JPanel supportMojang = new JPanel();
		supportMojang.add(new JLabel("Support Mojang: "));
		supportMojang.add(new SimpleLinkedLabel("minecraft.net", "http://minecraft.net/store"));
		supportPanel.add(supportMojang, c);

		c.gridx = 1;
		JPanel supportMS2 = new JPanel();
		supportMS2.add(new JLabel("Support Us: "));
		supportMS2.add(new SimpleLinkedLabel("Beer Me!", "https://www.beerdonation.com/p/KayoticSully/"));
		supportPanel.add(supportMS2, c);

		return supportPanel;
	}
}
