<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Displays the results data from the search query.
  --%>

<%@ page language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-nested.tld" prefix="nested"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<jsp:useBean id="user" scope="session"
    class="uk.ac.ebi.intact.application.editor.business.EditUser"/>

Search class: <c:out value="${user.searchClass}"/>
&nbsp;Query: <c:out value="${user.searchQuery}"/>

<html:form action="/results">

<table width="100%">
    <tr class="tableRowHeader">
        <th class="tableCellHeader" width="10%">AC</th>
        <th class="tableCellHeader" width="20%">Short Label</th>
        <th class="tableCellHeader" width="70%">Full Name</th>
    </tr>
    <nested:iterate property="items">
        <!-- Different styles for even or odd rows -->
        <c:choose>
            <c:when test="${row % 2 == 0}">
                <tr class="tableRowEven">
            </c:when>
            <c:otherwise>
                <tr class="tableRowOdd">
            </c:otherwise>
        </c:choose>

        <!-- Increment row by 1 -->
        <c:set var="row" value="${row + 1}"/>

        <td class="tableCell">
            <nested:link page="/do/results"
                paramId="shortLabel" paramProperty="shortLabel">
                <nested:write property="ac"/>
            </nested:link>
        </td>
        <td class="tableCell"><nested:write property="shortLabel"/></td>
        <td class="tableCell"><nested:write property="fullName"/></td>
    </tr>
    </nested:iterate>
</table>
</html:form>
