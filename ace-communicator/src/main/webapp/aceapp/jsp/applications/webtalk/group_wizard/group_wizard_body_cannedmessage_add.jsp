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

<h3>Add Canned Messages</h3>
<p>
Here is where you create canned messages. For each one you need to create, fill in the information below and click the
'Add Canned Message' button at the bottom of the page.

<html:form action="group_wizard_cannedmessage_add.do" focus="content">
<html:hidden property="domain"/>
<html:hidden property="counter"/>
<table>
<tr>
<td colspan="3">When you're done, move on to the next page.&nbsp;&nbsp;&nbsp;<html:submit property="submit">Finished, move on</html:submit></td>
</tr>
<tr><td><hr align="left" width="50%"><br></td></tr>
<tr>
<td>Specify the message content here:</td>
</tr>
<tr>
<td colspan="3"><html:textarea cols="60" property="content"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td colspan="3">Enter the description that the operators will see for this message in their list of predefined messages:</td>
</tr>
<tr>
<td colspan="3"><html:text property="description" maxlength="100"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td colspan="3">Select the group that this canned message should apply to:</td>
</tr>
<tr>
<td><html:select property="group">
        <html:optionsCollection property="userGroups"/>
    </html:select></td>
</tr>
</table>
<table cellspacing="20">
<tr>
<td><html:submit property="submit">Add Canned Message</html:submit></td>
<td><html:button property="submit" onclick="javascript:openLog()">View Session Log</html:button></td>
<td><html:submit property="submit" onclick="return displayWarning()">Cancel Wizard</html:submit></td>
</tr>
</table>
</html:form>
