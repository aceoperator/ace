package com.quikj.ace.messages.vo.talk;

import java.util.ArrayList;
import java.util.List;

public class StringListElement implements TalkMessageInterface {
	private static final long serialVersionUID = 275473611703348702L;
	private List<String> elements = new ArrayList<>();

	public StringListElement() {
	}

	public void addElement(String element) {
		elements.add(element);
	}

	public String elementAt(int index) {
		return elements.get(index);
	}

	public List<String> getElements() {
		return elements;
	}

	public int numElements() {
		return elements.size();
	}

	public void setElements(List<String> elements) {
		this.elements = elements;
	}
}