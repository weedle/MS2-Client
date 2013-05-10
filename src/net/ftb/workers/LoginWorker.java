package net.ftb.workers;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.SwingWorker;

import net.ftb.util.AppUtils;
import net.ftb.util.ErrorUtils;

public class LoginWorker extends SwingWorker<String, Void> {
	private String username;
	private String password;

	public LoginWorker(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	protected String doInBackground() {
		try {
			String str = "http://login.minecraft.net/?user=" + URLEncoder.encode(this.username, "UTF-8") + "&password="
					+ URLEncoder.encode(this.password, "UTF-8") + "&version=13";
			System.out.println("LOGIN URL: " + str);
			return AppUtils.downloadString(new URL(str));
		} catch (IOException e) {
			ErrorUtils.tossError("IOException, minecraft servers might be down. Check @ help.mojang.com");
		}
		return "";
	}
}