package com.mineshaftersquared.gui;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.swing.SimpleSwingWaiter;
import com.mineshaftersquared.models.LocalMCVersion;
import com.mineshaftersquared.models.MCVersion;
import com.mineshaftersquared.resources.MCDownloader;
import com.mineshaftersquared.resources.Utils;

public class DuplicateVersionMenu extends JDialog {
private final LocalMCVersion version;
	
	public DuplicateVersionMenu(Window owner, LocalMCVersion version) {
		super(owner, "Duplicate Version " + version.versionId, JDialog.DEFAULT_MODALITY_TYPE);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.version = version;
		this.setContentPane(this.createMainPanel());
	}
	
	private JPanel createMainPanel() {
		JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		
		JPanel versionNameRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel versionNameLabel = new JLabel("Installation Name");
		final JTextField versionName = new JTextField(this.version.versionId, 20);
		JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JLabel statusLabel = new JLabel();
		JPanel locationRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel locationLabel = new JLabel("Installation Location");
		final JRadioButton local = new JRadioButton("Local");
		JRadioButton appdata = new JRadioButton("Default");
		JPanel okayRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JButton doneButton = new JButton("Make Copy");
		
		versionName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent event) {
				this.onChange();
			}
			@Override
			public void insertUpdate(DocumentEvent event) {
				this.onChange();
			}
			@Override
			public void removeUpdate(DocumentEvent event) {
				this.onChange();
			}
			private void onChange() {
				DuplicateVersionMenu.this.updateVersionStatus(versionName.getText(), local.isSelected(), statusLabel, doneButton);
			}
		});
		ActionListener onChange = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				DuplicateVersionMenu.this.updateVersionStatus(versionName.getText(), local.isSelected(), statusLabel, doneButton);
			}
		};
		local.addActionListener(onChange);
		appdata.addActionListener(onChange);
		local.setSelected(true);
		ButtonGroup locationGroup = new ButtonGroup();
		locationGroup.add(local);
		locationGroup.add(appdata);
		doneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				DuplicateVersionMenu.this.doneAction(versionName.getText(), local.isSelected());
			}
		});
		
		versionNameRow.add(versionNameLabel);
		versionNameRow.add(versionName);
		statusRow.add(statusLabel);
		locationRow.add(locationLabel);
		locationRow.add(local);
		locationRow.add(appdata);
		okayRow.add(doneButton);
		root.add(versionNameRow);
		root.add(statusRow);
		root.add(locationRow);
		root.add(okayRow);
		
		this.updateVersionStatus(versionName.getText(), local.isSelected(), statusLabel, doneButton);
		return root;
	}
	
	private void updateVersionStatus(String name, boolean isLocal, JLabel status, JButton doneButton) {
		File base = new File(Utils.getMCPath(isLocal ? Utils.PATH_LOCAL : Utils.PATH_DEFAULTMC));
		File target = new File(new File(base, "versions"), name);
		if (target.exists()) {
			status.setText(name + " already exists in " + (isLocal ? "local" : "default") + " path");
			doneButton.setEnabled(false);
		} else {
			if (name.matches("[a-zA-Z0-9\\.]+") && name.length() <= 20) {
				status.setText(name + " is a valid installation name");
				doneButton.setEnabled(true);
			} else {
				status.setText("Name must be alphanumeric, and between 1 20 characters inclusive");
				doneButton.setEnabled(false);
			}
		}
	}
	private void doneAction(final String name, boolean isLocal) {
		final File destinationRoot = new File(Utils.getMCPath(isLocal ? Utils.PATH_LOCAL : Utils.PATH_DEFAULTMC));
		File destSpecifics = new File(destinationRoot, "versions/" + name);
		destSpecifics.mkdirs();
		try {
			SimpleUtils.copyFile(new File(this.version.versionLocation, this.version.name + ".jar"), new File(destSpecifics, this.version.name + ".jar"));
			SimpleUtils.copyFile(this.version.configFile, new File(destSpecifics, name + ".json"));
			File[] all = this.version.versionLocation.listFiles();
			for (int i = 0; i < all.length; i++) {
				if (all[i].isDirectory()) {
					SimpleUtils.copyFile(all[i], new File(destSpecifics, all[i].getName()));
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error copying versions. Please see the console");
			this.dispose();
			return;
		}
		if (isLocal != this.version.isLocal) {
			final SimpleSwingWaiter waiter = new SimpleSwingWaiter("MS2 - Downloading");
			final MCDownloader downloader = new MCDownloader();
			waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
				@Override
				public Void doInBackground() {
					if (downloader.downloadGenerics(DuplicateVersionMenu.this.version, destinationRoot, name)) {
						waiter.doneMessage = "Download appears to have completed succesfully";
					} else {
						waiter.doneMessage = "There appears to be an error downloading the files. Please check the console";
					}
					return null;
				}
			};
			downloader.aggregate.addListener(waiter.stdout());
			new PrintStream(downloader.aggregate).println("Ensuring libraries and resources/assets are present...");
			waiter.run();
		}
		this.dispose();
	}
}
