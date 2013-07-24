package com.mineshaftersquared.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;

public class JavaProcessOutputRedirector implements Runnable {
	public final Process process;
	public final String format;
	public JavaProcessOutputRedirector(Process process, String format) {
		this.process = process;
		this.format = format;
	}

	public void run() {
		try {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						JavaProcessOutputRedirector.this.pipeStreams(JavaProcessOutputRedirector.this.process.getInputStream(), System.out);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
			t.start();
			this.pipeStreams(this.process.getErrorStream(), System.err);
			t.join();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println(String.format(this.format, "Done."));
	}

	private void pipeStreams(InputStream in, OutputStream out) throws IOException {
		BufferedReader br = null;
		PrintStream ps = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			ps = new PrintStream(out);
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				ps.println(String.format(this.format, line));
			}
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(ps);
		}
	}
}
