package com.mineshaftersquared.misc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.mineshaftersquared.models.MCOneSixAuth;
import com.mineshaftersquared.models.profile.Profile;
import com.mineshaftersquared.models.version.CompleteVersion;
import com.mineshaftersquared.models.version.ReleaseType;
import com.mineshaftersquared.models.version.Version;
import com.mineshaftersquared.models.version.VersionSyncInfo;

public class MCLauncher {

	private final UniversalLauncher app;

	public MCLauncher(UniversalLauncher app) {
		this.app = app;
	}

	private void ensureDependencies(final Profile profile, final Version version, final MCDownloader downloader) {
		SimpleSwingWaiter waiter = new SimpleSwingWaiter("Downloading Minecraft", this.app.mainWindow());
		OutputStream out = waiter.stdout();
		waiter.worker = new SimpleSwingWaiter.Worker(waiter) {
			@Override
			protected Void doInBackground() throws Exception {
				downloader.downloadVersion(version, profile.getGameDir());
				return null;
			}
		};
		waiter.run();
		downloader.aggregate.removeListener(out);
	}

	public void launch(Profile profile) {
		final MCDownloader downloader = new MCDownloader(this.app);
		String lastId = profile.getLastVersionId();
		VersionSyncInfo syncinfo = null;
		if (lastId == null) {
			syncinfo = this.app.versionManager.getVersions(profile.getVersionFilter()).get(0);
		} else {
			syncinfo = this.app.versionManager.getVersionSyncInfo(lastId);
		}
		Version v = null;
		CompleteVersion version = null;
		try {
			v = this.app.versionManager.getLatestCompleteVersion(syncinfo);
			this.ensureDependencies(profile, v, downloader);

			version = this.app.versionManager.localVersionList.getCompleteVersion(v);
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this.app.mainWindow(), "Unable to launch. Please see debug console");
			return;
		}
		
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
		map.put("version_name", version.getId());

		File root = profile.getGameDir();
		File gameDir = new File(new File(root, "versions"), version.getId());
		try {
			map.put("game_directory", gameDir.getCanonicalPath());
			map.put("game_assets", new File(root, "assets").getCanonicalPath());
			String[] mcArgs = version.getMinecraftArguments().split(" ");
			for (int i = 0; i < mcArgs.length; i++) {
				String replacement = map.get(mcArgs[i].substring("${".length(), mcArgs[i].length() - "}".length()));
				if (replacement != null) {
					mcArgs[i] = replacement;
				}
			}

			File natives = downloader.unpackNatives(version, profile.getGameDir());
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
			Collection<File> paths = version.getClassPath(SimpleOS.getOS(), root);
			paths.add(SimpleUtils.getJarPath(UniversalLauncher.class));
			
			args.add(this.buildClassPath(paths.toArray(new File[paths.size()])));
			
			
			args.add(version.getMainClass());
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
