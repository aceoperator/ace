/**
 * 
 */
package com.quikj.server.app.adapter;

/**
 * @author amit
 * 
 */
public class AppServerAdapterManagement implements
		AppServerAdapterManagementMBean {

	@Override
	public String getMeasurements() {
		return PolledAppServerAdapter.getInstance().formatMeasurementsOutput();
	}

	@Override
	public void resetMeasurements() {
		PolledAppServerAdapter.getInstance().resetMeasurements();
	}
}
