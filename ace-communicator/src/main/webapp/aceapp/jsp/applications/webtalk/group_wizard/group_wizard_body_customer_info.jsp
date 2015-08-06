<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>Customer Information</h3>
<p>
Fill in the information below about the company that will be using the new operator group(s).

<html:form action="group_wizard_customer_info.do" focus="companyName">
<table>
<tr>
<td colspan="4">What is the name of the company?</td>
</tr>
<tr>
<td><html:text size="35" property="companyName"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td colspan="4">Give the acronym, symbol, or a one-word shortened name for the company<br>
(all data stored for this customer will be associated with this acronym):
</td>
</tr>
<tr>
<td><html:text size="15" property="companyNickname" maxlength="25"/></td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td colspan="4">What is the company URL?</td>
</tr>
<tr>
<td><html:text size="40" property="companyUrl"/></td>
</tr>
</table>
<table cellspacing="20">
<tr>
<td>
<html:submit property="submit">Next</html:submit>
</td>
<td>
<html:submit property="submit">Cancel Wizard</html:submit>
</td>
</tr>
</table>
</html:form>
