<%@ page language="java" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<tiles:insert page="/aceapp/jsp/layout.jsp" flush="true">
 <tiles:put name="title" value="RESTRICTED ACCESS"/>
 <tiles:put name="header" value="/aceapp/jsp/header.jsp"/>
 <tiles:put name="footer" value="/aceapp/jsp/footer.jsp"/>
 <tiles:put name="body" value="/aceapp/jsp/restricted_access_error_body.jsp"/>
</tiles:insert>