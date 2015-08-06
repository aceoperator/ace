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

<h3>Add Operator Group(s)</h3>
<p>
Now we will add the operator group(s) for your customer company. For each group to be added, fill in the information below and click the
'Add Group' button at the bottom of the page. 

<html:form action="group_wizard_group_add.do" focus="groupName">
<table>
<tr>
<td>When you're done, move on to the next page.&nbsp;&nbsp;&nbsp;<html:submit property="submit">Finished, move on</html:submit></td>
</tr>
<tr><td><hr align="left" width="50%"><br></td></tr>
<tr>
<td>Enter the name of the new group:</td>
</tr>
<tr>
<td><c:out value="${sessionScope.groupWizardDomain}"/>-<html:text property="groupName"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>What is the maximum number of operators allowed to handle visitor sessions for this group at any given time?</td>
</tr>
<tr>
<td><html:text size="5" property="maxOperators"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>How many visitor sessions can one operator handle at a time?</td>
</tr>
<tr>
<td><html:text size="5" property="maxSessions"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>When no operators in this group are available, an online visitor who clicks on the "Live Help" button can leave a 
    message. Enter the email address to which these messages should be sent:</td>
</tr>
<tr>
<td><html:text size="35" property="messageboxEmail"/></td>
</tr>
</table>
<table cellspacing="20">
<tr>
<td><html:submit property="submit">Add Group</html:submit></td>
<td><html:button property="submit" onclick="javascript:openLog()">View Session Log</html:button></td>
<td><html:submit property="submit" onclick="return displayWarning()">Cancel Wizard</html:submit></td>
</tr>
</table>
</html:form>
