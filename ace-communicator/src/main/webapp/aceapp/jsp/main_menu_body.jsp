<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


<table cellpadding="10">
<tr valign="top">
<td>
    <h4>System Logs</h4>
      <li><html:link forward="display_log_search">View Logs</html:link></li>
      <li><html:link forward="display_log_delete">Delete Logs</html:link></li>
    <br>

    <h4>Account Administration</h4>
        <li><html:link forward="display_account_management">
        <bean:message key="prompt.account.manage"/>
        </html:link></li>
    <br>
</td>
<td>
    <h4>Applications</h4>
    <c:forEach var="app_element" items="${applicationScope.adminConfig.applications}">
        <c:set var="forward" scope="request" value="${app_element.forwardName}"/>
        <li><html:link forward="<%= (String)request.getAttribute(\"forward\")%>">
            <c:out value="${app_element.displayName}"/>
            </html:link>
        </li>
    </c:forEach>
</td>
</tr>
</table>