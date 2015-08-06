/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author amit
 * 
 */
public class HtmlUtils {

	public static String scrubTags(String text) {
		text = text.replaceAll("&nbsp;", " ").trim();
		if (text.length() == 0) {
			return text;
		}

		HTML html = new HTML(text);

		// remove unnecessary whitespace
		removeEmptyWhitespaceElements(html.getElement());

		// check for <a tags and ensure target=_blank
		NodeList<Element> nodes = html.getElement().getElementsByTagName("a");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element node = nodes.getItem(i);
			AnchorElement ele = (AnchorElement) AnchorElement.as(node);
			ele.setTarget("_blank");
		}

		// eliminate any script elements
		nodes = html.getElement().getElementsByTagName("script");
		for (int i = nodes.getLength() - 1; i >= 0; i--) {
			Element node = nodes.getItem(i);
			html.getElement().removeChild(node);
		}

		// end of iterating DOM

		// eliminate blank lines
		text = html.getHTML();
		while ((text.startsWith("<p>") || text.startsWith("<P>"))
				&& (text.endsWith("</p>") || text.endsWith("</P>"))) {
			text = text.substring(3, text.length() - 4).trim();
		}

		while (text.startsWith("<br>") || text.startsWith("<BR>")) {
			text = text.substring(4).trim();
		}

		while (text.endsWith("<br>") || text.endsWith("<BR>")) {
			text = text.substring(0, text.length() - 4).trim();
		}

		return text.trim();
	}

	private static void removeEmptyWhitespaceElements(
			com.google.gwt.dom.client.Element element) {
		cleanElement(element, "p");
		cleanElement(element, "div");
	}

	private static void cleanElement(com.google.gwt.dom.client.Element element,
			String tag) {
		NodeList<Element> nodes = element.getElementsByTagName(tag);

		for (int i = 0; i < nodes.getLength(); i++) {
			Element node = nodes.getItem(i);
			removeEmptyWhitespaceElements(node);

			String contents = node.getInnerHTML().trim();
			if (contents.length() == 0 || contents.equalsIgnoreCase("<br>")) {
				element.removeChild(node);
			}
		}
	}

}
