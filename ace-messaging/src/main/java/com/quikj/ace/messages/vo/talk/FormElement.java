/**
 * 
 */
package com.quikj.ace.messages.vo.talk;

/**
 * @author amit
 *
 */
public class FormElement implements MediaElementInterface {
	private static final long serialVersionUID = 2223570122157628050L;

	private String formDef;

	public FormElement() {
	}

	public FormElement(String formDef) {
		this.formDef = formDef;
	}

	public String getFormDef() {
		return formDef;
	}

	public void setFormDef(String formDef) {
		this.formDef = formDef;
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new FormElement(formDef);
	}
}
