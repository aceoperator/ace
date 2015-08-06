<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>Wizard Complete!</h3>

<html:form action="drop_customer_finished.do">

Below is a log of the data deleted during this wizard session.<br>
If you want to save this information, print this page now.<br>
<p>
<h4>Remove Customer Log</h4>
<p>
<table border="1" bgcolor="lightgrey">
<c:forEach var="logmessage" items="${requestScope.dropCustomerLog}">
    <tr>
    <td><c:out value="${logmessage}"/></td>
    </tr>
</c:forEach>
</table>

<table>
<tr>
<td colspan="1" align="center">
&nbsp;<p>
<html:submit property="submit">Exit</html:submit>
</td>
</tr>
</table>
</html:form>
