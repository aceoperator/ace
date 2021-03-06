<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>Canceling Wizard</h3>

<html:form action="group_wizard_cancel.do">

Below are the operations that have been performed during your wizard session. <i>This data remains in the system after you leave this wizard.</i>
Use the Remove Customer Wizard to delete it if you no longer want it. 
<p>
If you want to save this information, print this page now.<br>
<p>
<h4>Wizard Session Log</h4>
<p>
<table border="1" bgcolor="lightgrey">
<c:forEach var="logmessage" items="${sessionScope.groupWizardLog}">
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
