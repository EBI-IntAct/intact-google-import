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
package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.view.webapp.controller.search.facet.ExpansionCount;
import uk.ac.ebi.intact.view.webapp.model.LazySearchResultDataModel;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("request")
public class FacetController {

    private static final Log log = LogFactory.getLog( FacetController.class );

    @Autowired
    private SearchController searchController;

    public FacetController() {

    }

    public ExpansionCount getExpansionCount() {
        FacetField facetField = getFacetField(FieldNames.COMPLEX_EXPANSION_FACET);
        return new ExpansionCount(facetField);
    }

    private FacetField getFacetField(String field) {
        LazySearchResultDataModel model = searchController.getResults();

        if (model == null) {
            return null;
        } else {
            log.debug( "LazySearchResultDataModel is null for FacetController.getFacetField(\""+ field +"\")" );
        }

        final IntactSolrSearchResult result = model.getResult();
        if( result != null ) {
            return result.getFacetField(field);
        } else {
            log.debug( "SolrSearchResult is null" );
        }

        return null;
    }
}
