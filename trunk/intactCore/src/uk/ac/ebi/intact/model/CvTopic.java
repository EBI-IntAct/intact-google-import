/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.  
All rights reserved. Please see the file LICENSE 
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.model;


/**
 * Controlled vocabulary for description topics.
 *
 * @author hhe
 * @version $Id$
 */
public class CvTopic extends CvObject implements Editable {

    //////////////////////////
    // Constants

    public static final String XREF_VALIDATION_REGEXP = "id-validation-regexp";
    public static final String INTERNAL_REMARK = "remark-internal";
    public static final String UNIPROT_DR_EXPORT = "uniprot-dr-export";
    public static final String UNIPROT_CC_EXPORT = "uniprot-cc-note";
    public static final String NEGATIVE = "negative";
    public static final String AUTHOR_CONFIDENCE = "author-confidence";
    public static final String COFIDENCE_MAPPING = "confidence-mapping";

    /**
     * This constructor should <b>not</b> be used as it could
     * result in objects with invalid state. It is here for object mapping
     * purposes only and if possible will be made private.
     *
     * @deprecated Use the full constructor instead
     */
    private CvTopic() {
        //super call sets creation time data
        super();
    }

    /**
     * Creates a valid CvTopic instance. Requires at least a shortLabel and an
     * owner to be specified.
     *
     * @param shortLabel The memorable label to identify this CvTopic
     * @param owner      The Institution which owns this CvTopic
     * @throws NullPointerException thrown if either parameters are not specified
     */
    public CvTopic( Institution owner, String shortLabel ) {
        //super call sets up a valid CvObject
        super( owner, shortLabel );
    }

} // end CvTopic




