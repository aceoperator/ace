/*
 * CannedMessageElement.java
 *
 * Created on June 6, 2003, 11:29 AM
 */

package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;


/**
 * 
 * @author amit
 */
public class CannedMessageElement implements MediaElementInterface, Serializable {

	private static final long serialVersionUID = 4497154879982065897L;

	private long id;

	private String description;

	private String group;
	
	private String message;

	public CannedMessageElement() {
	}

	public CannedMessageElement(long id, String description, String group) {
		this.id = id;
		this.description = description;
		this.group = group;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDescription() {
		return this.description;
	}

	public String getGroup() {
		return this.group;
	}

	public long getId() {
		return this.id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new CannedMessageElement(id, description, group);
	}
}
