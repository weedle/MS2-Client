package com.mineshaftersquared.resources;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class MS2Container extends Applet implements AppletStub {

	private Map<String, String> parameters;
	private Applet applet;
	private boolean active = false;

	public MS2Container(Map<String, String> parameters, Applet applet) {
		this.parameters = parameters;
		this.applet = applet;
		this.setLayout(new BorderLayout());
	}

	@Override
	public void start() {
		this.applet.setStub(this);
		this.applet.setSize(this.getWidth(), this.getHeight());
		this.add(this.applet, BorderLayout.CENTER);
		this.applet.init();
		this.active = true;
		this.applet.start();
		this.validate();
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void stop() {
		this.applet.stop();
		this.active = false;
	}

	@Override
	public String getParameter(String name) {
		System.out.println("getting param" + name);
		String local = this.parameters.get(name);
		if (local != null) {
			return local;
		}
		try {
			return super.getParameter(name);
		} catch (Exception ex) {
			this.parameters.put(name, null);
		}
		return null;
	}

	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://www.minecraft.net/game/");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void replace(Applet applet) {
		this.applet.stop();
		this.applet.destroy();
		// applet.stop();
		// applet.destroy();

		this.applet = applet;
		this.start();
	}

	@Override
	public void appletResize(int width, int height) {
		System.out.println(width + ", " + height);
		this.applet.resize(width, height);
	}

	@Override
	public void resize(int width, int height) {
		System.out.println(width + ", " + height);
		this.applet.resize(width, height);
	}

	@Override
	public void resize(Dimension d) {
		System.out.println(d.width + ", " + d.height);
		this.applet.resize(d);
	}

	@Override
	public void init() {
		if (this.applet != null) {
			// this.applet.init();
		}
	}

	@Override
	public void destroy() {
		this.applet.destroy();
	}

}