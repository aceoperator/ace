<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h3>Customer Selection</h3>
<p>
Select the acronym, or domain name, of the customer whose data you want to delete. Pressing the 'Next' button will 
display the relevant data.

<html:form action="drop_customer_select.do" focus="domains">
<table>
<tr>
<td><html:select property="domain">
        <html:optionsCollection property="domains"/>
    </html:select></td>
</tr>
</table>
<table cellspacing="20">
<tr>
<td><html:submit property="submit">Next</html:submit></td>
<td><html:submit property="submit">Cancel Wizard</html:submit></td>
</tr>
</table>
</html:form>
