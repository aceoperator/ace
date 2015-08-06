/**
 * 
 */
package com.quikj.server.app.adapter;

/**
 * @author amit
 *
 */
public interface AppServerAdapterManagementMBean {
	public static final String MBEAN_NAME = "com.quikj.server.app.adapter:type=AppServerAdapterManagement";

	public String getMeasurements();
	public void resetMeasurements();
}
