package com.quikj.ace.messages.vo.talk;

public class HtmlElement implements MediaElementInterface {
	
	private static final long serialVersionUID = -4527890972255034432L;
	
	private String html = "";

	public HtmlElement() {
	}
	
	public HtmlElement(String html) {
		this.html = html;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	@Override
	public MediaElementInterface cloneMe() {
		return new HtmlElement(html);
	}	
}
