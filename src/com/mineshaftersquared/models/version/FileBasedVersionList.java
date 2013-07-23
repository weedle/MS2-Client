package com.mineshaftersquared.models.version;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class FileBasedVersionList extends VersionList {
	@Override
	protected String getUrl(String uri) throws IOException {
		InputStream inputStream = this.getFileInputStream(uri);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder result = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			if (result.length() > 0) {
				result.append("\n");
			}
			result.append(line);
		}

		reader.close();

		return result.toString();
	}

	protected abstract InputStream getFileInputStream(String paramString) throws FileNotFoundException;
}