package com.quikj.ace.web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.SourceElement;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;

public class AudioUtils {

	// Ace audio types -- TODO separate out Ace-specific code to diff class
	public static final int CHIME = 1;
	public static final int DOORBELL = 2;
	public static final int KNOCK = 3;
	public static final int OPEN = 4;
	public static final int RING = 5;
	public static final int WELCOME = 6;

	private static AudioUtils instance = null;
	private Audio audio;
	private Logger logger;
	List<SourceElement> currentSources = new ArrayList<SourceElement>();
	private boolean html5Audio;
	private HTML legacyAudioTag;

	public AudioUtils(LayoutPanel parent) {
		instance = this;
		logger = Logger.getLogger(getClass().getName());

		audio = Audio.createIfSupported();
		html5Audio = true;
		if (audio == null) {
			html5Audio = false;
			if (ApplicationController.getInstance().isOperator()
					&& !ClientProperties.getInstance().getBooleanValue(
							ClientProperties.LEGACY_MEDIA_PLAYER, false)) {
				logger.warning("HTML5 Audio is not supported. In addition, the property "
						+ ClientProperties.LEGACY_MEDIA_PLAYER
						+ " is not set to \"true\"."
						+ " There will be no audio for this session");
			}
			return;
		}

		parent.add(audio);
		audio.setVolume(1.0);
		audio.setControls(false);
		audio.setMuted(false);
		audio.setLoop(false);
		audio.setAutoplay(true);
		audio.setPreload(MediaElement.PRELOAD_AUTO);

	}

	public static AudioUtils getInstance() {
		return instance;
	}

	public void play(int type) {
		AudioSettings audioSettings = (AudioSettings) SessionInfo.getInstance()
				.get(SessionInfo.AUDIO_SETTINGS);
		List<String> urls = new ArrayList<String>();

		switch (type) {
		case CHIME: {
			if (audioSettings != null && !audioSettings.isChime()) {
				return;
			}
			String audioChime = ClientProperties.getInstance().getStringValue(
					ClientProperties.AUDIO_CHIME,
					GWT.getModuleBaseURL() + "../audio/chime");
			urls.add(audioChime + ".mp3");
			urls.add(audioChime + ".ogg");
		}
			break;
		case RING: {
			if (audioSettings != null && !audioSettings.isRinging()) {
				return;
			}
			urls.add(GWT.getModuleBaseURL() + "../audio/ring.mp3");
			urls.add(GWT.getModuleBaseURL() + "../audio/ring.ogg");
		}
			break;
		case KNOCK: {
			if (audioSettings != null && !audioSettings.isPresence()) {
				return;
			}
			urls.add(GWT.getModuleBaseURL() + "../audio/knock.mp3");
			urls.add(GWT.getModuleBaseURL() + "../audio/knock.ogg");
		}
			break;
		case OPEN: {
			if (audioSettings != null && !audioSettings.isPresence()) {
				return;
			}
			urls.add(GWT.getModuleBaseURL() + "../audio/open.mp3");
			urls.add(GWT.getModuleBaseURL() + "../audio/open.ogg");
		}
			break;
		case DOORBELL: {
			if (audioSettings != null && !audioSettings.isBuzz()) {
				return;
			}
			urls.add(GWT.getModuleBaseURL() + "../audio/doorbell.mp3");
			urls.add(GWT.getModuleBaseURL() + "../audio/doorbell.ogg");
		}
			break;
		case WELCOME: {
			urls.add(GWT.getModuleBaseURL() + "../audio/chime.mp3");
			urls.add(GWT.getModuleBaseURL() + "../audio/chime.ogg");
		}
			break;
		default: {
			logger.warning("Unknown Ace audio type specified: " + type);
		}
		}

		Map<String, String> sources = new HashMap<String, String>();
		for (String url : urls) {
			if (url.endsWith(".mp3")) {
				sources.put(com.google.gwt.dom.client.AudioElement.TYPE_MP3,
						url);
			} else if (url.endsWith(".ogg")) {
				sources.put(com.google.gwt.dom.client.AudioElement.TYPE_OGG,
						url);
			} else {
				logger.warning("Audio type encoding not yet implemented for type: "
						+ url);
			}
		}

		play(sources, false);
	}

