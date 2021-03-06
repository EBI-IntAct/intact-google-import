package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for assigning IMEx id to a publication and updating intact publications, experiments and interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public interface IntactImexAssigner {

    /**
     * Assign a new IMEx id to a publication that has not been assigned yet and update/clean up the annotations of the publication (full coverage and imex curation). It adds an IMEx primary reference
     * to the intact publication. It is only possible to assign a new IMEx id to publications having valid pubmed ids (no unassigned and no DOI number)
     * @param intactPublication : the publication in IntAct
     * @param imexPublication : the publication in IMEx
     * @return the imex id if the IMEx assigner was successful, null otherwise
     * @throws ImexCentralException if no record found or no IMEx id
     */
    public String assignImexIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws ImexCentralException;

    /**
     * Add a IMEx primary reference to the experiment
     * @param intactExperiment
     * @param imexId
     * @return true if the IMEx assigner did an update of the experiment, false otherwise
     */
    public boolean updateImexIdentifierForExperiment(Experiment intactExperiment, String imexId);

    /**
     * Add a IMEx primary reference to the interaction
     * @param intactInteraction
     * @param imexId
     * @return true if the IMEx assigner did an update of the interaction, false otherwise
     */
    public boolean updateImexIdentifierForInteraction(Interaction intactInteraction, String imexId) ;

    /**
     * Copy the IMEx id to experiments of a publication if they don't have the IMEx primary reference. It will not add the IMEx primary reference if
     * there is already an IMEx primary reference to another IMEx id. It will clean up duplicated IMEx ids
     * @param expAcs : list of experiments acs
     * @param imexId
     * @param imexCentralManager ; to fire events if provided.
     * @param updatedExpAcs : the experiment acs which have been updated
     * @throws PublicationImexUpdaterException if IMEx id is null or imex conflict and no imexCentralManager was provided to fire an error event
     */
    public void assignImexIdentifierToExperiments(Collection<String> expAcs, String imexId, ImexCentralManager imexCentralManager, Set<String> updatedExpAcs) throws PublicationImexUpdaterException;

    /**
     * Collect all the interaction IMEx ids associated with this publication
     * @param intactPublication
     * @return a list of interaction IMEx ids associated with the publication
     */
    public List<String> collectExistingInteractionImexIdsForPublication(uk.ac.ebi.intact.model.Publication intactPublication);

    /**
     * Assign an IMEx id for the interactions if not already done. Does not assign IMEx ids to interactions already having a primary reference to another
     * IMEx id or interactions which are not PPI. It will clean up duplicated IMEx id and a IMEx primary reference which is invalid will become imex secondary. It will not update interactions having valid imex primary reference which are in conflict with publication imex primary reference
     * @param interactionAcs : list of interaction acs to update
     * @param imexId
     * @param imexCentralManager ; to fire events if provided.
     * @param updatedIntAcs : the updated interaction acs
     * @return a list of experiments which have been updated
     * @throws PublicationImexUpdaterException when IMEx id is null or imex conflict and no imexCentralManager was provided to fire an error event
     */
    public void assignImexIdentifierToInteractions(Collection<String> interactionAcs, String imexId, ImexCentralManager imexCentralManager, Set<String> updatedIntAcs) throws PublicationImexUpdaterException;

    /**
     * Add imex curation and full coverage annotations if not already present
     * @param intactPublication
     * @return true if annotations have been updated, false otherwise
     */
    public boolean updatePublicationAnnotations(uk.ac.ebi.intact.model.Publication intactPublication);

    /**
     * Add or update imex primary ref in Intact publication
     * @param intactPublication
     * @param imexPublication
     * @return true if imex primary ref have been updated, false otherwise
     */
    public boolean updateImexPrimaryRef(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication);

    public ImexCentralClient getImexCentralClient();

    /**
     * This method allows to clean cache methods to collect information about existing interaction IMEx ids attached to a publicatioon/interactions
     * @param pub
     * @param imexId
     */
    public void resetPublicationContext(uk.ac.ebi.intact.model.Publication pub, String imexId);

    /**
     * Collect experiment acs associated with this publication which need to be updated for this IMEx identifier
     * @param pub
     * @param imex
     * @return list if experiment acs needing to be updated
     */
    public List<String> collectExperimentsToUpdateFrom(uk.ac.ebi.intact.model.Publication pub, String imex);

    /**
     * Collect interaction acs associated with this publication which need to be updated for this IMEx identifier
     * @param pub
     * @param imex
     * @return list of interaction acs
     */
    public List<String> collectInteractionsToUpdateFrom(uk.ac.ebi.intact.model.Publication pub, String imex);

    public void setImexCentralClient(ImexCentralClient imexClient);
}
