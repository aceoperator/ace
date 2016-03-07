<%@ page language="java"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>

<script type="" language="JavaScript">
function displayWarning()
{
    if (!confirm("Are you sure you want to delete the feature?"))
    {
        return false;
    }
    else
    {
        return true;
    }
}

</script>

<h3>Operator Feature Data Administration</h3>

<html:form action="feature_operator_management.do">
	<table>
		<tr>
			<td>Feature Name:</td>
			<td><html:text property="name" /> &nbsp;&nbsp;&nbsp;&nbsp;<html:submit
					property="submit">Find</html:submit> &nbsp;&nbsp;&nbsp;&nbsp;<html:submit
					property="submit" onclick="return displayWarning()">Delete</html:submit>
			</td>
		<tr>
			<td>Domain:</td>
			<td><html:text property="domain" /></td>
		<tr>
			<td>Max Operators:</td>
			<td><html:text property="maxOperators" /></td>
		</tr>
		<tr>
			<td>Max Sessions per Operator:</td>
			<td><html:text property="maxSessions" /></td>
		</tr>
		<tr>
			<td>Max Visitor Queue Size:</td>
			<td><html:text property="maxQueueSize" /></td>
		</tr>

		<tr>
			<td>Display Wait Time:</td>
			<td><html:checkbox property="displayWaitTimeEstimation" /></td>
		</tr>

		<c:if test="${!empty requestScope.featureStatus}">
			<tr>
				<td>Status:</td>
				<td><c:out value="${requestScope.featureStatus}" /></td>
			</tr>
		</c:if>

		<c:if test="${!empty requestScope.pausedUntil}">
			<tr>
				<td>Paused Until:</td>
				<td><c:out value="${requestScope.pausedUntil}" /></td>
			</tr>
		</c:if>

		<tr>
			<td colspan="4" align="center">&nbsp;
				<p>
					<html:submit property="submit">Create</html:submit>
					<html:submit property="submit">Modify</html:submit>
					<html:reset>Clear</html:reset>

					<c:if test="${!empty requestScope.featureStatus}">
						<c:choose>
							<c:when test="${requestScope.featureStatus == 'Active'}">
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit">Deactivate</html:submit>
							</c:when>
							<c:otherwise>
&nbsp;&nbsp;&nbsp;&nbsp;<html:submit property="submit">Activate</html:submit>
							</c:otherwise>
						</c:choose>
					</c:if>

					<c:if test="${!empty requestScope.featurePaused}">
						<c:choose>
							<c:when test="${requestScope.featurePaused == 'true'}">
								<html:submit property="submit">Resume</html:submit>
							</c:when>
							<c:otherwise>
								<html:submit property="submit">Pause</html:submit>
							</c:otherwise>
						</c:choose>
					</c:if>
			</td>
		</tr>
	</table>
</html:form>

