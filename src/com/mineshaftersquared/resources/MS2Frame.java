package com.mineshaftersquared.resources;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mineshaftersquared.UniversalLauncher;

public class MS2Frame extends JFrame {

	private JPanel wrapper;
	private Applet applet;

	public MS2Frame() {
		super("Mineshafter Squared");
		this.setBackground(Color.BLACK);

		this.wrapper = new JPanel();
		this.setPreferredSize(new Dimension(854, 520));
		// this.wrapper.setOpaque(false);
		this.wrapper.setLayout(new BorderLayout());
		this.add(this.wrapper, BorderLayout.CENTER);
		this.pack();
		this.setLocationRelativeTo(null);

		final MS2Frame that = this;

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (that.applet != null) {
					that.applet.stop();
					that.applet.destroy();
				}
				System.exit(0);
			}
		});
	}

	public void start(Applet applet) {
		UniversalLauncher.log.info("Starting " + applet.getClass().getCanonicalName());
		applet.init();
		this.wrapper.add(applet, BorderLayout.CENTER);
		this.validate();
		applet.start();
		this.applet = applet;
	}

}