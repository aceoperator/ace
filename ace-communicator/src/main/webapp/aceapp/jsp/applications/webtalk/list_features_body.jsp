<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>List of Features</h3>

<table border="1" cellpadding="3">
<tr>
<th>Feature Name</th>
<th>Status</th>
</tr>
<c:forEach items="${requestScope.features}" var="feature">
<tr> 
<td>
    <c:set var="forward-to" scope="request" value="${feature.forward}"/>  
    <html:link forward="<%= (String)request.getAttribute(\"forward-to\")%>" name="feature"><c:out value="${feature.name}"/>
    </html:link>
</td>
<c:choose>
<c:when test="${feature.status == 'active'}">
<td style="color:green">
    <c:out value="${feature.status}"/>
</td>
</c:when>
<c:otherwise>
<td style="color:red">
    <c:out value="${feature.status}"/>
</td>
</c:otherwise>
</c:choose>
</tr>
</c:forEach>
</table>