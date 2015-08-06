<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<h3>View System Logs</h3>

<html:form action="log_search.do" focus="startDate">
<table>
  <tr>
    <td>
      <bean:message key="prompt.start.date"/>
    </td>
    <td>
      <html:text property="startDate" size="12" maxlength="10"/>
    </td>
    <td>
        <bean:message key="message.date.format"/>
    </td>
  </tr>
  <tr>
    <td>
      <bean:message key="prompt.end.date"/>
    </td>
    <td>
      <html:text property="endDate" size="12" maxlength="10"/>
    </td>
    <td>
        <bean:message key="message.date.format"/>
    </td>
  </tr>
 <tr>
    <td>Severity Level(s):
    </td>
    <td>
        <html:select property="severityLevels" multiple="true">
        <html:options name="logSeverityLevelStrings"/>
        </html:select>
    </td>
 </tr>
 <tr>
    <td>Process Name(s):
    </td>
    <td>
        <html:select property="processNames" multiple="true">
        	<html:options name="logProcessNames"/>
        </html:select>
    </td>
 </tr>
 <tr>
    <td>Log Message Text:
    </td>
    <td>
        <html:text property="messageText"/>
    </td>
    <td>
        <bean:message key="message.wildcard"/>
    </td>
 </tr>
<tr>
<td colspan="2" align="center">
&nbsp;<p>
<html:submit>Search</html:submit>
<html:reset>Reset</html:reset>
</td>
</tr>
</table>
</html:form>
