/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.model.Protein;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Batch convertion of IntAct protein into TrEMBL flat file.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>05-Oct-2005</pre>
 */
public class ExportToTrEMBL {


    public static final String TREMBL_RELEASE_DATE = "${TREMBL_RELEASE_DATE}";
    public static final String TREMBL_RELEASE_NUMBER = "${TREMBL_RELEASE_NUMBER}";

    public static final String R_LINE_REFERENCE_POSITION = "${REFERENCE_POSITION}";
    public static final String R_LINE_TISSUE = "${TISSUE}";
    public static final String R_LINE_REFERENCE_AUTHOR = "${REFERENCE_AUTHORS}";
    public static final String R_LINE_REFERENCE_TITLE = "${REFERENCE_TITLE}";
    public static final String R_LINE_REFERENCE_LINE = "${REFERENCE_LINE}";

    public static final String NCBI_TAXID = "${NCBI_TAXID}";

    public static final String PROTEIN_AC = "${PROTEIN_AC}";
    public static final String PROTEIN_FULLNAME = "${PROTEIN_FULLNAME}";
    public static final String PROTEIN_CREATION_DATE = "${PROTEIN_CREATION_DATE}";
    public static final String PROTEIN_SEQUENCE = "${SEQUENCE}";
    public static final String PROTEIN_SEQUENCE_LENGTH = "${SEQUENCE_LENGTH}";
    public static final String PROTEIN_CRC64 = "${CRC64}";

    private static final String NEW_LINE = System.getProperty( "line.separator" );
    public static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat( "dd-MMM-yyyy" );

    public static final String
            TREMBL_TEMPLATE = "ID   XXXX_XXXX     PRELIMINARY;   PRT;  " + PROTEIN_SEQUENCE_LENGTH + " AA." + NEW_LINE +
                              "AC   ;" + NEW_LINE +
                              "DT   " + TREMBL_RELEASE_DATE + " (TrEMBLrel. " + TREMBL_RELEASE_NUMBER + ", Created)" + NEW_LINE +
                              "DT   " + TREMBL_RELEASE_DATE + " (TrEMBLrel. " + TREMBL_RELEASE_NUMBER + ", Last sequence update)" + NEW_LINE +
                              "DT   " + TREMBL_RELEASE_DATE + " (TrEMBLrel. " + TREMBL_RELEASE_NUMBER + ", Last annotation update)" + NEW_LINE +
                              "DE   " + PROTEIN_FULLNAME + "." + NEW_LINE +
                              "OX   NCBI_TaxID=" + NCBI_TAXID + "." + NEW_LINE +
                              "RN   [1]{EI1}" + NEW_LINE +
                              "RP   " + R_LINE_REFERENCE_POSITION + "." + NEW_LINE +
                              "RC   TISSUE=" + R_LINE_TISSUE + "{EI1};" + NEW_LINE +
                              "RA   " + R_LINE_REFERENCE_AUTHOR + ";" + NEW_LINE +
                              "RT   \"" + R_LINE_REFERENCE_TITLE + ".\";" + NEW_LINE +
                              "RL   " + R_LINE_REFERENCE_LINE + "." + NEW_LINE +
                              "**" + NEW_LINE +
                              "**   #################    INTERNAL SECTION    ##################" + NEW_LINE +
                              "**EV EI1; IntAct; -; " + PROTEIN_AC + "; " + PROTEIN_CREATION_DATE + "." + NEW_LINE +
                              "SQ   SEQUENCE   " + PROTEIN_SEQUENCE_LENGTH + " AA;  0 MW;   " + PROTEIN_CRC64 + " CRC64;" + NEW_LINE +
                              "     " + PROTEIN_SEQUENCE + NEW_LINE +
                              "//";

    public static final int COLUMN_LENGTH = 10;
    public static final int COLUMN_COUNT = 6;

    //////////////////////////
    // public methods

