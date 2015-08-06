<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="" language="JavaScript">
function openHomework()
{
    <c:url var="url" value="/aceapp/html/hosting_homework.html"/>
    win = window.open('<c:out value="${url}"/>', 
                      "hosting_homework",
                      "width=600,height=650,scrollbars=yes,resizable=yes,toolbar=yes");
}
function openTerminology()
{
    <c:url var="url" value="/aceapp/html/hosting_terminology.html"/>
    win = window.open('<c:out value="${url}"/>', 
                      "Hosting_terminology",
                      "width=400,height=200,scrollbars=yes,status=yes");
}
</script>

<h3>Welcome to the Customer Setup Wizard</h3>

<html:form action="group_wizard_intro.do">
This wizard is especially designed for the scenario where you are hosting Ace Operator as a service for your customers. 
The term "<i>company</i>" is sometimes used to refer to the customer who has signed up for the Ace Operator service you offer. 
<a href="javascript:openTerminology()"><i>Other terminology.</i></a>

<p>Using this wizard, you can set up a new customer in your system. You will define:<br>
<ul>
<li>information about the customer company</li>
<!--<li>customizations and characteristics of the service</li> -->
<li>one or more operator groups, or "queues", exclusively for this customer's use</li>
<li>operators</li>
<li>canned messages</li>
</ul>
<p>This wizard sets up the data in the system and provides <!--:<br>
<ul>
<li>operator login access HTML page(s)</li>
<li>"Live Help" visitor access button(s)</li>
<li>instructions for your customer on how to put the "Live Help" button into their web pages and how to access the new group(s)</li>
<li>information for you regarding the system data that has been set up</li>
</ul> -->
a log of what was set up during this wizard session.
<!--<p>When this wizard completes, your customer can start using the service immediately. -->
<p>Refer to the Ace Operator System Manual for instructions on how to provide operator login access and "Live Help" visitor access (ie, Ace Contact Center).
<p>Before you start, <a href="javascript:openHomework()"><i>do your homework first.</i></a>
<table cellspacing="20">
<tr>
<td><html:submit property="submit">Next</html:submit></td>
<td><html:submit property="submit">Cancel</html:submit></td>
</tr>
</table>
</html:form>
