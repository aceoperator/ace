<%-- @author Vinod Batra --%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<table border="0" width="100%">

<TR> 
<td>
<c:url var="url" value="${applicationScope.menuProperties.url}"/>
<a href='<c:out value="${url}"/>'>
<c:url value="${applicationScope.menuProperties.image}" var="logo"/>
<img src='<c:out value="${logo}"/>' 
border="0" height='75' width='100'></a>
</td>
<td>
<h1>Ace Communicator</h1>
</td>
</TR>

<TR>
<TD>&nbsp</TD>
<td>
 <c:forEach var="link" items="${applicationScope.menuProperties.links}"> 
    <c:url var="url" value="${link.source}"/>
    <a href ='<c:out value="${url}"/>' target="_blank">
        <c:out value="${link.caption}"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
</c:forEach>
</TD>
</TR>
</table>

