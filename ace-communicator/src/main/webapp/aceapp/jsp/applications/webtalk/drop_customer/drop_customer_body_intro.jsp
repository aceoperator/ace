<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<h3>Welcome to the Remove Customer Wizard</h3>

<html:form action="drop_customer_intro.do">
This wizard is especially designed for the scenario where you are hosting Ace Operator as a service for your customers. 

<p>Using this wizard, you can easily remove data associated with a customer in your system. This includes the customer's:<br>
<ul>
<li>operators</li>
<li>groups</li>
<li>group owners and features</li>
<li>canned messages</li>
</ul>
<p>This wizard displays the data first. After you confirm, it removes the data from the system and provides a log of what was removed.
<p>Refer to the Ace Operator System Manual for instructions on how to remove operator login access and "Live Help" visitor access (ie, Ace Contact Center).

<table cellspacing="20">
<tr>
<td><html:submit property="submit">Next</html:submit></td>
<td><html:submit property="submit">Cancel</html:submit></td>
</tr>
</table>
</html:form>
