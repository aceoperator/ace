/**
 * 
 */
package com.quikj.application.web.talk.plugin;

import com.thoughtworks.xstream.XStream;

/**
 * @author amit
 *
 */
public class XMLSerializer {

	private static XMLSerializer instance = null;
	private XStream xstream;

	private XMLSerializer() {
		xstream = new XStream();
	}
	
	public static XMLSerializer getInstance() {
		if (instance == null) {
			instance = new XMLSerializer();
		}
		
		return instance;
	}
	
	public String serialize(Object obj) {
		return xstream.toXML(obj);
	}	
}
