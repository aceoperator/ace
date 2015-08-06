<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script>
function displayWarning()
{
    if (!confirm("Are you sure you want to remove this identifier?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>Black List Administration</h3>

<html:form action="blacklist_management.do">
<html:hidden property="id" />
<html:hidden property="userId" />
<table>
<tr>
<td>Identifier:</td>
<td><html:text property="identifier"/></td>
<td><html:submit property="submit" onclick="return displayWarning()">Delete</html:submit></td>
</tr>
<tr>
<td>Type:</td>
<td>
<html:select property="type">
	<html:option value="0">Cookie</html:option>
	<html:option value="1">IP Address</html:option>
</html:select>
</td>
</tr>
<tr>
<tr>
<td>Level:</td>
<td><html:text property="level"/></td>
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