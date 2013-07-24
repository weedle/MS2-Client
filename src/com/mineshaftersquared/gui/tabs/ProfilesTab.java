package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

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
import com.mineshaftersquared.gui.profiles.AddEditProfileForm;
import com.mineshaftersquared.models.profile.Profile;

public class ProfilesTab extends JPanel {
	private final UniversalLauncher app;
	private Profile[] profiles;
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
		try {
			this.app.profilesManager.loadProfiles();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Map<String, Profile> profilesMap = this.app.profilesManager.getProfiles();
		this.profiles = new Profile[profilesMap.size()];
		int i = 0;
		for (String key : profilesMap.keySet()) {
			this.profiles[i] = profilesMap.get(key);
			i++;
		}
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
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Profile profile = ProfilesTab.this.selectedProfile();
				if (profile == null) {
					return;
				}
				JFrame frame = new AddEditProfileForm(ProfilesTab.this.app, ProfilesTab.this, profile);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Profile profile = ProfilesTab.this.selectedProfile();
				if (profile == null) {
					return;
				}
				try {
					if (!ProfilesTab.this.app.profilesManager.deleteProfile(profile)) {
						JOptionPane.showMessageDialog(ProfilesTab.this.app.mainWindow(), "Unknown error deleting profile. Please contact the developer");
					}
					if (!ProfilesTab.this.app.profilesManager.saveProfiles()) {
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
	
	private Profile selectedProfile() {
		int selectedIndex = this.table.getSelectedRow();
		if (selectedIndex == -1) {
			return null;
		}
		return this.profiles[selectedIndex];
	}
	
	private class ProfilesTableModel extends AbstractTableModel {
		
		private final String[] COLUMNS = new String[] {"Name", "Version", "Game Dir", "Java Args"};
		
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
			Profile profile = ProfilesTab.this.profiles[row];
			switch (col) {
			case 0:
				return profile.getName();
			case 1:
				return profile.getLastVersionId();
			case 2:
				return profile.getGameDir();
			case 3:
				return profile.getJavaArgs();
			}
			return "Unknown column";
		}
		
	}
}
