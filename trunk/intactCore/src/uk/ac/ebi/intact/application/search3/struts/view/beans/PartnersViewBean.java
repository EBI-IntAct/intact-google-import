/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/


package uk.ac.ebi.intact.application.search3.struts.view.beans;

import uk.ac.ebi.intact.application.search3.struts.util.ProteinUtils;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.SearchReplace;

import java.util.*;

/**
 * <p/>
 * This bean provides JSPs with the information needed to display a 'partners' view of a search
 * result. This view is defined by the mock web page specifications of June 2004. Typically a user
 * will search for a Protein, and a corresponding 'partners' view of related information will be
 * displayed. </p>
 * <p/>
 * This class provides JSPs with as much information as possible in a display-friendly (ie String)
 * format - however for complex beans the JSP itself will need to obtain the information it needs to
 * render the web page. </p>
 * <p/>
 * As at June 2004, the summary page needs to show the following information for each Protein found
 * in the search results: <ul> <li>Intact Name</li> <li>Intact Ac</li> <li>Number of
 * Interactions</li> <li>Uniprot AC</li> <li>Gene Name</li> <li>Description</li> </ul> After a row
 * for the primary result, the Interaction partners are lisetd, with the same information. </p>
 * <p/>
 * NOTE: If a JSP wishes to display multiple search results then a SummaryViewBean should be created
 * for each result and displayed in turn. This avoids unnecessary complications in the code itself
 * and leaves the display decisions to the JSP, where it belongs. </p>
 *
 * @author Chris Lewington
 * @version $Id$
 */
public class PartnersViewBean extends AbstractViewBean {


    /**
     * The original search result - a single Protein
     */
    private Protein protein;

    /**
     * The partner Protein which interaction with the protein
     */
    private Protein partner;

    /**
     * This Protein's Uniprot AC
     */
    private String uniprotAc;

    /**
     * The String representing the uniprot URL for this Protein
     */
    private String uniprotURL;

    /**
     * The specific search URL for this Protein (ie with its AC)
     */
    private String protSearchURL;

    /**
     * The general form of search URL to be used.
     */
    private String searchURL;

    /**
     * The search URL for all of the Interactions containing this Protein.
     */
    private String interactionSearchURL;

    /**
     * The Set of this Protein's Interaction partners
     */
    private Set interactionPartners;

    /**
     * Assumed a List of these, as that is what is in the single Protein view. BUT the 'summary' web
     * page only has ONE gene name. NEEDS TO BE CLARIFIED
     */
    private Collection geneNames;

    /**
     * A String representation of the number of Interactions this Protein takes part in.
     */
    private String numberOfInteractions;

    /**
     * if true, the bean will only show self interaction. if false (default), the been will show all
     * interactions.
     */
    private boolean selfInteraction = false;
    /**
     * Filter to provide filtering on GeneNames
     */
    private static ArrayList geneNameFilter = new ArrayList();

    // nested implementation for providing the gene filter
    static {
        geneNameFilter.add("gene-name");
        geneNameFilter.add("gene-name-synonym");
        geneNameFilter.add("orf-name");
        geneNameFilter.add("locus-name");
    }


    /**
     * The bean constructor requires a Protein to wrap, plus beans on the context path to the search
     * application, a general searchURL and the help link.
     *
     * @param prot        The Protein whose beans are to be displayed
     * @param helpLink    The link to the help pages
     * @param searchURL   The general URL to be used for searching (can be filled in later).
     * @param contextPath The path to the search application.
     */
    public PartnersViewBean(Protein prot, String helpLink, String searchURL, String contextPath) {
        super(helpLink, contextPath);
        protein = prot;
        this.searchURL = searchURL;
    }


