<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<h3>Traffic Report</h3> 

<b> Report generated on <c:out value="${requestScope.today}"/> 
	for group - <c:out value="${requestScope.groupId}"/><p>

  <c:choose>
    <c:when test="${!empty requestScope.trafficData}">
        <table cellspacing="10" border="0">
        <tr>
            <th>Time</th>
            <th># Active Operators</th>
            <th># Active Conversations</th>
            <th># Visitor Queue Size</th>
        </tr>
        <c:forEach var="record" items="${requestScope.trafficData}">
            <tr>  
                <td nowrap><fmt:formatDate type="both" value="${record.timestamp.time}" /></td>
                <td nowrap><fmt:formatNumber type="number" maxFractionDigits="2" value="${record.numActiveOperators}" /></td>
                <td nowrap><fmt:formatNumber type="number" maxFractionDigits="2" value="${record.numConversations}" /></td>
                <td nowrap><fmt:formatNumber type="number" maxFractionDigits="2" value="${record.numUsersInQueue}" /></td>
            </tr>
         </c:forEach>
         </table>
     </c:when>
     <c:otherwise>
       <H4> No data matches the specified query.</H4>
     </c:otherwise>
   </c:choose>
