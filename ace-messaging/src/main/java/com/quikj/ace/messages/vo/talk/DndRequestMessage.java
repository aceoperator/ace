package com.quikj.ace.messages.vo.talk;

public class DndRequestMessage implements TalkMessageInterface {
	private static final long serialVersionUID = 5152953507453678100L;
	
	private boolean enable = false;

	public DndRequestMessage() {
	}

	/**
	 * @return Returns the enable.
	 */
	public boolean isEnable() {
		return enable;
	}

	/**
	 * @param enable
	 *            The enable to set.
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
