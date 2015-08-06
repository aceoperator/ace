/*
 * BuzzElement.java
 *
 * Created on May 27, 2002, 5:40 AM
 */

package com.quikj.ace.messages.vo.talk;

/**
 * 
 * @author amit
 */
public class TypingElement implements MediaElementInterface {
	
	private static final long serialVersionUID = 8498903577888631222L;

	public TypingElement() {
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new TypingElement();
	}
}
