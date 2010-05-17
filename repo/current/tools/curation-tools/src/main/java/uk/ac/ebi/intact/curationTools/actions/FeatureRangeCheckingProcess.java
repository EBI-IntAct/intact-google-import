package uk.ac.ebi.intact.curationTools.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.ncbiblast.model.BlastProtein;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.curationTools.actions.exception.ActionProcessingException;
import uk.ac.ebi.intact.curationTools.model.actionReport.ActionName;
import uk.ac.ebi.intact.curationTools.model.actionReport.BlastReport;
import uk.ac.ebi.intact.curationTools.model.actionReport.status.Status;
import uk.ac.ebi.intact.curationTools.model.actionReport.status.StatusLabel;
import uk.ac.ebi.intact.curationTools.model.contexts.FeatureRangeCheckingContext;
import uk.ac.ebi.intact.curationTools.model.contexts.IdentificationContext;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class is checking that the new sequences found during a Blast process (usually a Blast remapping process) are not in conflict with the
 * feature ranges of the protein to identify in the IntAct database.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-May-2010</pre>
 */

public class FeatureRangeCheckingProcess extends ActionNeedingIntactContext{


    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( FeatureRangeCheckingProcess.class);

    /**
     * Create a FeatureRangeCheckingProcess with an Intact context which is null and should be set later using the setIntactContext method
     */
    public FeatureRangeCheckingProcess(){
        super();
    }

    /**
     * Create a FeatureRangeCheckingProcess with an IntactContext 'context'
     * @param context : the Intact context
     */
    public FeatureRangeCheckingProcess(IntactContext context){
        super(context);
    }

