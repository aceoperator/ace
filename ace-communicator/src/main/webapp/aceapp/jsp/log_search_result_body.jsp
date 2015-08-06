<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h3>List of Logs</h3>

<table border="0" cellpadding="5">
        <tr>
            <th align=center>Time</th>
            <th align=center>Severity</th>
             <th align=center>Process Name</th>
            <th align=center>Message</th>
         </tr>

        <c:forEach var="record" items="${requestScope.logResultList}">
            <c:choose>
                <c:when test="${record.severity == \"INFO\"}">
                    <c:set var="color" value="color:blue"/>
                </c:when>
                <c:when test="${record.severity == \"WARN\"}">
                    <c:set var="color" value="color:orange"/>
                </c:when>
                <c:when test="${record.severity == \"ERROR\"}">
                    <c:set var="color" value="color:indianred"/>
                </c:when>
                <c:when test="${record.severity == \"FATAL\"}">
                    <c:set var="color" value="color:red"/>
                </c:when>
                <c:otherwise>
                    <c:set var="color" value="color:black"/>
                </c:otherwise>
            </c:choose>
            <tr style="<c:out value="${color}"/>">  
                <td valign="top" nowrap><fmt:formatDate type="both" value="${record.timestamp.time}" /></td>
                <td valign="top"><c:out value="${record.severity}"/></td>
                 <td valign="top"><c:out value="${record.process}"/></td>    
                <td valign="top">
                	<pre><c:out value="${record.message}"/></pre>
                </td>
            </tr>
            
         </c:forEach>
</table>
