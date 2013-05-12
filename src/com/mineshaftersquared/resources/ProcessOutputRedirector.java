/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.creatifcubed.simpleapi.SimpleStreams;
import com.mineshaftersquared.UniversalLauncher;

/**
 * 
 * @author Adrian
 */
public class ProcessOutputRedirector implements Runnable {
	private Process process;
	private String log;

	public ProcessOutputRedirector(Process process, String log) {
		this.process = process;
		this.log = log;
	}

	@Override
	public void run() {
		//SimpleStreams.pipeStreamsConcurrently(this.process.getInputStream(), System.out);
		//SimpleStreams.pipeStreamsConcurrently(this.process.getErrorStream(), System.err);
		new Thread(new InputStreamRedirector(this.process.getInputStream(), System.out, "out")).start();
		new Thread(new InputStreamRedirector(this.process.getErrorStream(), System.err, "err")).start();
	}

	private class InputStreamRedirector implements Runnable {
		private final BufferedReader in;
		private final PrintStream out;
		private final String streamName;
		public InputStreamRedirector(InputStream in, PrintStream out, String streamName) {
			this.in = new BufferedReader(new InputStreamReader(in));
			this.out = out;
			this.streamName = streamName;
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					String str = this.in.readLine();
					if (str == null) {
						break;
					}
					this.out.println(String.format(ProcessOutputRedirector.this.log, str, this.streamName));
				} catch (Exception ex) {
					UniversalLauncher.log.info("ProcessOutputRedirector: caught exception {" + ex.getMessage() + "}");
					break;
				}
			}
			UniversalLauncher.log.info("ProcessOutputRedirector for {" + ProcessOutputRedirector.this.log + "} is done");
		}
	}
}
