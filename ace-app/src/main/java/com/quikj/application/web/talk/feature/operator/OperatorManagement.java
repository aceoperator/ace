/**
 * 
 */
package com.quikj.application.web.talk.feature.operator;

/**
 * @author amit
 *
 */
public class OperatorManagement implements OperatorManagementMBean {

	private Operator operator;

	public OperatorManagement(Operator operator) {
		this.operator = operator;
	}
	
	@Override
	public String getOperatorQueueSize() {
		return Integer.toString(operator.getOperatorQueueSize());
	}

	@Override
	public String getSubscriberQueueSize() {
		return Integer.toString(operator.getSubscriberQueueSize());
	}
	
	@Override
	public String getOperatorSummary() {
		return operator.getOperatorSummary();
	}
	
	@Override
	public String getVisitorSummary() {
		return operator.getVisitorSummary();
	}
}