	/**
	 * Plays audio given one or more sources. If the browser doesn't support
	 * HTML 5 Audio, no audio is provided. If the browser does support HTML 5
	 * Audio, it will select from the given sources the one it is able to play
	 * and download the corresponding source url.
	 * 
	 * @param sources
	 *            Map of audio type and source url
	 * @param loop
	 */
	public void play(Map<String, String> sources, boolean loop) {

		// remove previous sources if any
		stopPlaying();

		if (html5Audio) {
			// put ogg first, fire fox has a bug if mp3 is first
			SourceElement src = audio.addSource(sources
					.get(com.google.gwt.dom.client.AudioElement.TYPE_OGG),
					com.google.gwt.dom.client.AudioElement.TYPE_OGG);
			currentSources.add(src);
			sources.remove(com.google.gwt.dom.client.AudioElement.TYPE_OGG);

			// add remaining sources
			for (String audioType : sources.keySet()) {
				src = audio.addSource(sources.get(audioType), audioType);
				currentSources.add(src);
			}

			audio.load();
			audio.setLoop(loop);
			audio.play();
		} else if (ClientProperties.getInstance().getBooleanValue(
				ClientProperties.LEGACY_MEDIA_PLAYER, false)) {
			String src = sources
					.get(com.google.gwt.dom.client.AudioElement.TYPE_MP3);
			if (src == null) {
				return;
			}

			StringBuffer buffer = new StringBuffer();
			if (getUserAgent().contains("ie")) {
				buffer.append("<object classid=\"clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95\"");
				buffer.append(" width=\"1\" height=\"1\">");
				buffer.append("<param name=\"FileName\" value=\"");
				buffer.append(src);
				buffer.append("\" />");
				buffer.append("<param name=\"ShowControls\" value=\"0\" />");
				buffer.append("<param name=\"ShowStatusBar\" value=\"0\" />");
				buffer.append("<param name=\"ShowDisplay\" value=\"0\" />");
				buffer.append("<param name=\"autostart\" value=\"1\" />");
				buffer.append("<param name=\"autoplay\" value=\"1\" />");
				buffer.append("</object>");
				legacyAudioTag = new HTML(buffer.toString());
				ApplicationController.getInstance().getRootPanel()
						.add(legacyAudioTag);
				legacyAudioTag.setVisible(false);
			} else {
				buffer.append("<object type=\"audio/x-mpeg\" data=\"");
				buffer.append(src);
				buffer.append("\" width=\"1\" height=\"1\">");
				buffer.append("<param name=\"controller\" value=\"false\" />");
				buffer.append("<embed src=\"");
				buffer.append(src);
				buffer.append("\" type=\"audio/x-mpeg\" width=\"200\" height=\"16\" autostart=\"true\" controller=\"0\" ></embed>");
				buffer.append("</object>");
				legacyAudioTag = new HTML(buffer.toString());
				ApplicationController.getInstance().getRootPanel()
						.add(legacyAudioTag);
				legacyAudioTag.setVisible(true);
			}
		}
	}

	public void stopPlaying() {
		if (audio != null) {
			try {
				audio.pause();
			} catch (Exception e) {
				logger.fine("Exception pausing audio : "
						+ e.getClass().getName() + " : " + e.getMessage());
			}

			for (SourceElement src : currentSources) {
				audio.removeSource(src);
			}
			currentSources.clear();
		} else if (legacyAudioTag != null) {
			legacyAudioTag.removeFromParent();
		}
	}

	private static native String getUserAgent() /*-{
												return navigator.userAgent.toLowerCase();
												}-*/;
}