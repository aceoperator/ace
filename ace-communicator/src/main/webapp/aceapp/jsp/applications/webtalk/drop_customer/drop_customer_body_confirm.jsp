<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("All the data shown will get deleted. You'll have a log of it. Are you sure you want to delete the data?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>Customer Data</h3>

<html:form action="drop_customer_confirm.do">
<html:hidden property="domain"/>

Below is the data associated with the customer you selected.<br>
<p>
Pressing the 'Remove Customer Data' button will remove this data from the system.
<table cellspacing="15">
<tr>
<td valign='bottom'><html:submit property="submit" onclick="return displayWarning()">Remove Customer Data</html:submit></td>
<td valign='bottom'><html:submit property="submit">Cancel Wizard</html:submit></td>
</tr>
</table>
<hr align="center" width="100%">
<p>
<b>Data in customer domain:  <c:out value="${domain}"/></b><br>
<p>
<c:if test="${requestScope.operators != null}">
<table width="30%" border="1">
<th><b>Operators</b></th>
<c:forEach var="item" items="${requestScope.operators}">
    <tr>
    <td align="center"><c:out value="${item}"/></td>
    </tr>
</c:forEach>
</table>
<p>
</c:if>
<c:if test="${requestScope.groups != null}">
<table width="30%" border="1">
<th><b>Groups</b></th>
<c:forEach var="item" items="${requestScope.groups}">
    <tr>
    <td align="center"><c:out value="${item}"/></td>
    </tr>
</c:forEach>
</table>
<p>
</c:if>
<c:if test="${requestScope.owners != null}">
<table width="30%" border="1">
<th><b>Group Owners</b></th>
<c:forEach var="item" items="${requestScope.owners}">
    <tr>
    <td align="center"><c:out value="${item}"/></td>
    </tr>
</c:forEach>
</table>
<p>
</c:if>
<c:if test="${requestScope.features != null}">
<table width="30%" border="1">
<th><b>Features</b></th>
<c:forEach var="item" items="${requestScope.features}">
    <tr>
    <td align="center"><c:out value="${item.name}"/></td>
    </tr>
</c:forEach>
</table>
<p>
</c:if>
<c:if test="${requestScope.cannedMessages != null}">
<table border="1">
<th colspan='2'><b>Canned Messages</b></th>
<tr><td><b>Message ID</b></td><td><b>Group</b></td></tr>
<c:forEach var="item" items="${requestScope.cannedMessages}">
    <tr>
    <td><c:out value="${item.id}"/></td>
    <td><c:out value="${item.groupName}"/></td>
    </tr>
</c:forEach>
</table>
<p>
</c:if>
<c:if test="${requestScope.strayUsers != null}">
<table width="30%" border="1">
<th><b>Stray Users</b></th>
<c:forEach var="item" items="${requestScope.strayUsers}">
    <tr>
    <td align="center"><c:out value="${item}"/></td>
    </tr>
</c:forEach>
</table>
<p>
</c:if>
</html:form>
