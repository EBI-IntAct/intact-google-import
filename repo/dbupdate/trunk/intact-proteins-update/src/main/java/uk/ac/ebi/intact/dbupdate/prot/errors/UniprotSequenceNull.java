package uk.ac.ebi.intact.dbupdate.prot.errors;

/**
 * Error when sequence of uniprot protein is null
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/08/11</pre>
 */

public class UniprotSequenceNull extends DefaultProteinUpdateError {

    private String intactSequence;
    private String uniprotAc;

    public UniprotSequenceNull(String proteinAc, String uniprotAc, String intactSequence) {
        super(null, proteinAc);
        this.uniprotAc = uniprotAc;
        this.intactSequence = intactSequence;
    }

    public String getIntactSequence() {
        return intactSequence;
    }

    public String getUniprotAc() {
        return uniprotAc;
    }

    @Override
    public String getErrorMessage(){
        if (this.proteinAc == null || this.uniprotAc == null){
            return super.getErrorMessage();
        }

        StringBuffer error = new StringBuffer();
        error.append("The uniprot entry ");
        error.append(uniprotAc);
        error.append(" does not have a sequence ");
        error.append(" and the sequence of the protein ");
        error.append(this.proteinAc);
        error.append(" cannot be updated.");

        if (intactSequence != null){
            error.append("The sequence of the protein in Intact is not null.");
        }

        return error.toString();
    }
}
