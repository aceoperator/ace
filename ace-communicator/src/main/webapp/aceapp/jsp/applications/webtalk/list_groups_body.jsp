<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>List of Groups</h3>

<table border="1" cellpadding="3">
<tr>
<th>Group Name</th>
<th>Domain</th>
</tr>
<c:forEach items="${requestScope.groups}" var="group">
<tr> 
<td>   
    <html:link forward="group_management" name="group"><c:out value="${group.name}"/>
    </html:link>
</td>
<td>
    <c:out value="${group.domain}"/>
</td>
</tr>
</c:forEach>
</table>