<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The part to link/unlink a feature.
  --%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<script language="JavaScript" type="text/javascript">

    // Returns true if two checkboxes have been selected
    function checkLink(form) {
        // Counter to count how many check items are selected.
        var count = 0;

        for (var i = 0; i < form.elements.length; i++) {
            // Only interested in 'checkbox' fields.
            if (form.elements[i].type == "checkbox") {
                // Only porcess if they are checked.
                if (form.elements[i].checked) {
                    //window.alert(form.elements[i].name);
                    ++count;
                }
            }
        }
        if (count != 2) {
            window.alert('Exactly two features must be selected to link them');
            return false;
        }
        return true;
    }
</script>

<%-- The anchor name for this page --%>
<a name="feature.link.unlink"/>

<table width="70%" border="0" cellspacing="1" cellpadding="2">
    <tr class="tableRowHeader">
        <th class="tableCellHeader" colspan="2">Link/Unlink Features </th>
        <th><intact:documentation section="editor.int.proteins"/></th>
    </tr>

    <tr class="tableRowEven">

        <td class="tableCell" align="right" valign="top">
            <html:submit property="dispatch" onclick="return checkLink(this.form);"
                titleKey="int.proteins.button.feature.link.titleKey">
                <bean:message key="int.proteins.button.feature.link"/>
            </html:submit>
        </td>
        <td class="tableCell" align="right" valign="top">
            <html:submit property="dispatch" onclick="return checkLink(this.form);"
                titleKey="int.proteins.button.feature.unlink.titleKey">
                <bean:message key="int.proteins.button.feature.unlink"/>
            </html:submit>
        </td>
    </tr>
    </tr>
</table>

<%-- Display errors for linking --%>
<html:errors property="feature.link"/>
