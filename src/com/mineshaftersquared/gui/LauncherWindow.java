/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleResources;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.gui.tabs.AboutTabPane;
import com.mineshaftersquared.gui.tabs.ConsoleTabPane;
import com.mineshaftersquared.gui.tabs.FeedbackTabPane;
import com.mineshaftersquared.gui.tabs.IndexTabPane;
import com.mineshaftersquared.gui.tabs.ModsTabPane;
import com.mineshaftersquared.gui.tabs.NewsTabPane;
import com.mineshaftersquared.gui.tabs.ServerAdminsTabPane;
import com.mineshaftersquared.gui.tabs.SettingsTabPane;

/**
 * 
 * @author Adrian
 */
public class LauncherWindow extends JFrame {

	public static final int MIN_WIDTH = 700;
	public static final int MIN_HEIGHT = 400;

	private JTabbedPane tabbedPane;
	private SimpleISettings prefs;

	public LauncherWindow(String title, SimpleISettings prefs) {
		super(title);
		// this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.prefs = prefs;

		SimpleSwingUtils.setSystemLookAndFeel();

		this.setIcon("com/mineshaftersquared/resources/ms2.png");

		JPanel contentPane = new JPanel(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();

		this.prefs.tmpPut("launcher.window", this);

		tabbedPane.add("Index", new IndexTabPane(this.prefs));
		tabbedPane.add("Settings", new SettingsTabPane(this.prefs));
		tabbedPane.add("About", new AboutTabPane());
		tabbedPane.add("Feedback", new FeedbackTabPane(this.prefs));
		tabbedPane.add("Mods", new ModsTabPane(this.prefs));
		tabbedPane.add("Server Admins", new ServerAdminsTabPane(this.prefs));
		tabbedPane.add("News", new NewsTabPane());
		tabbedPane.add("Dev Console", new ConsoleTabPane());
		this.tabbedPane = tabbedPane;

		contentPane.add(tabbedPane);

		this.setContentPane(contentPane);
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.pack();
		this.setMinimumSize(new Dimension(MIN_WIDTH, this.getSize().height));
		// this.setResizable(false);
		this.setLocationRelativeTo(null);

	}

	public void setActiveTab(int index) {
		this.tabbedPane.setSelectedIndex(index);
	}

	public void updateServerStatusMessage(String msg) {
		this.setTitle(UniversalLauncher.getWindowTitle() + " - " + msg);
	}

	private void setIcon(String path) {
		URL iconURL = SimpleResources.loadAsURL(path);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(iconURL);
		this.setIconImage(img);
	}

}
