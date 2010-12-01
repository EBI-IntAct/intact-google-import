package uk.ac.ebi.intact.update.model.proteinmapping.results;

import uk.ac.ebi.intact.update.model.HibernatePersistentImpl;
import uk.ac.ebi.intact.update.model.proteinmapping.actions.PICRReport;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the cross references that returns PICR for an identifier/sequence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19-May-2010</pre>
 */
@Entity
@Table(name = "ia_picr_xrefs")
public class PICRCrossReferences extends HibernatePersistentImpl {

    /**
     * The database name returned by PICR
     */
    private String database;

    /**
     * The list of accessions from this database PICR returned
     */
    private Set<String> accessions = new HashSet<String>();

    /**
     * The parent report
     */
    private PICRReport picrReport;

    /**
     * Create a new PICRCrossReferences instance
     */
    public PICRCrossReferences() {
        database = null;
    }

    /**
     *
     * @return the database name
     */
    @Column(name = "database", nullable = false)
    public String getDatabase() {
        return database;
    }

    /**
     *
     * @return the list of accessions
     */
    @Transient
    public Set<String> getAccessions() {
        return accessions;
    }

    /**
     *
     * @return the list of accessions as a String, separated by a semi-colon
     */
    @Column(name = "accessions", nullable = false, length = 500)
    public String getListOfAccessions(){

        if (this.accessions.isEmpty()){
            return null;
        }
        StringBuffer concatenedList = new StringBuffer( 1064 );

        for (String ref : this.accessions){
            concatenedList.append(ref+";");
        }

        if (concatenedList.length() > 0){
            concatenedList.deleteCharAt(concatenedList.length() - 1);
        }

        return concatenedList.toString();
    }

    /**
     * set the list of accessions
     * @param possibleAccessions : the list of accessions as a String, separated by a semi colon
     */
    public void setListOfAccessions(String possibleAccessions){
        this.accessions.clear();

        if (possibleAccessions != null){
            if (possibleAccessions.contains(";")){
                String [] list = possibleAccessions.split(";");

                for (String s : list){
                    this.accessions.add(s);
                }
            }
            else {
                this.accessions.add(possibleAccessions);
            }
        }
    }

    /**
     * Set the database name
     * @param database
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Set the list of accessions
     * @param accessions
     */
    public void setAccessions(Set<String> accessions) {
        this.accessions = accessions;
    }

    /**
     * Add a new accession
     * @param accession
     */
    public void addAccession(String accession){
        this.accessions.add(accession);
    }

    /**
     *
     * @return the parent report
     */
    @ManyToOne
    @JoinColumn(name="picr_report_id")
    public PICRReport getPicrReport() {
        return picrReport;
    }

    /**
     * Set the parent report
     * @param picrReport
     */
    public void setPicrReport(PICRReport picrReport) {
        this.picrReport = picrReport;
    }
}
