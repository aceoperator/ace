/**
 * 
 */
package com.quikj.application.web.talk.feature.operator;

import java.util.Date;

/**
 * @author amit
 *
 */
public interface OperatorMBean {

	public static final String MBEAN_SUFFIX = "com.quikj.application.web.talk.feature.operator:type=Operator-";

	int getOperatorAvailableQueueSize();

	int getOperatorsWithDNDSize();

	int getSubscriberQueueSize();

	String getOperatorSummary();

	String getVisitorSummary();

	Date getPausedUntil();
}
