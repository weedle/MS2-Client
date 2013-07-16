package com.mineshaftersquared.gui.tabs.index;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.net.URL;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import com.creatifcubed.simpleapi.SimpleVersion;
import com.creatifcubed.simpleapi.swing.WebsitePanel;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.misc.EventBus;
import com.mineshaftersquared.misc.EventBus.EventData;

public class NewsPanel extends JPanel {
	
	private final UniversalLauncher app;
	
	public NewsPanel(UniversalLauncher app) {
		super(new BorderLayout());
		this.app = app;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		tabbedPane.add("Minecraft News", this.createMinecraftPanel());
		tabbedPane.add("MS2 News", this.createMS2Panel());
		tabbedPane.add("Launcher Updates", this.createUpdatesPanel());
		
		this.add(tabbedPane, BorderLayout.CENTER);
	}
	
	private JPanel createMinecraftPanel() {
		return new WebsitePanel("http://mcupdate.tumblr.com/");
	}
	
	private JPanel createMS2Panel() {
		return new WebsitePanel("http://mineshaftersquared.tumblr.com/mobile");
	}
	
	private JPanel createUpdatesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel websitePanel = new WebsitePanel("http://ms2.creatifcubed.com/polling_scripts/updates_messages.php");
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // 5 is default
		JLabel currentVersion = new JLabel(String.format("Current Version: %s", UniversalLauncher.MS2_VERSION.toString()));
		final JLabel latestVersion = new JLabel("Latest Version: Loading...");
		JButton update = new JButton("Update");
		
		this.app.eventBus.on("latestversion", new EventBus.Listener() {
			@Override
			public void fire(EventObject obj) {
				if (obj instanceof EventBus.EventData) {
					EventBus.EventData eventData = (EventData) obj;
					if (eventData.obj instanceof SimpleVersion) {
						latestVersion.setText("Latest Version: " + eventData.obj.toString());
					}
				}
			}
		});
		
		toolbar.add(currentVersion);
		toolbar.add(latestVersion);
		toolbar.add(update);
		panel.add(websitePanel, BorderLayout.CENTER);
		panel.add(toolbar, BorderLayout.SOUTH);
		
		return panel;
	}
}
