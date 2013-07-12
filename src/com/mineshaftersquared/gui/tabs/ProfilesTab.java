package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.mineshaftersquared.UniversalLauncher;

public class ProfilesTab extends JPanel {
	private final UniversalLauncher app;
	
	public ProfilesTab(UniversalLauncher app) {
		super(new BorderLayout());
		this.app = app;
		
		this.add(this.createProfilesPanel(), BorderLayout.CENTER);
		this.add(this.createToolbarPanel(), BorderLayout.SOUTH);
	}
	
	private JPanel createProfilesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		ProfilesTableModel tableModel = new ProfilesTableModel();
		
		JTable table = new JTable(tableModel);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel createToolbarPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // 5 is default
		
		JButton add = new JButton("Add");
		JButton edit = new JButton("Edit");
		JButton delete = new JButton("Delete");
		JButton refresh = new JButton("Refresh");
		
		panel.add(add);
		panel.add(edit);
		panel.add(delete);
		panel.add(refresh);
		
		return panel;
	}
	
	private static class ProfilesTableModel extends AbstractTableModel {
		
		private final String[] COLUMNS = new String[] {"Name", "Version"};
		
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
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return "Hello world";
		}
		
	}
}
