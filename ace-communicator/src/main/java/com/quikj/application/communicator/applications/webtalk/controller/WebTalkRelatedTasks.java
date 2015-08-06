/*
 * WebTalkRelatedTasks.java
 *
 * Created on October 3, 2004, 8:09 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.RelatedTasks;

/**
 * 
 * @author amit
 */
public class WebTalkRelatedTasks extends RelatedTasks {

	/** Creates a new instance of WebTalkRelatedTasks */
	public WebTalkRelatedTasks() {
		super();
		addLink(new LinkAttribute("Talk Application Menu", "webtalk_main_menu"));
	}
}
