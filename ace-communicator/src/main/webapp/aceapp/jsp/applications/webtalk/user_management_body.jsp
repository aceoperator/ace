<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script>
function displayWarning()
{
    if (!confirm("Deleting user, groups owned by this user and canned messages specific to those groups. Before doing this operation, you should search for users who are members of those groups and remove or reassign them to another group. Do you want to proceed?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>User Data Administration</h3>

<html:form action="user_management.do">
<table>
<tr>
<td>User Name:</td>
<td><html:text property="name"/></td>
<td><html:submit property="submit">Find</html:submit></td>
<td><html:submit property="submit">Manage Blacklist</html:submit></td>
<td><html:submit property="submit" onclick="return displayWarning()">Delete</html:submit></td>
</tr>
<tr>
<td>Password:</td>
<td><html:password property="password"/></td>
</tr>
<tr>
<td>Re-enter Password:</td>
<td><html:password property="verifyPassword"/></td>
</tr>
<tr>
<td>Full Name:</td>
<td><html:text property="fullName"/></td>
</tr>
<tr>
<td>Domain:</td>
<td><html:text property="domain"/></td>
</tr>
<tr>
<td>Avatar:</td>
<td><html:text property="avatar"/></td>
</tr>
<tr>
<td>Additional Information:</td>
<td><html:textarea property="additionalInfo"/></td>
</tr>
<tr>
<td>Hide Account Information from Visitors:</td>
<td><html:checkbox property="privateInfo" /></td>
</tr>
<tr>
<td>Account Locked:</td>
<td><html:checkbox property="locked" /></td>
</tr>
<tr>
<td>Force Change Password:</td>
<td><html:checkbox property="changePassword" /></td>
</tr>
<tr>
<td>E-mail:</td>
<td><html:text property="address"/></td>
</tr>
<tr>
<td>Unavailable Transfer-to:</td>
<td><html:text property="unavailXferTo"/></td>
</tr>
<tr>
<td>Gatekeeper:</td>
<td><html:text property="gatekeeper"/></td>
</tr>
<tr>
<td>Flags:</td>
<td><html:text property="flags"/></td>
</tr>
<tr><td><br></td></tr>
<tr>
<td>Security Question:</td>
<td colspan="2"><html:text property="securityQuestion1" maxlength="128" size="35"/></td>
<td colspan="2" align="right">Answer:</td>
<td><html:text property="securityAnswer1" maxlength="40" size="10"/></td>
</tr>
<tr>
<td>Security Question:</td>
<td colspan="2"><html:text property="securityQuestion2" maxlength="128" size="35"/></td>
<td colspan="2" align="right">Answer:</td>
<td><html:text property="securityAnswer2" maxlength="40" size="10"/></td>
</tr>
<tr>
<td>Security Question:</td>
<td colspan="2"><html:text property="securityQuestion3" maxlength="128" size="35"/></td>
<td colspan="2" align="right">Answer:</td>
<td><html:text property="securityAnswer3" maxlength="40" size="10"/></td>
</tr>
<tr><td><br></td></tr>
<tr>
<td>Owns Groups:<br>
    <html:select property="ownsGroups" multiple="true">
    <html:optionsCollection property="userGroups"/>
</html:select>
</td>
<td>Is a Member of Groups:<br>
    <html:select property="belongsToGroups" multiple="true">
    <html:optionsCollection property="userGroups"/>
</html:select>
</td>
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

