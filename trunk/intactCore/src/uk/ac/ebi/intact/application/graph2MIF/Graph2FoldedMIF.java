/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.graph2MIF;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.simpleGraph.BasicGraph;
import uk.ac.ebi.intact.simpleGraph.Edge;
import uk.ac.ebi.intact.simpleGraph.Graph;

import java.util.*;

/**
 * This class gives IntAct the possibillity to give out a graph to MIF-PSI-Format in XML.
 * see http://psidev.sf.net for details
 *
 * @author Henning Mersch (hmersch@ebi.ac.uk), Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Graph2FoldedMIF {
    /**
     *  logger for proper information
     *  see config/log4j.properties for more informtation
     */
    static Logger logger = Logger.getLogger("graph2MIF");

    /**
     * Keep the global experiemnt definition.
     */
    private Element globalExperimentList;

    /**
     * Distinct set of experiment AC
     */
    private HashSet globalExperiments;

    /**
     * Keep the global interactor definition.
     */
    private Element globalInteractorList;

    /**
     * Distinct set of Interactor AC
     */
    private HashSet globalInteractors;


    /**
     * The general DOM-Object accessible for all methods is required for creating Elements
     */
    private Document doc;

    /**
     * This Boolean indicates if an exception will be thrown if a required tag of MIF is missing
     * at the IntAct graph
     */
    private boolean STRICT_MIF;

    /**
     * Contructor without parameter - strict MIF will be produced or Exception will be thrown
     */
    public Graph2FoldedMIF() {
        this(new Boolean(true));
    }

    /**
     * Contructor with parameter
     * @param strictmif defines wether MIF should be strict produced or not.
     */
    public Graph2FoldedMIF(Boolean strictmif) {
        STRICT_MIF = strictmif.booleanValue();
    }

    /**
     * The only public function.
     * Calling this with a Graph object will give you a
     * complete DOM object according to PSI MIF specification, which you can easily can print out.
     * @param graph to convert to PSI-Format
     * @return DOM-Object, representing a XML Document in PSI-Format
     * @exception GraphNotConvertableException if PSIrequired Elements are missing within graph
     */
    public Document getMIF(Graph graph) throws GraphNotConvertableException {

        //initialise a DOM-level2 Document ...
        DOMImplementationImpl impl = new DOMImplementationImpl();
        //The root-element is entrySet ... with a lot of Attributes ...
        //DocumentType dt = impl.createDocumentType("baseelement", "SYSTEM", "MIF.xsd");
        doc = impl.createDocument("net:sf:psidev:mi", "entrySet", null);  //doctype only used by dtds !
        Element psiEntrySet = doc.getDocumentElement();
        psiEntrySet.setAttribute("xmlns", "net:sf:psidev:mi");
        psiEntrySet.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        psiEntrySet.setAttribute("xsi:schemaLocation", "net:sf:psidev:mi http://psidev.sourceforge.net/mi/xml/src/MIF.xsd");
        psiEntrySet.setAttribute("level", "1");
        psiEntrySet.setAttribute("version", "1");
        //Beginning to parse through graph
        //a graph represents only one entry at PSI, but PSI gives possibility for more than one.
        try {
            Element psiEntry = procGraph(graph);
            psiEntrySet.appendChild(psiEntry);
        } catch (ElementNotParseableError e) {
            logger.warn("could not proceed graph: " + e.getMessage());
            if (STRICT_MIF) {
                throw new GraphNotConvertableException(e.getMessage());
            }
        }
        //returning finished Dom-Document
        if (!doc.hasChildNodes()) {
            logger.warn("graph failed, no child elements.");
            throw new GraphNotConvertableException("doc has no Child Elements");
        }
        return doc;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // private methods
    // For every object there is a extra function, which proceeds it.
    // Every Funktion can throw a ElementNotparseableError and the above object has to determine
    // if these error is important or not


    /**
     * Start of processing the graph
     * @param graph to convert to PSI-Format
     * @return DOM-Object, representing a <entry>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Graph
     *
     */
    private Element procGraph(Graph graph) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiEntry = doc.createElement("entry");
        //local elements processing...
        //getId/Label are names
        Element psiSource = doc.createElement("source");
        psiEntry.appendChild(psiSource);
        try {
            Element psiNames = getPsiNamesOfBasicGraph(graph);
            psiSource.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.info("source/names failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        //releaseDate will be current date
        Calendar cal = Calendar.getInstance();
        //appending "0" for 1-9th day of a month
        String dayString;
        if( cal.get(Calendar.DAY_OF_MONTH) <= 9 ) {
            dayString = "0"+Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        } else {
            dayString = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
        }
        //appending "0" for 1-9th month of a year
        String monthString;
        int month = cal.get(Calendar.MONTH)+1;
        if( month <= 9 ) {
            monthString = "0"+month;
        } else {
            monthString = Integer.toString(month);
        }
        String date = cal.get(Calendar.YEAR) +
                "-" + monthString +
                "-" + dayString;
        psiSource.setAttribute("releaseDate", date);

        globalExperimentList = doc.createElement("experimentList");
        psiEntry.appendChild(globalExperimentList);

        globalInteractorList = doc.createElement("interactorList");
        psiEntry.appendChild(globalInteractorList);

        globalExperiments = new HashSet();
        globalInteractors = new HashSet();

        //getNodes() @todo
        //getEdges()
        try {
            Element psiInteractionList = procEdges(graph.getEdges());
            psiEntry.appendChild(psiInteractionList);
        } catch (ElementNotParseableError e) {
            logger.warn("edges failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Interaction without Interactions:" + e.getMessage());
            }
        }
        //returning result DOMObject
        if (!psiEntry.hasChildNodes()) {
            logger.warn("graph failed, no child elements.");
            throw new ElementNotParseableError("graph has no Child Elements");
        }
        return psiEntry;
    }


    /**
     * process edges of graph
     * @param edges to convert to PSI-Format
     * @return DOM-Object, representing a <interaction>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Edge
     */
    private Element procEdges(Collection edges) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiInteractionList = doc.createElement("interactionList");
        //local elements processing...
        Iterator iterator = edges.iterator();
        while (iterator.hasNext()) {
            try {
                Element psiInteraction = procEdge((Edge) iterator.next());
                psiInteractionList.appendChild(psiInteraction);
            } catch (ElementNotParseableError e) {
                logger.info("edge failed (not required):" + e.getMessage());
            } // not required here - so dont worry
        }
        //returning result DOMObject
        if (!psiInteractionList.hasChildNodes()) {
            logger.warn("edges failed, no child elements.");
            throw new ElementNotParseableError("InteractionList has no Child Elements");
        }
        return psiInteractionList;
    }


    /**
     * process edge
     * @param edge to convert to PSI-Format
     * @return DOM-Object, representing a <proteinInteractor>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Edge
     */
    private Element procEdge(Edge edge) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiInteraction = doc.createElement("interaction");
        //local elements processing...
        //getID and getLabel are names
        try {
            Element psiNames = getPsiNamesOfBasicGraph(edge);
            psiInteraction.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.info("names failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        //because we cannot directly get related Experiments, we access them from component1->interaction->experiments
        Collection relExperiments = edge.getComponent1().getInteraction().getExperiment();
        // TODO: what about the experiment of Component2 ?
        Element psiExperimentList = null;
        try {
            psiExperimentList = procExperiments(relExperiments);
            psiInteraction.appendChild(psiExperimentList);
        } catch (ElementNotParseableError e) {
            logger.warn("experiments failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Interactor without experiment:" + e.getMessage());
            }
        }
        //getComponent1/2 ...
        Element psiParticipantList = doc.createElement("participantList");
        psiInteraction.appendChild(psiParticipantList);
        try {
            Element psiProteinParticipant1 = procComponent(edge.getComponent1());
            psiParticipantList.appendChild(psiProteinParticipant1);
            Element psiProteinParticipant2 = procComponent(edge.getComponent2());
            psiParticipantList.appendChild(psiProteinParticipant2);
        } catch (ElementNotParseableError e) {
            logger.warn("compartment failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no 2 proteinParticipants in interaction:" + e.getMessage());
            }
        }

        // TODO: shouldn't it be in the experiment scope ?
        //because we cannot directly get related interactionTypes, we generate them out of related Experiments
        Collection relInteractionTypes = getRelInteractionTypes(relExperiments);
        Iterator relInteractionTypesIterator = relInteractionTypes.iterator();
        while (relInteractionTypesIterator.hasNext()) {
            CvInteraction cvInteraction = (CvInteraction) relInteractionTypesIterator.next();
            Element psiInteractionType = null;
            try {
                psiInteractionType = procCvInteractionType(cvInteraction);
                psiInteraction.appendChild(psiInteractionType);
            } catch (ElementNotParseableError e) {
                logger.info("interactionType failed (not required):" + e.getMessage());
            } // not one is required - so dont worry
        }
        //because we cannot directly get Xref , we get it this way
        try {
            Collection xrefs = edge.getComponent1().getInteraction().getXref();
            Element psiXref = procXrefCollection(xrefs);
            psiInteraction.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.info("xref failed (not required):" + e.getMessage());
        } catch (NullPointerException e) {
            logger.info("xref failed (not required):" + e.getMessage());
        } //dont worry - not required here
        //returning result DOMObject
        if (!psiInteraction.hasChildNodes()) {
            logger.warn("edge failed, no child elements.");
            throw new ElementNotParseableError("Interaction has no Child Elements");
        }
        return psiInteraction;
    }



    /**
     * process experiments
     * @param experiments to convert to PSI-Format
     * @return DOM-Object, representing a <experimentList>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Experiment
     */
    private Element procExperiments(Collection experiments) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiExperimentList = doc.createElement("experimentList");
        //local elements processing...
        Iterator experimentList = experiments.iterator();
        if (!experimentList.hasNext()) {
            logger.warn("experimentlist without one experiment -  failed (required):");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("not one experiment");
            }
        }
        while (experimentList.hasNext()) {
            Experiment experiment = (Experiment) experimentList.next();
            try {
                if ( ! globalExperiments.contains( experiment.getAc() )) {
                    Element psiExperimentDescription = procExperiment(experiment);
                    globalExperimentList.appendChild(psiExperimentDescription);
                    globalExperiments.add( experiment.getAc() );
                }
                Element psiExperimentRef = procExperimentRef(experiment);
                psiExperimentList.appendChild( psiExperimentRef );
            } catch (ElementNotParseableError e) {
                logger.info("experiment failed (not required):" + e.getMessage());
            } //not required - so dont worry
        }
        //returning result DOMObject
        if (!psiExperimentList.hasChildNodes()) {
            logger.warn("experiments failed, no child elements.");
            throw new ElementNotParseableError("ExperimentList has no Child Elements");
        }
        return psiExperimentList;
    }

    /**
     * process experiment
     * @param experiment to convert to PSI-Format
     * @return DOM-Object, representing a <exteriment>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Experiment
     */
    private Element procExperimentRef(Experiment experiment) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiExperimentRef = doc.createElement("experimentRef");
        try {
            psiExperimentRef.setAttribute("ref", experiment.getAc());
        } catch (NullPointerException e) {
            logger.warn("Ac of Experiment failed - required !");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Experiment without Ac:" + e.getMessage());
            }
        }
        return psiExperimentRef;
    }

    /**
     * process experiment
     * @param experiment to convert to PSI-Format
     * @return DOM-Object, representing a <exteriment>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Experiment
     */
    private Element procExperiment(Experiment experiment) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiExperimentDescription = doc.createElement("experimentDescription");
        //local elements processing...
        //getFullName & getShortLabel
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(experiment);
            psiExperimentDescription.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.info("names failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        // getXref
        try {
            Element psiXrefPubMed = procXrefCollectionSelectingPubMed(experiment.getXref());
            Element psiBibref = doc.createElement("bibref");
            psiBibref.appendChild(psiXrefPubMed);
            psiExperimentDescription.appendChild(psiBibref);
        } catch (ElementNotParseableError e) {
            logger.info("xref(pubmed) failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        try {
            Element psiXrefNotPubMed = procXrefCollectionSelectingNotPubMed(experiment.getXref());
            psiExperimentDescription.appendChild(psiXrefNotPubMed);
        } catch (ElementNotParseableError e) {
            logger.info("xref(not pubmed) failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        // getBioSource()
        try {
            Element psiHostOrganism = procBioSourceAsHost(experiment.getBioSource());
            psiExperimentDescription.appendChild(psiHostOrganism);
        } catch (ElementNotParseableError e) {
            logger.info("biosource failed (not required):" + e.getMessage());
        } // not required here - so dont worry
        // getBioSourceAc() @todo
        // getCvIdentificationAc() @todo
        // getCvInteraction() is interactiondetection
        try {
            Element psiInteractionDetection = procCvInteractionDetection(experiment.getCvInteraction());
            psiExperimentDescription.appendChild(psiInteractionDetection);
        } catch (ElementNotParseableError e) {
            logger.warn("cvInteraction failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no interactionDetection created::" + e.getMessage());
            }
        }
        // getCvIdentification()
        try {
            Element psiIdentification = procCvIdentification(experiment.getCvIdentification());
            psiExperimentDescription.appendChild(psiIdentification);
        } catch (ElementNotParseableError e) {
            logger.info("cvIdentification failed (not required):" + e.getMessage());
        } // not required here - so dont worry
        //
        // PSI - FeatureDetection is IntAct cvFeatureIdentification
        // Due to mapping problems this is not yet implemented
        // In IntAct a Feature is not part of Experiement so retireval would
        // be a Collection Experiement->Interaction->Component->Feature->cvFeatureIdentification
        // and than one has do decide which one should be represented in PSI ...
        // Feature isn't implemnted in PSI so we do not care
        //
        // getCvInteractionAc() @todo
        // getInteraction() @todo
        // getRelatedExperiment() @todo
        // getRelatedExperimentAc() @todo
        // getAnnotation @todo
        // getCurator @todo
        // getCuratorAc @todo
        // getMenuList @todo
        // getReference @todo
        // getAc
        try {
            psiExperimentDescription.setAttribute("id", experiment.getAc());
        } catch (NullPointerException e) {
            logger.warn("Ac of Experiment failed - required !");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Experiment without Ac:" + e.getMessage());
            }
        }
        // getCreated @todo
        // getEvidence - not defined
        // getOwner @todo
        // getOwnerAc @todo
        // getUpdated  @todo
        //returning result DOMObject
        if (!psiExperimentDescription.hasChildNodes()) {
            logger.warn("Experiment failed, no child elements.");
            throw new ElementNotParseableError("ExperimentDescription has no Child Elements");
        }
        return psiExperimentDescription;
    }

    /**
     * process cvInteractionDetection
     * @param cvInteractionDetection to convert to PSI-Format
     * @return DOM-Object, representing a <interactionDetection>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see CvInteraction
     */
    private Element procCvInteractionDetection(CvInteraction cvInteractionDetection)
            throws ElementNotParseableError {
        //generate DOM-Element
        Element psiInteractionDetection = doc.createElement("interactionDetection");
        //local elements processing...
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(cvInteractionDetection);
            psiInteractionDetection.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.warn("names failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteractionDetection ShortLabel");
            }
        }
        try {
            Element psiXref = procXrefCollection(cvInteractionDetection.getXref());
            psiInteractionDetection.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteractionDetection Xref");
            }
        } catch (NullPointerException e) {
            if (STRICT_MIF) {
                logger.warn("xref failed (required):" + e.getMessage());
                throw new ElementNotParseableError("no cvInteractionDetection Xref");
            }
        }
        //returning result DOMObject
        if (!psiInteractionDetection.hasChildNodes()) {
            logger.warn("cvInteractionDetection failed, no child elements.");
            throw new ElementNotParseableError("interactionDetection has no Child Elements");
        }
        return psiInteractionDetection;
    }

    /**
     * process cvInteractionType
     * @param cvInteractionType to convert to PSI-Format
     * @return DOM-Object, representing a <interactionDetection>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see CvInteraction
     */
    private Element procCvInteractionType(CvInteraction cvInteractionType) throws ElementNotParseableError {
       /* TODO: could be factorized with the procCvInteractionDetection if
          TODO: the name of the node is given in parameter.
        */
        //generate DOM-Element
        Element psiInteractionDetection = doc.createElement("interactionType");
        //local elements processing...
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(cvInteractionType);
            psiInteractionDetection.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.warn("names failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteractionType ShortLabel");
            }
        }
        try {
            Element psiXref = procXrefCollection(cvInteractionType.getXref());
            psiInteractionDetection.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteractionType Xref");
            }
        } catch (NullPointerException e) {
            if (STRICT_MIF) {
                logger.warn("xref failed (required):" + e.getMessage());
                throw new ElementNotParseableError("no cvInteractionType Xref");
            }
        }
        //returning result DOMObject
        if (!psiInteractionDetection.hasChildNodes()) {
            logger.warn("cvInteractionType failed, no child elements.");
            throw new ElementNotParseableError("interactionDetection has no Child Elements");
        }
        return psiInteractionDetection;
    }

    /**
     * process cvIdentification
     * @param cvIdentification to convert to PSI-Format
     * @return DOM-Object, representing a <participantDetection>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see CvIdentification
     */
    private Element procCvIdentification(CvIdentification cvIdentification) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiParticipantDetection = doc.createElement("participantDetection");
        //local elements processing...
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(cvIdentification);
            psiParticipantDetection.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.warn("names failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteraction ShortLabel");
            }
        }
        try {
            Element psiXref = procXrefCollection(cvIdentification.getXref());
            psiParticipantDetection.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteraction Xref");
            }
        } catch (NullPointerException e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvInteraction Xref");
            }
        }
        //returning result DOMObject
        if (!psiParticipantDetection.hasChildNodes()) {
            logger.warn("cvIdentification failed, no child elements.");
            throw new ElementNotParseableError("ParticipantDetection has no Child Elements");
        }
        return psiParticipantDetection;
    }

    /**
     * process component
     * @param component to convert to PSI-Format
     * @return DOM-Object, representing a <proteinParticipant>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Component
     */
    private Element procComponent(Component component) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiProteinParticipant = doc.createElement("proteinParticipant");
        //local elements processing...
        //getInteractor()
        try {
            Interactor interactor = component.getInteractor();
            if ( ! globalInteractors.contains( interactor.getAc() ) ) {
                Element psiProteinInteractor = procInteractor(interactor);
                globalInteractorList.appendChild( psiProteinInteractor );
                globalInteractors.add( interactor.getAc() );
            }

            Element psiProteinInteractorRef = procInteractorRef( interactor );
            psiProteinParticipant.appendChild( psiProteinInteractorRef );
        } catch (ElementNotParseableError e) {
            logger.warn("interactor failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no interactor:" + e.getMessage());
            }
        }
        //getBindingDomain
        try {
            Element psiFeatureList = procFeatureList(component.getBindingDomain());
            psiProteinParticipant.appendChild(psiFeatureList);
        } catch (ElementNotParseableError e) {
            logger.info("featureList failed (not required):" + e.getMessage());
        } //not required here - dont worry
        //getcvComponentRole
        try {
            Element psiRole = procRole(component.getCvComponentRole());
            psiProteinParticipant.appendChild(psiRole);
        } catch (NullPointerException e) {
            logger.info("cvComponentRole failed (not required):" + e.getMessage());
        } //not required here - dont worry
        // getCvComponentRoleAc() @todo
        // getExpressedIn() @todo
        // getExpressedInAc() @todo
        // getInteraction() @todo
        // getInteractionAc() @todo
        // getInteractorAc() @todo
        // getStoichiometry()  @todo
        // getCreated @todo
        // getEvidence @todo
        // getOwner @todo
        // getOwnerAc @todo
        // getUpdated @todo
        //returning result DOMObject
        if (!psiProteinParticipant.hasChildNodes()) {
            logger.warn("component failed, no child elements.");
            throw new ElementNotParseableError("proteinParticipant has no Child Elements");
        }
        return psiProteinParticipant;
    }

    /**
     * process role
     * @param cvComponentRole to convert to PSI-Format
     * @return DOM-Object, representing a <featureList>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see CvComponentRole
     */
    private Element procRole(CvComponentRole cvComponentRole) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiRole = doc.createElement("role");
        //local elements processing...
        try {
            psiRole.appendChild(doc.createTextNode(cvComponentRole.getShortLabel()));
        } catch (NullPointerException e) {
            logger.warn("shortLabel failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("role without shortLabel");
            }
        }
        //returning result DOMObject
        if (!psiRole.hasChildNodes()) {
            logger.warn("cvComponentRole failed, no child elements.");
            throw new ElementNotParseableError("role has no Child Elements");
        }
        return psiRole;
    }

    /**
     * process FeatureList
     * @param features to convert to PSI-Format
     * @return DOM-Object, representing a <featureList>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Feature
     */
    private Element procFeatureList(Collection features) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiFeatureList = doc.createElement("featureList");
        //local elements processing...
        Iterator featureList = features.iterator();
        if (!featureList.hasNext()) {
            logger.warn("empty FeatureList failed (required):");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no features found");
            }
        }
        while (featureList.hasNext()) {
            Feature feature = (Feature) featureList.next();
            try {
                Element psiFeature = procFeature(feature);
                psiFeatureList.appendChild(psiFeature);
            } catch (ElementNotParseableError e) {
                logger.info("feature failed (not required):" + e.getMessage());
            } // not required here - so dont worry
        }
        //returning result DOMObject
        if (!psiFeatureList.hasChildNodes()) {
            logger.warn("featurelist failed, no child elements.");
            throw new ElementNotParseableError("FeatureList has no Child Elements");
        }
        return psiFeatureList;
    }

    /**
     * process Feature
     * @param feature to convert to PSI-Format
     * @return DOM-Object, representing a <featureList>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Feature
     */
    private Element procFeature(Feature feature) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiFeature = doc.createElement("feature");
        //local elements processing...
        // getBoundDomain() @todo
        // getBoundDomainAc() @todo
        // getComponent() @todo
        // getComponentAc() @todo
        // getCvFeatureIdentificationAc() @todo
        // getProtein() @todo
        // getProteinAc() @todo
        // getXref()
        try {
            Element psiXref = procXref(feature.getXref());
            psiFeature.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.info("xref failed (not required):" + e.getMessage());
        }  //not required here - so dont worry
        // getCvFeatureType()
        try {
            Element psiFeatureDescription = procCvFeatureType(feature.getCvFeatureType());
            psiFeature.appendChild(psiFeatureDescription);
        } catch (ElementNotParseableError e) {
            logger.info("cvFeatureTyoe failed (not required):" + e.getMessage());
        }  //not required here - so dont worry
        // getCvFeatureIdentification()
        try {
            Element psiFeatureDetection = procCvFeatureIdentification(feature.getCvFeatureIdentification());
            psiFeature.appendChild(psiFeatureDetection);
        } catch (ElementNotParseableError e) {
            logger.info("cvFeatureIdentification failed (not required):" + e.getMessage());
        }  //not required here - so dont worry
        // psi Location is not yet implemented - but required - so we have to throw an exception anyway
        try {
            Element psiLocation = procLocation();
            psiFeature.appendChild(psiLocation);
        } catch (ElementNotParseableError e) {
            logger.warn("location failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Feature-Location cant created:" + e.getMessage());
            }
        }
        // getRange()  @todo
        // getXrefAc() @todo
        // getAc @todo
        // getCreated @todo
        // getEvidence
        // getOwner @todo
        // getOwnerAc @todo
        //  getUpdated @todo
        //returning result DOMObject
        if (!psiFeature.hasChildNodes()) {
            logger.warn("feature failed, no child elements.");
            throw new ElementNotParseableError("Feature has no Child Elements");
        }
        return psiFeature;
    }

    /**
     * process Location - which is not yet implemented in IntAct
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Feature (Binding Domain)
     */
    private Element procLocation() throws ElementNotParseableError {
        logger.warn("location failed (required): NOT IMPLEMENTED");
        throw new ElementNotParseableError("not implemented in IntAct");
    }

    /**
     * process cvFeatureType
     * @param cvFeatureType to convert to PSI-Format
     * @return DOM-Object, representing a <featureDescription>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see CvFeatureType
     */
    private Element procCvFeatureType(CvFeatureType cvFeatureType) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiFeatureDescription = doc.createElement("featureDescription");
        //local elements processing...
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(cvFeatureType);
            psiFeatureDescription.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.warn("names failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvFeatureType ShortLabel");
            }
        }
        try {
            Element psiXref = procXrefCollection(cvFeatureType.getXref());
            psiFeatureDescription.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvFeatureType Xref");
            }
        } catch (NullPointerException e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvFeatureType Xref");
            }
        }
        //returning result DOMObject
        if (!psiFeatureDescription.hasChildNodes()) {
            logger.warn("cvFeatureType failed, no child elements.");
            throw new ElementNotParseableError("FeatureDescription has no Child Elements");
        }
        return psiFeatureDescription;
    }

    /**
     * process cvFeatureIdentification
     * @param cvFeatureIdentification to convert to PSI-Format
     * @return DOM-Object, representing a <featureDescription>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see CvFeatureIdentification
     */
    private Element procCvFeatureIdentification(CvFeatureIdentification cvFeatureIdentification) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiFeatureDetection = doc.createElement("featureDetection");
        //local elements processing...
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(cvFeatureIdentification);
            psiFeatureDetection.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.warn("cvFeatureIdentification failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvFeatureIdentification ShortLabel");
            }
        }
        try {
            Element psiXref = procXrefCollection(cvFeatureIdentification.getXref());
            psiFeatureDetection.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvFeatureIdentification Xref");
            }
        } catch (NullPointerException e) {
            logger.warn("xref failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no cvFeatureIdentification Xref");
            }
        }
        //returning result DOMObject
        if (!psiFeatureDetection.hasChildNodes()) {
            logger.warn("cvFeatureIdentification failed, no child elements.");
            throw new ElementNotParseableError("FeatureDetection has no Child Elements");
        }
        return psiFeatureDetection;
    }

    /**
     * process interactor
     * @param interactor to convert to PSI-Format
     * @return DOM-Object, representing a <proteinInteractor>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Interactor
     */
    private Element procInteractorRef(Interactor interactor) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiProteinInteractorRef = doc.createElement("proteinInteractorRef");
        //local elements processing...
        try {
            psiProteinInteractorRef.setAttribute("ref", interactor.getAc());
        } catch (NullPointerException e) {
            logger.warn("Ac of interactor failed - required !");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Interactor without Ac:" + e.getMessage());
            }
        }
        //returning result DOMObject
        return psiProteinInteractorRef;
    }

    /**
     * process interactor
     * @param interactor to convert to PSI-Format
     * @return DOM-Object, representing a <proteinInteractor>
     * @exception ElementNotParseableError if PSIrequired Elements are missing within graph
     * @see Interactor
     */
    private Element procInteractor(Interactor interactor) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiProteinInteractor = doc.createElement("proteinInteractor");
        //local elements processing...
        //getBioSourceAc() @todo
        //getProduct() @todo
        //getAnnotation @todo
        //getCurator @todo
        //getCuratorAc @todo
        //getShortLabel, getFullName -> names
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(interactor);
            psiProteinInteractor.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.warn("names failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Interactor without name" + e.getMessage());
            }
        }
        //getMenuList @todo
        //getReference @todo
        //getXref
        try {
            Element psiXref = procXrefCollection(interactor.getXref());
            psiProteinInteractor.appendChild(psiXref);
        } catch (ElementNotParseableError e) {
            logger.info("xref failed (not required):" + e.getMessage());
        }  //not required here - so dont worry
        //getAc @todo
        try {
            psiProteinInteractor.setAttribute("id", interactor.getAc());
        } catch (NullPointerException e) {
            logger.warn("Ac of interactor failed - required !");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Interactor without Ac:" + e.getMessage());
            }
        }
        //getCreated @todo
        //getEvidence @todo
        //getOwner @todo
        //getOwnerAc @todo
        //getBioSource()
        try {
            Element psiOrganism = procBioSource(interactor.getBioSource());
            psiProteinInteractor.appendChild(psiOrganism);
        } catch (ElementNotParseableError e) {
            logger.info("BioSource failed (not required):" + e.getMessage());
        } // not required here - so dont worry
        //sequence is not directly accessible ...
        try {
            Element psiSequence = procSequence((Protein) interactor);
            psiProteinInteractor.appendChild(psiSequence);
        } catch (ElementNotParseableError e) {
            logger.info("sequence failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        //returning result DOMObject
        if (!psiProteinInteractor.hasChildNodes()) {
            logger.warn("Interactor failed, no child elements.");
            throw new ElementNotParseableError("ProteinInteractor has no Child Elements");
        }
        return psiProteinInteractor;
    }

    /**
     * processing protein
     * @param protein - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <sequence>
     * @exception  ElementNotParseableError if it is not defined
     * @see Protein
     */
    private Element procSequence(Protein protein) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiSequence = doc.createElement("sequence");
        //local elements processing...
        try {
            psiSequence.appendChild(doc.createTextNode(protein.getSequence()));
        } catch (NullPointerException e) {
            logger.warn("sequence failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("no sequence in protein");
            }
        }
        ;
        //returning result DOMObject
        if (!psiSequence.hasChildNodes()) {
            logger.warn("protein failed, no child elements.");
            throw new ElementNotParseableError("Sequence has no Child Elements");
        }
        return psiSequence;
    }


    /**
     * This method gets an Collection of Xfres and, beacause PSI often does not allow a list of xrefs,
     * takes the 1st primary ref as psiPrimaryRef and all others as secondary Refs.
     * if no SPTR xref is found, make the first one the primary Xref.
     * This SPTR Xref is only valid for Interactor, however that function is used also with :
     * CvInteraction
     * CvIdentification
     * CvFeature
     * cvFeatureIdentification
     * Interactor
     * Interaction
     *
     * @param xrefs - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <xref>
     * @exception  ElementNotParseableError if it is not defined
     * @see Xref
     */
    private Element procXrefCollection(Collection xrefs) throws ElementNotParseableError {
        // keep track of the existence of a primary Xref
        Xref primaryXref = null;

        //generate DOM-Element
        Element psiXref = doc.createElement("xref");
        //local elements processing...
        Iterator iteratorPrim = xrefs.iterator();
        //the first SPTR Intact Xref will become primary & secondary ref in PSI
        while (iteratorPrim.hasNext()) {
            Xref xref = (Xref) iteratorPrim.next();
            try {
                if ((xref.cvDatabase.getShortLabel().equalsIgnoreCase("SPTR") && xref.getPrimaryId() != null) //SPTR found
                        || !iteratorPrim.hasNext()) { //if no SPTR found, take any (here we take last)
                    Element psiPrimaryRef = procPrimaryRef(xref);
                    psiXref.appendChild(psiPrimaryRef);
                    primaryXref = xref;
                    break;
                }
            } catch (NullPointerException e) {    //dont worry - try next one
                logger.warn("Xref without primary db or id found ! Ignoring !");
            } catch (ElementNotParseableError e) { //dont worry - try next one
                logger.warn("Xref without primary db or id found ! Ignoring !");
            }
        }

        //if we could not create a primaryID - throw exception
//        if (!psiXref.hasChildNodes()) {
//            logger.warn("xrefCollection failed, no child element as primary.");
//            throw new ElementNotParseableError("couldn't generate primaryID - Xref not parseabel");
//        }


        //the rest becomes secondary Refs
        Iterator iteratorSnd = xrefs.iterator();
        while (iteratorSnd.hasNext()) {
            Xref xref = (Xref) iteratorSnd.next();
            try {
                Element psiRef = null;
                if ( primaryXref == null )
                    psiRef = procPrimaryRef( xref );
                else {
                    if (primaryXref == xref) break; // don't put the primary as secondary !
                    psiRef = procSecondaryRef( xref );
                }

                psiXref.appendChild(psiRef);
            } catch (NullPointerException e) {
                logger.warn("Xref without primary db or id found ! Ignoring !"); // not required here - so dont worry
            } catch (ElementNotParseableError e) {
                logger.warn("Xref without primary db or id found ! Ignoring !"); // not required here - so dont worry
            }
        }
        //returning result DOMObject
        if (!psiXref.hasChildNodes()) {
            logger.warn("xrefcollection failed, no child elements.");
            throw new ElementNotParseableError("Xref has no Child Elements");
        }
        return psiXref;
    }

    /**
     * This method gets a Xref and convert it to xref with one primaryRef
     * @param xref - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <xref>
     * @exception  ElementNotParseableError if it is not defined
     * @see Xref
     */
    private Element procXref(Xref xref) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiXref = doc.createElement("xref");
        //local elements processing...
        try {
            Element psiPrimaryRef = procPrimaryRef(xref);
            psiXref.appendChild(psiPrimaryRef);
        } catch (ElementNotParseableError e) {
            logger.warn("Xref without primary db or id found ! Ignoring !");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("");
            }
        }
        //returning result DOMObject
        if (!psiXref.hasChildNodes()) {
            logger.warn("xref failed, no child elements.");
            throw new ElementNotParseableError("Xref has no Child Elements");
        }
        return psiXref;
    }

    /**
     * This method gets a collection of Xfres and, beacause PSI often does not allow a list of xrefs,
     * takes the 1st primary ref as psiPrimaryRef,  and all others as secondary Refs.
     * But only from PubMed-DB will be processed.
     * @param xrefs - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <xref>
     * @exception  ElementNotParseableError if it is not defined
     * @see Xref
     */
    private Element procXrefCollectionSelectingPubMed(Collection xrefs) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiXref = doc.createElement("xref");
        //local elements processing...
        Iterator iteratorPrim = xrefs.iterator();
        while (iteratorPrim.hasNext()) {
            Xref xref = (Xref) iteratorPrim.next();
            try {
                if ((xref.cvDatabase.getShortLabel().equalsIgnoreCase("PubMed") && xref.getPrimaryId() != null)) { //PubMed found
                    Element psiPrimaryRef = procPrimaryRef(xref);
                    psiXref.appendChild(psiPrimaryRef);
                    xrefs.remove(xref); //this was already processed
                    break;
                }
            } catch (NullPointerException e) {    //dont worry - try next one
                logger.warn("Xref without primary db or id found ! Ignoring !");
            } catch (ElementNotParseableError e) { //dont worry - try next one
                logger.warn("Xref without primary db or id found ! Ignoring !");
            }

        }
        //if we could not create a primaryID - throw exception
        if (!psiXref.hasChildNodes()) {
            logger.warn("xrefcollection failed, no child elements.");
            throw new ElementNotParseableError("couldn't generate primaryID - Xref not parseabel");
        }
        //the rest becomes secondary Refs
        Iterator iteratorSnd = xrefs.iterator();
        while (iteratorSnd.hasNext()) {
            Xref xref = (Xref) iteratorSnd.next();
            try {
                if ((xref.cvDatabase.getShortLabel().equalsIgnoreCase("PubMed") && xref.getPrimaryId() != null)) { //PubMed found
                    Element psiSecondaryRef = procSecondaryRef(xref);
                    psiXref.appendChild(psiSecondaryRef);
                }
            } catch (NullPointerException e) {
                logger.warn("Xref without primary db or id found ! Ignoring !"); // not required here - so dont worry
            } catch (ElementNotParseableError e) {
                logger.warn("Xref without primary db or id found ! Ignoring !"); // not required here - so dont worry
            }

        }
        //returning result DOMObject
        if (!psiXref.hasChildNodes()) {
            logger.warn("xrefcol failed, no child elements.");
            throw new ElementNotParseableError("Xref has no Child Elements");
        }
        return psiXref;
    }


    /**
     * This method gets an Collection of Xfres and, beacause PSI often does not allow a list of xrefs,
     * takes the 1st primary ref as psiPrimaryRef,  and all others as secondary Refs.
     * But only from NOT PubMed-DB will be processed.
     * @param xrefs - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <xref>
     * @exception  ElementNotParseableError if it is not defined
     * @see Xref
     */
    private Element procXrefCollectionSelectingNotPubMed(Collection xrefs) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiXref = doc.createElement("xref");
        //local elements processing...
        Iterator iteratorPrim = xrefs.iterator();
        while (iteratorPrim.hasNext()) {
            Xref xref = (Xref) iteratorPrim.next();
            try {
                if (!(xref.cvDatabase.getShortLabel().equalsIgnoreCase("PubMed") && xref.getPrimaryId() != null)) { //PubMed found
                    Element psiPrimaryRef = procPrimaryRef(xref);
                    psiXref.appendChild(psiPrimaryRef);
                    xrefs.remove(xref); //this was already processed
                    break;
                }
            } catch (NullPointerException e) {    //dont worry - try next one
                logger.warn("Xref without primary db or id found ! Ignoring !");
            } catch (ElementNotParseableError e) { //dont worry - try next one
                logger.warn("Xref without primary db or id found ! Ignoring !");
            }

        }
        //if we could not create a primaryID - throw exception
        if (!psiXref.hasChildNodes()) {
            logger.warn("xrefcol failed, no child elements.");
            throw new ElementNotParseableError("couldn't generate primaryID - Xref not parseabel");
        }
        //the rest becomes secondary Refs
        Iterator iteratorSnd = xrefs.iterator();
        while (iteratorSnd.hasNext()) {
            Xref xref = (Xref) iteratorSnd.next();
            try {
                if (!(xref.cvDatabase.getShortLabel().equalsIgnoreCase("PubMed") && xref.getPrimaryId() != null)) { //PubMed found
                    Element psiSecondaryRef = procSecondaryRef(xref);
                    psiXref.appendChild(psiSecondaryRef);
                }
            } catch (NullPointerException e) {
                logger.warn("Xref without primary db or id found ! Ignoring !"); // not required here - so dont worry
            } catch (ElementNotParseableError e) {
                logger.warn("Xref without primary db or id found ! Ignoring !"); // not required here - so dont worry
            }

        }
        //returning result DOMObject
        if (!psiXref.hasChildNodes()) {
            logger.warn("xrefcol failed, no child elements.");
            throw new ElementNotParseableError("Xref has no Child Elements");
        }
        return psiXref;
    }

    /**
     * This method gets a Xref and returns a PSI PrimaryRef.
     * @param xref - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <primaryRef>
     * @see Xref
     */
    private Element procPrimaryRef(Xref xref) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiPrimaryRef = doc.createElement("primaryRef");
        //local elements processing...
        try {
            psiPrimaryRef.setAttribute("db", xref.cvDatabase.getShortLabel());
            psiPrimaryRef.setAttribute("id", xref.getPrimaryId());
        } catch (NullPointerException e) {
            logger.warn("xref without db or id - failed (required):");
            throw new ElementNotParseableError("PrimaryRef without id or db");
        }
        try {
            psiPrimaryRef.setAttribute("secondary", xref.getSecondaryId());
        } catch (NullPointerException e) {
            logger.info("no secondaryRef");
        } // not required here - so dont worry
        try {
            psiPrimaryRef.setAttribute("version", xref.getDbRelease());
        } catch (NullPointerException e) {
            logger.info("no dbRelease");
        } // not required here - so dont worry

        return psiPrimaryRef;
    }

    /**
     * This method gets a Xref and return a PSI SecondaryRef.
     * @param xref - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <secondaryRef>
     * @see Xref
     */
    private Element procSecondaryRef(Xref xref) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiSecondaryRef = doc.createElement("secondaryRef");
        //local elements processing...
        try {
            psiSecondaryRef.setAttribute("db", xref.cvDatabase.getShortLabel());
            psiSecondaryRef.setAttribute("id", xref.getPrimaryId());
        } catch (NullPointerException e) {
            logger.warn("xref without db or id - failed (required):");
            throw new ElementNotParseableError("SecondaryRef without id or db");
        }
        try {
            psiSecondaryRef.setAttribute("secondary", xref.getSecondaryId());
        } catch (NullPointerException e) {
            logger.info("no secondaryId");
        } // not required here - so dont worry
        try {
            psiSecondaryRef.setAttribute("version", xref.getDbRelease());
        } catch (NullPointerException e) {
            logger.info("no dbRelease");
        } // not required here - so dont worry

        return psiSecondaryRef;
    }


