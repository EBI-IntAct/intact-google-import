<%@ page import="uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants,
                 uk.ac.ebi.intact.application.editor.business.EditorService"
%>
 <!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - This page accepts changes to an Annotated object's short label and full name.
  --%>

<%@ page language="java"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/editor.tld" prefix="editor"%>

<jsp:useBean id="user" scope="session"
    class="uk.ac.ebi.intact.application.editor.business.EditUser"/>

<%-- Class wide declarations. --%>
<%!
    String formName = EditorConstants.FORM_CVINFO;
%>

<%
    // To Allow access to Editor Service.
    EditorService service = (EditorService)
            application.getAttribute(EditorConstants.EDITOR_SERVICE);
%>

<script language="JavaScript" type="text/javascript">
    // This is a global variable to setup a window.
    var newWindow;

    // Create a new window if it hasnt' created before and bring it to the
    // front if it is focusable.
    function makeNewWindow(link) {
        if (!newWindow || newWindow.closed) {
            newWindow = window.open(link, "display", "height=500,width=600");
            newWindow.focus();
        }
        else if (newWindow.focus) {
            newWindow.focus();
            newWindow.location.href = link;
        }
    }

    // Will be invoked when the user selects on a link.
    function show(topic, label) {
        var link = "<%=service.getSearchLink()%>"
            + "?searchString=" + label + "&searchClass=" + topic;
        //    window.alert(link);
        makeNewWindow(link);
    }
</script>

<html:form action="/cv/info" focus="shortLabel" onsubmit="return validateCvInfoForm(this)">
    <table width="80%" border="0" cellspacing="1" cellpadding="2">
        <tr class="tableRowHeader">
            <th class="tableCellHeader">Action</th>
            <th class="tableCellHeader">AC</th>
            <th class="tableCellHeader">
                    <bean:message key="cvinfo.label.shortlabel"/>
            </th>
            <th class="tableCellHeader">Full Name</th>
            <th>
                <editor:helpLink tag="short.labels"/>
            </th>
        </tr>
        <tr class="tableRowEven">
            <td class="tableCell">
                <html:submit titleKey="cvinfo.buton.save.titleKey">
                    <bean:message key="button.save"/>
                </html:submit>
            </td>
            <td class="tableCell">
                <bean:write property="ac" name="<%=formName%>"/>
            </td>
            <td class="tableCell">
                <html:text property="shortLabel"  size="15" maxlength="20"
                    name="<%=formName%>"/>
            </td>
            <td class="tableCell">
                <html:text property="fullName"  size="80" maxlength="250"
                    name="<%=formName%>"/>
            </td>
        </tr>

        <%-- These errors are shown when the client side validation is
             turned off.
        --%>
        <logic:messagesPresent>
            <logc:messages id="error">
                <tr class="tableRowOdd">
                    <td class="tableErrorCell" colspan="4">
                        <html:errors/>
                    </td>
                </tr>
            </logic:messages>
        </logic:messagesPresent>

        <%-- Filter error messages relevant to this page only. --%>
        <logic:messagesPresent property="cvinfo">
            <tr class="tableRowOdd">
                <td class="tableErrorCell" colspan="4">
                    <html:errors/>
                </td>
            </tr>
        </logic:messagesPresent>

        <%-- Filter error messages relevant to the short label. --%>
        <logic:messagesPresent property="cvinfo.label">
            <tr class="tableRowOdd">
                <td class="tableErrorCell" colspan="4">
                    <html:errors/>
                </td>
            </tr>
            <tr class="tableRowEven">
                <td class="tableCell" colspan="4">
                    The existing short labels are:
                </td>
            </tr>
            <tr class="tableRowOdd">
                <td class="tableCell" colspan="4">
                    <editor:displayShortLabels/>
                </td>
            </tr>
        </logic:messagesPresent>

    </table>
</html:form>
<html:javascript formName="cvInfoForm"/>
