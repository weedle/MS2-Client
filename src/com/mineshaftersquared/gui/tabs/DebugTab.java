package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;

public class DebugTab extends JPanel {
	
	public DebugTab() {
		super(new BorderLayout());
		this.add(this.createConsolePanel());
	}
	
	private JPanel createConsolePanel() {
		JPanel container = new JPanel(new BorderLayout());

		container.add(new JScrollPane(UniversalLauncher.console.getOutputField()), BorderLayout.CENTER);

		final JCheckBox autoscroll = new JCheckBox("Autoscroll?", true);
		SimpleSwingUtils.setAutoscroll(UniversalLauncher.console.getOutputField(), true);
		autoscroll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SimpleSwingUtils.setAutoscroll(UniversalLauncher.console.getOutputField(), autoscroll.isSelected());
			}

		});

		final JButton copy = new JButton("Copy All");
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				UniversalLauncher.console.getOutputField().copy();
			}

		});

		JPanel controlsUI = new JPanel(new FlowLayout(FlowLayout.CENTER));
		controlsUI.add(autoscroll);
		controlsUI.add(copy);
		container.add(controlsUI, BorderLayout.SOUTH);

		return container;
	}
}
