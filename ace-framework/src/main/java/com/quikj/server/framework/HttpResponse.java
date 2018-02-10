/*
 * Copyright (C) Alexandre Harvey-Tremblay - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexandre Harvey-Tremblay <ahtremblay@gmail.com>, 2014
 */
package com.quikj.server.framework;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ahtremblay on 2016-08-31.
 */

public class HttpResponse implements Serializable {
	private static final long serialVersionUID = -8412228629949325535L;

	public HttpResponse() {
	}

	@SerializedName("error-codes")
	private List<String> errorCodes;
	private Boolean success;

	public List<String> getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(List<String> errorCodes) {
		this.errorCodes = errorCodes;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}