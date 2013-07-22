package com.mineshaftersquared.misc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.creatifcubed.simpleapi.SimpleUtils;
import com.creatifcubed.simpleapi.swing.SimpleSwingWaiter;
import com.creatifcubed.simpleapi.SimpleOS;
import com.mineshaftersquared.UniversalLauncher;
import com.mineshaftersquared.models.LocalMCVersion;
import com.mineshaftersquared.models.MCOneSixAuth;
import com.mineshaftersquared.models.MCProfile;
import com.mineshaftersquared.models.MCVersion;
import com.mineshaftersquared.models.MCVersion.MCVersionDetails;

public class MCLauncher {

	private final UniversalLauncher app;

	public MCLauncher(UniversalLauncher app) {
		this.app = app;
	}

	private void ensureDependencies(final MCProfile profile, final MCVersion version, final MCDownloader downloader) {
		SimpleSwingWaiter waiter = new SimpleSwingWaiter("Downloading Minecraft", this.app.mainWindow());
		OutputStream out = waiter.stdout();
		waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
			@Override
			protected Void doInBackground() throws Exception {
				downloader.downloadVersion(version, profile.getGameDir(), version.versionId);
				return null;
			}
		};
		waiter.run();
		downloader.aggregate.removeListener(out);
	}

	public void launch(MCProfile profile) {
		final MCDownloader downloader = new MCDownloader(this.app);
		MCVersion version = this.app.versionsManager.find(profile.getVersionId());
		this.ensureDependencies(profile, version, downloader);
		
		MCOneSixAuth.Response authResponse = this.app.authResponse();
		if (authResponse == null) {
			authResponse = new MCOneSixAuth.Response(null, null, null);
		}
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("auth_username", authResponse.username == null ? System.getProperty("user.name") : authResponse.username);
		map.put("auth_player_name", authResponse.username == null ? System.getProperty("user.name") : authResponse.username);
		map.put("auth_uuid", authResponse.accessToken == null ? this.randomAuthToken() : authResponse.clientToken);
		map.put("auth_session", authResponse.accessToken == null ? this.randomAuthToken() : authResponse.accessToken);

		map.put("profile_name", "default");
		map.put("version_name", version.versionId);

		File root = profile.getGameDir();
		File gameDir = new File(new File(root, "versions"), version.versionId);
		try {
			map.put("game_directory", gameDir.getCanonicalPath());
			map.put("game_assets", new File(root, "assets").getCanonicalPath());
			MCVersionDetails details = version.getDetails();
			String[] mcArgs = details.minecraftArguments;
			for (int i = 0; i < mcArgs.length; i++) {
				String replacement = map.get(mcArgs[i].substring("${".length(), mcArgs[i].length() - "}".length()));
				if (replacement != null) {
					mcArgs[i] = replacement;
				}
			}

			File natives = downloader.unpackNatives(version, profile.getGameDir(), version.versionId);
			if (natives == null) {
				throw new IOException("Error unpacking natives");
			}
			natives.deleteOnExit();

			List<String> args = new LinkedList<String>();
			args.add("java");
			int min = this.app.prefs.getInt("runtime.ram.min", 0);
			int max = this.app.prefs.getInt("runtime.ram.max", 0);
			if (min > 0) {
				args.add("-Xms" + min + "m");
			}
			if (max > 0) {
				args.add("-Xmx" + max + "m");
			}
			args.add("-Djava.library.path=" + natives.getCanonicalPath());
			args.add("-cp");
			List<File> paths = version.getClassPath(SimpleOS.getOS(), root);
			paths.add(SimpleUtils.getJarPath(UniversalLauncher.class));
			
			args.add(this.buildClassPath(paths.toArray(new File[paths.size()])));
			
			
			args.add(details.mainClass);
			args.addAll(Arrays.asList(mcArgs));
			UniversalLauncher.log.info("Launching args:" + args);
			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();

			new Thread(new JavaProcessOutputRedirector(p, "[MS2-Game]: %s")).start();
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this.app.mainWindow(), "Unable to launch game. Please see debug console");
		}
	}

	private String buildClassPath(File[] files) throws IOException {
		List<String> paths = new ArrayList<String>(files.length);
		for (File each : files) {
			paths.add(each.getCanonicalPath());
		}
		return StringUtils.join(paths, System.getProperty("path.separator"));
	}
	
	private String randomAuthToken() {
		Random r = new Random(System.nanoTime());
		return Long.toHexString(r.nextLong()) + Long.toHexString(r.nextLong());
	}
}
