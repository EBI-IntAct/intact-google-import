<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/hierarchView.tld" prefix="hierarchView" %>

<%@ page import="uk.ac.ebi.intact.application.hierarchView.highlightment.*,
                 uk.ac.ebi.intact.application.hierarchView.business.IntactUserIF" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchView.highlightment.source.HighlightmentSource" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchView.business.graph.*" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchView.business.Constants" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchView.struts.StrutsConstants" %>

<%@ page import="uk.ac.ebi.intact.application.hierarchView.struts.view.OptionGenerator" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchView.business.PropertyLoader" %>
<%@ page import="uk.ac.ebi.intact.application.hierarchView.business.image.ImageBean" %>
<%@ page import="java.util.ArrayList" %>
<%@page import="java.io.*" %>
<%@page import="java.util.Collection"%>


<html:html locale="true">

<head>

    <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Expires" CONTENT="-1">


    <title>
       <bean:message key="hierarchView.view.title"/>
    </title>

    <link rel="stylesheet" href="hierarchview.css" type="text/css">

    <script language="JavaScript">
     <!--
     /**
      * Allows to forward to a specified URL inside a frame
      */
     function forward ( absoluteUrl ) {
        parent.<%=Constants.RIGHT_FRAME_NAME%>.location.href = absoluteUrl;
     }

     //-->
    </script>

    <html:base/>

</head>

<body>

<html:errors/>

<!--hierarchView:checkInit forwardOnError="/index.jsp" /-->

<%
   /**
    * Retreive data from the session
    */
   IntactUserIF user = (IntactUserIF) session.getAttribute (Constants.USER_KEY);


   String AC           = user.getAC();
   String depth        = user.getDepth();
   Boolean depthLimit  = new Boolean(user.getHasNoDepthLimit());
   String noDepthLimit = "null";
   if (null != depthLimit) {
     noDepthLimit = depthLimit.toString();
   }

   Collection keys       = user.getKeys();
   String methodLabel    = user.getMethodLabel();
   String methodClass    = user.getMethodClass();
   String behaviour      = user.getBehaviour();
   InteractionNetwork in = user.getInteractionNetwork();

   /**
    * get the data to display in the highlightment form
    */
   String fieldAC     = (null == AC ? "" : AC);
   String fieldDepth  = (null == depth ? "" : depth);
   String fieldMethod = (null == methodLabel ? "" : methodLabel);

   if (null == keys) {
      // don't refresh the right frame if the user try to highlight.
%>
    <script language="JavaScript">
     <!--

        forward ("/hierarchView/hierarchy.jsp");

     //-->
    </script>
<%
   } // if
%>


<center>


 <html:form action="/visualize" target="_self" focus="AC">

    <table width =" 50%" cellspacing =" 0" cellpadding =" 4" border="1" bordercolor ="#999999"   bgcolor ="#cee3f7" >
      <tr bgcolor="#a5bace">
	<td width="100%" align ="center" valign =" top">
	  <!-- Insert your title here -->

	    <font class="tableTitle">
	      Highlighting form
	    </font>

	  <!-- End of title section -->
	</td>
      </tr>

      <tr align="left">
	<td valign =" top">
	    <!-- Insert your table body here -->

		<table border="0" width="100%">
		  <tr>
		    <td align="right">
		      <bean:message key="hierarchView.view.visualizeForm.AC.prompt"/>
		    </td>
		    <td align="left">
		      <html:text property="AC" size="10" maxlength="10" value="<%= fieldAC %>"/>
		    </td>
		  </tr>

		  <tr>
		    <td align="right" valign="top">
		      <bean:message key="hierarchView.view.visualizeForm.depth.prompt"/>
		    </td>
		    <td align="left">
		      <html:text property="depth" size="6" maxlength="5" value="<%= fieldDepth %>"/>
		      <br>
		      <html:checkbox property="hasNoDepthLimit"/>
		      <bean:message key="hierarchView.view.visualizeForm.noDepthLimit.prompt"/>
		    </td>
		  </tr>

		  <tr>
		    <td align="right">
		      <bean:message key="hierarchView.view.visualizeForm.method.prompt"/>
		    </td>
		    <td align="left">

		    <%
			/**
			 * get a collection of highlightment sources.
			 */
			ArrayList sources = OptionGenerator.getHighlightmentSources ();
			pageContext.setAttribute("sources", sources, PageContext.PAGE_SCOPE);

		    %>

		    <html:select property="method" size="1" value="<%= fieldMethod %>">
		      <html:options collection="sources" property="value" labelProperty="label"/>
		    </html:select>


		    </td>
		  </tr>

		  <tr>
		    <td align="right">
		      <html:submit>
			 <bean:message key="hierarchView.view.visualizeForm.submit.label"/>
		      </html:submit>
		    </td>
		    <td align="left">
		      <html:reset>
			 <bean:message key="hierarchView.view.visualizeForm.reset.label"/>
		      </html:reset>
		    </td>
		  </tr>
		</table>

	    <!-- End of table body-->
	</td>
      </tr>
    </table>



 </html:form>
