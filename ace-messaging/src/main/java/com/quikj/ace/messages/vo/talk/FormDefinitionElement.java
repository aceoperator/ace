/**
 * 
 */
package com.quikj.ace.messages.vo.talk;

/**
 * @author amit
 *
 */
public class FormDefinitionElement implements MediaElementInterface {
	private static final long serialVersionUID = 2223570122157628050L;

	private String formDef;
	
	private String formId;
	
	private String[] toEmail;
	
	private String fromEmail;
	
	private String[] ccEmail;
	
	private String subject;

	public FormDefinitionElement() {		
	}
	
	public FormDefinitionElement(String formDef, String formId, String[] toEmail, String fromEmail, String[] ccEmail,
			String subject) {
		super();
		this.formDef = formDef;
		this.formId = formId;
		this.toEmail = toEmail;
		this.fromEmail = fromEmail;
		this.ccEmail = ccEmail;
		this.subject = subject;
	}
	
	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String[] getToEmail() {
		return toEmail;
	}

	public void setToEmail(String[] toEmail) {
		this.toEmail = toEmail;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String[] getCcEmail() {
		return ccEmail;
	}

	public void setCcEmail(String[] ccEmail) {
		this.ccEmail = ccEmail;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFormDef() {
		return formDef;
	}

	public void setFormDef(String formDef) {
		this.formDef = formDef;
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new FormDefinitionElement(formDef, formId, toEmail, fromEmail, ccEmail, subject);
	}
}
