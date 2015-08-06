<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<h3>Search Canned Messages</h3>

<html:form action="canned_message_search.do">
<table>
<caption>
Use '%' as a wild-card character in non-blank fields.
</caption>
<tr><td><br></td></tr>
<tr>
<td>Message ID:</td>
<td><html:text property="id"/></td>
</tr>
<tr>
<td>Group:</td>
<td><html:select property="group">
<html:optionsCollection property="userGroups"/>
</html:select></td>
</tr>
<tr>
<td>Description:</td>
<td><html:textarea property="description"/></td>
</tr>
<tr>
<td>Message Content:</td>
<td><html:textarea property="message"/></td>
</tr>
<tr>
<td>Sort By:</td>
<td>
<html:radio property="sortBy" value="ID"/>ID<br>
<html:radio property="sortBy" value="Group"/>Group<br>
</td>
</tr>
<tr>
<td colspan="2" align="center">
&nbsp;<p>
<html:submit>Search</html:submit>
<html:reset>Clear</html:reset>
</td>
</tr>
</table>
</html:form>

