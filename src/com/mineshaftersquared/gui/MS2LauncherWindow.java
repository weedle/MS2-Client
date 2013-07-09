package com.mineshaftersquared.gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.gui.tabs.AboutTab;
import com.mineshaftersquared.gui.tabs.DebugTab;
import com.mineshaftersquared.gui.tabs.IndexTab;
import com.mineshaftersquared.gui.tabs.ProfilesTab;
import com.mineshaftersquared.gui.tabs.ServerAdminsTab;

public class MS2LauncherWindow extends JFrame {
	
	public final UniversalLauncher app;
	
	public static final int PREFERRED_WIDTH = 900;
	public static final int PREFERRED_HEIGHT = 600;
	public static final String MS2_RESOURCES_DIR = "ms2-resources";
	public static final String MS2_SETTINGS_NAME = "settings.xml";
	
	public MS2LauncherWindow(UniversalLauncher app) {
		super("Mineshafter Squared - Universal Launcher v" + UniversalLauncher.MS2_VERSION.toString());
		this.app = app;
		
		SimpleSwingUtils.setSystemLookAndFeel();
		SimpleSwingUtils.setIcon(this, "com/mineshaftersquared/resources/ms2.png");
		
		JTabbedPane tabbedPane = new JTabbedPane() {{
			add("Index", new IndexTab(MS2LauncherWindow.this.app));
			add("Profiles", new ProfilesTab());
			add("Server Admins", new ServerAdminsTab());
			add("About", new AboutTab());
			add("Debug", new DebugTab());
		}};
		
		this.getContentPane().add(tabbedPane);
	}
}