    /**
     * The bean constructor requires a Protein to wrap, plus beans on the context path to the search
     * application, a general searchURL and the help link.
     *
     * @param prot            The Protein whose beans are to be displayed
     * @param selfInteraction define if we limit the bean to self interactions.
     * @param helpLink        The link to the help pages
     * @param searchURL       The general URL to be used for searching (can be filled in later).
     * @param contextPath     The path to the search application.
     */
    public PartnersViewBean(Protein prot, boolean selfInteraction, String helpLink,
                            String searchURL, String contextPath) {
        super(helpLink, contextPath);
        protein = prot;
        this.searchURL = searchURL;
        this.selfInteraction = selfInteraction;
    }

    /**
     * The bean constructor requires a Protein to wrap, plus beans on the context path to the search
     * application, a general searchURL and the help link.
     *
     * @param prot        The Protein whose beans are to be displayed
     * @param helpLink    The link to the help pages
     * @param searchURL   The general URL to be used for searching (can be filled in later).
     * @param contextPath The path to the search application.
     */
    public PartnersViewBean(Protein prot, Protein partner, String helpLink, String searchURL,
                            String contextPath) {
        super(helpLink, contextPath);
        protein = prot;
        this.partner = partner;
        this.searchURL = searchURL;

    }

    /**
     * This is left over from the earlier version - will be removed. It does nothing here.
     */
    public void getHTML(java.io.Writer writer) {
    };

    /**
     * Graph buttons are shown.
     *
     * @return whether or not the graph buttons are displayed
     */
    public boolean showGraphButtons() {
        return true;
    }

    // Just honouring the contract.
    public String getHelpSection() {
        return "interaction.beans.view";
    }

    // Implementing abstract methods

    /**
     * Adds the shortLabel of the Protein to an internal list used later for highlighting in a
     * display. NOT SURE IF WE STILL NEED THIS!!
     */
    public void initHighlightMap() {
        Set set = new HashSet(1);
        set.add(protein.getShortLabel());
        setHighlightMap(set);
    }

    /**
     * Basic accessor, provided in case anything ever needs access to the wrapped object.
     *
     * @return Protein the Protein instance wrapped by this view bean.
     */
    public Protein getMainProtein() {
        return protein;
    }

    /**
     * Returns the partner protein of this PartnerViewBean
     *
     * @return Protein the Partner Protein instance wrapped by this view bean
     */
    public Protein getPartner() {
        return partner;
    }

    /**
     * NB In the overview webpage mockup, this is hyperlinked to search with the shortLabel.
     *
     * @return String of the shortlabel of the protein from the 1 partner
     */
    public String getIntactName() {
        return protein.getShortLabel();
    }

    /**
     * NB in the overview mockup page, this is hyperlinked to provide the single Protein view.
     *
     * @return String representation of the ac from the 1 partner
     */
    public String getAc() {
        return protein.getAc();
    }

    public String getDescription() {
        return protein.getFullName();
    }

    /**
     * Provides a comma-separated list of gene names for this Protein.
     *
     * @return String al list of gene names as a String.
     */
    public Collection getGeneNames() {
        //populate on first request
        if (geneNames == null) {
            this.geneNames = new HashSet();
            //geneNames = new StringBuffer();
            //the gene names are obtained from the Aliases for the Protein
            //which are of type 'gene name'...
            Collection aliases = protein.getAliases();
            for (Iterator it = aliases.iterator(); it.hasNext();) {
                Alias alias = (Alias) it.next();

                if (geneNameFilter.contains(alias.getCvAliasType().getShortLabel())) {
                    geneNames.add(alias.getName());
                }
            }
            //now strip off trailing comma - if there are any names....
            if (geneNames.isEmpty()) {
                geneNames.add("-");
            }

        }
        return geneNames;
    }


