<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%--
    The cvedit default look & feel layout.
    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id$
--%>

<html:html>

<head>
    <title><tiles:getAsString name="title"/></title>
    <html:base/>
    <link rel="stylesheet" type="text/css" href="styles/cvedit.css"/>
</head>

<body topmargin="0" leftmargin="0">
<table border="0" height="100%" width="100%" cellspacing="5">

<%-- Sidebar section --%>
<tr>
    <td bgcolor="#boc4de" width='150' valign='top' align='left'>
        <tiles:insert attribute="sidebar"/>
    </td>

    <%-- Next cell is the header --%>
    <td valign="top" height="100%" width="*">
        <table width="100%" height="100%">

            <%-- Header section --%>
            <tr>
                <td bgcolor="#ffeeaa" height="8%">
                    <tiles:insert attribute="header"/>
                </td>
            </tr>

            <%-- Content section --%>
            <tr>
                <td valign="top" height="*">
                    <!-- No errors if none specified -->
                    <tiles:insert attribute="content" ignore="true"/>
                </td>
            </tr>

            <%-- The footer --%>
            <tr>
                <td valign="bottom" height="15%">
                    <tiles:insert attribute="footer"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</body>
</html:html>
