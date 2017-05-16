<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">
  <font color="red">
    ERROR:  Application resources not loaded -- check servlet container
    logs for error messages.
  </font>
</logic:notPresent>

<h4 style="color:green"><bean:message key="index.message"/></h4>

<c:if test="${param.error != null}"> 
	<p style="color:red"><b>Invalid username / password</b></p>
</c:if>

<c:url var="loginUrl" value="/login"/>
<form name="login" action="${loginUrl}" method="post">
<table border="0">

  <tr>
    <th align="right">
      <bean:message key="prompt.username"/>
    </th>
    <td align="left">
      <input type="text" id="username" name="username" size='16' maxlength='16'/> 
    </td>
  </tr>

  <tr>
    <th align="right">
      <bean:message key="prompt.password"/>
    </th>
    <td align="left">
      <input type="password" id="password" name="password" maxlength='16' /> 
    </td>
  </tr>
  
  <tr>
    <td><p></td>
  </tr>

  <tr>
    <td align="right">
      <input name="submit" type="submit" value="Submit" />
    </td>
    <td align="left">
      <input name="reset" type="reset" value="Reset" />
    </td>
  </tr>

</table>

<script>
	document.login.username.focus();
</script>

</form>
