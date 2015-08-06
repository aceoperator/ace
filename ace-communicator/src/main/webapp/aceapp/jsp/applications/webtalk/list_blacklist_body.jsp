<%@ page language="java"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>
	Black List for User
	<c:out value="${requestScope.userContext}" />
</h3>

<html:link forward="blacklist_management" paramId="userName"
	paramName="userContext">
		Add to the Black List
	</html:link>
<p />

<table border="1" cellpadding="3">
	<tr>
		<th>Identifier</th>
		<th>Type</th>
		<th>Level</th>
		<th>Last Modified</th>
	</tr>
	<c:forEach items="${requestScope.blacklist}" var="blacklist">
		<tr>
			<td><html:link forward="blacklist_management" paramId="id"
					paramName="blacklist" paramProperty="id">
					<c:out value="${blacklist.identifier}" />
				</html:link></td>
			<td>
				<c:choose>
					<c:when test="${blacklist.type == 0}">
						Cookie
					</c:when>
					<c:otherwise>
						IP Address
					</c:otherwise>
				</c:choose>
			</td>
			<td><c:out value="${blacklist.level}" /></td>
			<c:set var="lastModified" value="${blacklist.lastModified}" scope="request"/>
			<% 
				long lm = (Long) request.getAttribute("lastModified");
				request.setAttribute("lastModified", new java.util.Date(lm));
			%>
			
			<td><c:out value="${requestScope.lastModified}" /></td>
		</tr>
	</c:forEach>
</table>