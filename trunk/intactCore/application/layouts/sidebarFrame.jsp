<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%--
    Content of the sidebar frame, it rely on the Tiles configuration.

    Author: Samuel Kerrien (skerrien@ebi.ac.uk)
    Version: $Id$
--%>

<html:html>

<head>
    <html:base target="_top"/>
    <link rel="stylesheet" type="text/css" href="styles/intact.css"/>
</head>

<body bgcolor="#boc4de" topmargin="0" leftmargin="0">

   <tiles:insert definition="intact.sidebar.layout" ignore="true"/>

</body>
</html:html>
