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
package uk.ac.ebi.intact.view.webapp.controller.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.view.webapp.application.SpringInitializedService;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import java.io.IOException;

/**
 * Container for the application statistics (e.g. database counts)
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@ApplicationScoped
public class StatisticsController extends SpringInitializedService {

    private static final Log log = LogFactory.getLog( StatisticsController.class );

    @Autowired
    private IntactViewConfiguration viewConfiguration;

    @Autowired
    private DaoFactory daoFactory;

    private int binaryInteractionCount;
    private int proteinCount;
    private int experimentCount;
    private int publicationCount;
    private int cvTermsCount;
    private int interactorCount;
    private int interactionCount;
    private int interactorsWithNoInteractions;

    private IntactSolrSearcher searcher;

    public StatisticsController() {

    }

    @Override
    public void initialize(){
        if (log.isInfoEnabled()) log.info("Calculating statistics");

        // index stats
        binaryInteractionCount = countBinaryInteractionsFromIndex();

        // database stats
        proteinCount = daoFactory.getProteinDao().countAll();
        experimentCount = daoFactory.getExperimentDao().countAll();
        publicationCount = daoFactory.getPublicationDao().countAll();
        cvTermsCount = daoFactory.getCvObjectDao().countAll();
        interactionCount = daoFactory.getInteractionDao().countAll();
        interactorCount = daoFactory.getInteractorDao().countAll();
        interactorsWithNoInteractions = interactorCount - interactionCount;
    }

    @PostConstruct
    public void createSolrIntactSearcher(){

        SolrServer solrServer = viewConfiguration.getInteractionSolrServer();
        this.searcher = new IntactSolrSearcher(solrServer);
    }

    @Transactional(readOnly = true)
    public synchronized void calculateStats() throws IOException {
        if (log.isInfoEnabled()) log.info("Calculating statistics");

        initialize();
    }

    public int countBinaryInteractionsFromIndex() {

        final IntactSolrSearchResult result;
        try {
            result = searcher.search("*:*", 0, 0);
            int count = Long.valueOf(result.getNumberResults()).intValue();
            return count;
        } catch (Exception e) {
            log.error("Impossible to check statistics in solr", e);
            return 0;
        }
    }

    public int getCvTermsCount() {
        return cvTermsCount;
    }

    public int getBinaryInteractionCount() {
        return binaryInteractionCount;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    public int getExperimentCount() {
        return experimentCount;
    }

    public int getPublicationCount() {
        return publicationCount;
    }

    public int getInteractorCount() {
        return interactorCount;
    }

    public int getInteractorsWithNoInteractions() {
        return interactorsWithNoInteractions;
    }

    public int getInteractionCount() {
        return interactionCount;
    }
}