    /**
     * This provides beans of the number of Interactions this Protein participates in. ISSUE: This
     * is a hyperlinked number on the web page mockup, pointing to the 'detail-blue' page mockup.
     * This seems crazy since if eg a Protein has 30 Interactions, all in different Experiments, the
     * amount of detail displayed would be unreadable!! DESIGN DECISION: see search link comment
     * later.
     *
     * @return String a String representing the number of Interactions for this Protein.
     */
    public String getNumberOfInteractions() {

        //set on first call
        if (numberOfInteractions == null) {

            if (partner != null) {
                if (partner.equals(protein)) {
                    // here only count the binary Interactions
                    numberOfInteractions = ProteinUtils.getSelfInteractions(protein)
                            .size() +
                            "";
                }
                else {
                    // protein and partner are different
                    numberOfInteractions = ProteinUtils.getNnaryInteractions(partner, protein)
                            .size() +
                            "";
                }
            }
            else {
                // no interaction partner
                numberOfInteractions = ProteinUtils.getNnaryInteractions(protein).size() + "";
            }
        }
        return numberOfInteractions;
    }


    /**
     * Returns a fully populated URL to perform a search for all this Protein's Interactions. ISSUE:
     * This is used in the mockups to fdisplay DETAIL. This would be unmanageable for large
     * Interaction lists spread across Experiments. DESIGN DECISION: make th link go to the main
     * 'simple' page to list the Interactions *there*, so users can then choose which detail they
     * want.
     *
     * @return String The complete search URL to perform a (possibly multiple) search for this
     *         Protein's Interactions
     */
    public String getInteractionsSearchURL() {

        Collection interactions = null;

        if (partner != null) {

            if (partner.equals(protein)) {
                // here only count the binary Interactions
                interactions = ProteinUtils.getSelfInteractions(protein);

            }
            else {
                // protein and partner are different
                interactions = ProteinUtils.getNnaryInteractions(partner, protein);
            }

        }
        else {
            // no interaction partner
            interactions = ProteinUtils.getNnaryInteractions(protein);
        }

        // now create the URL-based searchquery
        StringBuffer sb = new StringBuffer();
        for (Iterator it = interactions.iterator(); it.hasNext();) {
            Interaction anInteraction = (Interaction) it.next();
            sb.append(anInteraction.getAc());
            if (it.hasNext()) sb.append(",");
            interactionSearchURL = searchURL + sb.toString();
        }
        return interactionSearchURL;
    }

    /**
     * Provides the beans of the wrapped Protein's interaction partners for display. The beans are
     * in the form of a Set of SummaryViewBeans so that each partner's beans can be displayed in the
     * same way as the wrapped Protein. Sets are used because we don't want to display a set of
     * beans more than once.
     *
     * @return Set a Set of SummaryViewBeans for each interaction partner, or empty if there are
     *         none.
     */
    public Collection getInteractionPartners() {

        Collection interactions = null;
        if (partner == null) {

            // this protein got no partner in the view, just grab all interactions
            // and that's it
            interactions = ProteinUtils.getNnaryInteractions(protein);
            //TODO unefficent, find better way for that

            interactionPartners = new HashSet(interactions.size());
            if (selfInteraction) {
                interactionPartners.add(new PartnersViewBean(protein, protein, getHelpLink(), searchURL,
                                                             getContextPath()));
            }
            else {
                boolean hasNoSelfInteraction = ProteinUtils.getSelfInteractions(protein).isEmpty();

                for (Iterator iterator = interactions.iterator(); iterator.hasNext();) {
                    Interaction anInteraction = (Interaction) iterator.next();
                    Collection someComponents = anInteraction.getComponents();

                    for (Iterator iterator1 = someComponents.iterator(); iterator1.hasNext();) {
                        Component aComponent = (Component) iterator1.next();
                        Interactor anInteractor = aComponent.getInteractor();

                        //TODO this can write much clearer and easier
                        if (hasNoSelfInteraction) {
                            if (!protein.equals(anInteractor)) {
                                interactionPartners.add(new PartnersViewBean((Protein) anInteractor, protein, getHelpLink(), searchURL,
                                                                             getContextPath()));
                            }
                        }
                        else {
                            // we got a self interaction here
                            interactionPartners.add(new PartnersViewBean((Protein) anInteractor, protein, getHelpLink(), searchURL,
                                                                         getContextPath()));
                        }
                    }
                }
            }

        }
        else {
            // we should never arrive here (look assert)
        }

        if (interactionPartners == null) {
            interactionPartners = Collections.EMPTY_SET;
        }

        return interactionPartners;

    }


