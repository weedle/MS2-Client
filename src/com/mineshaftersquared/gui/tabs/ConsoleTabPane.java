package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;

public class ConsoleTabPane extends AbstractTabPane {
	
	public ConsoleTabPane() {
		this.add(this.createConsoleTab());
	}
	
	private JPanel createConsoleTab() {
		JPanel container = new JPanel(new BorderLayout());
		
		container.add(UniversalLauncher.console.getOutputField(), BorderLayout.CENTER);
		
		final JCheckBox autoscroll = new JCheckBox("Autoscroll?", true);
		autoscroll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SimpleSwingUtils.setAutoscroll(UniversalLauncher.console.getOutputField(), autoscroll.isSelected());
			}
			
		});
		
		final JButton selectall = new JButton("Select all");
		selectall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				UniversalLauncher.console.getOutputField().requestFocus();
				UniversalLauncher.console.getOutputField().selectAll();
			}
			
		});
		
		final JButton copy = new JButton("Copy selected");
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UniversalLauncher.console.getOutputField().copy();
			}
			
		});
		
		JPanel controlsUI = new JPanel(new FlowLayout(FlowLayout.CENTER));
		controlsUI.add(autoscroll);
		controlsUI.add(selectall);
		controlsUI.add(copy);
		container.add(controlsUI, BorderLayout.SOUTH);
		
		return container;
	}
}