    /**
     * Tramsform an IntAct protein into a TrEMBL file.
     * <br>
     * We apply Search replace to the TrEMBL template.
     *
     * @param protein             the protein to transform.
     * @param tremblReleaseDate
     * @param tremblReleaseNumber
     * @param referencePosition
     * @param referenceTissue
     * @param referenceAuthor
     * @param referenceTitle
     * @param referenceLine
     *
     * @return the TrEMBL representation of the given IntAct protein.
     */
    public static String formatTremblEntry( Protein protein,
                                            String tremblReleaseDate,
                                            String tremblReleaseNumber,
                                            String referencePosition,
                                            String referenceTissue,
                                            String referenceAuthor,
                                            String referenceTitle,
                                            String referenceLine ) {

        String tremblEntry = SearchReplace.replace( TREMBL_TEMPLATE, PROTEIN_SEQUENCE_LENGTH, "" + protein.getSequence().length() );

        tremblEntry = SearchReplace.replace( tremblEntry, TREMBL_RELEASE_DATE, tremblReleaseDate );
        tremblEntry = SearchReplace.replace( tremblEntry, TREMBL_RELEASE_NUMBER, tremblReleaseNumber );

        // todo remove last '.' if it exists
        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_FULLNAME, protein.getFullName() );

        tremblEntry = SearchReplace.replace( tremblEntry, NCBI_TAXID, protein.getBioSource().getTaxId() );

        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_POSITION, referencePosition );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_TISSUE, referenceTissue );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_AUTHOR, referenceAuthor );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_TITLE, referenceTitle );
        tremblEntry = SearchReplace.replace( tremblEntry, R_LINE_REFERENCE_LINE, referenceLine );

        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_AC, protein.getAc() );

        // generate the creation date in the right format
        String time = DATE_FORMATER.format( new Date( protein.getCreated().getTime() ) );
        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_CREATION_DATE, time.toUpperCase() );

        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_CRC64, Crc64.getCrc64( protein.getSequence() ) );
        String formatedSequence = formatSequence( protein.getSequence() );
        tremblEntry = SearchReplace.replace( tremblEntry, PROTEIN_SEQUENCE, formatedSequence );

        return tremblEntry;
    }


    /**
     * Format the sequence into TrEMBL style.
     * <br>
     * chunk it in 6 columns of 10 AA each.
     *
     * @param sequence
     *
     * @return the formated sequence.
     */
    private static String formatSequence( String sequence ) {
        StringBuffer sb = new StringBuffer( 1024 );

        for ( int i = 0; i < sequence.length(); i++ ) {
            char aa = sequence.charAt( i );
            if ( i> 0 && ( i % COLUMN_LENGTH ) == 0 ) {

                if ( ( i % ( COLUMN_LENGTH * COLUMN_COUNT ) ) == 0 ) {
                    sb.append( NEW_LINE ).append( "     " );
                } else {
                    sb.append( ' ' );
                }
            }

            sb.append( aa );
        }

        // if the last char is NEW_LINE, remove it.
        if ( sb.charAt( sb.length() - 1 ) == NEW_LINE.toCharArray()[ 0 ] ) {
            System.out.println( "INFO: delete last NEW_LINE." );
            sb.deleteCharAt( sb.length() );
        }

        return sb.toString();
    }


    /**
     * M A I N
     */
    public static void main( String[] args ) throws IntactException {

        IntactHelper helper = null;
        try {
            helper = new IntactHelper();
            Collection proteins = helper.search( Protein.class, "shortlabel", "*afcs*" );

            System.out.println( proteins.size() + " protein(s) found." );


            // TODO rt rc rx are not mandatory ... if the user input is empty, then remove those lines.

            String tremblReleaseDate = "24-JAN-2006";
            String tremblReleaseNumber = "32";
            String rp = "NUCLEOTIDE SEQUENCE [MRNA]";
            String rx = "B-cell";
            String ra = "Papin J., Subramaniam S.";
            String rt = "Bioinformatics and cellular signaling";
            String rl = "Curr Opin Biotechnol. 15:78-81(2004)";

            for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
                Protein protein = (Protein) iterator.next();

                System.out.println( formatTremblEntry( protein,
                                                       tremblReleaseDate,
                                                       tremblReleaseNumber,
                                                       rp,
                                                       rx,
                                                       ra,
                                                       rt,
                                                       rl ) );
            }
        } finally {
            if ( helper != null ) {
                helper.closeStore();
            }
        }
    }
}