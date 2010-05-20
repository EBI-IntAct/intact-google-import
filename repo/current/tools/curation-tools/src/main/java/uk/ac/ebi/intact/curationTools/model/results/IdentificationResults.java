package uk.ac.ebi.intact.curationTools.model.results;

import uk.ac.ebi.intact.curationTools.model.actionReport.ActionName;
import uk.ac.ebi.intact.curationTools.model.actionReport.ActionReport;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * This class contains all the results of the protein identification process.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Mar-2010</pre>
 */
@MappedSuperclass
public class IdentificationResults {

    /**
     * the unique uniprot id identifying the protein
     */
    private String finalUniprotId;

    /**
     * the list of actions done to identify the protein
     */
    private List<ActionReport> listOfActions = new ArrayList<ActionReport>();

    /**
     * Create a new Identificationresult
     */
    public IdentificationResults(){
        this.finalUniprotId = null;
    }

    public void setListOfActions(List<ActionReport> listOfActions) {
        this.listOfActions = listOfActions;
    }

    /**
     * set the final uniprot accession identifying the protein
     * @param id : uniprot accession
     */
    public void setFinalUniprotId(String id){
        this.finalUniprotId = id;
    }

    /**
     *
     * @return the final uniprot accession identifying the protein
     */
    @Column(name="uniprot_ac", length = 10)
    public String getFinalUniprotId(){
        return this.finalUniprotId;
    }

    /**
     *
     * @return true if the unique uniprot id is not null
     */
    public boolean hasUniqueUniprotId(){
        return this.finalUniprotId != null;
    }

    /**
     *
     * @return the list of actions done to identify the protein
     */
    @OneToMany
    @JoinColumn(name="action_ac")
    public List<ActionReport> getListOfActions(){
        return this.listOfActions;
    }

    /**
     * add a new action report to the list of reports
     * @param report : action report
     */
    public void addActionReport(ActionReport report){
        this.listOfActions.add(report);
    }

    /**
     *
     * @return the last action report added to this result
     */
    @Transient
    public ActionReport getLastAction(){
        if (listOfActions.isEmpty()){
            return null;
        }
        return this.listOfActions.get(this.listOfActions.size() - 1);
    }

    /**
     *
     * @param name : name of a specific action
     * @return the list of actions with this specific name which have been done to identify the protein
     */
    @Transient
    public List<ActionReport> getActionsByName(ActionName name){
        ArrayList<ActionReport> reports = new ArrayList<ActionReport>();

        for (ActionReport action : this.listOfActions){
            if (action.getName() != null && action.getName().equals(name)){
                reports.add(action);
            }
        }
        return reports;
    }
}