</center>
<hr>


<%

// Write a link to display the GO term list for the selected AC number
if (AC != null)
{
%>
<a href="/hierarchView/hierarchy.jsp" target="frameHierarchy"> Display the source element list for the current AC number </a> <br>
<%

}

   /**
   * Apply an highlighting if AC != null, keys != null and behaviour != null
   */
   if ((null != AC) && (null != keys) && (behaviour != null) && (null != in)) {
       HighlightProteins.perform (methodClass, behaviour, session, in) ;
   }
%>

    <!-- Displays the interaction network if the picture has been generated
         and stored in the session.
      -->
    <hierarchView:displayInteractionNetwork/>


<%
    /**
     *  Display only that form if an AC is in the session and the user have already requested an highlighting
     */
    if (null != AC && keys != null && null != in) {

%>

    <hr>
    <center>
     <html:form action="/highlightment">

    <table width ="50%" cellspacing ="0" cellpadding ="4" border="1" bordercolor ="#999999"   bgcolor ="#cee3f7" >
      <tr bgcolor="#a5bace">
	<td width="100%" align ="center" valign ="top">
	  <!-- Insert your title here -->

	    <font class="tableTitle">
	      Highlighting's behaviour and options
	    </font>

	  <!-- End of title section -->
	</td>
      </tr>

      <tr align="left">
	<td valign =" top">
	    <!-- Insert your table body here -->

	      <table border="0" width="100%">
		<tr>
		  <!-- Available behaviour for the selected method -->
		  <td align="right">
		      <bean:message key="hierarchView.view.highlightmentForm.behaviour.prompt"/>
		  </td>
		  <td align="left">

		    <%
			/**
			 * get a collection of authorized behaviour for the selected highlightment.
			 */
			ArrayList sources = OptionGenerator.getAuthorizedBehaviour (methodLabel);
			pageContext.setAttribute("behaviours", sources, PageContext.PAGE_SCOPE);
		    %>

		    <html:select property="behaviour" size="1" value="<%= behaviour %>">
		       <html:options collection="behaviours" property="value" labelProperty="label"/>
		    </html:select>

		  </td>
		</tr>


		<tr>
		  <!-- Highlightment option available for the selected method -->
		  <td align="right">

		     <bean:message key="hierarchView.view.highlightmentForm.options.prompt"/>

		  </td>
		  <td align="left">
		  <br>

		  <%

              // Search the list of protein to highlight
               HighlightmentSource highlightmentSource = HighlightmentSource.getHighlightmentSource(methodClass);
               String htmlCode = null;
               if (null != highlightmentSource) {
                  htmlCode = highlightmentSource.getHtmlCodeOption(session);
               }
               out.println(htmlCode);

		  %>

		  </td>
		</tr>

		<tr>
		  <td align="right">
		    <html:submit>
		       <bean:message key="hierarchView.view.highlightmentForm.submit.label"/>
		    </html:submit>
		  </td>
		  <td align="left">
		    <html:reset>
		       <bean:message key="hierarchView.view.highlightmentForm.reset.label"/>
		    </html:reset>
		  </td>
		</tr>

	      </table>

	    <!-- End of table body-->
	</td>
      </tr>
    </table>

     </html:form>
    </center>

<%
   } // end - displaying of the highlightment form
%>


</body>
</html:html>








