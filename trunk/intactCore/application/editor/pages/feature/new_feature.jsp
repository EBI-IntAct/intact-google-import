<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - New feature part of the Feature editor.
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<%-- The current view --%>
<c:set var="view" value="${user.view}"/>

<%-- Individual menu lists --%>
<c:set var="type_menu" value="${view.cvFeatureTypeMenu}"/>
<c:set var="ident_menu" value="${view.cvFeatureIdentificationMenu}"/>

<h3>New features</h3>

<%-- The anchor name for this page --%>
<a name="feature.new"/>

<table width="100%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableRowHeader">
        <th class="tableCellHeader">
            <bean:message key="label.ac"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="label.shortlabel"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="label.fullname"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="feature.new.label.type"/>
        </th>
        <th class="tableCellHeader">
            <bean:message key="feature.new.label.ident"/>
        </th>
        <th>
            <intact:documentation section="editor.short.labels"/>
        </th>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell">
            <bean:write property="ac" name="featureForm" filter="false"/>
        </td>
        <td class="tableCell">
            <html:text property="shortLabel" size="20" maxlength="20" name="featureForm"
                styleClass="inputRequired"/>
            <html:errors property="shortLabel"/>
        </td>

        <td class="tableCell">
            <html:text property="fullName" size="80" maxlength="80" name="featureForm"/>
        </td>

        <td class="tableCell" align="left" valign="top">
            <html:select property="featureType" name="featureForm" styleClass="inputRequired">
                <html:options name="type_menu"/>
            </html:select>
            <html:errors property="feature.type"/>
        </td>
        <td class="tableCell" align="left" valign="top">
            <html:select property="featureIdent" name="featureForm">
                <html:options name="ident_menu"/>
            </html:select>
        </td>
    </tr>
</table>

<%----%>
<%--<html:messages id="message" message="true">--%>
<%--	<span class="warning">--%>
<%--		<bean:write name="message" filter="false"/>--%>
<%--	</span>--%>
<%--</html:messages>--%>
