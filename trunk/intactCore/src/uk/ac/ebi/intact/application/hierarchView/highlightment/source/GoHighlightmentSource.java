
package uk.ac.ebi.intact.application.hierarchView.highlightment.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import uk.ac.ebi.intact.application.hierarchView.business.Constants;
import uk.ac.ebi.intact.application.hierarchView.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchView.business.graph.GraphHelper;
import uk.ac.ebi.intact.application.hierarchView.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchView.struts.StrutsConstants;
import uk.ac.ebi.intact.application.hierarchView.struts.view.utils.SourceBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.simpleGraph.BasicGraphI;
import uk.ac.ebi.intact.simpleGraph.Node;
import uk.ac.ebi.intact.util.SearchReplace;

/**
 * Interface allowing to wrap an highlightment source.
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class GoHighlightmentSource extends HighlightmentSource {

    static Logger logger = Logger.getLogger( Constants.LOGGER_NAME );

    /**
     * separator of keys, use to create and parse key string.
     */
    private static final String KEY_SEPARATOR = ",";
    private static final String ATTRIBUTE_OPTION_CHILDREN = "CHILDREN";
    private static final String PROMPT_OPTION_CHILDREN = "With children of the selected GO term";

    /**
     * The key for this source 'go'
     */
    private static final String SOURCE_KEY = "go";

    /**
     * Return the html code for specific options of the source to integrate int
     * the highlighting form. if the method return null, the source hasn't
     * options.
     * 
     * @return the html code for specific options of the source.
     */
    public String getHtmlCodeOption(HttpSession aSession) {
        String htmlCode;
        IntactUserI user = (IntactUserI) aSession
                .getAttribute( uk.ac.ebi.intact.application.hierarchView.business.Constants.USER_KEY );
        String check = (String) user
                .getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );

        //String check = (String) aSession.getAttribute
        // (ATTRIBUTE_OPTION_CHILDREN);

        if ( check == null ) {
            check = "";
        }

        htmlCode = "<INPUT TYPE=\"checkbox\" NAME=\""
                + ATTRIBUTE_OPTION_CHILDREN + "\" " + check
                + " VALUE=\"checked\">" + PROMPT_OPTION_CHILDREN;

        return htmlCode;
    }

    /**
     * Return a collection of keys specific to the selected protein and the
     * current source. e.g. If the source is GO, we will send the collection of
     * GO term owned by the given protein. Those informations are retreived from
     * the Intact database
     * 
     * @param aProteinAC a protein identifier (AC)
     * @return a set of keys (this keys are a String) (this Keys are a String[]
     *         which contains the GOterm and a description)
     */
    public Collection getKeysFromIntAct(String aProteinAC, HttpSession aSession) {

        Collection result = null;
        Iterator iterator;
        Collection listGOTerm = new ArrayList();
        IntactUserI user = (IntactUserI) aSession
                .getAttribute( uk.ac.ebi.intact.application.hierarchView.business.Constants.USER_KEY );

        if ( null == user ) {
            logger
                    .error( "No user found in the session, unable to search for GO terms" );
            return null;
        }

        try {
            logger.info( "Try to get a list of GO term (from protein AC="
                    + aProteinAC + ")" );
            result = user.getHelper().search( Protein.class.getName(), "ac",
                    aProteinAC );
        }
        catch ( IntactException ie ) {
            logger.error( "When trying to get a list of GO", ie );
            return null;
        }

        // no object
        if ( result.isEmpty() )
            return null;

        iterator = result.iterator();
        Interactor interactor = (Interactor) iterator.next();

        // get Xref collection
        Collection xRef = interactor.getXrefs();
        logger.info( xRef.size() + " Xref found" );
        listGOTerm = filterInteractorXref( xRef );

        return listGOTerm;
    } // getKeysFromIntAct

    /**
     * get a collection of XRef and filter to keep only GO terms
     * 
     * @param xRef the XRef collection
     * @return a GO term collection or an empty collection if none exists.
     */
    private Collection filterInteractorXref(Collection xRef) {
        Collection listGOTerm = new ArrayList( xRef.size() ); // size will be >=
        // to needed
        // capacity
        Iterator xRefIterator = xRef.iterator();

        while ( xRefIterator.hasNext() ) {
            String[] goterm = new String[2];
            Xref xref = (Xref) xRefIterator.next();

            if ( ( xref.getCvDatabase().getShortLabel() ).toLowerCase().equals(
                    SOURCE_KEY ) ) {
                goterm[0] = xref.getPrimaryId();
                goterm[1] = xref.getSecondaryId();
                listGOTerm.add( goterm );
                logger.info( xref.getPrimaryId() );
            }
        }

        return listGOTerm;
    }

    /**
     * get a collection of the xrefs and filter to keep only GO terms.
     * 
     * The collection has the acutally just one entry which is a map structure.
     * This map maps a source (e.g. GO) to a collection of strings which stores
     * the primary and the secondary IDs of the central proteins.
     * 
     * Method is used when the graph was built with the mine database table
     * 
     * @param xRef all xrefs of the centralnodes
     * @return a collection with just the GO terms
     */
    private Collection filterXref(Collection xRef) {
        Collection listGOTerm = null;
        String[] goterm;
        // set to keep track of the GO Terms which are added to avoid duplicate
        // entries. This is needed because we are adding String[] to the actual
        // collection and the method 'contains' fails when testing if an array
        // (with the same entries) is already in the collection (even if it is
        // in)
        Set doubleEntrieCheckSet = new HashSet( xRef.size() );

        Iterator xRefIterator = xRef.iterator();
        if ( xRefIterator.hasNext() ) {
            // the source map is fetched
            // Map<String, Collection<String> >
            Map sourceMap = (Map) xRefIterator.next();

            Collection goTerms = (Collection) sourceMap.get( SOURCE_KEY );
            // the collection stores the filterd xrefs - it has the size of the
            // number of GO terms divided by 2 because in the goTerms collection
            // two following entries are represanting one GO Term (even: primary
            // ID, odd: secondary ID)
            listGOTerm = new ArrayList( goTerms.size() / 2 );

            Iterator sourceIterator = goTerms.iterator();
            while ( sourceIterator.hasNext() ) {
                goterm = new String[2];
                // the two entries of the collection are stored as one GO Term
                goterm[0] = (String) sourceIterator.next();
                goterm[1] = (String) sourceIterator.next();

                // if both entries are not already in the collection they are
                // added to the list of GO Terms
                if ( !doubleEntrieCheckSet.contains( goterm[0] )
                        && !doubleEntrieCheckSet.contains( goterm[1] ) ) {
                    listGOTerm.add( goterm );
                    doubleEntrieCheckSet.add( goterm[0] );
                    doubleEntrieCheckSet.add( goterm[1] );
                }
            }
        }
        return listGOTerm;
    }

    /**
     * Returns a collection of nodes to highlight for the display
     * 
     * @param aSession the current session
     * @param aGraph the network to display
     * @param selectedGOTerm the selected GO Term
     * @param children the children GO Terms of the selected GO Term
     * @param searchForChildren whether it shall be searched for the children
     * @return
     * @throws IntactException
     * @throws SQLException
     */
    private Collection proteinToHighlightInteractor(InteractionNetwork aGraph,
            String selectedGOTerm, Collection children,
            boolean searchForChildren, IntactUserI user) throws SQLException,
            IntactException {

        // if the source highlight map of the network is empty
        // it is filled with the source informations from each
        // node of the graph
        if ( aGraph.isSourceHighlightMapEmpty() ) {
            GraphHelper gh = new GraphHelper( user );

            ArrayList listOfNode = aGraph.getOrderedNodes();
            int size = listOfNode.size();

            for (int i = 0; i < size; i++) {
                BasicGraphI node = (BasicGraphI) listOfNode.get( i );
                // add all sources to the source highglighting map
                // TODO: every node is set as non central node....
                // this was done because the additional information which is
                // added to the central node is not used when the graph is not
                // built by the mine database table
                gh.addSourcesToNode( node, false, aGraph );
            }
        }
        // return the set of proteins to highlight based on the source
        // highlighting map of the graph
        return proteinToHighlightSourceMap( aGraph, children, selectedGOTerm,
                searchForChildren );
    }

    private Collection proteinToHighlightDatabase(HttpSession aSession,
            InteractionNetwork aGraph, String selectedGOTerm,
            Collection children, boolean searchForChildren) {
        Collection nodeList = new ArrayList( 20 ); // should be enough for 90%
        // cases
        ArrayList listOfNode = aGraph.getOrderedNodes();
        int size = listOfNode.size();
        String[] goTermInfo = null;
        String goTerm = null;
        for (int i = 0; i < size; i++) {
            Node node = (Node) listOfNode.get( i );
            String ac = node.getAc();
            // Search all GoTerm for this ac number
            Collection listGOTerm = this.getKeysFromIntAct( ac, aSession );
            if ( ( listGOTerm != null ) && ( listGOTerm.isEmpty() == false ) ) {
                Iterator list = listGOTerm.iterator();
                while ( list.hasNext() ) {
                    goTermInfo = (String[]) list.next();
                    goTerm = goTermInfo[0];

                    if ( selectedGOTerm.equals( goTerm ) ) {
                        nodeList.add( node );
                        break;
                    }

                    //                    if (searchForChildren == true) {
                    Iterator it = children.iterator();
                    while ( it.hasNext() ) {
                        String newGOTerm = (String) it.next();
                        if ( newGOTerm.equals( goTerm ) ) {
                            nodeList.add( node );
                            break;
                        }
                    }
                    //                    }
                } // while
            } // if
        } // for
        return nodeList;
    }

    /**
     * Returns a collection of proteins to be highlighted in the graph.
     * 
     * Method is called when the graph was built by the mine database table.
     * 
     * @param aGraph the graph
     * @param children the children of the selected GO Term
     * @param selectedGOTerm the selected GO Term
     * @param searchForChildren whether it shall be searched for the children
     * @return
     */
    private Collection proteinToHighlightSourceMap(InteractionNetwork aGraph,
            Collection children, String selectedGOTerm,
            boolean searchForChildren) {

        Collection nodeList = new ArrayList( 20 ); // should be enough for 90%
        // cases

        // retrieve the set of proteins to highlight for the source key (e.g.
        // GO) and the selected GO Term
        Set proteinsToHighlight = aGraph.getProteinsForHighlight( SOURCE_KEY,
                selectedGOTerm );

        // if we found any proteins we add all of them to the collection
        if ( proteinsToHighlight != null ) {
            nodeList.addAll( proteinsToHighlight );
        }

        /*
         * for every children all proteins related to the current GO Term are
         * fetched from the source highlighting map of the graph and added to
         * the collection
         */
        if ( searchForChildren ) {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                proteinsToHighlight = aGraph.getProteinsForHighlight(
                        SOURCE_KEY, (String) iter.next() );
                if ( proteinsToHighlight != null ) {
                    nodeList.addAll( proteinsToHighlight );
                }
            }
        }
        return nodeList;
    }

    /**
     * Create a set of protein we must highlight in the graph given in
     * parameter. The protein selection is done according to the source keys
     * stored in the IntactUser. Keys are GO terms, so we select (and highlight)
     * every protein which awned that GO term. If the children option is
     * activated, all proteins which owned a children of the selected GO term
     * are selected.
     * 
     * @param aSession the session where to find selected keys.
     * @param aGraph the graph we want to highlight
     * @return a collection of node to highlight
     */
    public Collection proteinToHightlight(HttpSession aSession,
            InteractionNetwork aGraph) {

        IntactUserI user = (IntactUserI) aSession
                .getAttribute( Constants.USER_KEY );
        Collection children = user.getKeys();
        String selectedGOTerm = user.getSelectedKey();

        if ( children.remove( selectedGOTerm ) ) {
            logger.info( selectedGOTerm + " removed from children collection" );
        }

        // get source option
        String check = (String) user
                .getHighlightOption( ATTRIBUTE_OPTION_CHILDREN );
        boolean searchForChildren;
        if ( check != null ) {
            searchForChildren = check.equals( "checked" );
        }
        else {
            searchForChildren = false;
        }
        logger.info( "Children option activated ? " + searchForChildren );
        /**
         * T E S T
         */
        searchForChildren = true;

        // if the graph was built the mine dabase table a differen way to
        // retrieve the proteins to be highlighted are used
        if ( GraphHelper.BUILT_WITH_MINE_TABLE ) {
            return proteinToHighlightSourceMap( aGraph, children,
                    selectedGOTerm, searchForChildren );
        }
        else {
            try {
                return proteinToHighlightInteractor( aGraph, selectedGOTerm,
                        children, searchForChildren, user );
            }
            catch ( Exception e ) {
                return proteinToHighlightDatabase( aSession, aGraph,
                        selectedGOTerm, children, searchForChildren );
            }
        }
    }

    /**
     * Allows to update the session object with parameters' request. These
     * parameters are specific of the implementation.
     * 
     * @param aRequest request in which we have to get parameters to save in the
     *            session
     * @param aSession session in which we have to save the parameter
     */
    public void saveOptions(HttpServletRequest aRequest, HttpSession aSession) {

        IntactUserI user = (IntactUserI) aSession
                .getAttribute( Constants.USER_KEY );
        String[] result = aRequest
                .getParameterValues( ATTRIBUTE_OPTION_CHILDREN );

        if ( result != null )
            user.addHighlightOption( ATTRIBUTE_OPTION_CHILDREN, result[0] );
    } // saveOptions

    public List getSourceUrls(Collection xRefs, Collection selectedXRefs,
            String applicationPath) throws IntactException {

        // get in the Highlightment properties file where is interpro
        Properties props = IntactUserI.HIGHLIGHTING_PROPERTIES;

        if ( null == props ) {
            String msg = "Unable to find the interpro hostname. "
                    + "The properties file '"
                    + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                    + "' couldn't be loaded.";
            logger.error( msg );
            throw new IntactException( msg );
        }

        String goPath = props
                .getProperty( "highlightment.source.GO.applicationPath" );

        if ( null == goPath ) {
            String msg = "Unable to find the interpro hostname. "
                    + "Check the 'highlightment.source.GO.applicationPath' property in the '"
                    + StrutsConstants.HIGHLIGHTING_PROPERTY_FILE
                    + "' properties file";
            logger.error( msg );
            throw new IntactException( msg );
        }

        // filter to keep only GO terms
        logger.info( xRefs.size() + " Xref before filtering" );
        Collection listGOTerm = null;
        if ( GraphHelper.BUILT_WITH_MINE_TABLE ) {
            listGOTerm = filterXref( xRefs );
        }
        else {
            listGOTerm = filterInteractorXref( xRefs );
        }

        logger.info( listGOTerm.size() + " GO term after filtering" );

        // create url collection with exact size
        List urls = new ArrayList( listGOTerm.size() );

        // Create a collection of label-value object (GOterm, URL to access a
        // nice display in interpro)
        String[] goTermInfo;
        String goTermId, goTermDescription;

        /*
         * In order to avoid the browser to cache the response to that request
         * we stick at its end of the generated URL.
         */
        String randomParam = "&now=" + System.currentTimeMillis();

        if ( listGOTerm != null && ( false == listGOTerm.isEmpty() ) ) {
            Iterator list = listGOTerm.iterator();
            while ( list.hasNext() ) {
                goTermInfo = (String[]) list.next();
                goTermId = goTermInfo[0];
                goTermDescription = goTermInfo[1];

                String directHighlightUrl = applicationPath
                        + "/source.do?keys=${selected-children}&clicked=${id}"
                        + randomParam;
                String hierarchViewURL = null;

                try {
                    hierarchViewURL = URLEncoder.encode( directHighlightUrl,
                            "UTF-8" );
                }
                catch ( UnsupportedEncodingException e ) {
                    logger.error( e );
                }

                // replace ${selected-children} and ${id} by the GO id.
                logger.info( "direct highlight URL: " + directHighlightUrl );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl,
                        "${selected-children}", goTermId );
                directHighlightUrl = SearchReplace.replace( directHighlightUrl,
                        "${id}", goTermId );
                logger.info( "direct highlight URL (modified): "
                        + directHighlightUrl );

                String quickGoUrl = goPath + "/DisplayGoTerm?selected="
                        + goTermId + "&intact=true&format=contentonly&url="
                        + hierarchViewURL + "&frame=_top";
                logger.info( "Xref: " + goTermId );

                boolean selected = false;
                if ( selectedXRefs != null && selectedXRefs.contains( goTermId ) ) {
                    logger.info( goTermId + " SELECTED" );
                    selected = true;
                }
                urls.add( new SourceBean( goTermId, goTermDescription,
                        quickGoUrl, directHighlightUrl, selected,
                        applicationPath ) );
            }
        }

        return urls;
    } // getSourceUrls

    /**
     * Parse the set of key generate by the source and give back a collection of
     * keys.
     * 
     * @param someKeys a string which contains some key separates by a
     *            character.
     * @return the splitted version of the key string as a collection of String.
     */
    public Collection parseKeys(String someKeys) {
        Collection keys = new Vector();

        if ( ( null == someKeys ) || ( someKeys.length() < 1 ) ) {
            return null;
        }

        StringTokenizer st = new StringTokenizer( someKeys, KEY_SEPARATOR );

        while ( st.hasMoreTokens() ) {
            String key = st.nextToken();
            keys.add( key );
        }

        return keys;
    }
}

