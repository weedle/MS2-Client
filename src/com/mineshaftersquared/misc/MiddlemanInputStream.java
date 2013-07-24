package com.mineshaftersquared.misc;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;

public class MiddlemanInputStream extends FilterInputStream {
	private final LinkedHashSet<Listener> listeners;
	public MiddlemanInputStream(InputStream in) {
		super(in);
		this.listeners = new LinkedHashSet<Listener>();
	}
	
	@Override
	public int read() throws IOException {
		int bytes = super.read();
		this.onChange(bytes);
		return bytes;
	}
	
	@Override
	public long skip(long n) throws IOException {
		long bytes = super.skip(n);
		this.onChange(bytes);
		return bytes;
	}
	
	public synchronized void addListener(Listener l) {
		this.listeners.add(l);
	}
	
	
	public synchronized void removeListener(Listener l) {
		this.listeners.add(l);
	}
	
	public synchronized void clearListeners() {
		this.listeners.clear();
	}
	
	public synchronized boolean containsListener(Listener l) {
		return this.listeners.remove(l);
	}
	
	private void onChange(long bytes) {
		for (Listener each : this.listeners) {
			each.onChange(bytes);
		}
	}
	
	public static interface Listener {
		public void onChange(long bytes);
	}
}
