package com.mineshaftersquared.gui;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.creatifcubed.simpleapi.swing.SimpleSwingWaiter;
import com.mineshaftersquared.models.MCVersion;
import com.mineshaftersquared.resources.MCDownloader;
import com.mineshaftersquared.resources.Utils;

public class DownloadVersionMenu extends JDialog {
	private final MCVersion version;
	
	public DownloadVersionMenu(Window owner, String versionId) {
		super(owner, "Download Version " + versionId, JDialog.DEFAULT_MODALITY_TYPE);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.version = MCVersion.find(versionId);
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
		final JButton doneButton = new JButton("Download");
		
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
				DownloadVersionMenu.this.updateVersionStatus(versionName.getText(), local.isSelected(), statusLabel, doneButton);
			}
		});
		ActionListener onChange = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				DownloadVersionMenu.this.updateVersionStatus(versionName.getText(), local.isSelected(), statusLabel, doneButton);
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
				DownloadVersionMenu.this.doneAction(versionName.getText(), local.isSelected());
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
		final File location = new File(Utils.getMCPath(isLocal ? Utils.PATH_LOCAL : Utils.PATH_DEFAULTMC));
		final SimpleSwingWaiter waiter = new SimpleSwingWaiter("MS2 - Downloading");
		final MCDownloader downloader = new MCDownloader();
		waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
			@Override
			public Void doInBackground() {
				if (downloader.downloadVersion(DownloadVersionMenu.this.version, location, name)) {
					waiter.doneMessage = "Download appears to have completed succesfully";
				} else {
					waiter.doneMessage = "There appears to be an error downloading the files. Please check the console";
				}
				return null;
			}
		};
		downloader.aggregate.addListener(waiter.stdout());
		waiter.run();
		this.dispose();
	}
}
