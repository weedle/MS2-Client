/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
		new Thread(new InputStreamRedirector(this.process.getInputStream())).start();
		new Thread(new InputStreamRedirector(this.process.getErrorStream())).start();
		
	}

	private class InputStreamRedirector implements Runnable {
		private final BufferedReader out;
		public InputStreamRedirector(InputStream in) {
			this.out = new BufferedReader(new InputStreamReader(in));
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					String str = this.out.readLine();
					if (str == null) {
						throw new NullPointerException("reading stdout was null (is okay)");
					}
					UniversalLauncher.log.info(ProcessOutputRedirector.this.log + " - " + str);
				} catch (Exception ex) {
					UniversalLauncher.log.info("{ProcessOutputRedirector: caught exception {" + ex.getMessage() + "}, is done}");
					break;
				}
			}
		}
	}
}
