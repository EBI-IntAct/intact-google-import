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
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;

import java.io.Serializable;
import java.util.*;

/**
 * Ontology term wrapper.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyTermWrapper implements Serializable {

    private OntologyTerm term;
    private long interactionCount;

    private SolrServer interactionSolrServer;
    private SolrQuery userQuery;
    private String fieldName;
    private boolean showIfEmpty = false;

    private OntologyTermWrapper parent;

    private List<OntologyTermWrapper> children;
    private boolean isLeaf;
    private static final Log log = LogFactory.getLog(OntologyTermWrapper.class);

    public OntologyTermWrapper(OntologyTerm term, SolrServer interactionSolrServer, SolrQuery userQuery, String fieldName, boolean showIfEmpty) throws SolrServerException {
        this.term = term;
        this.interactionSolrServer = interactionSolrServer;
        this.showIfEmpty = showIfEmpty;
        this.userQuery = userQuery;
        this.fieldName = fieldName;

        interactionCount = countNumberOfResults();
    }

    public OntologyTermWrapper(OntologyTerm term, SolrServer interactionSolrServer, SolrQuery userQuery, String fieldName, boolean showIfEmpty, int interactionCount) throws SolrServerException {
        this.term = term;
        this.interactionSolrServer = interactionSolrServer;
        this.showIfEmpty = showIfEmpty;
        this.userQuery = userQuery;
        this.fieldName = fieldName;

        this.interactionCount = interactionCount;
    }

    public OntologyTerm getTerm() {
        return term;
    }

    private String createFacetQuery(String id){
        return fieldName + ":\"" + id+"\"";
    }

    private long countNumberOfResults() throws SolrServerException {

        // we copy the query, because we don't want to modify the current query instance.
        // otherwise the cache would not have the same key.
        SolrQuery queryCopy = userQuery.getCopy();
        queryCopy.setRows(0);
        queryCopy.addFilterQuery(createFacetQuery(term.getId()));

        if (log.isDebugEnabled()) log.debug("Loading ontology counts : "+queryCopy+", term id : "+term.getId());

        final QueryResponse queryResponse = interactionSolrServer.query(queryCopy);

        return queryResponse.getResults().getNumFound();
    }

    public List<OntologyTermWrapper> getChildren() throws SolrServerException {
        initChildren();
        return children;
    }

    public boolean isLeaf() throws SolrServerException {
        initChildren();
        return isLeaf;
    }

    private void initChildren() throws SolrServerException {
        if (children != null || isLeaf) {
            return;
        }

        // collect interaction count for children
        Map<String, Integer> facetQueries = collectInteractionCountForChildren();

        for (OntologyTerm child : term.getChildren()){
            Integer childCount = facetQueries.get(createFacetQuery(child.getId()));
            if (childCount == null){
                childCount = 0;
            }

            OntologyTermWrapper otwChild = new OntologyTermWrapper(child, this.interactionSolrServer, this.userQuery, this.fieldName, this.showIfEmpty, childCount);

            if (showIfEmpty || otwChild.getInteractionCount() > 0 || (term instanceof RootTerm)) {
                children.add(otwChild);
                otwChild.setParent(this);
            }
        }

        Collections.sort(children, new OntologyTermWrapperComparator());

        if (children.isEmpty()) isLeaf = true;
    }

    private Map<String, Integer> collectInteractionCountForChildren() throws SolrServerException {
        // we copy the query, because we don't want to modify the current query instance.
        // otherwise the cache would not have the same key.
        SolrQuery queryCopy = userQuery.getCopy();
        queryCopy.setRows(0);
        queryCopy.setFacet(true);

        children = new ArrayList<OntologyTermWrapper>();

        for (OntologyTerm child : term.getChildren()) {
            queryCopy.addFacetQuery(createFacetQuery(child.getId()));
        }

        if (log.isDebugEnabled()) log.debug("Loading child ontology counts : "+queryCopy+", term id : "+term.getId());
        final QueryResponse queryResponse = interactionSolrServer.query(queryCopy);

        Map<String, Integer> facetQueries = queryResponse.getFacetQuery();

        return facetQueries;
    }

    public OntologyTermWrapper getParent() {
        return parent;
    }

    public void setParent(OntologyTermWrapper parent) {
        this.parent = parent;
    }

    public long getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
    }



    private class OntologyTermWrapperComparator implements Comparator<OntologyTermWrapper> {

        public int compare(OntologyTermWrapper o1, OntologyTermWrapper o2) {
            if (o1.getInteractionCount() != o2.getInteractionCount()) {
                return Long.valueOf(o2.getInteractionCount()).compareTo(o1.getInteractionCount());
            }
            
            return o1.getTerm().getName().compareTo(o2.getTerm().getName());
        }
    }

    @Override
    public String toString() {
        return "OntologyTermWrapper{"+term.getId()+":"+term.getName()+"}";
    }
}
