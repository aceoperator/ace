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

<c:if test="${not empty error}">
		<div class="errorblock">
			Your login attempt was not successful, try again.<br /> Caused :
			${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
		</div>
</c:if>

<form name="login" action="<c:url value='/j_spring_security_check' />" method='POST'>
<table border="0">

  <tr>
    <th align="right">
      <bean:message key="prompt.username"/>
    </th>
    <td align="left">
      <input type='text' name='j_username' value='' size='16' maxlength='16'>
    </td>
  </tr>

  <tr>
    <th align="right">
      <bean:message key="prompt.password"/>
    </th>
    <td align="left">
      <input type='password' name='j_password' size='16' maxlength='16' />
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
	document.login.j_username.focus();
</script>

</form>
