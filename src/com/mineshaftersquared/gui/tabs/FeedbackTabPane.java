/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.creatifcubed.simpleapi.SimpleHTTPRequest;
import com.creatifcubed.simpleapi.SimpleISettings;
import com.creatifcubed.simpleapi.SimpleWaiter;
import com.creatifcubed.simpleapi.swing.SimpleSwingUtils;
import com.mineshaftersquared.UniversalLauncher;

/**
 * 
 * @author Adrian
 */
public class FeedbackTabPane extends AbstractTabPane {

	private SimpleISettings prefs;

	public FeedbackTabPane(SimpleISettings prefs) {
		this.prefs = prefs;
		JPanel feedbackPane = new JPanel(new GridBagLayout());
		feedbackPane.setBorder(SimpleSwingUtils.createLineBorder("Feedback/Bug Reports"));
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipadx = 40;
		c.ipady = 20;

		c.gridx = 0;

		c.gridy = 0;
		c.gridwidth = 2;
		feedbackPane.add(this.createFeedbackNoticePanel(), c);
		c.gridwidth = 1;

		final JTextField emailField = new JTextField(20);
		final JTextArea feedbackContentField = new JTextArea(10, 40);

		feedbackContentField.setLineWrap(true);
		feedbackContentField.setWrapStyleWord(true);

		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String email = emailField.getText().trim();

				if (email.isEmpty()) {
					String[] options = new String[] { "Submit now", "Let me add my email" };
					if (JOptionPane.showOptionDialog(null,
							"Your email is empty. We cannot help you if you don't give us your email.?",
							"MS2 - Feedback", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]) == 1) {
						return;
					}
				}
				new SimpleWaiter("Submitting", new Runnable() {
					@Override
					public void run() {
						try {
							String email = emailField.getText().trim();
							String feedbackContent = feedbackContentField.getText().trim();

							if (email.isEmpty()) {
								email = "n/a";
							}

							byte[] returned = new SimpleHTTPRequest("http://"
									+ UniversalLauncher.POLLING_SERVER + "feedback.php")
									.addPost("content", feedbackContent)
									.addPost("email", email)
									.addPost("version", UniversalLauncher.MS2_VERSION.toString())
									.addPost("os", System.getProperty("os.name"))
									.addPost("username", FeedbackTabPane.this.prefs.tmpGetString("username", ""))
									.addPost("java", System.getProperty("java.version"))
									.doPost(SimpleHTTPRequest.NO_PROXY);
							System.out.println("Feedback: " + new String(returned));
							JOptionPane
									.showMessageDialog(
											null,
											"Thanks for your feedback! Please be patient - we get a lot of mail!"
													+ "\nIn the mean time, why don't you check out the FAQs for common problems/solutions"
													+ "\nat ms2.creatifcubed.com/more.php (it's long because it's useful)");
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}, null).run();
			}
		});

		c.gridx = 0;
		c.gridy++;
		c.ipadx = 5;
		c.ipady = 5;
		feedbackPane.add(new JLabel("Email (recommended)"), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		feedbackPane.add(emailField, c);
		c.ipadx = 40;
		c.ipady = 20;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		feedbackPane.add(new JScrollPane(feedbackContentField), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.ipadx = 5;
		c.ipady = 5;
		// c.anchor = GridBagConstraints.WEST;
		feedbackPane.add(submit, c);

		this.add(feedbackPane);
	}

	private JPanel createFeedbackNoticePanel() {
		JPanel feedbackNoticePanel = new JPanel(new GridLayout(0, 1));

		feedbackNoticePanel.add(new JLabel("<html>If you need help, you need to include your email."));
		feedbackNoticePanel.add(new JLabel(
				"We are not psychic; the more info you give us, the faster we can fix something."));
		feedbackNoticePanel.add(new JLabel("READ FAQ FOR COMMON PROBLEMS AT ms2.creatifcubed.com/more.php"));

		return feedbackNoticePanel;
	}
}
