/**
 * 
 */
package com.quikj.ace.web.client;

/**
 * @author amit
 *
 */
public class AudioSettings {

	private boolean buzz = true;
	private boolean chime = true;
	private boolean presence = true;
	private boolean ringing = true;
	
	public AudioSettings() {
	}

	public boolean isBuzz() {
		return buzz;
	}

	public void setBuzz(boolean buzz) {
		this.buzz = buzz;
	}

	public boolean isChime() {
		return chime;
	}

	public void setChime(boolean chime) {
		this.chime = chime;
	}

	public boolean isPresence() {
		return presence;
	}

	public void setPresence(boolean presence) {
		this.presence = presence;
	}

	public boolean isRinging() {
		return ringing;
	}

	public void setRinging(boolean ringing) {
		this.ringing = ringing;
	}
}
