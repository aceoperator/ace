/**
 * 
 */
package com.quikj.ace.db.webtalk.value;

import java.util.Calendar;

/**
 * @author amit
 *
 */
public class TrafficMeasurement {

	private String param;
	private float value;
	private Calendar timestamp;
	
	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
}
