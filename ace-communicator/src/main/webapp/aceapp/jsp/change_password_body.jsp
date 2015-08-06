<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<h3>Change Password</h3>
<html:form action="change_password.do">
<table>
<tr>
<td>Old Password: </td>
<td><html:password property="oldPassword"/></td>
</tr>
<tr>
<td>New Password:</td>
<td><html:password property="newPassword"/></td>
</tr>
<tr>
<td>Verify New Password:</td>
<td><html:password property="newPasswordAgain"/></td>
</tr>
<tr>
<td><html:submit value="Change"/></td>
</tr>
</table>
</html:form>
