package com.mineshaftersquared.misc;

import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class EventBus {
	private final Map<String, LinkedHashSet<Listener>> listeners;
	
	public EventBus() {
		this.listeners = new HashMap<String, LinkedHashSet<Listener>>();
	}
	
	public synchronized boolean on(String event, Listener listener) {
		return this.autovivicate(event).add(listener);
	}
	
	public synchronized boolean off(String event, Listener listener) {
		return this.autovivicate(event).remove(listener);
	}
	
	public synchronized void emit(String event, EventObject obj) {
		for (Listener each : this.autovivicate(event)) {
			each.fire(obj);
		}
	}
	
	public synchronized void removeAllListeners() {
		this.listeners.clear();
	}
	public synchronized void removeAllListeners(String event) {
		this.autovivicate(event).clear();
	}
	public synchronized boolean removeListener(String event, Listener listener) {
		return this.autovivicate(event).remove(listener);
	}
	
	private LinkedHashSet<Listener> autovivicate(String event) {
		if (this.listeners.get(event) == null) {
			this.listeners.put(event, new LinkedHashSet<Listener>());
		}
		return this.listeners.get(event);
	}
	
	public static interface Listener extends EventListener {
		public void fire(EventObject eventObj);
	}
	
	public static class EventData extends EventObject {
		
		public final Object obj;
		public static final Object NULL_SOURCE = new Object();
		
		public EventData(Object data) {
			this(NULL_SOURCE, data);
		}
		
		public EventData(Object source, Object data) {
			super(source);
			this.obj = data;
		}
		
	}
}
