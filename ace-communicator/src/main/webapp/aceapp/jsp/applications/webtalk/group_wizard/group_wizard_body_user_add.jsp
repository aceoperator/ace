<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Some data has been added to the system during your wizard session. Use the Remove Customer Wizard to delete it if you no longer want it. You'll be informed of all operations performed during this session up to this point. If you want to proceed with cancelling out of the wizard now, click 'OK'."))
    {
        return false;
    }
    else
    {
        return true;
    }
}

function openLog()
{
    width = 340

    if (window.screenLeft) // IE
    {
        if (window.screenLeft > width)
        {
            position = "left=" + (window.screenLeft - width) + ",top=" + window.screenTop
        }
        else
        {
            position = "left=0,top=" + window.screenTop
        }
    }
    else
    {
        if (window.screenX > width)
        {
            position = "screenX=" + (window.screenX - width) + ",screenY=" + window.screenY
        }
        else
        {
            position = "screenX=0,screenY=" + window.screenY
        }
    }

    <c:url var="url" value="/view_group_wizard_log.do"/>
    logWindow = window.open('<c:out value="${url}"/>', "wizardLogWindow", position + ",dependent=yes,width=" + width + ",height=400,scrollbars=yes,resizable=yes,toolbar=yes")
    logWindow.focus()
}
</script>

<h3>Add Operators</h3>
<p>
Now we will create operators. For each one, fill in the information below and click the
'Create Operator' button at the bottom of the page.

<html:form action="group_wizard_user_add.do" focus="name">
<table>
<tr>
<td>When you're done, move on to the next page.&nbsp;&nbsp;&nbsp;<html:submit property="submit">Finished, move on</html:submit></td>
</tr>
<tr><td><hr align="left" width="50%"><br></td></tr>
<tr>
<td>What login name do you want to give the new operator?</td>
</tr>
<tr>
<td><html:text property="name"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>Enter a password for the operator login:</td>
</tr>
<tr>
<td><html:password property="password"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>Re-enter the password to confirm:</td>
</tr>
<tr>
<td><html:password property="verifyPassword"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>What is the operator's full name? This is the name that others will see during a chat session with this operator:</td>
</tr>
<tr>
<td><html:text property="fullName"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>Enter the operator's e-mail address:</td>
</tr>
<tr>
<td><html:text property="address"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>Enter any additional or identifying information about this operator (title, etc.):</td>
</tr>
<tr>
<td><html:textarea property="additionalInfo"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>What group(s) should this operator be a member of?</td>
</tr>
<tr>
<td><html:select property="belongsToGroups" multiple="true">
        <html:optionsCollection property="userGroups"/>
    </html:select></td>
</tr>
</table>
<table cellspacing="20">
<tr>
<td><html:submit property="submit">Create Operator</html:submit></td>
<td><html:button property="submit" onclick="javascript:openLog()">View Session Log</html:button></td>
<td><html:submit property="submit" onclick="return displayWarning()">Cancel Wizard</html:submit></td>
</tr>
</table>
</html:form>
