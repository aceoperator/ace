<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Are you sure you want to delete the canned message?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>Canned Message Administration</h3>

<html:form action="canned_message_management.do">
<table>
<tr>
<td>Message ID:</td>
<td><html:text property="id"/>
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit">Find</html:submit>
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit" onclick="return displayWarning()">Delete</html:submit></td>
</tr>
<tr>
<td>Group:</td>
<td><html:select property="group">
<html:optionsCollection property="userGroups"/>
</html:select></td>
</tr>
<tr>
<td>Description:</td>
<td><html:textarea property="description"/></td>
</tr>
<tr>
<td>Message Content:</td>
<td><html:textarea rows="10" cols="65" property="message"/></td>
</tr>
<tr>
<td colspan="3" align="center">
&nbsp;<p>
<html:submit property="submit">Create</html:submit>
<html:submit property="submit">Modify</html:submit>
<html:reset>Clear</html:reset>
</td>
</tr>
</table>
</html:form>

