<%-- @author Vinod Batra --%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<h3>Visitor Report</h3> 

<b> Report generated on <c:out value="${requestScope.today}"/><p>

  <c:choose>
    <c:when test="${!empty requestScope.visitor_list}">
        <table cellspacing="10" border="0">
        <tr>
            <th>Name</th>
            <th>Address</th>
             <th>Login Time</th>
            <th align="left">Additional Info</th>
        </tr>
        <c:forEach var="record" items="${requestScope.visitor_list}">
            <tr>  
                <td nowrap="true"><c:out value="${record.fullName}"/></td>
                <td nowrap="true"><c:out value="${record.email}"/></td>
                <td nowrap="true"><fmt:formatDate type="both" value="${record.loginTime.time}" /></td>
                <td nowrap="true"><c:out value="${record.additionalInfo}"/></td>
            </tr>
         </c:forEach>
         </table>
     </c:when>
     <c:otherwise>
       <h4>No data matches the specified query.</h4>
     </c:otherwise>
   </c:choose>
