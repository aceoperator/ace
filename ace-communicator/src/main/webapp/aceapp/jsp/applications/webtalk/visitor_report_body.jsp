<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">
  <font color="red">
    ERROR:  Application resources not loaded -- check servlet container
    logs for error messages.
  </font>
</logic:notPresent>

<h3><bean:message key="genReport.unregUser" bundle="WEB_TALK_RESOURCES"/></h3>
<H4 style="color:green"><bean:message key="index.dates" bundle="WEB_TALK_RESOURCES"/></H4>

<html:form action="/visitor_report" focus="startDate">
<table border="0" cellspacing="5">
  <tr>    
    <td>
      <bean:message key="prompt.RSDate" bundle="WEB_TALK_RESOURCES"/>
    </td>   
    <td>
      <html:text property="startDate" size="10" maxlength="10"/>
    </td>
  </tr>
  <tr>
    <td  size="20">
      <bean:message key="prompt.REDate" bundle="WEB_TALK_RESOURCES"/>
    </td>
    <td >
      <html:text property="endDate" size="10" maxlength="10"/>
    </td>
  </tr>
 <tr>
    <td  size="20">
      <bean:message key="prompt.sortBy" bundle="WEB_TALK_RESOURCES"/>
    </td>
    <td >
      
      <html:radio property="orderBy" value="name"/> Name
      </TD></TR>

    <TR><TD>&nbsp</TD><TD>
      <html:radio property="orderBy" value="login_date"/> Login Date
    </td>
    
  </tr>
  <tr>
    <td colspan="2" align="center">
    &nbsp;<p>
      <html:submit property="submit" value="Submit"/>
      <html:reset/>
    </td>
  </tr>

</table>

</html:form>

