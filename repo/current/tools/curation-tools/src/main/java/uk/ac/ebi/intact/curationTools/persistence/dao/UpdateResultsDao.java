package uk.ac.ebi.intact.curationTools.persistence.dao;

import uk.ac.ebi.intact.annotation.Mockable;
import uk.ac.ebi.intact.curationTools.model.actionReport.ActionName;
import uk.ac.ebi.intact.curationTools.model.actionReport.ActionReport;
import uk.ac.ebi.intact.curationTools.model.actionReport.status.StatusLabel;
import uk.ac.ebi.intact.curationTools.model.results.UpdateResults;

import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19-May-2010</pre>
 */
@Mockable
public interface UpdateResultsDao extends UpdateBaseDao<UpdateResults> {

    public UpdateResults getUpdateResultsWithId(long id);

    public UpdateResults getUpdateResultsForProteinAc(String proteinAc);

    public List<ActionReport> getActionReportsByNameAndProteinAc(ActionName name, String proteinAc);

    public List<ActionReport> getActionReportsByNameAndResultId(ActionName name, long resultId);

    public List<ActionReport> getActionReportsByStatusAndProteinAc(StatusLabel status, String proteinAc);

    public List<ActionReport> getActionReportsByStatusAndResultId(StatusLabel label, long resultId);

    public List<ActionReport> getActionReportsWithBlastResultsByProteinAc(String protAc);

    public List<ActionReport> getActionReportsWithSwissprotRemappingResultsByProteinAc(String protAc);

    public List<ActionReport> getActionReportsWithPICRCrossReferencesByProteinAc(String protAc);

    public List<ActionReport> getActionReportsWithBlastResultsByResultsId(long id);

    public List<ActionReport> getActionReportsWithSwissprotRemappingResultsByResultsId(long id);

    public List<ActionReport> getActionReportsWithPICRCrossReferencesByResultsId(long id);

    public List<ActionReport> getActionReportsWithWarningsByProteinAc(String proteinAc);
}
