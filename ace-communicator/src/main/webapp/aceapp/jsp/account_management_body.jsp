<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Are you sure you want to delete the user account?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>User Account Administration</h3>

<html:form action="account_management.do">
<table>
<tr>
<td>User Name:</td>
<td><html:text property="name"/></td>
<td><html:submit property="submit">Find</html:submit></td>
<td><html:submit property="submit" onclick="return displayWarning()">Delete</html:submit></td>
</tr>
<tr>
<td>Password:</td>
<td><html:password property="password"/></td>
</tr>
<tr>
<td>Verify Password:</td>
<td><html:password property="verifyPassword"/></td>
</tr>
<tr>
<td>Additional Information:</td>
<td><html:textarea property="additionalInfo"/></td>
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
