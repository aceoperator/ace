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
	public String getOperatorAvailableQueueSize() {
		return Integer.toString(operator.getOperatorAvailableQueueSize());
	}
	
	@Override
	public String getOperatorsWithDNDSize() {
		return Integer.toString(operator.getOperatorsWithDNDSize());
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
