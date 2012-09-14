package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.view.webapp.application.OntologyInteractorTypeConfig;
import uk.ac.ebi.intact.view.webapp.application.PsicquicThreadConfig;
import uk.ac.ebi.intact.view.webapp.controller.JpaBaseController;
import uk.ac.ebi.intact.view.webapp.controller.application.StatisticsController;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;
import uk.ac.ebi.intact.view.webapp.controller.details.DetailsController;
import uk.ac.ebi.intact.view.webapp.controller.list.InteractorListController;
import uk.ac.ebi.intact.view.webapp.controller.moleculeview.MoleculeViewController;
import uk.ac.ebi.intact.view.webapp.model.InteractorSearchResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.InteractorWrapper;
import uk.ac.ebi.intact.view.webapp.model.LazySearchResultDataModel;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Search controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
public class SearchController extends JpaBaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    private int totalResults;
    private int interactorTotalResults;
    private int proteinTotalResults;
    private int smallMoleculeTotalResults;
    private int geneTotalResults;
    private int nucleicAcidTotalResults;

    private boolean showProperties;
    private boolean showAlternativeIds;
    private boolean showBrandNames;

    private UserQuery userQuery;

    private String currentQuery;

    // results
    private LazySearchResultDataModel results;
    private InteractorSearchResultDataModel proteinResults;
    private InteractorSearchResultDataModel smallMoleculeResults;
    private InteractorSearchResultDataModel nucleicAcidResults;
    private InteractorSearchResultDataModel geneResults;

    // io
    private String exportFormat;

    //sorting
    private static final String DEFAULT_SORT_COLUMN = FieldNames.INTACT_SCORE_NAME;
    private static final boolean DEFAULT_SORT_ORDER = false;

    private String userSortColumn = DEFAULT_SORT_COLUMN;
    //as the Sort constructor is Sort(String field, boolean reverse)
    private boolean ascending = DEFAULT_SORT_ORDER;

    private int threadTimeOut = 10;

    private ExecutorService executorService;

    @Autowired
    private PsicquicThreadConfig psicquicThreadConfig;

    private PsicquicSearchManager psicquicController;

    private String acPrefix;

    public SearchController() {
    }

    @PostConstruct
    public void initialSearch() {

        StatisticsController statisticsController = (StatisticsController) getBean("statisticsController");
        this.totalResults = statisticsController.getBinaryInteractionCount();
        this.currentQuery = null;

        if (executorService == null){

            executorService = psicquicThreadConfig.getExecutorService();
        }

        try {
            this.psicquicController = new PsicquicSearchManager(this.executorService, this.intactViewConfiguration);
        } catch (PsicquicRegistryClientException e) {
            addErrorMessage("Problem counting results in other databases", "Registry not available");
            e.printStackTrace();
        }

        /*if (!FacesContext.getCurrentInstance().isPostback()) {
            UserQuery userQuery = getUserQuery();
            SolrQuery solrQuery = userQuery.createSolrQuery();
            doBinarySearch( solrQuery );
        }*/
    }

    public void searchOnLoad(ComponentSystemEvent evt) {
        FacesContext context = FacesContext.getCurrentInstance();

        String statusParam = context.getExternalContext().getRequestParameterMap().get("status");

        if (statusParam != null && "exp".equals(statusParam)) {
            addWarningMessage("Session expired", "The user session was expired due to intactivity or the server being restarted");
        }
    }

    public void searchBinaryInteractions(ComponentSystemEvent evt) {
        UserQuery userQuery = getUserQuery();
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);
    }

    public String doBinarySearchAction() {
        UserQuery userQuery = getUserQuery();
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);

        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doBinarySearchBRCA2Action() {
        UserQuery userQuery = getUserQuery();
        userQuery.setSearchQuery(userQuery.getGeneQuery());
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);

        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doBinarySearchUniprotAcAction() {
        UserQuery userQuery = getUserQuery();
        userQuery.setSearchQuery(userQuery.getUniprotAcQuery());
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);

        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doBinarySearchUniprotGeneAction() {
        UserQuery userQuery = getUserQuery();
        userQuery.setSearchQuery(userQuery.getUniprotGeneQuery());
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);

        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doBinarySearchPubmedAction() {
        UserQuery userQuery = getUserQuery();
        userQuery.setSearchQuery(userQuery.getPubmedQuery());
        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);

        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doBinarySearchActionFromOntologySearch() {
        UserQuery userQuery = getUserQuery();
        SolrQuery solrQuery = userQuery.createSolrQueryForOntologySearch();

        doBinarySearch(solrQuery);

        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doBinarySearchActionFilterSpokeExpanded() {
        UserQuery userQuery = getUserQuery();
        SolrQuery solrQuery = userQuery.createSolrQuery();

        // the true binary interactions have - in the complex expansion column
        solrQuery.addFilterQuery(FieldNames.COMPLEX_EXPANSION+":\"-\"");

        doBinarySearchOnly(solrQuery);
        return "/pages/interactions/interactions.xhtml?faces-redirect=true&includeViewParams=true";
    }

    public String doNewBinarySearch() {
        resetDetailControllers();

        UserQuery userQuery = getUserQuery();
        userQuery.setOntologySearchQuery(null);

        return doBinarySearchAction();
    }

    public void doBinarySearch(ActionEvent evt) {
        doBinarySearchAction();
    }

    public void doClearFilterAndSearch(ActionEvent evt) {
        UserQuery userQuery = getUserQuery();
        userQuery.clearFilters();
        doBinarySearch(evt);
    }

    public String doOntologySearchAction() {
        UserQuery userQuery = getUserQuery();
        final String query = userQuery.getOntologySearchQuery();

        if ( query == null) {
            addErrorMessage("The ontology query box was empty", "No search was submitted");
            return "search";
        }

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch( solrQuery );

        return "interactions";
    }

    public String doNewOntologySearch() {
        UserQuery userQuery = getUserQuery();
        userQuery.resetSearchQuery();
        resetDetailControllers();
        return doOntologySearchAction();
    }

    private void resetDetailControllers() {
        DetailsController detailsController = (DetailsController) getBean("detailsBean");
        MoleculeViewController moleculeViewController = (MoleculeViewController) getBean("moleculeViewBean");

        detailsController.setInteraction(null);
        moleculeViewController.setInteractor(null);
    }

    public void doBinarySearch(SolrQuery solrQuery) {

        try {
            if ( log.isDebugEnabled() ) {log.debug( "\tSolrQuery:  "+ solrQuery.getQuery() );}

            final SolrQuery solrQueryCopy = solrQuery;
            final SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();
            final int pageSize = getUserQuery().getPageSize();
            final PsicquicSearchManager psicquicController = this.psicquicController;
            final String query = solrQuery.getQuery();

            // prepare solr search
            Callable<Integer> intactRunnable = createIntactSearchRunnable(solrQueryCopy, solrServer, pageSize);

            Future<Integer> intactFuture = executorService.submit(intactRunnable);

            // count psicquic results while searching in Intact
            psicquicController.countResultsInOtherDatabases(query);

            // resume tasks
            checkAndResumeSearchTasks(intactFuture);
            psicquicController.checkAndResumePsicquicTasks();

            if ( log.isDebugEnabled() ) log.debug( "\tResults: " + totalResults );


        } catch ( uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException solrException ) {

            final String query = solrQuery.getQuery();
            if ( query != null && ( query.startsWith( "*" ) || query.startsWith( "?" ) ) ) {
                getUserQuery().setSearchQuery( "*:*" );
                addErrorMessage( "Your query '"+ query +"' is not correctly formatted",
                        "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                                "However, wildcard characters can be used anywhere else in one's query (eg. g?vin or gav* for gavin). " +
                                "Please do reformat your query." );
            }
        }
    }

    public void doBinarySearchOnly(SolrQuery solrQuery) {
        try {
            if ( log.isDebugEnabled() ) {log.debug( "\tSolrQuery:  "+ solrQuery.getQuery() );}

            final SolrQuery solrQueryCopy = solrQuery;
            final SolrServer solrServer = intactViewConfiguration.getInteractionSolrServer();
            final int pageSize = getUserQuery().getPageSize();

            results = createInteractionDataModel( solrQueryCopy, solrServer, pageSize );

            totalResults = results.getRowCount();

            if ( log.isDebugEnabled() ) log.debug( "\tResults: " + results.getRowCount() );

            // store the current query
            this.currentQuery = solrQuery.getQuery();

        } catch ( uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrException solrException ) {

            final String query = solrQuery.getQuery();
            if ( query != null && ( query.startsWith( "*" ) || query.startsWith( "?" ) ) ) {
                getUserQuery().setSearchQuery( "*:*" );
                addErrorMessage( "Your query '"+ query +"' is not correctly formatted",
                        "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                                "However, wildcard characters can be used anywhere else in one's query (eg. g?vin or gav* for gavin). " +
                                "Please do reformat your query." );
            }
        }
    }

    private void checkAndResumeSearchTasks(Future<Integer> intactFuture) {

        try {
            this.totalResults = intactFuture.get();

        } catch (InterruptedException e) {
            log.error("The intact search was interrupted, we cancel the task.", e);
            if (!intactFuture.isCancelled()){
                intactFuture.cancel(true);
            }
        } catch (ExecutionException e) {
            log.error("The intact search could not be executed, we cancel the task.", e);
            if (!intactFuture.isCancelled()){
                intactFuture.cancel(true);
            }
        }
    }

    private Callable<Integer> createIntactSearchRunnable(final SolrQuery solrQueryCopy, final SolrServer solrServer,
                                                         final int pageSize) {
        return new Callable<Integer>() {
                    public Integer call() {

                        results = createInteractionDataModel( solrQueryCopy, solrServer, pageSize );

                        // store the current query
                        currentQuery = solrQueryCopy.getQuery();

                        return results.getRowCount();
                    }
                };
    }

    public String doClearSearchAndGoHome() {
        getUserQuery().reset();

        return "/main?forces-redirect=true";
    }


    public void onTabChanged(TabChangeEvent evt) {
        // load interactions if necessary
        if (evt.getTab() != null && "interactionsTab".equals(evt.getTab().getId())){
            UserQuery userQuery = getUserQuery();
            if (this.currentQuery == null || !userQuery.getSearchQuery().equals(this.currentQuery)){
                doBinarySearch(userQuery.createSolrQuery());
            }
        }
        /*if (evt.getTab() != null && "listsTab".equals(evt.getTab().getId())) {
            doInteractorsSearch();

        } else if ("browseTab".equals(evt.getTab().getId())){
            BrowseController browseController = (BrowseController) getBean("browseBean");
            browseController.createListOfIdentifiers();
        }*/
    }


    private LazySearchResultDataModel createInteractionDataModel(SolrQuery query, SolrServer solrServer, int pageSize) {

        final LazySearchResultDataModel resultDataModel = new LazySearchResultDataModel(solrServer, query);
        resultDataModel.setPageSize(pageSize);
        return resultDataModel;
    }

    public void doInteractorsSearch() {

        OntologyInteractorTypeConfig typeConfig = (OntologyInteractorTypeConfig) getBean("ontologyInteractorTypeConfig");

        doProteinsSearch(typeConfig);
        doSmallMoleculeSearch(typeConfig);
        doNucleicAcidSearch(typeConfig);
        doGeneSearch(typeConfig);

        interactorTotalResults = smallMoleculeTotalResults + proteinTotalResults + nucleicAcidTotalResults + geneTotalResults;

    }

    private void doProteinsSearch(OntologyInteractorTypeConfig typeConfig) {
        proteinResults = doInteractorSearch(typeConfig.getProteinTypes());
        proteinTotalResults = proteinResults.getRowCount();
    }

    private void doSmallMoleculeSearch(OntologyInteractorTypeConfig typeConfig) {
        smallMoleculeResults = doInteractorSearch(typeConfig.getCompoundTypes());
        smallMoleculeTotalResults = smallMoleculeResults.getRowCount();
    }

    private void doNucleicAcidSearch(OntologyInteractorTypeConfig typeConfig) {
        nucleicAcidResults = doInteractorSearch(typeConfig.getNucleicAcidTypes());
        nucleicAcidTotalResults = nucleicAcidResults.getRowCount();
    }

    private void doGeneSearch(OntologyInteractorTypeConfig typeConfig) {
        geneResults = doInteractorSearch(typeConfig.getGeneTypes());
        geneTotalResults = geneResults.getRowCount();
    }

    public InteractorSearchResultDataModel doInteractorSearch(String interactorTypeMi) {
        return doInteractorSearch(new String[] {interactorTypeMi});
    }

    public InteractorSearchResultDataModel doInteractorSearch(String[] interactorTypeMis) {
        UserQuery userQuery = getUserQuery();
        final SolrQuery solrQuery = userQuery.createSolrQuery();

        if (log.isDebugEnabled()) log.debug("Searching interactors of type ("+ Arrays.toString(interactorTypeMis)+") for query: " + solrQuery);

        final InteractorSearchResultDataModel interactorResults
                = new InteractorSearchResultDataModel(intactViewConfiguration.getInteractionSolrServer(),
                                                      solrQuery,
                                                      interactorTypeMis);
        interactorResults.setPageSize(getUserQuery().getPageSize());
        return interactorResults;
    }

    public void doSearchInteractionsFromCompoundListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("compoundListController"));
    }

    public void doSearchInteractionsFromGeneListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("geneListController"));
    }

    public void doSearchInteractionsFromProteinListSelection(ActionEvent evt) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("proteinListController"));
    }

    public void doSearchInteractionsFromDnaListSelection( ActionEvent evt ) {
        doSearchInteractionsFromListSelection((InteractorListController) getBean("nucleicAcidListController"));
    }

    private void doSearchInteractionsFromListSelection(InteractorListController interactorListController) {
        final List<InteractorWrapper> selected = Arrays.asList(interactorListController.getSelected());

        if (selected.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder( selected.size() * 10 );
        sb.append("identifier:(");

        for (Iterator<InteractorWrapper> iterator = selected.iterator(); iterator.hasNext();) {
            InteractorWrapper interactorWrapper = iterator.next();

            sb.append(interactorWrapper.getAc());

            if (iterator.hasNext()) {
                sb.append(" ");
            }
        }

        sb.append(")");

        final String query = sb.toString();

        UserQuery userQuery = getUserQuery();
        userQuery.setSearchQuery( query );

        SolrQuery solrQuery = userQuery.createSolrQuery();

        doBinarySearch(solrQuery);
    }

    public String getIntactAc(BinaryInteraction binaryInteraction) {
        return getIntactAcXref(binaryInteraction).getIdentifier();
    }

    public CrossReference getMainAccessionXref(BinaryInteraction<?> binaryInteraction) {

        if (acPrefix == null){
            acPrefix = getIntactContext().getConfig().getAcPrefix();
        }
        CrossReference intactXref = null;

        for (CrossReference xref : binaryInteraction.getInteractionAcs()) {
            if (xref.getIdentifier().startsWith(acPrefix)) {
                return xref;
            }
            else if ("intact".equals(xref.getDatabase())) {
                intactXref = xref;
            }
        }

        return intactXref;
    }

    public CrossReference getIntactAcXref(BinaryInteraction<?> binaryInteraction) {

        for (CrossReference xref : binaryInteraction.getInteractionAcs()) {
            if ("intact".equals(xref.getDatabase())) {
                return xref;
            }
        }

        return null;
    }


    // Getters & Setters
    /////////////////////

    public LazySearchResultDataModel getResults() {
        return results;
    }

    public void setResults( LazySearchResultDataModel results ) {
        this.results = results;
    }

    public boolean isShowProperties() {
        return showProperties;
    }

    public void setShowProperties( boolean showProperties ) {
        this.showProperties = showProperties;
    }

    public boolean isShowAlternativeIds() {
        return showAlternativeIds;
    }

    public void setShowAlternativeIds( boolean showAlternativeIds ) {
        this.showAlternativeIds = showAlternativeIds;
    }

    public boolean isShowBrandNames() {
        return showBrandNames;
    }

    public void setShowBrandNames( boolean showBrandNames ) {
        this.showBrandNames = showBrandNames;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat( String exportFormat ) {
        this.exportFormat = exportFormat;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults( int totalResults ) {
        this.totalResults = totalResults;
    }

    public int getInteractorTotalResults() {
        return interactorTotalResults;
    }

    public void setInteractorTotalResults( int interactorTotalResults ) {
        this.interactorTotalResults = interactorTotalResults;
    }

    public InteractorSearchResultDataModel getProteinResults() {
        return proteinResults;
    }

    public void setProteinResults( InteractorSearchResultDataModel proteinResults) {
        this.proteinResults = proteinResults;
    }

    public int getSmallMoleculeTotalResults() {
        return smallMoleculeTotalResults;
    }

    public int getProteinTotalResults() {
        return proteinTotalResults;
    }

    public InteractorSearchResultDataModel getSmallMoleculeResults() {
        return smallMoleculeResults;
    }

    public String getUserSortColumn() {
        return userSortColumn;
    }

    public void setUserSortColumn( String userSortColumn ) {
        this.userSortColumn = userSortColumn;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending( boolean ascending ) {
        this.ascending = ascending;
    }

    public int getNucleicAcidTotalResults() {
        return nucleicAcidTotalResults;
    }

    public void setNucleicAcidTotalResults( int nucleicAcidTotalResults) {
        this.nucleicAcidTotalResults = nucleicAcidTotalResults;
    }

    public int getGeneTotalResults() {
        return geneTotalResults;
    }

    public void setGeneTotalResults(int geneTotalResults) {
        this.geneTotalResults = geneTotalResults;
    }

    public InteractorSearchResultDataModel getNucleicAcidResults() {
        return nucleicAcidResults;
    }

    public InteractorSearchResultDataModel getGeneResults() {
        return geneResults;
    }

    private UserQuery getUserQuery() {
        if (userQuery == null){
           userQuery = (UserQuery) getBean("userQuery");
        }
        return userQuery;
    }

    public PsicquicSearchManager getPsicquicSearchManager() {
        return psicquicController;
    }
}