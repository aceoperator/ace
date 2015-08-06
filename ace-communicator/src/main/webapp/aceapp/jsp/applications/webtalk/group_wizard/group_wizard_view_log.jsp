<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head><title>Group Hosting Wizard</title></head>
<body>

<h3 align="center">Customer Setup Log</h3>

<html:form action="view_group_wizard_log.do">
<table border="1" bgcolor="lightgrey" align="center">
<c:forEach var="logmessage" items="${sessionScope.groupWizardLog}">
    <tr>
    <td><c:out value="${logmessage}"/></td>
    </tr>
</c:forEach>
</table>
<table align="center">
<tr>
<td>&nbsp;<p>
<html:submit property="submit">Refresh</html:submit>
</td>
</tr>
</html:form>

</body>
</html>