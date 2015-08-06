<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>List of Users</h3>

<table border="1" cellpadding="3">
<c:forEach items="${requestScope.users}" var="user">
<tr> 
<td>   
    <html:link forward="user_management" name="user"><c:out value="${user.name}"/>
    </html:link>
</td>
</tr>
</c:forEach>
</table>