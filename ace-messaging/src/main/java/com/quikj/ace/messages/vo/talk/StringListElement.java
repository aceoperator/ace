package com.quikj.ace.messages.vo.talk;

import java.util.Vector;

public class StringListElement implements TalkMessageInterface {
	private static final long serialVersionUID = 275473611703348702L;
	private Vector<String> elements = new Vector<String>();

	public StringListElement() {
	}

	public void addElement(String element) {
		elements.addElement(element);
	}

	public String elementAt(int index) {
		return elements.elementAt(index);
	}

	/**
	 * Getter for property elements.
	 * 
	 * @return Value of property elements.
	 */
	public Vector<String> getElements() {
		return elements;
	}

	public int numElements() {
		return elements.size();
	}

	/**
	 * Setter for property elements.
	 * 
	 * @param elements
	 *            New value of property elements.
	 */
	public void setElements(Vector<String> elements) {
		this.elements = elements;
	}
}
