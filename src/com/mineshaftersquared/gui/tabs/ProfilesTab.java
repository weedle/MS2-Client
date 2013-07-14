package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;

import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.gui.misc.AddEditProfileForm;
import com.mineshaftersquared.models.MCProfile;

public class ProfilesTab extends JPanel {
	private final UniversalLauncher app;
	private MCProfile[] profiles;
	private JTable table;
	private ProfilesTableModel tableModel;
	
	public ProfilesTab(UniversalLauncher app) {
		super(new BorderLayout());
		this.app = app;
		
		this.tableModel = new ProfilesTableModel();
		this.table = new JTable(this.tableModel);
		this.profiles = null;
		this.refreshProfiles();
		
		this.add(this.createProfilesPanel(), BorderLayout.CENTER);
		this.add(this.createToolbarPanel(), BorderLayout.SOUTH);
	}
	
	public void refreshProfiles() {
		this.app.profileManager.refreshProfiles();
		this.profiles = this.app.profileManager.profilesAsArray();
		this.tableModel.fireTableDataChanged();
	}
	
	private JPanel createProfilesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel createToolbarPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // 5 is default
		
		JButton add = new JButton("Add");
		JButton edit = new JButton("Edit");
		JButton delete = new JButton("Delete");
		JButton refresh = new JButton("Refresh");
		
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JFrame frame = new AddEditProfileForm(ProfilesTab.this.app, ProfilesTab.this, null);
				frame.pack();
				frame.setVisible(true);
			}
		});
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				MCProfile profile = ProfilesTab.this.selectedProfile();
				if (profile == null) {
					return;
				}
				JFrame frame = new AddEditProfileForm(ProfilesTab.this.app, ProfilesTab.this, profile);
				frame.pack();
				frame.setVisible(true);
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				MCProfile profile = ProfilesTab.this.selectedProfile();
				if (profile == null) {
					return;
				}
				if (!ProfilesTab.this.app.profileManager.deleteProfile(profile)) {
					JOptionPane.showMessageDialog(ProfilesTab.this.app.mainWindow(), "Unknown error deleting profile. Please contact the developer");
				}
				try {
					if (!ProfilesTab.this.app.profileManager.saveProfiles()) {
						JOptionPane.showMessageDialog(ProfilesTab.this.app.mainWindow(), "Unknown error saving profiles. Please contact the developer");
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(ProfilesTab.this.app.mainWindow(), "Exception saving profiles: " + ex.getMessage());
				}
				ProfilesTab.this.refreshProfiles();
			}
		});
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ProfilesTab.this.refreshProfiles();
			}
		});
		
		panel.add(add);
		panel.add(edit);
		panel.add(delete);
		panel.add(refresh);
		
		return panel;
	}
	
	private MCProfile selectedProfile() {
		int selectedIndex = this.table.getSelectedRow();
		if (selectedIndex == -1) {
			return null;
		}
		return this.profiles[selectedIndex];
	}
	
	private class ProfilesTableModel extends AbstractTableModel {
		
		private final String[] COLUMNS = new String[] {"Name", "Version", "Game Dir", "Java Args", "Is Local"};
		
		@Override
		public String getColumnName(int index) {
			return COLUMNS[index];
		}
		
		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public int getRowCount() {
			return ProfilesTab.this.profiles.length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			MCProfile profile = ProfilesTab.this.profiles[row];
			switch (col) {
			case 0:
				return profile.getName();
			case 1:
				return profile.getVersionId();
			case 2:
				return profile.getGameDir().toString();
			case 3:
				return StringUtils.join(profile.getJavaArgs(), " ");
			case 4:
				return profile.getIsLocal();
			}
			return "Unknown column";
		}
		
	}
}
