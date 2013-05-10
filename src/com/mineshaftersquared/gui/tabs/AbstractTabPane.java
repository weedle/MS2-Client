/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * @author Adrian
 */
public class AbstractTabPane extends JPanel {

	public AbstractTabPane() {
		this(new GridLayout(0, 1));
	}

	public AbstractTabPane(LayoutManager lm) {
		super(lm);
	}

	public static JPanel createGenericLaunchPanel(String label, JTextField nameField, JCheckBox remember, JButton launch) {

		JPanel launchPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		launchPanel.add(new JLabel(label), c);
		c.gridy = 1;
		c.anchor = GridBagConstraints.CENTER;

		launchPanel.add(nameField, c);

		c.gridx++;
		launchPanel.add(new JLabel(".jar"), c);

		c.gridx++;
		launchPanel.add(Box.createHorizontalStrut(20), c);

		c.gridx++;
		launchPanel.add(remember, c);

		c.gridx++;
		launchPanel.add(launch, c);

		return launchPanel;
	}
}
