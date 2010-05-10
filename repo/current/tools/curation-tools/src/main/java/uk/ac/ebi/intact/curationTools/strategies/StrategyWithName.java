package uk.ac.ebi.intact.curationTools.strategies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.curationTools.actions.IntactNameSearchProcess;
import uk.ac.ebi.intact.curationTools.actions.UniprotNameSearchProcess;
import uk.ac.ebi.intact.curationTools.actions.exception.ActionProcessingException;
import uk.ac.ebi.intact.curationTools.model.contexts.IdentificationContext;
import uk.ac.ebi.intact.curationTools.model.results.IdentificationResults;
import uk.ac.ebi.intact.curationTools.strategies.exceptions.StrategyException;

/**
 * This strategy aims at identifying a protein using a gene name and/or a protein name, or a general name and its organism.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19-Mar-2010</pre>
 */

public class StrategyWithName extends IdentificationStrategyImpl {

    /**
     * The intact context. Can be null if we don't want to query Intact
     */
    private IntactContext intactContext;

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( StrategyWithName.class );

    /**
     * Create a new Strategy with name with an IntAct context null.
     */
    public StrategyWithName(){
        super();
        this.intactContext = null;
    }

    /**
     * Create a new Strategy with name with this intact context
     * @param context
     */
    public void setIntactContext(IntactContext context){
        this.intactContext = context;
    }

    /**
     * Do a search on uniprot first to try to map an unique uniprot entry to the name of the protein, if the search on uniprot fails, this strategy
     * is doing a search on Intact.
     * @param context : the context of the protein to identify
     * @return the results
     * @throws StrategyException
     */
    @Override
    public IdentificationResults identifyProtein(IdentificationContext context) throws StrategyException {

        // Create a result instance
        IdentificationResults result = new IdentificationResults();

        // If the context doesn't contain any names for this protein, this strategy can't be used
        if (context.getProtein_name() == null && context.getGene_name() == null && context.getGlobalName() == null){
            throw new StrategyException("At least of of these names should be not null : protein name, gene name or a general name.");
        }
        else{

            try {
                // Get the result of the search on uniprot
                String uniprot = this.listOfActions.get(0).runAction(context);
                // process the isoforms and set the uniprot id of the result
                processIsoforms(uniprot, result);
                // add the reports
                result.getListOfActions().addAll(this.listOfActions.get(0).getListOfActionReports());

                // If the search on uniprot is unsuccessful
                if (uniprot == null && result.getLastAction().getPossibleAccessions().isEmpty()){
                    // if the intact context is not null, we can do a search on Intact
                    if (this.intactContext != null){
                        // set the intact context of the action
                        IntactNameSearchProcess intactProcess = (IntactNameSearchProcess) this.listOfActions.get(1);
                        intactProcess.setIntactContext(this.intactContext);

                        // run the action. as this method is looking for an Intact accession, we don't have any uniprot accession to expect
                        intactProcess.runAction(context);
                        // add the reports
                        result.getListOfActions().addAll(this.listOfActions.get(1).getListOfActionReports());
                    }
                }

            } catch (ActionProcessingException e) {
                throw new StrategyException("Problem trying to match a gene name/protein name to an uniprot entry.");
            }
        }
        return result;
    }

    /**
     * Initialises the set of actions for this strategy
     */
    @Override
    protected void initialiseSetOfActions() {
        // The first action is a search on uniprot
        UniprotNameSearchProcess firstAction = new UniprotNameSearchProcess();
        this.listOfActions.add(firstAction);

        // the second action is a search on Intact
        IntactNameSearchProcess secondAction = new IntactNameSearchProcess();
        this.listOfActions.add(secondAction);
    }
}
