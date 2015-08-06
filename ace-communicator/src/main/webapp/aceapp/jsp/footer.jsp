<%-- @author Vinod Batra --%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>



<hr width="100%">
<div align="center">
<em style="color:red">&#169
<c:url var="url" value="${applicationScope.menuProperties.url}"/>
<a href='<c:out value="${url}"/>'>
<c:out value="${applicationScope.menuProperties.company}"/></a> 2003-2014</em><br>
</div>
