<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Are you sure you want to delete the logs?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}
</script>

<h3>Delete System Logs</h3>

<html:form action="log_delete.do" focus="priorToInput">
<table>
  <tr>
    <td>
      <bean:message key="prompt.prior.to.input"/>
    </td>
    <td>
      <html:text property="priorToInput" size="12" maxlength="10"/>
    </td>
    <td>
        <bean:message key="message.date.format"/>
    </td>
  </tr>

<td colspan="2" align="center">
&nbsp;<p>
<html:submit onclick="return displayWarning()">Delete</html:submit>
<html:reset>Reset</html:reset>
</td>
</tr>
</table>
</html:form>

