<!--
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  - Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
  - All rights reserved. Please see the file LICENSE in the root directory of
  - this distribution.
  -->

<%--
  - The look & feel layout for editing a CV object.
  - Author: Sugath Mudali (smudali@ebi.ac.uk)
  - Version: $Id$
  --%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%-- The error page at the top --%>
<tiles:insert attribute="error"/>

<%-- The common information for all the CV objects. --%>
<tiles:insert attribute="cv-info"/>

<%-- The plugin for editor specific info. --%>
<tiles:insert attribute="editor-info" ignore="true"/>

<%-- The annotations. --%>
<tiles:insert attribute="comments"/>
<tiles:insert attribute="add-comments"/>

<%-- The cross references. --%>
<tiles:insert attribute="xrefs"/>
<tiles:insert attribute="add-xrefs"/>

<%-- The action button at the bottom. --%>
<tiles:insert attribute="action"/>
