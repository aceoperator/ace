/*
 * XMLFormRequestElement.java
 *
 * Created on June 9, 2002, 2:19 AM
 */

package com.quikj.ace.messages.vo.talk;


/**
 * 
 * @author amit
 */
public class ApplicationElement implements MediaElementInterface {
	private static final long serialVersionUID = -3635603173010134904L;
	
	private String applicationData = null;
	private String name = null;
	private String instance = null;

	public ApplicationElement() {
	}
	
	public ApplicationElement(String applicationData, String name,
			String instance) {
		this.applicationData = applicationData;
		this.name = name;
		this.instance = instance;
	}

	public String getApplicationData() {
		return applicationData;
	}

	public String getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}


	public void setApplicationData(String applicationData) {
		this.applicationData = applicationData;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new ApplicationElement(applicationData, name, instance);
	}
}
