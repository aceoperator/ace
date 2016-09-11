/**
 * 
 */
package com.quikj.ace.social;

/**
 * @author tomcat
 *
 */
public class TextMedia implements Media {

	private String text;
	
	public TextMedia(String text) {
		this.text = text;
	}
	
	@Override
	public Object getMessage() {
		return text;
	}
}
