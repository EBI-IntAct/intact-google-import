/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchView.business;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.model.Constants;
import uk.ac.ebi.intact.persistence.DAOFactory;
import uk.ac.ebi.intact.persistence.DAOSource;
import uk.ac.ebi.intact.persistence.DataSourceException;
import uk.ac.ebi.intact.simpleGraph.Graph;
import uk.ac.ebi.intact.application.hierarchView.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchView.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchView.struts.view.ClickBehaviourForm;

import javax.servlet.http.HttpSessionBindingEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Properties;


import org.apache.log4j.Logger;

/**
 * This class stores information about an Intact Web user session. <br>
 * Instead of binding multiple objects, only an object of this class is bound to a session,
 * thus serving a single access point for multiple information.
 * <p>
 * This class implements the <tt>HttpSessionBindingListener</tt> interface for it
 * can be notified of session time outs.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class IntactUser implements IntactUserI {

    static Logger logger = Logger.getLogger(uk.ac.ebi.intact.application.hierarchView.business.Constants.LOGGER_NAME);

    private static final int MINIMAL_DEPTH = 1;
    private static final int MAXIMAL_DEPTH = 2;
    private static final int DEFAULT_DEPTH = 1;

    /**
     * datasource entry point
     */
    private IntactHelper intactHelper;

    /**
     * The current network with which the user is working
     */
    private InteractionNetwork interactionNetwork;

    /**
     * The current click behaviour
     */
    private ClickBehaviourForm clickBehaviour;

    /**
     * Set of highlight options (key/value) given by the user via the web interface
     */
    private Map highlightOptions;

    /**
     * data relatives to the image producing (Image byte code, SVG DOM, HTML MAP)
     */
    private ImageBean imageBean;

    /**
     * Collection of keys received from the current source.
     * The highlightment of the current interaction network is done according to these keys.
     * <b>keys</b> contains a set of keys to take into account in the highlight process
     * <b>selectedKey</b> is the one which has been selected by the user
     */
    private Collection keys;
    private String selectedKey;

    /**
     * URL describes the link to the highlight source in the case the user has selected
     * one of those available for the current protein.
     * The URL is encoded in the UTF-8 format.
     */
    private String sourceURL;

    // User's form fields
    private String queryString;
    private int currentDepth;
    private int defaultDepth;
    private int minimalDepth;
    private int maximalDepth;

    //private boolean hasNoDepthLimit;
    private String methodLabel;
    private String methodClass;
    private String behaviour;
    private String applicationPath;


    public String getQueryString() {
        return queryString;
    }

    public String getApplicationPath () {
        return applicationPath;
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public int getMinimalDepth() {
        return minimalDepth;
    }

    public int getMaximalDepth() {
        return maximalDepth;
    }

    /**
     * says if the current depth is minimal
     * @return boolean true is the current depth is minimal, esle false.
     */
    public boolean minimalDepthReached(){
        return (currentDepth == minimalDepth);
    }

    /**
     * says if the current depth is maximal
     * @return boolean true is the current depth is maximal, esle false.
     */
    public boolean maximalDepthReached(){
        return (currentDepth == maximalDepth);
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public String getMethodClass() {
        return methodClass;
    }

    public String getBehaviour() {
        return behaviour;
    }

    /**
     * Allows the user to know if an interaction network will be displayed
     * @return
     */
    public boolean InteractionNetworkReadyToBeDisplayed(){
        return ((null != queryString) && (null != imageBean));
    }

    /**
     * Allows the user to know if an interaction network is ready to be highlighted.
     * i.e. all data needed to highlight the current interaction network are available.
     * @return boolean true if the interaction network can be highlighted, esle false.
     */
    public boolean InteractionNetworkReadyToBeHighlighted(){
        return (null != queryString) && (null != keys) && (behaviour != null) && (null != interactionNetwork);
    }


    public void setClickBehaviour (ClickBehaviourForm form) {
           clickBehaviour = form;
    }

    /**
     * is the current behaviour is to Add when the user click on the image map
     * @return true if Add the view is the current behaviour
     */
    public boolean clickBehaviourIsAdd () {
        if (clickBehaviour == null) return false;
        return clickBehaviour.addSelected();
    }

    /**
     * is the current behaviour is to Center the view when the user click on the image map
     * @return true if Center the view is the current behaviour
     */
    public boolean clickBehaviourIsCenter () {
        if (clickBehaviour == null) return true;
        return clickBehaviour.centerSelected();
    }

    public InteractionNetwork getInteractionNetwork() {
        return interactionNetwork;
    }

    public ImageBean getImageBean() {
        return imageBean;
    }

    public Collection getKeys() {
        return keys;
    }

    public String getSelectedKey() {
        return selectedKey;
    }

    public IntactHelper getHelper () {
        return intactHelper;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public boolean hasSourceUrlToDisplay() {
        return (sourceURL != null);
    }


    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Increase the depth of the interraction network up to the defined maximum.
     */
    public void increaseDepth(){
        if (currentDepth < maximalDepth)
            currentDepth++;
    }

    /**
     * Desacrease the depth of the interraction network up to the defined minimum.
     */
    public void desacreaseDepth(){
        if (currentDepth > minimalDepth)
            currentDepth--;
    }

    public void setDepthToDefault() {
        currentDepth = defaultDepth;
    }

    public void setMethodLabel (String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public void setMethodClass (String methodClass) {
        this.methodClass = methodClass;
    }

    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }

    public void setInteractionNetwork(InteractionNetwork in) {
        this.interactionNetwork = in;
    }

    public void setImageBean(ImageBean imageBean) {
        this.imageBean = imageBean;
    }

    public void setKeys(Collection keys) {
        this.keys = keys;
    }

    public void setSelectedKey(String key) {
        selectedKey = key;
    }

    public void setSourceURL(String aSourceURL) {
        sourceURL = aSourceURL;
    }

    public void resetSourceURL() {
        sourceURL = null;
    }


    /**
     * Constructs an instance of this class with given mapping file and
     * the name of the data source class.
     *
     * @param repositoryfile the name of the mapping file.
     * @param datasourceClass the class name of the Data Source.
     * @param applicationPath the current application path
     *
     * @exception DataSourceException for error in getting the data source; this
     *  could be due to the errors in repository files or the underlying
     *  persistent mechanism rejected <code>user</code> and
     *  <code>password</code> combination.
     * @exception IntactException thrown for any error in creating lists such
     *  as topics, database names etc.
     */
    public IntactUser (String repositoryfile, String datasourceClass, String applicationPath)
            throws DataSourceException, IntactException {

        init ();

        this.applicationPath = applicationPath;

        DAOSource dataSource = DAOFactory.getDAOSource (datasourceClass);

        // Pass config details to data source - don't need fast keys as only accessed once
        Map fileMap = new HashMap();
        fileMap.put (Constants.MAPPING_FILE_KEY, repositoryfile);
        dataSource.setConfig (fileMap);

        // build a helper for use throughout a session
        this.intactHelper = new IntactHelper (dataSource);
        logger.info ("IntactHelper created.");
    }

    /**
     * Set the default value of user's data
     */
    public void init () {
        this.queryString = null;

        // read the Graph.properties file
        Properties properties = GRAPH_PROPERTIES;

        if (null != properties) {
            String depth = properties.getProperty ("hierarchView.graph.depth.default");
            if (depth != null) {
                defaultDepth = Integer.parseInt(depth);
            } else {
                defaultDepth = DEFAULT_DEPTH;
            }
            currentDepth = defaultDepth;

            depth = properties.getProperty ("hierarchView.graph.depth.minimum");
            if (depth != null) {
                minimalDepth = Integer.parseInt(depth);
            } else {
                minimalDepth = MINIMAL_DEPTH;
            }

            depth = properties.getProperty ("hierarchView.graph.depth.maximum");
            if (depth != null) {
                maximalDepth = Integer.parseInt(depth);
            } else {
                maximalDepth = MAXIMAL_DEPTH;
            }
        }

        methodLabel = null;
        methodClass = null;
        behaviour = null;

        interactionNetwork = null;
        imageBean = null;
        keys = null;
        selectedKey = null;
        highlightOptions = new HashMap();
        //sourceURL = null;
        logger.info ("User's data set to default");
    }


    /**
     * Returns a subgraph centered on startNode (stored in the interaction network).
     * The subgraph will contain all nodes which are up to graphDepth interactions away from startNode.
     * Only Interactions which belong to one of the Experiments in experiments will be taken into account.
     * If experiments is empty, all Interactions are taken into account.
     *
     * Graph depth:
     * This parameter limits the size of the returned interaction graph. All baits are shown with all
     * the interacting preys, even if they would normally be on the "rim" of the graph.
     * Therefore the actual diameter of the graph may be 2*(graphDepth+1).
     *
     * Expansion:
     * If an Interaction has more than two interactors, it has to be defined how pairwise interactions
     * are generated from the complex data. The possible values are defined in the beginning of this file.
     *
     * @param in - the interaction network.
     * @param graphDepth - depth of the graph
     * @param experiments - Experiments which should be taken into account
     * @param complexExpansion - Mode of expansion of complexes into pairwise interactions
     *
     * @return a InteractionNetwork object.
     *
     * @exception IntactException - thrown if problems are encountered
     */
    public InteractionNetwork subGraph (InteractionNetwork in,
                                        int graphDepth,
                                        Collection experiments,
                                        int complexExpansion) throws IntactException {

        logger.info("Starting graph generation (" + in.getCentralProteinAC() + ", depth=" + graphDepth + ")");
        Graph graph = in;

//        intactHelper.clearCache();
//        logger.info ("Cache is wiped");

        Chrono chrono = new Chrono ();
        chrono.start();

        graph = intactHelper.subGraph ( in.getCentralProtein(),
                                        graphDepth,
                                        experiments,
                                        complexExpansion,
                                        graph);

        chrono.stop();
        String msg = "Creating the interaction network took: " + chrono;
        logger.info(msg);

        logger.info("Graph generation complete\n" + graph);

        return (InteractionNetwork) graph;
    } // subGraph



    public void resetHighlightOptions () {
          highlightOptions.clear();
    }

    public void addHighlightOption (String name, Object value) {
           highlightOptions.put(name, value);
    }

    public Object getHighlightOption (String name) {
        return highlightOptions.get (name);
    }

    public void setCurrentDepth(int depth) {
        if (depth > maximalDepth || depth < minimalDepth)
            currentDepth = defaultDepth;
        else
            currentDepth = depth;
    }


   public String getSearchUrl (String query) {
        String searchURL = null;

        // read the Search.properties file
        Properties properties = SEARCH_PROPERTIES;

        if (null != properties) {
            String url = properties.getProperty ("search.url");
            String queryParameter = properties.getProperty ("search.parameter.query.name");
//            String classParameter = properties.getProperty ("search.parameter.class.name");
//            String classValue     = properties.getProperty ("search.parameter.class.value");

            searchURL = url + "?" + queryParameter + "=" + query; //  + "&" + classParameter + "=" + classValue
        }

        logger.info ("search URL = " + searchURL);
        return searchURL;
    }


    public String getSearchUrl () {
        return getSearchUrl (queryString);
    }


    // Implements HttpSessionBindingListener

    /**
     * Will call this method when an object is bound to a session.
     * Not doing anything.
     */
    public void valueBound (HttpSessionBindingEvent event) {
    }

    /**
     * Will call this method when an object is unbound from a session.
     */
    public void valueUnbound (HttpSessionBindingEvent event) {
        try {
            this.intactHelper.closeStore();
            logger.info ("IntactHelper datasource closed (cause: removing attribute from session)");
        }
        catch(IntactException ie) {
            //failed to close the store - not sure what to do here yet....
            logger.error ("error when closing the IntactHelper store", ie);
        }
    }
}
