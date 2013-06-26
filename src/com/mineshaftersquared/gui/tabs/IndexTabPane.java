/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleSwingWaiter;
import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.SimpleWaiter;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.creatifcubed.simpleapi.swing.SimpleWrappedLabel;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.LocalMCVersion;
import com.mineshaftersquared.models.OldAuth;
import com.mineshaftersquared.resources.GameUpdaterProxy;
import com.mineshaftersquared.resources.JarProcessBuilder;
import com.mineshaftersquared.resources.ProcessOutputRedirector;
import com.mineshaftersquared.resources.Utils;

/**
 * 
 * @author Adrian
 */
public class IndexTabPane extends AbstractTabPane {

	private SimpleISettings prefs;
	private JButton launchButton;
	private JTextField usernameField;
	private JComboBox versionsChooser;
	private LocalMCVersion[] localVersions;

	public IndexTabPane(SimpleISettings prefs) {
		this.launchButton = null;
		this.usernameField = null;
		this.versionsChooser = null;
		this.prefs = prefs;
		this.localVersions = null;
		this.add(this.createUpdatesPanel());
		this.add(this.createLoginPanel());
		this.add(this.createLaunchPanel());

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				IndexTabPane.this.refreshVersions();
			}
		});
	}

	public JPanel createLaunchPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.setBorder(SimpleSwingUtils.createLineBorder("Launch"));

		this.versionsChooser = new JComboBox(new DefaultComboBoxModel());
		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = IndexTabPane.this.versionsChooser.getSelectedIndex();
				System.out.println("Selected index: " + index);
			}
		});
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				IndexTabPane.this.refreshVersions();
			}
		});
		JButton download = new JButton("Download/Manage Versions");
		download.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				((UniversalLauncher) IndexTabPane.this.prefs.tmpGetObject("instance")).getMainWindow().setActiveTab(1);
			}
		});

		panel.add(launch);
		panel.add(this.versionsChooser);
		panel.add(refresh);
		panel.add(download);
		return panel;
	}

	private void refreshVersions() {
		this.localVersions = SimpleUtils.appendArrays(Utils.getLocalLocationVersions(), Utils.getDefaultLocationVersions());
		String[] versions = new String[this.localVersions.length];
		for (int i = 0; i < versions.length; i++) {
			versions[i] = this.localVersions[i].name + " - " + (this.localVersions[i].isLocal ? "Local" : "App Data");
		}
		this.versionsChooser.setModel(new DefaultComboBoxModel(versions));
	}

	public JPanel oldcreateLaunchPanel() {
		JPanel launchPanel = new JPanel(new GridBagLayout());
		launchPanel.setBorder(SimpleSwingUtils.createLineBorder("Launch"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;

		JButton launchButton = new JButton("Play offline");
		this.launchButton = launchButton;
		launchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int pathfind = IndexTabPane.this.prefs.getInt("launcher.pathfind", Utils.PATH_LOCAL);
				String path = Utils.getMCPath(pathfind);
				// UniversalLauncher.log.info("EXISTS: " + !(new File(path).exists()));
				// UniversalLauncher.log.info("path: " + path);
				if (!Utils.existsInstallationIn(pathfind)) {
					JOptionPane.showMessageDialog(null, "Minecraft not found in: {" + path + "}"
							+ "\nAre you sure you chose the right path location (local vs. default location)?");
					return;
				}
				if (!IndexTabPane.this.prefs.tmpHas("username")) {
					String attemptUsername = IndexTabPane.this.usernameField.getText();
					if (attemptUsername.trim().isEmpty()) {
						attemptUsername = System.getProperty("user.name");
					}
					String username = JOptionPane.showInputDialog("Specify offline username", attemptUsername);
					if (username.trim().isEmpty()) {
						username = System.getProperty("user.name");
					}
					IndexTabPane.this.prefs.tmpPut("username", username);
				}
				try {
					Process p = JarProcessBuilder.create(
							SimpleUtils.getJarPath().getCanonicalPath(),
							IndexTabPane.this.prefs.getInt("runtime.ram.min", 0),
							IndexTabPane.this.prefs.getInt("runtime.ram.max", 0),
							null,
							new String[] {
								"regular",
								IndexTabPane.this.prefs.tmpGetString("username", "Player" + System.currentTimeMillis()
										% 1000),
										IndexTabPane.this.prefs.tmpGetString("sessionId", "-1"),
										IndexTabPane.this.prefs.getInt("launcher.pathfind", Utils.PATH_LOCAL).toString(),
										IndexTabPane.this.prefs
										.getString("proxy.authserver", UniversalLauncher.DEFAULT_AUTH_SERVER) });
					new Thread(new ProcessOutputRedirector(p, "[MS2-Game]: %s")).start();
					if (IndexTabPane.this.prefs.getInt("launcher.closeonlaunch", 1) == 1) {
						((JFrame) IndexTabPane.this.prefs.tmpGetObject("launcher.window")).dispose();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		final JCheckBox closeOnLaunch = new JCheckBox("Close on launch?",
				this.prefs.getInt("launcher.closeonlaunch", 1) == 1);
		closeOnLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IndexTabPane.this.prefs.put("launcher.closeonlaunch", closeOnLaunch.isSelected() ? 1 : 0);
				IndexTabPane.this.prefs.save();
			}
		});

		ButtonGroup pathfindOptions = new ButtonGroup();
		JRadioButton local = new JRadioButton("Local", true);
		JRadioButton defaultMC = new JRadioButton("Default MC");
		pathfindOptions.add(local);
		pathfindOptions.add(defaultMC);

		local.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IndexTabPane.this.prefs.put("launcher.pathfind", Utils.PATH_LOCAL);
				IndexTabPane.this.prefs.save();
			}
		});
		defaultMC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IndexTabPane.this.prefs.put("launcher.pathfind", Utils.PATH_DEFAULTMC);
				IndexTabPane.this.prefs.save();
			}
		});

		defaultMC.setSelected(this.prefs.getInt("launcher.pathfind", Utils.PATH_LOCAL) == Utils.PATH_DEFAULTMC);

		JButton detectInstallationsButton = new JButton("Detect");
		detectInstallationsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> locations = Utils.listMCInstallations();
				String bin = "";
				for (String loc : locations) {
					bin += "<li>";
					bin += loc;
					bin += "</li>";
				}

				JOptionPane.showMessageDialog(null, "<html><ul>" + bin + "</ul></html>");
			}
		});

		JButton downloadButton = new JButton("Download");
		downloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] map = new int[] { Utils.PATH_LOCAL, Utils.PATH_DEFAULTMC };
				String[] options = new String[] { "Local Folder", "Default MC", "Cancel" };
				final int i = JOptionPane.showOptionDialog(null, "Where do you want to download?",
						"MS2 - Download Options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						options, options[0]);
				if (i != options.length - 1 && i != -1) {
					int forceUpdate = 1; // "no" in dialog
					if (Utils.existsInstallationIn(map[i])) {
						forceUpdate = JOptionPane.showConfirmDialog(null, "Force update?", "MS2 - Download Options",
								JOptionPane.YES_NO_CANCEL_OPTION);
						if (forceUpdate == 2 || forceUpdate == -1) {
							return;
						}
					}
					final int finalForceUpdate = forceUpdate;

				}
			}
		});

		JButton openLocal = new JButton("Open local folder");
		openLocal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = Utils.getMCPath(Utils.PATH_LOCAL);
				String fileExplorer = SimpleUtils.getOSFileExplorer();
				if (fileExplorer == null) {
					fileExplorer = JOptionPane.showInputDialog("Could not find file browser."
							+ "\nSpecify your file browser, or manually go to " + path, "xterm");
				}
				if (fileExplorer != null) {
					SimpleUtils.openFolder(fileExplorer, path);
				}
			}
		});

		JButton openDefaultMC = new JButton("Open app data (default MC location)");
		openDefaultMC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = Utils.getAppDataPath();
				UniversalLauncher.log.info("App data path: " + path);
				String[] options = new String[] {"Go back", "Open app data folder"};
				if (JOptionPane.showOptionDialog(null, String.format("<html>Your app data folder is: %s", path)
						+ "<br />That is the parent directory where Minecraft is normally installed"
						+ String.format("<br />The actual Minecraft (sub)folder is %s", Utils.getMCPath(Utils.PATH_DEFAULTMC))
						, "Minecraft default folder", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]) == 1) {
					String fileExplorer = SimpleUtils.getOSFileExplorer();
					if (fileExplorer == null) {
						fileExplorer = JOptionPane.showInputDialog("Could not find file browser."
								+ "\nSpecify your file browser, or manually go to " + path, "xterm");
					}
					if (fileExplorer != null) {
						new File(Utils.getMCPath(Utils.PATH_DEFAULTMC)).mkdir();
						SimpleUtils.openFolder(fileExplorer, path);

					}
				}
			}
		});

		c.anchor = GridBagConstraints.WEST;
		launchPanel.add(new JLabel("Go!"), c);
		// c.anchor = GridBagConstraints.CENTER;
		c.gridy++;
		launchPanel.add(launchButton, c);
		c.gridx = 1;
		c.gridwidth = 2;
		launchPanel.add(closeOnLaunch, c);
		c.gridwidth = 1;
		c.gridx = 3;
		launchPanel.add(downloadButton, c);
		c.gridy++;
		c.gridx = 0;
		launchPanel.add(Box.createVerticalStrut(10), c);
		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		launchPanel.add(new JLabel("Game Location"), c);
		// c.anchor = GridBagConstraints.CENTER;
		c.gridy++;
		launchPanel.add(detectInstallationsButton, c);
		c.gridx = 1;
		launchPanel.add(local, c);
		c.gridx = 2;
		launchPanel.add(defaultMC, c);
		c.gridx = 3;
		launchPanel.add(openLocal, c);
		c.gridx = 4;
		launchPanel.add(openDefaultMC, c);

		return launchPanel;
	}

	public JPanel createLoginPanel() {
		JPanel loginPanel = new JPanel(new GridBagLayout());
		loginPanel.setBorder(SimpleSwingUtils.createLineBorder("Login"));
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty = 0.1;
		c.ipady = 5;
		c.ipadx = 5;

		loginPanel.add(new JLabel("Username"), c);
		c.gridy++;
		loginPanel.add(new JLabel("Password"), c);
		c.gridy++;
		c.anchor = GridBagConstraints.NORTHEAST;
		final JCheckBox rememberme = new JCheckBox("Remember me?",
				this.prefs.getInt("launcher.remembercredentials", 0) == 1);
		loginPanel.add(rememberme, c);

		rememberme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IndexTabPane.this.prefs.put("launcher.remembercredentials", rememberme.isSelected() ? 1 : 0);
				IndexTabPane.this.prefs.save();
			}
		});

		c.gridx = 1;
		c.gridy = 0;
		final JTextField usernameField = new JTextField(20);
		usernameField.setText(this.prefs.getString("launcher.username", ""));
		this.usernameField = usernameField;
		loginPanel.add(usernameField, c);
		c.gridy++;
		final JPasswordField passwordField = new JPasswordField(20);
		passwordField.setText(this.prefs.getString("launcher.password", ""));
		loginPanel.add(passwordField, c);
		c.gridy++;
		c.anchor = GridBagConstraints.NORTHEAST;
		JButton loginButton = new JButton("Login");
		loginPanel.add(loginButton, c);

		ActionListener loginAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleSwingWaiter waiter = new SimpleSwingWaiter("Logging in");
				waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
					@Override
					public Void doInBackground() {
						String username = usernameField.getText();
						String password = new String(passwordField.getPassword());

						OldAuth.Response response = OldAuth.login(username, password, IndexTabPane.this.prefs.getString("proxy.authserver",
								UniversalLauncher.DEFAULT_AUTH_SERVER));
						if (response.sessionId != null) {
							IndexTabPane.this.prefs.tmpPut("username", response.username);
							IndexTabPane.this.prefs.tmpPut("sessionId", response.sessionId);
							IndexTabPane.this.launchButton.setText("Launch");
							if (rememberme.isSelected()) {
								IndexTabPane.this.prefs.put("launcher.username", username);
								IndexTabPane.this.prefs.put("launcher.password", password);
							} else {
								IndexTabPane.this.prefs.put("launcher.username", "");
								IndexTabPane.this.prefs.put("launcher.password", "");
							}
							IndexTabPane.this.prefs.save();
							JOptionPane.showMessageDialog(null, "Thanks for loggin in!");
						} else {
							JOptionPane.showMessageDialog(null, "Unable to log in: " + response.message
									+ "\nYou can still play offline"
									+ "\nDo not send feedback. Read FAQs at ms2.creatifcubed.com/more.php");
							if (usernameField.getText().trim().toUpperCase().startsWith("[MS2]")) {
								if (!IndexTabPane.this.prefs.getString("proxy.authserver",
										UniversalLauncher.DEFAULT_AUTH_SERVER).equals(
												UniversalLauncher.BETA_AUTH_SERVER)) {
									JOptionPane.showMessageDialog(null, "It looks like you have a new [MS2] account"
											+ "\nYou need to set the auth server to the alpha server (settings tab)");
								}
							}
							IndexTabPane.this.prefs.tmpRemove("username");
							IndexTabPane.this.prefs.tmpRemove("sessionId");
							IndexTabPane.this.launchButton.setText("Play offline");
						}
						return null;
					}
				};
				waiter.run();
			}
		};

		loginButton.addActionListener(loginAction);
		passwordField.addActionListener(loginAction);

		return loginPanel;
	}

	public JPanel createUpdatesPanel() {
		JPanel updatesPanel = new JPanel(new BorderLayout());
		updatesPanel.setBorder(SimpleSwingUtils.createLineBorder("Updates"));

		final SimpleWrappedLabel updatesLabel = new SimpleWrappedLabel(
				"<html><ul><li>Checking updates...</li></ul></html>", 550);

		new Thread(new Runnable() {
			@Override
			public void run() {
				String updates = "<ul><li>Unable to check updates</li>"
						+ "<li>You should check manually at ms2.creatifcubed.com</li></ul>";
				try {
					updates = new String(new SimpleHTTPRequest("http://" + UniversalLauncher.POLLING_SERVER
							+ "updates_messages.php").doGet(Proxy.NO_PROXY));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				updatesLabel.wrapText(updates, 550);
			}
		}).start();

		JScrollPane scroll = new JScrollPane(updatesLabel);
		scroll.getVerticalScrollBar().setUnitIncrement(4);
		updatesPanel.add(scroll);

		return updatesPanel;
	}
}
