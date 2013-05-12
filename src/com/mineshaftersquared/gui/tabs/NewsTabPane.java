/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;

/**
 * 
 * @author Raidriar
 */
public class NewsTabPane extends AbstractTabPane {

	public NewsTabPane() {
		JTabbedPane tabs = new JTabbedPane();

		tabs.add("Mojang Minecraft", this.createMojangNews());
		tabs.add("Mineshafter Squared", this.createMS2News());

		this.add(tabs);
	}

	public JPanel createMojangNews() {
		JPanel panel = new JPanel(new BorderLayout());

		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
//		browser.setBackground(Color.DARK_GRAY);
		browser.setContentType("text/html");

		browser.setText("<html><body><h1>Loading Mojang Minecraft news..</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					browser.setPage(new URL("http://mcupdate.tumblr.com/"));
				} catch (Exception ex) {
					ex.printStackTrace();
					browser.setText("<html><body><h1>Failed to fetch Minecraft news</h1><br>Error: " + ex.toString()
							+ "</body></html>");
				}
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);

		return panel;
	}

	public JPanel createMS2News() {
		JPanel panel = new JPanel(new BorderLayout());
		
		final JTextPane browser = new JTextPane();

		browser.setEditable(false);
		browser.setMargin(null);
//		browser.setBackground(Color.DARK_GRAY);
		browser.setContentType("text/html");

		browser.setText("<html><body><h1>Loading Mineshafter Squared news..</h1></body></html>");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					browser.setPage(new URL("http://mineshaftersquared.tumblr.com/"));
				} catch (Exception ex) {
					ex.printStackTrace();
					browser.setText("<html><body><h1>Failed to fetch Mineshafter Squared news</h1><br>Error: " + ex.toString()
							+ "</body></html>");
				}
			}
		}).start();

		panel.add(new JScrollPane(browser), BorderLayout.CENTER);
		
		return panel;
	}
}
