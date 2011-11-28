<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<!--
    The layout for the side bar, which consists of intact logo, contents and the footer.
    and menu.

    Author: Sugath Mudali (smudali@ebi.ac.uk)
    Version: $Id: sidebarLayout.jsp 10107 2007-10-22 12:35:10Z skerrien $
-->

<table width="100%" height="100%">
    <tr>
        <td valign="top" height="*">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">

                <%-- Sidebar logo --%>
                <tr>
                    <td valign="top" align="left" width="113" height="75">
                        <img src="<%=request.getContextPath()%>/images/logo_intact.png" border="0" width="113">
                    </td>
                </tr>

                <!-- The input dialog box -->
                <tr>
                    <td>
                        <tiles:insert attribute="input-dialog" ignore="true"/>
                    </td>
                </tr>

                <%-- Sidebar menu links --%>
                <tr>
                    <td>
                        <tiles:insert attribute="menu" ignore="true"/>
                    </td>
                </tr>

            </table>
        </td>
    </tr>

    <%-- Sidebar footer --%>
    <tr>
        <span class="footer">
            <td valign="top" height="5%">
                <tiles:insert attribute="footer" ignore="true"/>
            </td>
        </span>
    </tr>
</table>
