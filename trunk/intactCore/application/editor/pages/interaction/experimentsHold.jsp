<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - Displays Experiments not yest added for an Interaction.
  --%>

<%@ page language="java"%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<style type="text/css">
    <%@ include file="/layouts/styles/editor.css" %>
</style>

<h3>Experiments not yet added to the Interaction</h3>

<c:if test="${not empty intHoldExpForm.experiments}">

    <html:form action="/interaction/experiment/hold">
        <table width="100%" border="0" cellspacing="1" cellpadding="2">
            <tr class="tableRowHeader">
                <th class="tableCellHeader" width="2%"></th>
                <th class="tableCellHeader" width="10%" colspan="2">Action</th>
                <th class="tableCellHeader" width="10%">Short Label</th>
                <th class="tableCellHeader" width="10%">Pubmed Id</th>
                <th class="tableCellHeader" width="10%">IntAct AC</th>
                <th class="tableCellHeader" width="58%">Full Name</th>
            </tr>
            <%-- To calculate odd or even row --%>
            <c:set var="row"/>
            <c:forEach var="experiments" items="${intHoldExpForm.experiments}">
                <%-- Different styles for even or odd rows --%>
                <c:choose>
                    <c:when test="${row % 2 == 0}">
                        <tr class="tableRowEven">
                    </c:when>
                    <c:otherwise>
                        <tr class="tableRowOdd">
                    </c:otherwise>
                </c:choose>

                    <td class="experimentHoldCell"/>

                    <td class="tableCell">
                        <html:submit indexed="true" property="cmd"
                            titleKey="int.exp.button.add.titleKey">
                            <bean:message key="button.add"/>
                        </html:submit>
                    </td>

                    <td class="tableCell">
                        <html:submit indexed="true" property="cmd"
                            titleKey="int.exp.button.hide.titleKey">
                            <bean:message key="int.exp.button.hide"/>
                        </html:submit>
                    </td>

                    <td class="tableCell">
                        <bean:write name="experiments" property="shortLabelLink" filter="false"/>
                    </td>
                    <td class="tableCell">
                    </td>
                    <td class="tableCell">
                        <bean:write name="experiments" property="ac"/>
                    </td>
                    <td class="tableCell">
                        <bean:write name="experiments" property="fullName"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </html:form>
</c:if>