<%@ page language="java"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>

<h3>User Search</h3>

<html:form action="user_search.do">
	<table>
		<caption>Use '%' as a wild-card character in non-blank
			fields.</caption>
		<tr>
			<td><br>
			</td>
		</tr>
		<tr>
			<td>User Name:</td>
			<td><html:text property="name" />
			</td>
		</tr>
		<tr>
			<td>Full Name:</td>
			<td><html:text property="fullName" />
			</td>
		</tr>
		<tr>
			<td>Domain:</td>
			<td><html:text property="domain" />
			</td>
		</tr>
		<tr>
			<td>Avatar:</td>
			<td><html:text property="avatar" />
			</td>
		</tr>
		<tr>
			<td>Additional Information:</td>
			<td><html:textarea property="additionalInfo" />
			</td>
		</tr>
		<tr>
			<td>Account Locked:</td>
			<td><html:select property="locked">
					<html:option value="0">Any</html:option>
					<html:option value="1">Locked</html:option>
					<html:option value="2">Unlocked</html:option>
				</html:select></td>
		</tr>
		<tr>
			<td>Force Password Change:</td>
			<td><html:select property="changePassword">
					<html:option value="0">Any</html:option>
					<html:option value="1">Set</html:option>
					<html:option value="2">Cleared</html:option>
				</html:select></td>
		</tr>
		<tr>
			<td>E-mail:</td>
			<td><html:text property="address" />
			</td>
		</tr>
		<tr>
			<td>Unavailable Transfer-to:</td>
			<td><html:text property="unavailXferTo" />
			</td>
		</tr>
		<tr>
			<td>Gatekeeper:</td>
			<td><html:text property="gatekeeper" />
			</td>
		</tr>
		<tr>
			<td><br>
			</td>
		</tr>
		<tr>
			<td>Owns Groups:<br> <html:select property="ownsGroups"
					multiple="true">
					<html:optionsCollection property="userGroups" />
				</html:select></td>
			<td>Belongs to Groups:<br> <html:select
					property="belongsToGroups" multiple="true">
					<html:optionsCollection property="userGroups" />
				</html:select></td>
		</tr>
		<tr>
			<td colspan="2" align="center">&nbsp;
				<p>
					<html:submit>Search</html:submit>
					<html:reset>Clear</html:reset>
			</td>
		</tr>
	</table>
</html:form>

