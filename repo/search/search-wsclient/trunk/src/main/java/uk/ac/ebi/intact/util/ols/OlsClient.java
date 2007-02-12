/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.ols;

import uk.ac.ebi.ontology_lookup.ontologyquery.Query;
import uk.ac.ebi.ontology_lookup.ontologyquery.QueryService;

import javax.xml.namespace.QName;
import java.net.URL;

/**
 * Client for the Search Web Service
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08-Aug-2006</pre>
 */
public class OlsClient
{

    private static final String DEFAULT_URL = "http://www.ebi.ac.uk/ontology-lookup/services/OntologyQuery?wsdl";

    /**
     * Stub to handle the search web service
     */
    private Query ontologyQuery;

    /**
     * Prepare the web service.
     */
    public OlsClient()
    {

        try
        {
            QueryService olsService = new QueryService(new URL(DEFAULT_URL), new QName("http://www.ebi.ac.uk/ontology-lookup/OntologyQuery", "QueryService"));
            this.ontologyQuery = olsService.getOntologyQuery();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    } // constructor

    public OlsClient(String searchWsUrl)
    {

        try
        {
            QueryService olsService = new QueryService(new URL(searchWsUrl), new QName("http://www.ebi.ac.uk/ontology-lookup/OntologyQuery", "QueryService"));
            this.ontologyQuery = olsService.getOntologyQuery();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    } // constructor


    public Query getOntologyQuery()
    {
        return ontologyQuery;
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("Version: "+new OlsClient().getOntologyQuery().getVersion());
        System.out.println("Test2: "+new OlsClient().getOntologyQuery().getOntologyNames().getItem().size());
    }

}