////////////////THIS method th mainly the same like  procBioSource down ...psiElement diffs !
    /**
     * process bioSource
     * @param bioSource to convert to PSI-Format
     * @return DOM-Object, representing a <hostOrganism>
     * @exception  ElementNotParseableError if it is not defined
     * @see BioSource
     */
    private Element procBioSourceAsHost(BioSource bioSource) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiOrganism = doc.createElement("hostOrganism");
        //local elements processing...
        //getShortLabel & getFullName are oragnism/names
        try {
            Element psiNames = getPsiNamesOfAnnotatedObject(bioSource);
            psiOrganism.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.info("names failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        //       getCvCellCycle()   @todo
        //       getCvCellCycleAc() @todo
        //       getCvCellType()
        try {
            Element psiCellType = procCvCelltype(bioSource.getCvCellType());
            psiOrganism.appendChild(psiCellType);
        } catch (ElementNotParseableError e) {
            logger.info("cvCellType failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        catch (NullPointerException e) {
            logger.info("cvCellType failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        //       getCvCellTypeAc()  @todo
        //       getCvCompartment()
        try {
            Element psiCompartment = procCvCompartment(bioSource.getCvCompartment());
            psiOrganism.appendChild(psiCompartment);
        } catch (ElementNotParseableError e) {
            logger.info("cvCompartment failed (not required):" + e.getMessage());
        }  //not required here - so dont worry
        catch (NullPointerException e) {
            logger.info("cvCompartment failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        //       getCvCompartmentAc() @todo
        //       getCvDevelopmentalStage() @todo
        //       getCvDevelopmentalStageAc()    @todo
        //       getCvTissue()
        try {
            Element psiTissue = procCvTissue(bioSource.getCvTissue());
            psiOrganism.appendChild(psiTissue);
        } catch (ElementNotParseableError e) {
            logger.info("cvTissue failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        catch (NullPointerException e) {
            logger.info(" failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        //       getCvTissueAc() @todo
        //       getTaxId()
        try {
            psiOrganism.setAttribute("ncbiTaxId", bioSource.getTaxId());
        } catch (NullPointerException e) {
            logger.warn("ncbiTaxID failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("organism without Taxid found");
            }
        }
        //       getAnnotation @todo
        //       getCurator @todo
        //       getCuratorAc @todo
        //       getMenuList @todo
        //       getReference @todo
        //       getXref @todo
        //       getCreated @todo
        //       getEvidence @todo
        //       getOwner @todo
        //       getOwnerAc @todo
        //       getUpdated @todo
        //returning result DOMObject
        if (!psiOrganism.hasChildNodes()) {
            logger.warn("bioSource failed, no child elements.");
            throw new ElementNotParseableError("Organism has no Child Elements");
        }
        return psiOrganism;
    }


    ////////////////THIS method th mainly the same like  procBioSourceAsHost above ...psiElement diffs !
    /**
     * process bioSource
     * @param bioSource to convert to PSI-Format
     * @return DOM-Object, representing a <organism>
     * @exception  ElementNotParseableError if it is not defined
     * @see Xref
     */
    private Element procBioSource(BioSource bioSource) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiOrganism = doc.createElement("organism");
        //getShortLabel & getFullName are organism/names
        Element psiNames = null;
        try {
            psiNames = getPsiNamesOfAnnotatedObject(bioSource);
            psiOrganism.appendChild(psiNames);
        } catch (ElementNotParseableError e) {
            logger.info("names failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        //       getCvCellCycle()   @todo
        //       getCvCellCycleAc() @todo
        //       getCvCellType()
        try {
            Element psiCellType = procCvCelltype(bioSource.getCvCellType());
            psiOrganism.appendChild(psiCellType);
        } catch (ElementNotParseableError e) {
            logger.info("cvCelltype failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        catch (NullPointerException e) {
            logger.info("cvCelltype failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        //       getCvCellTypeAc()  @todo
        //       getCvCompartment()
        try {
            Element psiCompartment = procCvCompartment(bioSource.getCvCompartment());
            psiOrganism.appendChild(psiCompartment);
        } catch (ElementNotParseableError e) {
            logger.info("cvCompartment failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        catch (NullPointerException e) {
            logger.info("cvCompartment failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        //       getCvDevelopmentalStage() @todo
        //       getCvDevelopmentalStageAc()    @todo
        //       getCvTissue()
        try {
            Element psiTissue = procCvTissue(bioSource.getCvTissue());
            psiOrganism.appendChild(psiTissue);
        } catch (ElementNotParseableError e) {
            logger.info("cvTissue failed (not required):" + e.getMessage());
        } //not required here - so dont worry
        catch (NullPointerException e) {
            logger.info("cvTissue failed (not required):" + e.getMessage());
        }//not required here - so dont worry
        //       getCvTissueAc() @todo
        //       getTaxId()
        try {
            psiOrganism.setAttribute("ncbiTaxId", bioSource.getTaxId());
        } catch (NullPointerException e) {
            logger.warn("ncbiTaxID failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("organism without Taxid found");
            }
        }
        //       getAnnotation @todo
        //       getCurator @todo
        //       getCuratorAc @todo
        //       getMenuList @todo
        //       getReference @todo
        //       getXref @todo
        //       getCreated @todo
        //       getEvidence @todo
        //       getOwner @todo
        //       getOwnerAc @todo
        //       getUpdated @todo
        //returning result DOMObject
        if (!psiOrganism.hasChildNodes()) {
            logger.warn("organism failed, no child elements.");
            throw new ElementNotParseableError("Organism has no Child Elements");
        }
        return psiOrganism;
    }


    /**
     * process cvCellType
     * @param cvCellType to convert to PSI-Format
     * @return DOM-Object, representing a <cellType>
     * @exception ElementNotParseableError if it is not defined
     * @see CvCellType
     */
    private Element procCvCelltype(CvCellType cvCellType) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiCellType = doc.createElement("cellType");
        //local elements processing...
        try {
            psiCellType.appendChild(doc.createTextNode(cvCellType.getShortLabel()));
        } catch (NullPointerException e) {
            logger.warn("cvCelltype failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("cellType not defined");
            }
        }
        //returning result DOMObject
        if (!psiCellType.hasChildNodes()) {
            logger.warn("cvCellType failed, no child elements.");
            throw new ElementNotParseableError("CellType has no Child Elements");
        }
        return psiCellType;
    }

    /**
     * process cvCompartment
     * @param cvCompartment to convert to PSI-Format
     * @return DOM-Object, representing a <compartment>
     * @exception  ElementNotParseableError if it is not defined
     * @see CvCompartment
     */
    private Element procCvCompartment(CvCompartment cvCompartment) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiCompartment = doc.createElement("compartment");
        //local elements processing...
        try {
            psiCompartment.appendChild(doc.createTextNode(cvCompartment.getShortLabel()));
        } catch (NullPointerException e) {
            logger.warn("cvCompartment failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Tissue not defined");
            }
        }
        //returning result DOMObject
        if (!psiCompartment.hasChildNodes()) {
            logger.warn("cvCompartment failed, no child elements.");
            throw new ElementNotParseableError("Compartment has no Child Elements");
        }
        return psiCompartment;
    }

    /**
     * process cvTissue
     * @param cvTissue to convert to PSI-Format
     * @return DOM-Object, representing a <tissue>
     * @exception  ElementNotParseableError if it is not defined
     * @see CvTissue
     */
    private Element procCvTissue(CvTissue cvTissue) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiTissue = doc.createElement("tissue");
        //local elements processing...
        try {
            psiTissue.appendChild(doc.createTextNode(cvTissue.getShortLabel()));
        } catch (NullPointerException e) {
            logger.warn("cvTissue failed (required):" + e.getMessage());
            if (STRICT_MIF) {
                throw new ElementNotParseableError("Tissue not defined");
            }
        }
        //returning result DOMObject
        if (!psiTissue.hasChildNodes()) {
            logger.warn("cvTissue failed, no child elements.");
            throw new ElementNotParseableError("Tissue has no Child Elements");
        }
        return psiTissue;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // helper methods
    // They differs from proc*-mothods in that way, that they get a basic object and processing only a
    // part of this object - they are only for reduce redundancy !

    /**
     * This method gets an BasicGraph Object (like node,edge ...) and will return a Element names,
     * while getting shortLabel as getID() and fullName as getLabel()
     * @param basicgraph - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <names>
     * @exception  ElementNotParseableError if it is not defined
     * @see BasicObject
     */
    private Element getPsiNamesOfBasicGraph(BasicGraph basicgraph) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiNames = doc.createElement("names");
        //local elements processing...
        try {
            if (basicgraph.getLabel() != null) {
                //getLabel is names/shortLabel
                Element psiShortLabel = doc.createElement("shortLabel");
                psiShortLabel.appendChild(doc.createTextNode(basicgraph.getLabel()));
                psiNames.appendChild(psiShortLabel);
                //names/fullName undef
            } else {
                logger.warn("names failed (required): no shortLabel");
                if (STRICT_MIF) {
                    throw new ElementNotParseableError("No shortlabel for Name found");
                }
            }
        } catch (NullPointerException e) {
            if (STRICT_MIF) {
                logger.warn("names failed (required): no Shortlabel");
                throw new ElementNotParseableError("No shortlabel for Name found");
            }
        }
        //returning result DOMObject
        if (!psiNames.hasChildNodes()) {
            logger.warn("names failed, no child elements.");
            throw new ElementNotParseableError("Names has no Child Elements");
        }
        return psiNames;
    }

    /**
     * This method gets an AnnotatedObject and will return a Element names,
     * while getting shortLabel as getShortLabel() and fullName as getFullName()
     * @param annotatedObject - Object  to convert to PSI-Format
     * @return DOM-Object, representing a <names>
     * @exception ElementNotParseableError if it is not defined
     * @see AnnotatedObject
     */
    private Element getPsiNamesOfAnnotatedObject(AnnotatedObject annotatedObject) throws ElementNotParseableError {
        //generate DOM-Element
        Element psiNames = doc.createElement("names");
        //local elements processing...
        try {
            //getshortLabel
            if (annotatedObject.getShortLabel() != null) {
                Element psiShortLabel = doc.createElement("shortLabel");
                psiShortLabel.appendChild(doc.createTextNode(annotatedObject.getShortLabel()));
                psiNames.appendChild(psiShortLabel);
                //getFullName is names/fullName
            } else {
                logger.warn("names failed (required): no shortLabel");
                if (STRICT_MIF) {
                    throw new ElementNotParseableError("No shortlabel for Name found");
                }
            }
        } catch (NullPointerException e) {
            logger.warn("names failed (required): no shortLabel");
            if (STRICT_MIF) {
                throw new ElementNotParseableError("No shortlabel for Name found");
            }
        }
        try {
            if (annotatedObject.getFullName() != null) {
                Element psiFullName = doc.createElement("fullName");
                psiFullName.appendChild(doc.createTextNode(annotatedObject.getFullName()));
                psiNames.appendChild(psiFullName);
            }   //else: dont need a FullName
        } catch (NullPointerException e) {
        } // donty worry - dont need a FullName

        //returning result DOMObject
        if (!psiNames.hasChildNodes()) {
            logger.warn("names failed, no child elements.");
            throw new ElementNotParseableError("Names has no Child Elements");
        }

        return psiNames;
    }


    /**
     * WorkAround Method getRelInteractionTypes retrieves a Collection of all related InteractionTypes
     * of an Collection of Experiments
     * @param relExperiments where related InteractionTypes are returned for
     * @return Collection of Features
     * @see Feature
     */

    private Collection getRelInteractionTypes(Collection relExperiments) {
        // TODO: an HashSet would me more appropriate ? this is a distinct set of CvInteraction !
        Collection relInteractionTypes = new ArrayList( relExperiments.size() );
        Iterator relExperimentsIterator = relExperiments.iterator();
        while (relExperimentsIterator.hasNext()) {
            Experiment experiment = (Experiment) relExperimentsIterator.next();
            relInteractionTypes.add(experiment.getCvInteraction());
        }
        return relInteractionTypes;
    }
}