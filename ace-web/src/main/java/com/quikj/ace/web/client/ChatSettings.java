package com.quikj.ace.web.client;

public class ChatSettings {

	private boolean autoAnswer;
	private boolean dnd;
	
	public ChatSettings() {
	}

	public boolean isAutoAnswer() {
		return autoAnswer;
	}

	public void setAutoAnswer(boolean autoAnswer) {
		this.autoAnswer = autoAnswer;
	}

	public boolean isDnd() {
		return dnd;
	}

	public void setDnd(boolean dnd) {
		this.dnd = dnd;
	}
}
