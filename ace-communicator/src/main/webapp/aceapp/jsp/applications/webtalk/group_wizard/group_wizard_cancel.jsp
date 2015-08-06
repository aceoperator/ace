<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<tiles:insert page="/aceapp/jsp/layout.jsp" flush="true">
 <tiles:put name="title" value="Group Hosting Wizard"/>
 <tiles:put name="header" value="/aceapp/jsp/header.jsp"/>
 <tiles:put name="menu" value="/aceapp/jsp/menu.jsp"/>
 <tiles:put name="footer" value="/aceapp/jsp/footer.jsp"/>
 <tiles:put name="body" value="/aceapp/jsp/applications/webtalk/group_wizard/group_wizard_body_cancel.jsp"/>
</tiles:insert>