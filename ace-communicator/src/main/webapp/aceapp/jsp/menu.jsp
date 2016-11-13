<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<sec:authorize url="/main_menu.do" >	
		User <i style="color: blue"><sec:authentication
			property="principal.username" /></i> logged in<br>
	<html:link forward="display_change_password">Change password        
	        </html:link>
	<br>
	 <html:link forward="logoff">Logout</html:link>
	<br>
	<hr width="100%">
	<html:link forward="main_menu">Main Menu</html:link>
	<br>

	<c:if test="${!empty requestScope.menu}">
		<hr width="100%">
	            Related Tasks<br>
		<c:forEach var="element" items="${requestScope.menu.links}">
	                &nbsp;&nbsp;&nbsp;&nbsp;
	                <c:set var="forward" scope="request"
				value="${element.link}" />
			<html:link forward="<%=(String) request.getAttribute(\"forward\")%>">
				<c:out value="${element.name}" />
			</html:link>
			<br>
		</c:forEach>
	</c:if>
</sec:authorize>