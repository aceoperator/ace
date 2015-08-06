/**
 * 
 */
package com.quikj.ace.web.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * @author amit
 * 
 */
public class Notifier {

	private static final int BLINK_PERIOD = 2000;
	private static String oldTitle = null;
	private static Timer timer = null;
	private static boolean titleShown = false;
	private static String title;

	public static void alert(String title) {
		cancelAlert(); // Cancel previous alerts

		focus();
		Notifier.title = title;
		oldTitle = Window.getTitle();
		Window.setTitle(title);
		titleShown = true;
		timer = new Timer() {

			@Override
			public void run() {
				if (titleShown) {
					Window.setTitle(".");
					titleShown = false;
				} else {
					Window.setTitle(Notifier.title);
					titleShown = true;
				}
			}
		};

		timer.scheduleRepeating(BLINK_PERIOD);
		ring();
	}

	public static void ring() {
		AudioSettings audioSettings = (AudioSettings) SessionInfo.getInstance()
				.get(SessionInfo.AUDIO_SETTINGS);
		if (audioSettings != null && !audioSettings.isRinging()) {
			return;
		}
		Map<String, String> sources = new HashMap<String, String>();
		sources.put(com.google.gwt.dom.client.AudioElement.TYPE_MP3,
				GWT.getModuleBaseURL() + "../audio/ring.mp3");
		sources.put(com.google.gwt.dom.client.AudioElement.TYPE_OGG,
				GWT.getModuleBaseURL() + "../audio/ring.ogg");
		AudioUtils.getInstance().play(sources, true);
	}

	public static void cancelAlert() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (oldTitle != null && oldTitle.length() > 0) {
			Window.setTitle(oldTitle);
		}

		cancelRing();
	}

	public static void cancelRing() {
		AudioSettings audioSettings = (AudioSettings) SessionInfo.getInstance()
				.get(SessionInfo.AUDIO_SETTINGS);
		if (audioSettings != null && !audioSettings.isRinging()) {
			return;
		}
		AudioUtils.getInstance().stopPlaying();
	}

	public static native void focus() /*-{
										$wnd.focus();
										}-*/;
}