    /**
     * Provides this Protein's Uniprot AC (ie its Xref to Uniprot).
     *
     * @return String the Protein's Uniprot AC.
     */
    public String getUniprotAc
            () {

        //the Uniprot AC is the primaryID from an Xref whose database is 'uniprot'..
        if (uniprotAc == null) {

            //set it on first call...
            Xref xref = getUniprotXref();
            if (xref != null) uniprotAc = xref.getPrimaryId();
        }
        return uniprotAc;
    }

    /**
     * Provides the URL for linking to Uniprot, already filled in with the correct AC from this
     * Protein (ie it has its PrimaryID added in)
     *
     * @return String the usable Uniprot URL
     */
    public String getUniprotURL
            () {

        //set on first call
        if (uniprotURL == null) {

            //get the uniprot Xref then get hold of its annotation 'search-url'
            Xref xref = getUniprotXref();
            if (xref != null) {

                Collection annotations = xref.getCvDatabase().getAnnotations();
                Annotation annot = null;
                String searchUrl = null;
                for (Iterator it = annotations.iterator(); it.hasNext();) {
                    annot = (Annotation) it.next();
                    if (annot.getCvTopic().getShortLabel().equals("search-url")) {
                        //found it - we are done
                        searchUrl = annot.getAnnotationText();
                        uniprotURL =
                                SearchReplace.replace(searchUrl, "${ac}", xref.getPrimaryId());
                        break;
                    }
                }
            }
        }
        return uniprotURL;
    }

    /**
     * Provides a String representation of a URL to perform a search on this Protein's beans
     * (curently via AC)
     *
     * @return String a String representation of a search URL link for Protein
     */
    public String getProteinSearchURL
            () {

        return getSimpleSearchURL() + "&amp;searchClass=Protein&amp;view=single" +
                "&filter=ac";

    }

    /**
     * Convenience method to provide a String representation of a URL to perform a search on
     * Protein. This method will provide a Protein 'partners' view.
     *
     * @return String a String representation of a search URL link for Protein.
     */
    public String getProteinPartnerURL
            () {

        protSearchURL = getSimpleSearchURL() + "&amp;searchClass=Protein&amp;view=partner" +
                "&filter=ac";

        return protSearchURL;
    }


    /**
     * Provides a String representation of a URL to perform a basic search on this Protein's AC.
     * Thus a general search is performed using this Proterin's AC.
     *
     * @return String a String representation of a search URL link
     */
    public String getSimpleSearchURL
            () {

        return searchURL + protein.getAc() + "&filter=ac";
    }

    /**
     * Have to override this because we accumulate 'partner' viewbeans and even though they are held
     * in a Set, equality based on identity will not be good enough to prevent duplicates being
     * added. Instead we base bean equality on the equality of the wrapped Proteins.
     *
     * @param obj The bean we want to check
     * @return boolean true if the beans are equal, false otherwise.
     */
    public boolean equals
            (Object
            obj) {

        if (obj == null) return false;
        if (!(obj instanceof PartnersViewBean)) return false;
        PartnersViewBean other = (PartnersViewBean) obj;
        return (other.getMainProtein().equals(this.getMainProtein()));
    }

    /**
     * This just uses the wrapped Protein's hashcode.
     *
     * @return int the bean's hashcode (ie the Protein's)
     */
    public int hashCode
            () {
        return this.getMainProtein().hashCode();
    }

    /**
     * Returns the Uniprot Xref associated with this Protein.
     *
     * @return Xref The Uniprot Xref for the Protein, or null if there isn't one
     */
    private Xref getUniprotXref
            () {

        Collection xrefs = protein.getXrefs();
        for (Iterator it = xrefs.iterator(); it.hasNext();) {
            Xref xref = (Xref) it.next();
            if (xref.getCvDatabase().getShortLabel().equals("uniprot")) {
                //done - cache it and return
                return xref;
            }
        }
        return null;
    }
}
