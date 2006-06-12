/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.imex.psivalidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.net.URL;

/**
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>12-Jun-2006</pre>
 */
public class PsiReportFactory
{

    private static final Log log = LogFactory.getLog(PsiReportFactory.class);

    public static PsiReport createPsiReport(String name, URL url) throws IOException
    {
        PsiReport report = new PsiReport(name);
        boolean xmlValid = validateXmlSyntax(report, url.openStream());

        if (xmlValid)
        {
            createHtmlView(report, url.openStream());
        }

        validateXmlSemantics(report, url.openStream());

        return report;
    }

    public static PsiReport createPsiReport(String name, InputStream is) throws IOException
    {
        PsiReport report = new PsiReport(name);
        boolean xmlValid = validateXmlSyntax(report, is);

        if (xmlValid)
        {
            String transformedOutput = null;
            try
            {
                is.reset();
                transformedOutput = transformToHtml(is);
            }
            catch (TransformerException e)
            {
                e.printStackTrace();
            }
            report.setXmlSyntaxReport(transformedOutput);
        }

        is.reset();
        validateXmlSemantics(report, is);

        return report;
    }

    private static boolean validateXmlSyntax(PsiReport report, InputStream is) throws IOException
    {
        //InputStream xsd = PsiReportFactory.class.getResourceAsStream("/uk/ac/ebi/imex/psivalidator/resource/MIF25.xsd");

        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        try
        {
            // parse an XML document into a DOM tree
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(is);

            // create a SchemaFactory capable of understanding WXS schemas
            //SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            //Source schemaFile = new StreamSource(xsd);
            //Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance, which can be used to validate an instance document
            //Validator validator = schema.newValidator();

            // validate the DOM tree
            //validator.validate(new DOMSource(document));
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace(writer);
        }
        catch (SAXException e)
        {
            e.printStackTrace(writer);
        }
        catch (IOException e)
        {
            e.printStackTrace(writer);
        }

        String output = sw.getBuffer().toString();

        if (log.isDebugEnabled())
            log.debug("XML Validation output: "+output);

        if (output.equals(""))
        {
            report.setXmlSyntaxStatus("valid");
            report.setXmlSyntaxReport("Valid document");

            return true;
        }

        report.setXmlSyntaxStatus("invalid");
        report.setXmlSyntaxReport(output);

        return false;
    }

    private static String transformToHtml(InputStream is) throws TransformerException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InputStream xslt = PsiReportFactory.class.getResourceAsStream("/uk/ac/ebi/imex/psivalidator/resource/MIF25_view.xsl");

        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(is);
        Source xsltSource = new StreamSource(xslt);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        trans.transform(xmlSource, new StreamResult(outputStream));

        return outputStream.toString();
    }

    private static void createHtmlView(PsiReport report, InputStream is)
    {
        String transformedOutput = null;
        try
        {
            transformedOutput = transformToHtml(is);
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        report.setHtmlView(transformedOutput);
    }

    private static boolean validateXmlSemantics(PsiReport report, InputStream is)
    {
        InputStream xsd = PsiReportFactory.class.getResourceAsStream("/uk/ac/ebi/imex/psivalidator/resource/MIF252.xsd");

        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        try
        {
            // parse an XML document into a DOM tree
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(is);

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(xsd);
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance, which can be used to validate an instance document
            Validator validator = schema.newValidator();

            // validate the DOM tree
            validator.validate(new DOMSource(document));
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace(writer);
        }
        catch (SAXException e)
        {
            e.printStackTrace(writer);
        }
        catch (IOException e)
        {
            e.printStackTrace(writer);
        }

        String output = sw.getBuffer().toString();

        if (log.isDebugEnabled())
            log.debug("XML Validation output: "+output);

        if (output.equals(""))
        {
            report.setSemanticsStatus("valid");
            report.setSemanticsReport("Document valid");
        }

        report.setSemanticsStatus("invalid");
        report.setSemanticsReport(output);

        return false;
    }

}
