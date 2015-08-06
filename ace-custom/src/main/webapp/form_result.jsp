<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<!-- Setup for mobile -->
<meta name="HandheldFriendly" content="true" />
<meta name="MobileOptimized" content="width" />
<meta name="viewport"
	content="width=device-width, user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />

<title>
    <fmt:bundle basename="com.quikj.ace.custom.server.CustomResources">
		<fmt:message key="submissionResultTitle"/>
	</fmt:bundle>
</title>

</head>
<body>
	<%
		if (request.getAttribute("error") != null) {
	%>
		<fmt:bundle basename="com.quikj.ace.custom.server.CustomResources">
			<fmt:message key="errorEncountered"/>
		</fmt:bundle>
		
		<blockquote style="color: red">
			<%= request.getAttribute("error") %>
		</blockquote>
		<br/>
		<a href="#" onclick="history.go(-1); return false;">
			<fmt:bundle basename="com.quikj.ace.custom.server.CustomResources">
				<fmt:message key="goBack"/>
			</fmt:bundle>
		</a>
	<%
		} else {
	%>
		<fmt:bundle basename="com.quikj.ace.custom.server.CustomResources">
			<fmt:message key="submissionEmailedOk"/>
		</fmt:bundle>
	<%
		}
	%>
</body>
</html>