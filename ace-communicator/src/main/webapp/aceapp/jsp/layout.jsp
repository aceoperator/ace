<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@page errorPage="/aceapp/jsp/talk_error.jsp" %>

<html>
<head>
<META http-equiv="Content-Type" content="text/html;charset=UTF-8">
<title><tiles:getAsString name="title"/></title>
</head>
<body>
<table cellpadding="5" width="100%">
<tr>
<td colspan="2"
    style="border-bottom-color: blue; border-bottom-style: solid; border-bottom-width: 2px;">
    <tiles:insert attribute="header"/></td>
</tr>
<tr>
<td width="200" valign="top" nowrap 
    style="border-right-color: blue; border-right-style: solid; border-right-width: 2px;">
<tiles:insert attribute="menu" ignore="true"/></td>
<td valign="top">
<ul>
<logic:messagesPresent message="true">
    <h4 style="color:green">System Message(s)</h4>
    <ul>
    <html:messages id="message" message="true">
    <li><bean:write name="message"/></li>
    </html:messages>
    </ul>
    <hr width="100%">
</logic:messagesPresent>

<html:errors/>

<tiles:insert attribute="body"/>
</ul>
</td>
</tr>
<tr>
<td colspan="2"><tiles:insert attribute="footer"/></td>
</tr>
</table>
</body>
</html>
