package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import com.mineshaftersquared.UniversalLauncher;

public class IndexTab extends JPanel {
	
	private final UniversalLauncher app;
	
	public IndexTab(UniversalLauncher app) {
		super(new BorderLayout());
		this.app = app;
		
		this.add(this.createNewsPanel(), BorderLayout.CENTER);
		this.add(this.createSidePanel(), BorderLayout.EAST);
	}
	
	private JPanel createNewsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();

		JTextPane mcBrowser = new JTextPane();
		mcBrowser.setEditable(false);
		mcBrowser.setMargin(null);
		mcBrowser.setContentType("text/html");
		mcBrowser.setText("<html><body><h1>Loading Minecraft news..</h1></body></html>");
		try {
			mcBrowser.setPage(new URL("http://mcupdate.tumblr.com/"));
		} catch (Exception ex) {
			ex.printStackTrace();
			mcBrowser.setText("<html><body><h1>Failed to fetch Minecraft news</h1><br>Error: " + ex.toString()
					+ "</body></html>");
		}
		
		JTextPane ms2Browser = new JTextPane();
		ms2Browser.setEditable(false);
		ms2Browser.setMargin(null);
		ms2Browser.setContentType("text/html");
		ms2Browser.setText("<html><body><h1>Loading Mineshafter Squared news..</h1></body></html>");
		try {
			ms2Browser.setPage(new URL("http://mineshaftersquared.tumblr.com/mobile"));
		} catch (Exception ex) {
			ex.printStackTrace();
			ms2Browser.setText("<html><body><h1>Failed to fetch Mineshafter Squared news</h1><br>Error: " + ex.toString()
					+ "</body></html>");
		}
		
		JPanel updatesPanel = new JPanel();
		
		tabbedPane.add("Minecraft News", new JScrollPane(mcBrowser));
		tabbedPane.add("MS2 News", new JScrollPane(ms2Browser));
		tabbedPane.add("Launcher Updates", updatesPanel);
		
		panel.add(tabbedPane, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createSidePanel() {
		JPanel panel = new JPanel();
		
		return panel;
	}
}
