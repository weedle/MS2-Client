package com.mineshaftersquared.gui.tabs.index;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.SimpleVersion;
import com.creatifcubed.simpleapi.swing.SimpleWebsitePanel;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.misc.EventBus;
import com.mineshaftersquared.misc.EventBus.EventData;

public class NewsPanel extends JPanel {
	
	private final UniversalLauncher app;
	
	public NewsPanel(UniversalLauncher app) {
		super(new BorderLayout());
		this.app = app;
		
		//JTabbedPane tabbedPane = new JTabbedPane();
		
		//tabbedPane.add("Minecraft News", this.createMinecraftPanel());
		//tabbedPane.add("MS2 News", this.createMS2Panel());
		//tabbedPane.add("Launcher Updates", this.createUpdatesPanel());
		
		//this.add(tabbedPane, BorderLayout.CENTER);
		this.add(this.createMS2Panel(), BorderLayout.CENTER);
	}
	
	private JPanel createMinecraftPanel() {
		return new SimpleWebsitePanel("http://mcupdate.tumblr.com/");
	}
	
	private JPanel createMS2Panel() {
		//return new WebsitePanel("http://mineshaftersquared.tumblr.com/mobile");
		JPanel panel = new JPanel(new BorderLayout());
		final String url = "http://api.mineshaftersquared.com/news";
		final SimpleWebsitePanel websitePanel = new SimpleWebsitePanel(url);
		//JPanel websitePanel = new SimpleWebsitePanel("http://creatifcubed.com/tmp/index.html");
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // 5 is default
		JButton refresh = new JButton("Refresh");
		JLabel currentVersion = new JLabel(String.format("Current Version: %s", UniversalLauncher.MS2_VERSION.toString()));
		final JLabel latestVersion = new JLabel("Latest Version: Loading...");
		JButton update = new JButton("Update");
		
		this.app.eventBus.on("latestversion", new EventBus.Listener() {
			@Override
			public void fire(EventObject obj) {
				if (obj instanceof EventBus.EventData) {
					EventBus.EventData eventData = (EventData) obj;
					latestVersion.setText("Latest Version: " + String.valueOf(eventData.obj));
				}
			}
		});
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SimpleUtils.openLink("http://ms2.creatifcubed.com");
			}
		});
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				latestVersion.setText("Latest Version: Loading...");
				websitePanel.setPage(url);
				new Thread(new Runnable() {
					@Override
					public void run() {
						NewsPanel.this.app.versionUpdates();
					}
				}).start();
			}
		});
		
		//toolbar.add(currentVersion);
		toolbar.add(refresh);
		toolbar.add(latestVersion);
		toolbar.add(update);
		panel.add(websitePanel, BorderLayout.CENTER);
		panel.add(toolbar, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JPanel createUpdatesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel websitePanel = new SimpleWebsitePanel("http://ms2.creatifcubed.com/polling_scripts/updates_messages.php");
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
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SimpleUtils.openLink("http://ms2.creatifcubed.com");
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
