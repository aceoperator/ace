<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<tiles:insert page="/aceapp/jsp/layout.jsp" flush="true">
 <tiles:put name="title" value="Ace Adminstration Main Menu"/>
 <tiles:put name="header" value="/aceapp/jsp/header.jsp"/>
 <tiles:put name="menu" value="/aceapp/jsp/menu.jsp"/>
 <tiles:put name="footer" value="/aceapp/jsp/footer.jsp"/>
 <tiles:put name="body" value="/aceapp/jsp/main_menu_body.jsp"/>
</tiles:insert>