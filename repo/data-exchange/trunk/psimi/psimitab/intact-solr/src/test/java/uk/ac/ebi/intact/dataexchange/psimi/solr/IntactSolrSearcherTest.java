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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;
import uk.ac.ebi.intact.dataexchange.psimi.solr.server.SolrJettyRunner;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrSearcherTest {

    private SolrJettyRunner solrJettyRunner;

    @Before
    public void before() throws Exception {
        solrJettyRunner = new SolrJettyRunner();
        
        solrJettyRunner.start();

    }

    @After
    public void after() throws Exception {
        //solrJettyRunner.join();
        solrJettyRunner.stop();
        solrJettyRunner = null;
    }

    @Test
    public void search() throws Exception  {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        assertCount(200L, "*:*");
        assertCount(100L, "\"CHEBI:39112\"");
        assertCount(183L, "experimentalRole:bait");
        assertCount(197L, "experimentalRole:prey");
        assertCount(1L, "-biologicalRole:\"unspecified role\"");
        assertCount(1L, "properties:ENSG00000169047");
        assertCount(183L, "experimentalRoleA:bait");
        assertCount(200L, "pubid:17721511");
    }

    @Test
    public void search_interactors1() throws Exception  {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        //MI:0326(protein)	MI:0328(small molecule)
        SolrQuery query = new SolrQuery("*:*")
                .setRows(0)
                .setFacet(true)
                .setFacetMinCount(1)
                .setFacetLimit(Integer.MAX_VALUE)
                .addFacetField("intact_byInteractorType_mi0326")
                .addFacetField("intact_byInteractorType_mi0328");

        QueryResponse response = solrJettyRunner.getSolrServer(CoreNames.CORE_PUB).query(query);

        FacetField ffProt = response.getFacetField("intact_byInteractorType_mi0326");
        Assert.assertEquals(129, ffProt.getValueCount());
        
        FacetField ffSm = response.getFacetField("intact_byInteractorType_mi0328");
        Assert.assertEquals(5, ffSm.getValueCount());
    }

    @Test
    public void search_interactors2() throws Exception {
        assertCount(0L, "*:*");

        indexFromClasspath("/mitab_samples/intact200.txt", true);

        SolrQuery query = new SolrQuery("*:*");

        IntactSolrSearcher searcher = new IntactSolrSearcher(solrJettyRunner.getSolrServer(CoreNames.CORE_PUB));

        Assert.assertEquals(129, searcher.searchInteractors(query, "MI:0326").size());
        Assert.assertEquals(5, searcher.searchInteractors(query, "MI:0328").size());

        
        final SolrSearchResult result1 = searcher.search( "GRB2", null, null );
        assertEquals(3, result1.getTotalCount());

        SolrQuery query2 = new SolrQuery("GRB2*");
        final SolrSearchResult result2 = searcher.search( query2 );
        assertEquals(3, result2.getTotalCount());

    }

    @Test
    public void search_integrityTest() throws Exception {
        String mitab = "uniprotkb:P35568|intact:EBI-517592\tuniprotkb:Q08345-2|intact:EBI-711903\tuniprotkb:IRS1(gene name)" +
                "\t-\tintact:irs1_human(shortLabel)\tuniprotkb:CAK II(isoform synonym)|uniprotkb:Short(isoform synonym)|intact:Q08345-2(shortLabel)" +
                "\tMI:0424(protein kinase assay)\tBantscheff et al. (2007)" +
                "\tpubmed:17721511\ttaxid:9606(human)\ttaxid:9606(human)\tMI:0217(phosphorylation)" +
                "\tMI:0469(intact)\tintact:EBI-1381086\t-\t" +
                "MI:0499(unspecified role)\tMI:0499(unspecified role)" +
                "\tMI:0502(enzyme target)\tMI:0501(enzyme)" +
                "\tgo:\"GO:0030188\"|go:\"GO:0005159\"|" +
                "go:\"GO:0005158\"|interpro:IPR002404|" +
                "interpro:IPR001849|interpro:IPR011993|ensembl:ENSG00000169047|rcsb pdb:1IRS|rcsb pdb:1K3A|rcsb pdb:1QQG" +
                "\tintact:EBI-711879\tMI:0326(protein)\tMI:0326(protein)\ttaxid:-1(in vitro)\t-\t-\t-";

        IntactSolrIndexer indexer = new IntactSolrIndexer(solrJettyRunner.getSolrServer(CoreNames.CORE_PUB), solrJettyRunner.getSolrServer(CoreNames.CORE_ONTOLOGY_PUB));
        indexer.indexOntologies(new OntologyMapping[] {
                new OntologyMapping("go", IntactSolrIndexerTest.class.getResource("/META-INF/goslim_generic.obo"))});
        indexer.indexMitab(new ByteArrayInputStream(mitab.getBytes()), false);

        SolrQuery solrQuery = new SolrQuery("P35568");

        IntactSolrSearcher searcher = new IntactSolrSearcher(solrJettyRunner.getSolrServer(CoreNames.CORE_PUB));

        SolrSearchResult result = searcher.search(solrQuery);

        IntactBinaryInteraction binaryInteraction = result.getBinaryInteractionList().iterator().next();
        
        Assert.assertEquals("P35568", get(0, binaryInteraction.getInteractorA().getIdentifiers()).getIdentifier());
        Assert.assertEquals("uniprotkb", get(0, binaryInteraction.getInteractorA().getIdentifiers()).getDatabase());
        Assert.assertEquals("EBI-517592", get(1, binaryInteraction.getInteractorA().getIdentifiers()).getIdentifier());
        Assert.assertEquals("intact", get(1, binaryInteraction.getInteractorA().getIdentifiers()).getDatabase());
        Assert.assertEquals("Q08345-2", get(0, binaryInteraction.getInteractorB().getIdentifiers()).getIdentifier());
        Assert.assertEquals("uniprotkb", get(0, binaryInteraction.getInteractorB().getIdentifiers()).getDatabase());
        Assert.assertEquals("EBI-711903", get(1, binaryInteraction.getInteractorB().getIdentifiers()).getIdentifier());
        Assert.assertEquals("intact", get(1, binaryInteraction.getInteractorB().getIdentifiers()).getDatabase());
        Assert.assertEquals("IRS1", get(0, binaryInteraction.getInteractorA().getAlternativeIdentifiers()).getIdentifier());
        Assert.assertEquals("uniprotkb", get(0, binaryInteraction.getInteractorA().getAlternativeIdentifiers()).getDatabase());
        Assert.assertEquals("gene name", get(0, binaryInteraction.getInteractorA().getAlternativeIdentifiers()).getText());
        Assert.assertEquals(0, binaryInteraction.getInteractorB().getAlternativeIdentifiers().size());
        Assert.assertEquals("irs1_human", get(0, binaryInteraction.getInteractorA().getAliases()).getName());
        Assert.assertEquals("intact", get(0, binaryInteraction.getInteractorA().getAliases()).getDbSource());
        Assert.assertEquals("shortLabel", get(0, binaryInteraction.getInteractorA().getAliases()).getAliasType());
        Assert.assertEquals("CAK II", get(0, binaryInteraction.getInteractorB().getAliases()).getName());
        Assert.assertEquals("uniprotkb", get(0, binaryInteraction.getInteractorB().getAliases()).getDbSource());
        Assert.assertEquals("isoform synonym", get(0, binaryInteraction.getInteractorB().getAliases()).getAliasType());
        Assert.assertEquals("MI:0424", get(0, binaryInteraction.getDetectionMethods()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getDetectionMethods()).getDatabase());
        Assert.assertEquals("protein kinase assay", get(0, binaryInteraction.getDetectionMethods()).getText());
        Assert.assertEquals("Bantscheff et al. (2007)", get(0, binaryInteraction.getAuthors()).getName());
        Assert.assertEquals("17721511", get(0, binaryInteraction.getPublications()).getIdentifier());
        Assert.assertEquals("pubmed", get(0, binaryInteraction.getPublications()).getDatabase());
        Assert.assertEquals("9606", get(0, binaryInteraction.getInteractorA().getOrganism().getIdentifiers()).getIdentifier());
        Assert.assertEquals("taxid", get(0, binaryInteraction.getInteractorA().getOrganism().getIdentifiers()).getDatabase());
        Assert.assertEquals("human", get(0, binaryInteraction.getInteractorA().getOrganism().getIdentifiers()).getText());
        Assert.assertEquals("9606", get(0, binaryInteraction.getInteractorB().getOrganism().getIdentifiers()).getIdentifier());
        Assert.assertEquals("taxid", get(0, binaryInteraction.getInteractorB().getOrganism().getIdentifiers()).getDatabase());
        Assert.assertEquals("human", get(0, binaryInteraction.getInteractorB().getOrganism().getIdentifiers()).getText());
        Assert.assertEquals("MI:0217", get(0, binaryInteraction.getInteractionTypes()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getInteractionTypes()).getDatabase());
        Assert.assertEquals("phosphorylation", get(0, binaryInteraction.getInteractionTypes()).getText());
        Assert.assertEquals("MI:0469", get(0, binaryInteraction.getSourceDatabases()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getSourceDatabases()).getDatabase());
        Assert.assertEquals("intact", get(0, binaryInteraction.getSourceDatabases()).getText());
        Assert.assertEquals("EBI-1381086", get(0, binaryInteraction.getInteractionAcs()).getIdentifier());
        Assert.assertEquals("intact", get(0, binaryInteraction.getInteractionAcs()).getDatabase());
        Assert.assertEquals(0, binaryInteraction.getConfidenceValues().size());
        Assert.assertEquals(1, binaryInteraction.getInteractorA().getExperimentalRoles().size());
        Assert.assertEquals("MI:0499", get(0, binaryInteraction.getInteractorA().getExperimentalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getInteractorA().getExperimentalRoles()).getDatabase());
        Assert.assertEquals("unspecified role", get(0, binaryInteraction.getInteractorA().getExperimentalRoles()).getText());
        Assert.assertEquals(1, binaryInteraction.getInteractorB().getExperimentalRoles().size());
        Assert.assertEquals("MI:0499", get(0, binaryInteraction.getInteractorB().getExperimentalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getInteractorB().getExperimentalRoles()).getDatabase());
        Assert.assertEquals("unspecified role", get(0, binaryInteraction.getInteractorB().getExperimentalRoles()).getText());
        Assert.assertEquals(1, binaryInteraction.getInteractorA().getBiologicalRoles().size());
        Assert.assertEquals("MI:0502", get(0, binaryInteraction.getInteractorA().getBiologicalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getInteractorA().getBiologicalRoles()).getDatabase());
        Assert.assertEquals("enzyme target", get(0, binaryInteraction.getInteractorA().getBiologicalRoles()).getText());
        Assert.assertEquals(1, binaryInteraction.getInteractorB().getBiologicalRoles().size());
        Assert.assertEquals("MI:0501", get(0, binaryInteraction.getInteractorB().getBiologicalRoles()).getIdentifier());
        Assert.assertEquals("psi-mi", get(0, binaryInteraction.getInteractorB().getBiologicalRoles()).getDatabase());
        Assert.assertEquals("enzyme", get(0, binaryInteraction.getInteractorB().getBiologicalRoles()).getText());
        Assert.assertEquals("GO:0030188", get(0, binaryInteraction.getInteractorA().getProperties()).getIdentifier());
        Assert.assertEquals("go", get(0, binaryInteraction.getInteractorA().getProperties()).getDatabase());
        Assert.assertEquals("chaperone regulator activity", get(0, binaryInteraction.getInteractorA().getProperties()).getText());
        Assert.assertEquals(10, binaryInteraction.getInteractorA().getProperties().size());
        Assert.assertEquals("EBI-711879", get(0, binaryInteraction.getInteractorB().getProperties()).getIdentifier());
        Assert.assertEquals("intact", get(0, binaryInteraction.getInteractorB().getProperties()).getDatabase());
        Assert.assertEquals(1, binaryInteraction.getInteractorB().getProperties().size());
        Assert.assertEquals("MI:0326", binaryInteraction.getInteractorA().getInteractorType().getIdentifier());
        Assert.assertEquals("psi-mi", binaryInteraction.getInteractorA().getInteractorType().getDatabase());
        Assert.assertEquals("protein", binaryInteraction.getInteractorA().getInteractorType().getText());
        Assert.assertEquals("MI:0326", binaryInteraction.getInteractorB().getInteractorType().getIdentifier());
        Assert.assertEquals("psi-mi", binaryInteraction.getInteractorB().getInteractorType().getDatabase());
        Assert.assertEquals("protein", binaryInteraction.getInteractorB().getInteractorType().getText());
        Assert.assertEquals("-1", get(0, binaryInteraction.getHostOrganism()).getIdentifier());
        Assert.assertEquals("taxid", get(0, binaryInteraction.getHostOrganism()).getDatabase());
        Assert.assertEquals("in vitro", get(0, binaryInteraction.getHostOrganism()).getText());
        Assert.assertEquals(0, binaryInteraction.getInteractorA().getAnnotations().size());
        Assert.assertEquals(0, binaryInteraction.getInteractorB().getAnnotations().size());
        Assert.assertEquals(0, binaryInteraction.getInteractorA().getParameters().size());
        Assert.assertEquals(0, binaryInteraction.getInteractorB().getParameters().size());
        Assert.assertEquals(0, binaryInteraction.getParameters().size());  
    }

    private <T> T get(int position, Collection<T> elements) {
        return new ArrayList<T>(elements).get(position);
    }

    private void assertCount(Number count, String searchQuery) throws IntactSolrException {
        IntactSolrSearcher searcher = new IntactSolrSearcher(solrJettyRunner.getSolrServer(CoreNames.CORE_PUB));
        SolrSearchResult result = searcher.search(searchQuery, null, null);

        assertEquals(count, result.getTotalCount());
    }

    private void indexFromClasspath(String resource, boolean hasHeader) throws IOException, IntactSolrException {
        IntactSolrIndexer indexer = new IntactSolrIndexer(solrJettyRunner.getSolrServer(CoreNames.CORE_PUB));
        indexer.indexMitabFromClasspath(resource, hasHeader);
    }

    @Test
    public void compare_facetted_nonfacetted_interactors_search() throws Exception {
        assertCount( 0L, "*:*" );

        indexFromClasspath( "/mitab_samples/p20053.txt", true );

        SolrQuery query = new SolrQuery( "P20053" );

        IntactSolrSearcher searcher = new IntactSolrSearcher( solrJettyRunner.getSolrServer( CoreNames.CORE_PUB ) );

        Assert.assertEquals( 34, searcher.searchInteractors( query, "MI:0326" ).size() );

        SolrQuery queryFacetted = new SolrQuery( "P20053" )
                .setRows( 0 )
                .setFacet( true )
                .setFacetMinCount( 1 )
                .setFacetLimit( Integer.MAX_VALUE )
                .addFacetField( "intact_byInteractorType_mi0326" )
                .addFacetField( "intact_byInteractorType_mi0328" );

        QueryResponse response = solrJettyRunner.getSolrServer( CoreNames.CORE_PUB ).query( queryFacetted );

        boolean ebi340 = false;
        boolean ebi421 = false;
        FacetField ffProt = response.getFacetField( "intact_byInteractorType_mi0326" );
        if ( ffProt != null && ffProt.getValues() != null ) {
            for ( FacetField.Count c : ffProt.getValues() ) {
                if ( c.getName().equals( "EBI-340" ) && c.getCount() == 8 ) {
                    ebi340 = true;
                }
                if ( c.getName().equals( "EBI-421" ) && c.getCount() == 5 ) {
                    ebi421 = true;
                }
            }
        }
        Assert.assertTrue( ebi340 );
        Assert.assertTrue( ebi421 );

        SolrQuery queryNonfacetEbi340 = new SolrQuery( "P20053 +EBI-340" );
        final SolrSearchResult nonFacetResult1 = searcher.search( queryNonfacetEbi340 );
        Assert.assertEquals( 8, nonFacetResult1.getTotalCount() );

        SolrQuery queryNonfacetEbi421 = new SolrQuery( "P20053 +EBI-421" );
        final SolrSearchResult nonFacetResult2 = searcher.search( queryNonfacetEbi421 );
        Assert.assertEquals( 5, nonFacetResult2.getTotalCount() );

        SolrQuery queryNonfacetEbi340_with_id = new SolrQuery( "P20053 +id:EBI-340" );
        final SolrSearchResult nonFacetResult3 = searcher.search( queryNonfacetEbi340_with_id );
        Assert.assertEquals( 8, nonFacetResult3.getTotalCount() );

        SolrQuery queryNonfacetEbi421_with_id = new SolrQuery( "P20053 +id:EBI-421" );
        final SolrSearchResult nonFacetResult4 = searcher.search( queryNonfacetEbi421_with_id );
        Assert.assertEquals( 5, nonFacetResult4.getTotalCount() );
    }
}
