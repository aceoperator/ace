<%-- @author Vinod Batra --%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<head><title>Visitor Report</title></head>
<h3 style='arial'> Report generated on  
    <c:out value="${requestScope.reportDate}"/>
</h3>
 <c:choose>
    <c:when test="${!empty requestScope.reg_user_list}">
        <table cellspacing="10" border="0">
        <tr>
            <th align=left>User Name</th>
            <th align=left>Login Time </th>
            <th align=left>Logout Time</th>
            <th align=left>Chats Initiated</th>
            <th align=left>Chats Answered</th>
            <th align=left>Chats Not Answered</th>
            <th align=left>Chats Busy</th>

         </tr>
        <c:forEach var="record" items="${requestScope.reg_user_list}">
            <tr>  
                <td>
                
                
                <c:out value="${record.userName}"/></td>
                <td nowrap><fmt:formatDate type="both" value="${record.loginTime.time}" /></td>
                <td nowrap><fmt:formatDate type="both" value="${record.logoutTime.time}" /></td>
                <td><c:out value="${record.chatsInitiated}"/></td>
                <td><c:out value="${record.chatsAnswered}"/></td>
                <td><c:out value="${record.chatsNotAnswered}"/></td>
                <td><c:out value="${record.chatsBusy}"/></td>                
            </tr>
         </c:forEach>
         
       </table>
     </c:when>
     <c:otherwise>
       <h4> No data matches the specified query. </h4>
     </c:otherwise>
   </c:choose>



