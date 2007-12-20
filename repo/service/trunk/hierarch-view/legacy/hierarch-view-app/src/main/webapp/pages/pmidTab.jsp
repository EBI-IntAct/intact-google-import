<%@ page import="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: nneuhaus
  Date: 12-Dec-2007
  Time: 11:13:31
--%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<table border="0" cellspacing="0" cellpadding="3" width="100%" style="margin-width:0;">

    <tr>
        <!-- Displays the available highlightment source -->

        <%
            List<SourceBean> ListSources = ( ArrayList ) session.getAttribute( "sources" );
            List<SourceBean> tmpListSources = new ArrayList( ListSources.size() );

            int j = 0;
            for ( SourceBean sourceBean : ListSources ) {
                if ( sourceBean.getType().equalsIgnoreCase( "PMID" ) ) {
                    tmpListSources.add( j, sourceBean );
                    j++;
                }
            }
            session.setAttribute( "tmpListSources", tmpListSources );
        %>

        <td valign="top">

            <!-- PMID terms -->
            <display:table
                    name="sessionScope.tmpListSources" width="100%" class="tsources"
                    decorator="uk.ac.ebi.intact.application.hierarchview.struts.view.utils.SourceDecorator">
                <display:column property="description" title="Description" width="63%" align="left"/>
                <display:column property="directHighlightUrl" title="Show" width="8%" align="center"/>
                <display:column property="count" title="Count" width="9%" align="center"/>
                <display:column property="id" title="ID" width="20%" align="left"/>
                <display:setProperty name="basic.msg.empty_list"
                                     value="No source available for that interaction network"/>
            </display:table>

        </td>
    </tr>
</table>