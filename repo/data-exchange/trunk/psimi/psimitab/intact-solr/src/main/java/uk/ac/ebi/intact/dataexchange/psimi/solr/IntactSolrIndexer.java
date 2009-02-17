/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.psimi.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.SolrDocumentConverter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologyIndexer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;

import java.io.*;
import java.net.MalformedURLException;

/**
 * Indexes information into a SOLR server
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrIndexer {

    private Logger log = LoggerFactory.getLogger(IntactSolrIndexer.class);

    private SolrServer solrServer;
    private SolrServer ontologySolrServer;
    private SolrDocumentConverter converter;

    public IntactSolrIndexer(String solrServerUrl) throws MalformedURLException {
        this(new CommonsHttpSolrServer(solrServerUrl));
    }

    public IntactSolrIndexer(SolrServer solrServer) {
        this.solrServer = solrServer;
        this.converter = new SolrDocumentConverter(new IntactDocumentDefinition());
    }

    public IntactSolrIndexer(String solrServerUrl, String ontologySolrServerUrl) throws MalformedURLException {
        this(new CommonsHttpSolrServer(solrServerUrl), new CommonsHttpSolrServer(ontologySolrServerUrl));
    }

    public IntactSolrIndexer(SolrServer solrServer, SolrServer ontologySolrServer) {
        this.solrServer = solrServer;
        this.ontologySolrServer = ontologySolrServer;
        this.converter = new SolrDocumentConverter(new IntactDocumentDefinition(), new OntologySearcher(ontologySolrServer));
    }

    public void indexOntologies(OntologyMapping[] ontologyMappings) throws IntactSolrException {
        if (ontologySolrServer == null) {
            throw new IllegalStateException("To index an ontology, an ontology SolrServer must be passed to the constructor");
        }
        
        if (log.isInfoEnabled()) log.info("Indexing ontologies: "+ontologyMappings);
        
        OntologyIndexer ontologyIndexer = new OntologyIndexer(ontologySolrServer);
        ontologyIndexer.indexObo(ontologyMappings);
    }

    public int indexMitab(File mitabFile, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitab(new FileInputStream(mitabFile), hasHeader);
    }

    public int indexMitab(File mitabFile, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        return indexMitab(new FileInputStream(mitabFile), hasHeader, firstLine, batchSize);
    }

    public int indexMitabFromClasspath(String resourceUrl, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitabFromClasspath(resourceUrl, hasHeader, null, null);
    }

    public int indexMitabFromClasspath(String resourceUrl, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        InputStream resourceStream = IntactSolrIndexer.class.getResourceAsStream(resourceUrl);

        if (resourceStream == null) throw new IntactSolrException("Resource not found in the classpath: "+resourceUrl);

        return indexMitab(resourceStream, hasHeader, firstLine, batchSize);
    }

    /**
     * Indexes a MITAB formatted input stream into the database.
     * @param mitabStream The stream to index
     * @param hasHeader Whether the data has header or not
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading the stream
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(InputStream mitabStream, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitab(mitabStream, hasHeader, null, null);
    }

    /**
     * Indexes a MITAB formatted input stream into the database.
     * @param mitabStream The stream to index
     * @param hasHeader Whether the data has header or not
     * @param firstLine The first line to process, being line 0 the first line in the file ignoring the header
     * @param batchSize Number of lines to process
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading the stream
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(InputStream mitabStream, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(mitabStream));

        return indexMitab(reader, hasHeader, firstLine, batchSize);
    }

    /**
     * Indexes MITAB data using a Reader into the database.
     * @param reader The reader to use
     * @param hasHeader Whether the data has header or not
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(BufferedReader reader, boolean hasHeader) throws IOException, IntactSolrException {
        return indexMitab(reader, hasHeader, null, null);
    }

    /**
     * Indexes MITAB data using a Reader into the database.
     * @param reader The reader to use
     * @param hasHeader Whether the data has header or not
     * @param firstLine The first line to process, being line 0 the first line in the file ignoring the header
     * @param batchSize Number of lines to process
     * @return Count of indexed lines
     * @throws IOException Thrown if there is a problem reading
     * @throws IntactSolrException Thrown if there is a problem indexing the data
     */
    public int indexMitab(BufferedReader reader, boolean hasHeader, Integer firstLine, Integer batchSize) throws IOException, IntactSolrException {
        int lineCount = 0;

        int first = (firstLine == null)? 0 : firstLine;
        int lastRes = (batchSize == null)? Integer.MAX_VALUE : batchSize;
        int end = first + lastRes;

        if (hasHeader) {
            first++;
            if (end != Integer.MAX_VALUE) end++;
        }

        int processed = 0;

        String line;

        while ((line = reader.readLine()) != null) {

            if (lineCount >= first && lineCount < end) {

                SolrInputDocument inputDocument = converter.toSolrDocument(line);

                try {
                    solrServer.add(inputDocument);
                } catch (Throwable e) {
                    throw new IntactSolrException("Problem processing line " + (lineCount+1) + ": " + line, e);
                }

                processed++;
                
                if (lineCount > 0 && lineCount % 100 == 0) {
                    commitSolr(false);
                }

                if (log.isDebugEnabled() && processed % 1000 == 0) {
                    if (log.isDebugEnabled()) log.debug("Processed: "+processed);
                }

            }

            if (lineCount >= end) {
                break;
            }

            lineCount++;
        }

        commitSolr(true);

        return processed;
    }

    private void commitSolr(boolean optimize) throws IOException, IntactSolrException {
        try {
            solrServer.commit();
            if (optimize) solrServer.optimize();
        } catch (SolrServerException e) {
            throw new IntactSolrException("Problem during commit", e);
        }
    }
}
