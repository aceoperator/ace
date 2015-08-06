package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MediaElements implements Serializable {

	private static final long serialVersionUID = 6481989589186030808L;

	private List<MediaElementInterface> elements = new ArrayList<MediaElementInterface>();

	public MediaElements() {
	}
	
	public MediaElements (MediaElements mediaElementsToClone) {
		if (mediaElementsToClone == null) {
			return;
		}
		
		for (MediaElementInterface medium: mediaElementsToClone.getElements()) {
			elements.add(medium.cloneMe());
		}		
	}
	
	public void replaceElement(int index, MediaElementInterface element) {
		elements.set(index, element);
	}

	public List<MediaElementInterface> getElements() {
		return elements;
	}

	public void setElements(List<MediaElementInterface> elements) {
		this.elements = elements;
	}
}
