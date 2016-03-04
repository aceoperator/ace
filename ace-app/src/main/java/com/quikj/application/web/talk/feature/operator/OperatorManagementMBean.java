/**
 * 
 */
package com.quikj.application.web.talk.feature.operator;

/**
 * @author amit
 *
 */
public interface OperatorManagementMBean {

	public static final String MBEAN_SUFFIX = "com.quikj.application.web.talk.feature.operator:type=Operator-";
	
	public String getOperatorAvailableQueueSize();
	public String getOperatorsWithDNDSize();
	public String getSubscriberQueueSize();
	public String getOperatorSummary();
	public String getVisitorSummary();
}
