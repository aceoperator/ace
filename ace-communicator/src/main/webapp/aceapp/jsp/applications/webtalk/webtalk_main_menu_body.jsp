<%@ page language="java"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>

<table cellpadding="25">
	<tr valign="top">
		<td>
			<h3>Administration</h3>
			<h4>Hosting</h4>
			<ul>
				<li><html:link forward="display_group_wizard_intro">Customer Setup Wizard</html:link></li>
				<li><html:link forward="display_drop_customer_intro">Remove Customer Wizard</html:link></li>
			</ul>

			<h4>Groups</h4>
			<ul>
				<li><html:link forward="list_groups">List All</html:link></li>
				<li><html:link forward="display_group_management">Administer</html:link></li>
			</ul>

			<h4>Users</h4>
			<ul>
				<li><html:link forward="display_user_search">Search</html:link></li>
				<li><html:link forward="display_user_management">Administer</html:link></li>
			</ul>

			<h4>Features</h4>
			<ul>
				<li><html:link forward="list_features">List All</html:link></li>
				<li><html:link forward="display_feature_operator_management">Administer Operator Feature</html:link></li>
				<li><html:link forward="display_feature_management">Administer Other Feature</html:link></li>
			</ul>

			<h4>Canned Messages</h4>
			<ul>
				<li><html:link forward="display_canned_message_search">Search</html:link></li>
				<li><html:link forward="display_canned_message_management">Administer</html:link></li>
			</ul>
		</td>
		<td>
			<h3>Operations</h3>
			<h4>Reports</h4>
			<ul>
				<li><html:link forward="visitor_report_input">Visitor Report</html:link></li>
				<li><html:link forward="reg_report_input">Registered User Report</html:link></li>
				<li><html:link forward="traffic_report_input">Usage Report</html:link></li>
			</ul>
		</td>
	</tr>
</table>
