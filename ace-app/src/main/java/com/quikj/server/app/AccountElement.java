package com.quikj.server.app;


public class AccountElement {
	private String name;

	private String additionalInfo;

	public AccountElement() {
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public String getName() {
		return name;
	}

	public void setAdditionalInfo(String info) {
		additionalInfo = info;
	}

	public void setName(String name) {
		this.name = name;
	}
}
