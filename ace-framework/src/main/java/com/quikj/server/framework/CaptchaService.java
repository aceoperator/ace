/**
 * 
 */
package com.quikj.server.framework;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.github.cage.GCage;

/**
 * @author amit
 * 
 */
public class CaptchaService {
	protected class CaptchaInfo {
		protected String token;
		protected int countDown = 5;

		public CaptchaInfo(String token) {
			this.token = token;
		}

		public int decrement() {
			return --countDown;
		}
	}
	
	private static CaptchaService instance;

	Map<String, CaptchaInfo> buffer = new HashMap<String, CaptchaInfo>();

	private Timer timer;

	public int getBufferSize() {
		return buffer.size();
	}
	
	public void init() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				synchronized (buffer) {
					Iterator<Entry<String, CaptchaInfo>> i = buffer.entrySet().iterator();
					while (i.hasNext()) {
						Entry<String, CaptchaInfo> element = i.next();
						int countDown = element.getValue().decrement();
						if (countDown == 0) {
							i.remove();
						}
					}
				}

				if (buffer.size() > 200) {
					System.out.println("Warning! There are " + buffer.size() + " images in the captcha list");
				}
			}
		};

		timer = new Timer("CaptchaTimer");
		timer.scheduleAtFixedRate(task, 60* 1000L, 60 * 1000L);
		instance = this;
	}

	public void destroy() {
		if (timer != null) {
			timer.cancel();
		}
		
		synchronized (buffer) {
			buffer.clear();
		}
		
		instance = null;
	}

	public static CaptchaService getInstance() {
		return instance;
	}

	public BufferedImage generateImage(String sessionId) {
		GCage cage = new GCage();
		String token = cage.getTokenGenerator().next();
		synchronized (buffer) {
			buffer.put(sessionId, new CaptchaInfo(token));
		}
		return cage.drawImage(token);
	}

	public boolean verify(String sessionId, String input) {
		synchronized (buffer) {
			CaptchaInfo info = buffer.get(sessionId);
			if (info == null) {
				return false;
			}
			buffer.remove(sessionId);
			return info.token.equalsIgnoreCase(input);
		}		
	}
}
