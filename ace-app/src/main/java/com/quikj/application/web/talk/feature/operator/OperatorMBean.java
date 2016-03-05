/**
 * 
 */
package com.quikj.application.web.talk.feature.operator;

/**
 * @author amit
 *
 */
public interface OperatorMBean {

	public static final String MBEAN_SUFFIX = "com.quikj.application.web.talk.feature.operator:type=Operator-";
	
	public int getOperatorAvailableQueueSize();
	public int getOperatorsWithDNDSize();
	public int getSubscriberQueueSize();
	public String  getOperatorSummary();
	public String getVisitorSummary();
}
