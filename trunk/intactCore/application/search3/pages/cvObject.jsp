<!--
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
-->

<!-- Page to display a single Object view.This page view will display single CvObjects and BioSource Objects.

    @author Michael Kleen
    @version $Id$
-->

<%@ page language="java" %>

<%-- Intact classes needed --%>
<%@ page import="uk.ac.ebi.intact.application.search3.struts.framework.util.SearchConstants,
                 java.util.Iterator,
                 uk.ac.ebi.intact.model.Annotation,
                 uk.ac.ebi.intact.model.Xref,
                 uk.ac.ebi.intact.application.search3.struts.view.beans.CvObjectViewBean,
                 uk.ac.ebi.intact.application.search3.struts.view.beans.SingleViewBean,
                 uk.ac.ebi.intact.application.search3.struts.view.beans.XrefViewBean,
                 java.util.Collection,
                 uk.ac.ebi.intact.application.search3.struts.view.beans.AnnotationViewBean"
    %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<%
    CvObjectViewBean bean = (CvObjectViewBean) session.getAttribute(SearchConstants.VIEW_BEAN);
%>

<h3>Search Results for
    <%=session.getAttribute(SearchConstants.SEARCH_CRITERIA) %>
</h3>

<br>
<!-- show the searched object ac -->
<span class="smalltext">(short labels of search criteria matches are
    <span style="color: rgb(255, 0, 0);">highlighted</span>
</span><span class="smalltext">)<br></span></p>

<form name="viewForm">
   <!-- create a table with the  Cvobject deatils -->
   <table style="width: 100%; background-color: rgb(51, 102, 102);" cellpadding="2">
        <tbody>

            <!-- header row -->
             <tr bgcolor="white">

                <!-- main label --->
                <td class="headerdark">
                    <nobr>
                    <!-- set Help Link -->
                    <span class = "whiteheadertext">
                      <%=bean.getIntactType() %>
                    </span>
                    <a href="<%= bean.getHelpLink() + "search.TableLayout"%>"
                    target="new"><sup><b><font color="white">?</font></b></sup></a></nobr>
                </td>

                  <!-- ac -->
                <td class="headerdarkmid">
                 <nobr>   <a href="<%= bean.getHelpLink() + "BasicObject.ac"%>"
                        target="new" class="tdlink">Ac:</a> <%= bean.getObjAc() %> &nbsp; </nobr> 
                </td>

                 <!-- shortlabel -->
                <td class="headerdarkmid">
                    <a href="<%= bean.getHelpLink() + "AnnotatedObject.shortLabel"%>"
                        class="tdlink"
                        target="new"><span style="color: rgb(102, 102, 204);">IntAct </span>name:</a>
                    <a href="<%= bean.getObjSearchURL() %>" class="tdlink" style="font-weight: bold;">
                    <b><span style="color: rgb(255, 0, 0);"><%= bean.getObjIntactName() %></span></b></a>
                </td>

<%  // get all Xrefs
    // find out which size the table have to be
    Collection someXrefBeans =  bean.getXrefs();
    Collection someAnnotations = bean.getFilteredAnnotations();
    // put in a extra field in the table
    if(someXrefBeans.size() != 0 && someAnnotations.size() != 0)  {  %>
                    <td class="headerdarkmid" colspan="2">
                         &nbsp;
                     </td>
   <% } %>
                </tr>
                 <tr bgcolor="white">
                    <!-- name label + help link   -->
                    <td class="headerdarkmid">
                            <a href="<%=bean.getHelpLink() + "AnnotatedObject.shortLabel" %>"class="tdlink">
                               Name
                            </a>
                   </td>
                    <!-- name of the xref -->
                    <td colspan="5"  class="data" >
                        <%= bean.getFullname() %>
                    </td>
                 </tr>


<!-- list all the anotations -->
<%
    // first get all annotations from the bean
    if(!someAnnotations.isEmpty())  {
    //get the first one and process it on its own - it seems we need it to put a search
    //link for it into the first cell of the row, then process the others as per
    //'usual'
    AnnotationViewBean firstAnnotation = (AnnotationViewBean) someAnnotations.iterator().next();
%>
     <tr bgcolor="white">

         <td class="headerdarkmid" rowspan="<%= someAnnotations.size() %>">
                    <a href="<%= bean.getHelpLink() + "ANNOT_HELP_SECTION" %>" class="tdlink">
                    Annotation<br>
                    </a>
                </td>

<%
        for(Iterator it = someAnnotations.iterator(); it.hasNext();) {
             AnnotationViewBean anAnnotation = (AnnotationViewBean) it.next();
            if(!anAnnotation.equals(firstAnnotation)) {
            //we need to have new rows for each Annotations OTHER THAN the first..
%>
               <tr bgcolor="white">
         <% } %>
                <td td class="data">
                    <a href="<%=bean.getSearchUrl(anAnnotation.getObject().getCvTopic()) %>" >
                    <%=anAnnotation.getName()%>
                 </td>
                      <!-- todo -->
                      <td td class="data" colspan="4" >
                         <%=anAnnotation.getText()%>
                     </td>
               </tr>
        <% } %>
  <% } %>

<!-- list all xref -->
<%
    if(!someXrefBeans.isEmpty())  {
        //get the first one and process it on its own - it seems we need it to put a search
        //link for it into the first cell of the row, then process the others as per
        //'usual'
        XrefViewBean firstXref = (XrefViewBean) someXrefBeans.iterator().next();
%>

     <tr bgcolor="white">

                       <td class="headerdarkmid" rowspan="<%= someXrefBeans.size() %>">
                            <a href="<%= bean.getHelpLink() + "XREF_HELP_SECTION" %>" class="tdlink">
                            Xref
                            </a>
                        </td> 
    <%  // now go on with all the other xrefs
        for(Iterator it1 = someXrefBeans.iterator(); it1.hasNext();) {
            XrefViewBean aXref = (XrefViewBean) it1.next();
%>

<%
       if(!aXref.equals(firstXref)) {
            //we need to have new rows for each Xref OTHER THAN the first..
%>
            <tr bgcolor="white">
<%
        }
%>
                  <!-- name of the xref -->
                  <td class="data">
                           <a href="<%= bean.getSearchUrl(aXref.getObject().getCvDatabase()) %>">
                           <%=aXref.getName()%>
                           </a>
                   </td>

                  <!-- primary id -->
                  <td class="data">
                        <% if(!aXref.getSearchUrl().equalsIgnoreCase("-")) { %>
                                <a href="<%=aXref.getSearchUrl() %>">
                           <% } %>
                            <%=aXref.getPrimaryId()%>
                     </td>

            <!-- ignore the secondary id if it we got no secondary id --> 
            <% if(!aXref.getSecondaryId().equalsIgnoreCase("-")) { %>


                     <td class="data">
                          <%=aXref.getSecondaryId()%>
                     </td>
            <%  }    %>

                      <!-- type -->
                     <td class="data">
                        <a href="<%=bean.getHelpLink() + "Xref.cvrefType" %>" target="new"/>
                        <%=aXref.getType()%>
                        </a>
                        &nbsp;
                        <!-- if we got no XrefQualifierName it's not "linkable" -->
                        <% if(!aXref.getXrefQualifierName().equalsIgnoreCase("-")) { %>
                                <a href="<%=bean.getSearchUrl(aXref.getObject().getCvXrefQualifier())%>">
                         <% } %>
                        <%=aXref.getXrefQualifierName() %>
                        </a>
                     </td>
               </tr>
    <% }  %>

  <%   } %>

        </tbody>
    </table>
</form>