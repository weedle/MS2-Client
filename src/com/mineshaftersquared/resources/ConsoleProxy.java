/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * 
 * @author Raidriar
 */
public class ConsoleProxy extends JFrame {

	InputStream in;
	OutputStream out;
	OutputStream err;

	public static void main(String[] args) {
		ConsoleProxy c = new ConsoleProxy("HellO");
		c.setVisible(true);
	}

	public ConsoleProxy(String title) {
		super(title);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.in = new InputStream() {

			@Override
			public int read() throws IOException {
				throw new UnsupportedOperationException("Not supported yet.");
			}

		};

		JPanel contentPane = new JPanel(new BorderLayout());

		JTextArea display = new JTextArea();
		display.setEditable(false);

		JScrollPane scroll = new JScrollPane(display);

		JTextField input = new JTextField();

		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});

		contentPane.add(scroll, BorderLayout.CENTER);
		contentPane.add(input, BorderLayout.SOUTH);

		this.setContentPane(contentPane);

		this.setLocationRelativeTo(null);
	}

}