    /**
     * Checks that the sequence of the BlastProtein is not in conflict with the feature ranges that the protein can have in Intact
     * @param range : the range of the feature to check
     * @param protein : the protein hit which could replace the old protein in Intact
     * @return true if there is no conflict between the sequence of this BlastProtein and the range
     */
    private boolean checkRangeValidWithNewSequence(Range range, BlastProtein protein){
        // The difference between the previous start position and the new one in the new sequence
        int diffStart = protein.getStartQuery() - protein.getStartMatch();
        // The difference between the previous end position and the new one in the new sequence
        int diffEnd = protein.getEndQuery() - protein.getEndMatch();

        // Shift the ranges in consequence
        int startFrom = range.getFromIntervalStart() - diffStart;
        int startTo = range.getToIntervalStart() - diffStart;
        int endFrom = range.getFromIntervalEnd() - diffEnd;
        int endTo = range.getToIntervalEnd() - diffEnd;

        // No ranges should be before the new start positions
        if (startFrom < protein.getStartMatch() || range.getFromIntervalStart() < protein.getStartQuery()){
            return false;
        }
        // No ranges should be before the new start positions
        else if (startTo < protein.getStartMatch() || range.getToIntervalStart() < protein.getStartQuery()){
            return false;
        }
        // No ranges should be after the new end positions
        else if (endFrom > protein.getEndMatch() || range.getFromIntervalEnd() > protein.getEndQuery()){
            return false;
        }
        // No ranges should be after the new end positions
        else if (endTo > protein.getEndMatch() || range.getToIntervalEnd() > protein.getEndQuery()){
            return false;
        }
        else {
            // Check that the amino acids involved in the feature ranges are identical
            if (startFrom > 0 && endFrom > 0){
                String rangeNewSequence = protein.getSequence().substring(startFrom - 1, endFrom);
                if (!range.getSequence().equals(rangeNewSequence)){
                    return false;
                }
            }
            if (startTo > 0 && endTo > 0){
                String rangeNewSequence = protein.getSequence().substring(startTo - 1, endTo);
                if (!range.getSequence().equals(rangeNewSequence)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check that all the ranges of the features that the Intact entry can contain in Intact are not in conflict with the new sequence(s)
     * proposed by the blast on Swissprot to replace the Trembl entry. 
     * @param context : the context of the protein
     * @return the trembl accession in the context if the sequences of the Swissprot proteins have some conflicts with the ranges of some features,
     * the Swissprot accession if there is only one Swissprot protein with no conflicts and null if there are several Swissprot proteins with no conflict.
     * @throws ActionProcessingException
     */
    public String runAction(IdentificationContext context) throws ActionProcessingException {
        // always clear the list of reports from previous actions
        this.listOfReports.clear();

        // Can't run the FeatureRangeCheckingProcess if we can't have access to IntAct database
        if (this.intactContext == null){
            throw new ActionProcessingException("We can't check if a feature is affected by the sequence changes if an IntactContext instance is not provided.");
        }

        // We need to have a specific context containing the previous Trembl accession which totally matched the Intact protein and the possible
        // proteins from Swissprot which can replace the Trembl match
        if (! (context instanceof FeatureRangeCheckingContext)){
            throw new ActionProcessingException("We can't process a feature range checking if the context is a " + context.getClass().getSimpleName() + " and not a FeatureRangeCheckingContext instance.");
        }
        else {

            FeatureRangeCheckingContext processContext = (FeatureRangeCheckingContext) context;
            int initialNumberOfBlastProtein = processContext.getResultsOfSwissprotRemapping().size();

            // Create a BlastReport
            BlastReport report = new BlastReport(ActionName.feature_range_checking);
            this.listOfReports.add(report);

            // If there were no Swissprot proteins which can replace the Trembl entry, it is an error and this action fails
            if (processContext.getResultsOfSwissprotRemapping().isEmpty()){
                Status status = new Status(StatusLabel.FAILED, "We don't have any valid results from the Swissprot-remapping process, so we will keep the Trembl entry " + processContext.getTremblAccession());
                report.setStatus(status);
                return processContext.getTremblAccession();
            }

            DaoFactory factory = this.intactContext.getDaoFactory();
            // get the components involving the Intact entry
            List<Component> components = factory.getComponentDao().getByInteractorAc(processContext.getIntactAccession());

            // If there is no component containing this protein, we don't have conflicts with feature ranges
            if (components.isEmpty()){
                report.getBlastMatchingProteins().addAll(processContext.getResultsOfSwissprotRemapping());
            }
            else {
                // to check that at least one component has a feature
                boolean hasAtLeastOneFeature = false;
                // to check when a conflict has been detected
                boolean hasRangeConflict = false;

                for (Component component : components){
                    Collection<Feature> features = component.getBindingDomains();

                    if (!features.isEmpty()){
                        hasAtLeastOneFeature = true;

                        for (Feature feature : features){
                            Collection<Range> ranges = feature.getRanges();

                            for (Range range : ranges){
                                // undetermined ranges are not affected by the new sequence
                                if (!range.isUndetermined()) {
                                    for (BlastProtein protein : processContext.getResultsOfSwissprotRemapping()){

                                        if (!checkRangeValidWithNewSequence(range, protein)){
                                            hasRangeConflict = true;
                                            report.addWarning("One of the ranges of the feature "+feature.getAc()+" is in conflict with the sequence of the Swissprot entry "+protein.getAccession()+". We will remove this protein from the matching Swissprot entries.");
                                        }
                                        else{
                                            // The sequence of this Blast protein is not in conflict with the range, we can keep it among the Blast results in the report
                                            report.addBlastMatchingProtein(protein);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

                // If all the components didn't contain any feature, there is no conflict, we can keep the previous blast results
                if (!hasAtLeastOneFeature){
                    report.getBlastMatchingProteins().addAll(processContext.getResultsOfSwissprotRemapping());
                }
                else {
                     // there is no conflict, we can keep the previous blast results
                    if (!hasRangeConflict){
                        report.getBlastMatchingProteins().addAll(processContext.getResultsOfSwissprotRemapping());
                    }
                }
            }

            if (report.getBlastMatchingProteins().isEmpty()){
                Status status = new Status(StatusLabel.FAILED, "The swissprot remapping is not possible as there are some conflicts between the sequence of the Swissprot entry and some feature ranges of the protein " + processContext.getIntactAccession() + ". We will keep the Trembl entry " + processContext.getTremblAccession());
                report.setStatus(status);

                return processContext.getTremblAccession();
            }
            else if (report.getBlastMatchingProteins().size() < initialNumberOfBlastProtein){
                Status status = new Status(StatusLabel.TO_BE_REVIEWED, processContext.getResultsOfSwissprotRemapping().size() + " Swissprot entries on the initial " + initialNumberOfBlastProtein + " matching Swissprot proteins have a conflict between their sequence and some feature ranges of the protein " + processContext.getIntactAccession());
                report.setStatus(status);

                return processContext.getTremblAccession();
            }
            else {
                if (report.getBlastMatchingProteins().size() == 1){
                    BlastProtein swissprot = report.getBlastMatchingProteins().iterator().next();
                    Status status = new Status(StatusLabel.COMPLETED, "We don't have any conflicts between the sequence of the Swissprot entry " + swissprot.getAccession() + " and the feature ranges of the protein " + processContext.getIntactAccession());
                    report.setStatus(status);
                    return swissprot.getAccession();
                }
                else {
                    Status status = new Status(StatusLabel.COMPLETED, "We don't have any conflicts between the sequence(s) of the " + report.getBlastMatchingProteins().size() + " possible Swissprot proteins and the feature ranges of the protein " + processContext.getIntactAccession());
                    report.setStatus(status);
                    ArrayList<BlastProtein> proteins = new ArrayList<BlastProtein> ();

                    // merge the isoforms
                    proteins.addAll(report.getBlastMatchingProteins());
                    Set<String> accessions = mergeIsoformsFromBlastProteins(proteins);

                    if (accessions.size() == 1){
                        return accessions.iterator().next();
                    }
                    else {
                        return processContext.getTremblAccession();
                    }
                }
            }
        }
    }
}
