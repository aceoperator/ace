<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Are you sure you want to delete the feature?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}

function openHelp()
{
    win = window.open("aceapp/html/feature_information_help.html", 
                      "talk_unreg_page",
                      "width=350,height=300,scrollbars=yes,status=yes");
}
</script>

<h3>Feature Data Administration</h3>

<html:form action="feature_management.do">
<table>
<tr>
<td>Feature Name:</td>
<td><html:text property="name"/>
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit">Find</html:submit>
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit" onclick="return displayWarning()">Delete</html:submit>
</td>
</tr>
<tr>
<td>Class Name:</td>
<td><html:text size="50" property="className"/></td>
</tr>
<tr>
<td>Parameters:</td>
<td><html:textarea rows="4" cols="35" property="params"/></td>
<td valign="top"><a href="javascript:openHelp()">Help</a></td>
</tr>
<c:if test="${!empty requestScope.featureStatus}">
<tr>
<td>Status:</td>
<td><c:out value="${requestScope.featureStatus}"/></td>
</tr>
</c:if>
<tr>
<td colspan="4" align="center">
&nbsp;<p>
<html:submit property="submit">Create</html:submit>
<html:submit property="submit">Modify</html:submit>
<html:reset>Clear</html:reset>
<c:if test="${!empty requestScope.featureStatus}">
<c:choose>
<c:when test="${requestScope.featureStatus == 'Active'}">
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit">Deactivate</html:submit>
</c:when>
<c:otherwise>
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit">Activate</html:submit>
</c:otherwise>
</c:choose>
</c:if>
</td>
</tr>
</table>
</html:form>

