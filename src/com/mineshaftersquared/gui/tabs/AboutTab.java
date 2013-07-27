package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.creatifcubed.simpleapi.swing.SimpleHyperlinkListener;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;

public class AboutTab extends JPanel {
	
	public AboutTab() {
		super(new BorderLayout());
		this.add(this.createAboutInfoPanel());
	}
	
	private JPanel createAboutInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
		browser.setContentType("text/html");

		browser.addHyperlinkListener(new SimpleHyperlinkListener("Unable to open link %s"));

		browser.setText("<html><body><h1>Loading about info...</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							browser.setPage(new URL(UniversalLauncher.POLLING_SERVER  + "about_info.php"));
						} catch (Exception ex) {
							ex.printStackTrace();
							browser.setText("<html><body><h1>Failed to fetch about info</h1><br>Error: " + ex.toString() + "</body></html>");
						}
					}
				});
				
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);

		return panel;
	}

}
