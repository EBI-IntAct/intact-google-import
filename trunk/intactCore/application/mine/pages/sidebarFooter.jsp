<%@ page language="java"%>

<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id: sidebarFooter.jsp,v 1.3 2003/08/28 08:31:17 skerrien Exp $
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The common footer for the sidebar.
--%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<c:if test="${not empty sessionScope.user}">

    <jsp:useBean id="user"
                 scope="session"
                 class="uk.ac.ebi.intact.application.commons.business.IntactUserI" />

    <c:if test="${not empty user.userName}">
        User: <c:out value="${user.userName}"/>
        <br/>
    </c:if>

    <c:if test="${not empty user.databaseName}">
        Database: <c:out value="${user.databaseName}"/>
    </c:if>

</c:if>