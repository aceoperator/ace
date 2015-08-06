<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Deleting group and canned messages specific to this group. Before doing this operation, you should search for users who are members of this group and remove or reassign them to another group. Do you want to proceed?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>Group Data Administration</h3>

<html:form action="group_management.do">
<table>
<tr>
<td>Group Name:</td>
<td><html:text property="name"/></td>
<td><html:submit property="submit">Find</html:submit></td>
<td><html:submit property="submit" onclick="return displayWarning()">Delete</html:submit></td>
</tr>
<tr>
<td>Domain:</td>
<td><html:text property="domain"/></td>
</tr>
</table>
<p>
<table>
<tr>
<td>On member login:</td>
</tr>
<tr>
<td>
&nbsp;&nbsp;&nbsp;&nbsp;<html:checkbox property="memberLoginNotifyOwner" value="true"/>Notify owner<br>
&nbsp;&nbsp;&nbsp;&nbsp;<html:checkbox property="memberLoginNotifyMembers" value="true"/>Notify members
</td>
</tr>
<tr>
<td><br>On member call count change:</td>
</tr>
<tr>
<td>
&nbsp;&nbsp;&nbsp;&nbsp;<html:checkbox property="memberCallCountNotifyOwner" value="true"/>Notify owner<br>
&nbsp;&nbsp;&nbsp;&nbsp;<html:checkbox property="memberCallCountNotifyMembers" value="true"/>Notify members
</td>
</tr>
</table>
<p>
<table>
<tr>
<td>
<html:checkbox property="ownerLoginNotifyMembers" value="true"/>Notify members when the owner logs in
</td>
</tr>
<tr>
<td>
<html:checkbox property="ownerCallCountNotifyMembers" value="true"/>Notify members when the owner's call count changes
</td>
</tr>
<tr>
<td colspan="2" align="center">
&nbsp;<p>
<html:submit property="submit">Create</html:submit>
<html:submit property="submit">Modify</html:submit>
</td>
</tr>
</table>
</html:form>
