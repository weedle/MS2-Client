package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.gui.tabs.index.NewsPanel;
import com.mineshaftersquared.gui.tabs.index.SidePanel;

public class IndexTab extends JPanel {
	
	private final UniversalLauncher app;
	
	public IndexTab(UniversalLauncher app) {
		super(new BorderLayout());
		this.app = app;
		
		this.add(new NewsPanel(this.app), BorderLayout.CENTER);
		this.add(new SidePanel(this.app), BorderLayout.EAST);
	}
}
