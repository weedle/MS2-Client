package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleWaiter;
import com.mineshaftersquared.models.MCVersion;
import com.mineshaftersquared.resources.GameUpdaterProxy;
import com.mineshaftersquared.resources.MCDownloader;
import com.mineshaftersquared.resources.Utils;

public class VersionsTabPane extends AbstractTabPane {
	private final SimpleISettings prefs;
	private static final String[] COLUMNS = {"Version ID", "Type", "Release Date", "Location"};
	private MCVersion[] mcVersions;
	private final JTable localVersionsTable;
	private final VersionsTableModel localVersionsTableModel;
	private final JComboBox remoteVersionsComboBox;
	
	public VersionsTabPane(SimpleISettings prefs) {
		super(new BorderLayout());
		this.prefs = prefs;
		this.localVersionsTableModel = new VersionsTableModel();
		this.mcVersions = null;
		this.localVersionsTable = new JTable(this.localVersionsTableModel);
		this.add(new JScrollPane(this.localVersionsTable), BorderLayout.CENTER);
		JPanel remotesToolbar = new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.remoteVersionsComboBox = new JComboBox(new DefaultComboBoxModel());
		
		JButton refreshRemotes = new JButton("Refresh");
		refreshRemotes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				VersionsTabPane.this.reloadData();
			}
		});
		JButton download = new JButton("Download");
		download.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				new SimpleWaiter("MS2 - Downloading", new Runnable() {
					@Override
					public void run() {
						MCDownloader.downloadVersion(MCVersion.getLatestSnapshot(), new File(System.getProperty("user.dir")));
					}
				}, null).run();
			}
		});
		JButton refreshResources = new JButton("Clean Resources");
		remotesToolbar.add(new JLabel("Remote Versions"));
		remotesToolbar.add(this.remoteVersionsComboBox);
		remotesToolbar.add(download);
		remotesToolbar.add(refreshRemotes);
		remotesToolbar.add(refreshResources);
		this.add(remotesToolbar, BorderLayout.NORTH);
		
		JPanel localsToolbar = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton delete = new JButton("Delete");
		JButton show = new JButton("Show in Folder");
		JButton duplicate = new JButton("Duplicate");
		JButton refreshLocals = new JButton("Refresh");
		
		localsToolbar.add(new JLabel("Local Versions"));
		localsToolbar.add(delete);
		localsToolbar.add(show);
		localsToolbar.add(duplicate);
		localsToolbar.add(refreshLocals);
		this.add(localsToolbar, BorderLayout.SOUTH);
		
		this.reloadData();
	}
	
	private void reloadData() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				VersionsTabPane.this.remoteVersionsComboBox.setModel(new DefaultComboBoxModel());
				VersionsTabPane.this.mcVersions = MCVersion.getVersions(MCVersion.VERSIONS_LIST_URL, true);
				VersionsTabPane.this.localVersionsTableModel.fireTableDataChanged();
				String[] versions = new String[VersionsTabPane.this.mcVersions.length];
				for (int i = 0; i < versions.length; i++) {
					versions[i] = VersionsTabPane.this.mcVersions[i].versionId;
				}
				VersionsTabPane.this.remoteVersionsComboBox.setModel(new DefaultComboBoxModel(versions));
			}
		}).start();
	}
	
	private class VersionsTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public int getRowCount() {
			if (VersionsTabPane.this.mcVersions != null) {
				return VersionsTabPane.this.mcVersions.length;
			}
			return 0;
		}

		@Override
		public Object getValueAt(int row, int col) {
			MCVersion version = VersionsTabPane.this.mcVersions[row];
			switch (col) {
			case 0:
				return version.versionId;
			case 1:
				return version.type.toString();
			case 2:
				return version.releaseTime.toString();
			case 3:
				return "Local";
			}
			return null;
		}
		
		@Override
		public String getColumnName(int column) {
			return COLUMNS[column];
		}
	}
}
