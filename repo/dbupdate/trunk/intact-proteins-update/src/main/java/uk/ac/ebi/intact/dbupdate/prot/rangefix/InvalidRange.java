package uk.ac.ebi.intact.dbupdate.prot.rangefix;

import uk.ac.ebi.intact.model.Range;

/**
 * This class contains informations about the invalid range
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-Aug-2010</pre>
 */

public class InvalidRange extends UpdatedRange{

    /**
     * The sequence of the protein
     */
    String sequence;

    /**
     * The version of the protein sequence for what this range was valid
     */
    int validSequenceVersion;

    /**
     * The uniprot ac associated with the sequence version
     */
    String uniprotAc;

    /**
     * The message to add at the feature level
     */
    String message;

    public InvalidRange(Range range, Range newRange, String sequence, String message) {
        super(range, newRange);

        this.sequence = sequence;
        this.message = message;
        this.validSequenceVersion = -1;
        this.uniprotAc = null;
    }

    public InvalidRange(Range range, Range newRange, String sequence, String message, int sequenceVersion) {
        super(range, newRange);

        this.sequence = sequence;
        this.message = message;
        this.validSequenceVersion = sequenceVersion;
        this.uniprotAc = null;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getValidSequenceVersion() {
        return validSequenceVersion;
    }

    public void setValidSequenceVersion(int validSequenceVersion) {
        this.validSequenceVersion = validSequenceVersion;
    }

    public String getUniprotAc() {
        return uniprotAc;
    }

    public void setUniprotAc(String uniprotAc) {
        this.uniprotAc = uniprotAc;
    }
}
