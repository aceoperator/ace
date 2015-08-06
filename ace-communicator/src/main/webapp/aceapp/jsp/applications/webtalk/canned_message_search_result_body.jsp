<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>List of Canned Messages</h3>

<table border="1" cellpadding="3">
<tr>
<th>Message ID</th>
<th>Group</th>
<th>Description</th>
</tr>
<c:forEach items="${requestScope.elements}" var="message">
<tr> 
<td>   
    <html:link forward="canned_message_management" name="message"><c:out value="${message.id}"/>
    </html:link>
</td>
<td>
    <c:out value="${message.group}"/>
</td>
<td>
    <c:out value="${message.description}"/>
</td>
</tr>
</c:forEach>
</table>