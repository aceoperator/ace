/**
 * 
 */
package com.quikj.ace.messages.vo.talk;

import java.util.HashMap;
import java.util.Map;

/**
 * @author amit
 *
 */
public class FormSubmissionElement implements MediaElementInterface {

	private static final long serialVersionUID = -29658246158004112L;

	private Map<String, String> response = new HashMap<>();
	
	private long formId;
	
	public FormSubmissionElement() {		
	}
	
	public FormSubmissionElement(Map<String, String> response, long formId) {
		super();
		this.response = response;
		this.formId = formId;
	}

	public Map<String, String> getResponse() {
		return response;
	}

	public void setResponse(Map<String, String> response) {
		this.response = response;
	}

	public long getFormId() {
		return formId;
	}

	public void setFormId(long formId) {
		this.formId = formId;
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new FormSubmissionElement(response, formId);
	}
}
