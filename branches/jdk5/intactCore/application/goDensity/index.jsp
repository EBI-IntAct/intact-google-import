<%@ page language="java" %>

<!--
   - Allows to forward to the main page of the application.
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
-->

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<html:html>

<head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="expires" content="-1">
</head>

<body bgcolor="white">

    <jsp:forward page="/init.do"/>

</body>

</html:html>