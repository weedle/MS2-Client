/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mineshaftersquared.resources;

import java.io.BufferedReader;
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
		BufferedReader stdout = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
		while (true) {
			try {
				String str = stdout.readLine();
				if (str == null) {
					throw new NullPointerException("reading stdout was null (is okay)");
				}
				UniversalLauncher.log.info(this.log + " - " + str);
			} catch (Exception ex) {
				UniversalLauncher.log.info("{ProcessOutputRedirector: caught exception {" + ex.getMessage() + "}, is done}");
				break;
			}
		}
	}

}
