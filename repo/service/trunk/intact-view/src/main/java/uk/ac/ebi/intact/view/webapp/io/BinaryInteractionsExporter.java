/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.io;

import org.apache.lucene.search.Sort;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrQuery;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.search.engine.SearchEngineException;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;
import psidev.psi.mi.xml.stylesheets.XslTransformException;
import uk.ac.ebi.intact.psimitab.search.IntactSearchEngine;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactTab2Xml;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.dataexchange.psimi.solr.SolrSearchResult;

/**
 * Exports to MITAB
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BinaryInteractionsExporter {

    private SolrServer solrServer;
    private String sortColumn;
    private SolrQuery.ORDER sortOrder;

    public BinaryInteractionsExporter(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public BinaryInteractionsExporter(SolrServer solrServer, String sortColumn, SolrQuery.ORDER sortOrder) {
        this(solrServer);
        this.sortColumn = sortColumn;
        this.sortOrder = sortOrder;
    }

    public void searchAndExport(OutputStream os, String searchQuery, String format) throws IOException {
        if ("mitab".equals(format)) {
            exportToMiTab(os, searchQuery);
        } else if ("mitab_intact".equals(format)) {
            exportToMiTabIntact(os, searchQuery);
        } else if ("xml_2_53".equals(format) || "xml_2_54".equals(format)) {
            exportToMiXml(os, searchQuery,format);
        } else if ("xml_html".equals(format)) {
            exportToMiXmlTransformed(os, searchQuery);
        } else {
            throw new IntactViewException("Format is not correct: " + format + ". Possible values: mitab, mitab_intact.");
        }
    }
    
     public void exportToMiTab(OutputStream os, String searchQuery) throws IOException {
         PsimiTabWriter writer = new PsimiTabWriter();
         Writer out = new OutputStreamWriter(os);
         writeMitab(out, writer, searchQuery);
    }

     private void exportToMiTabIntact(OutputStream os, String searchQuery) throws IOException, IntactViewException {
         PsimiTabWriter writer = new IntactPsimiTabWriter();
         Writer out = new OutputStreamWriter(os);
         writeMitab(out, writer, searchQuery);
    }

    private void writeMitab(Writer out, PsimiTabWriter writer, String searchQuery) throws IOException {
        Integer firstResult = 0;
        Integer maxResults = 50;

        boolean headerEnabled = true;

        Collection interactions;

        do {
            //SolrQuery query = new SolrQuery(searchQuery);
            SolrQuery query = convertToSolrQuery( searchQuery );
            query.setStart(firstResult);
            query.setRows(maxResults);

            if (sortColumn != null) {
                query.setSortField(sortColumn, sortOrder);
            }

            IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);
            SolrSearchResult result = searcher.search(query);

            interactions = result.getBinaryInteractionList();


            writer.setHeaderEnabled(headerEnabled);
            try {
                writer.write(interactions, out);
            } catch (ConverterException e) {
                throw new IntactViewException("Problem exporting interactions", e);
            }

            headerEnabled = false;

            firstResult = firstResult + maxResults;

            out.flush();

        } while (!interactions.isEmpty());
    }


    public void exportToMiXml( OutputStream os, String searchQuery ) throws IOException {
        exportToMiXml( os, searchQuery, "xml_2_54" );
    }

    public void exportToMiXml(OutputStream os, String searchQuery,String format) throws IOException {
        Writer out = new OutputStreamWriter(os, "UTF-8");

        IntactSolrSearcher searcher = new IntactSolrSearcher(solrServer);

        // count first as a security measure
        //SolrSearchResult result1 = searcher.search(searchQuery, 0, 0);
        SolrQuery solrQuery = convertToSolrQuery( searchQuery );
        SolrSearchResult result1 = searcher.search(solrQuery);

        System.out.println( " result1  "+ result1.getTotalCount() );

        if (result1.getTotalCount() > 1000) {
            throw new IntactViewException("Too many interactions to export to XML. Maximum is 1000");
        }

        //SolrSearchResult result = searcher.search(searchQuery, null, null);
        solrQuery.setRows(Integer.MAX_VALUE);
        SolrSearchResult result = searcher.search(solrQuery);
        Collection<IntactBinaryInteraction> interactions = result.getBinaryInteractionList();

        System.out.println( " interactions count " + interactions.size());

        Tab2Xml tab2Xml = new IntactTab2Xml();

        final EntrySet entrySet;
        try {
            entrySet = tab2Xml.convert(new ArrayList<BinaryInteraction>(interactions));
        } catch (Exception e) {
            throw new IntactViewException("Problem converting interactions from MITAB to XML", e);
        }

        PsimiXmlWriter writer = null;
        if ( "xml_2_53".equals( format ) ) {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_253 );
        } else if ( "xml_2_54".equals( format ) ) {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_254 );
        } else {
            writer = new PsimiXmlWriter( PsimiXmlVersion.VERSION_25_UNDEFINED );
        }

        try {
            writer.write(entrySet, out);
        } catch (Exception e) {
            throw new IntactViewException("Problem writing XML", e);
        }
    }

    public void exportToMiXmlTransformed(OutputStream os, String searchQuery) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        exportToMiXml(baos, searchQuery);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toString().getBytes());

        try {
            XslTransformerUtils.viewPsiMi25( bais, os );
            //transform(os, bais, BinaryInteractionsExporter.class.getResourceAsStream("/META-INF/MIF254_view.xsl"));
        }  catch ( XslTransformException e ) {
            throw new IntactViewException("Problem transforming XML to HTML(XslTransformException)", e);
        }
    }

    /**
     * The actual method that does the transformation
     * @param isToTransform The stream to transform
     * @param xslt The stream with the XSLT rules
     * @return The transformed stream
     * @throws TransformerException thrown if something has been wrong with the transformation
     */
    private static void transform(OutputStream outputStream, InputStream isToTransform, InputStream xslt) throws TransformerException
    {

        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(isToTransform);
        Source xsltSource = new StreamSource(xslt);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        trans.transform(xmlSource, new StreamResult(outputStream));
    }


    private SolrQuery convertToSolrQuery( String searchQuery ) {

        if ( searchQuery == null ) {
            throw new NullPointerException( "You must give a non null searchQuery" );
        }

        SolrQuery solrQuery = new SolrQuery();
        if ( searchQuery.contains( "&" ) ) {
            final String[] params = searchQuery.split( "&" );
            for ( String param : params ) {
                if ( param.contains( "=" ) ) {
                    final String[] keyval = param.split( "=" );
                    if ( keyval != null && keyval.length == 2 ) {
                        if ( "q".equals( keyval[0] ) ) {
                            solrQuery.setQuery( keyval[1] );
                        }
                    }
                }

            }

        }
        return solrQuery;
    }
}
