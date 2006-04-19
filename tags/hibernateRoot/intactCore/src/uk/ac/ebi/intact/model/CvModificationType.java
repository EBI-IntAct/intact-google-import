/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.model;



/**
 * Represents a type of modification.
 * 
 * example phosphorylation
 * @author hhe
 * @version $Id$
 */
public class CvModificationType extends CvObject {

    /**
     * Cache a Vector of all shortLabels of the class, e.g. for menus.
     * This should not be here as it has no model functionality but is
     * related to eg user interfaces.
     */
//    protected static Vector menuList = null;

    /**
     * This constructor should <b>not</b> be used as it could
     * result in objects with invalid state. It is here for object mapping
     * purposes only and if possible will be made private.
     * @deprecated Use the full constructor instead
     */
    @Deprecated
    private CvModificationType() {
        //super call sets creation time data
        super();
    }

    /**
     * Creates a valid CvModificationType instance. Requires at least a shortLabel and an
     * owner to be specified.
     * @param shortLabel The memorable label to identify this CvModificationType
     * @param owner The Institution which owns this CvModificationType
     * @exception NullPointerException thrown if either parameters are not specified
     */
    public CvModificationType(Institution owner, String shortLabel) {

        //super call sets up a valid CvObject
        super(owner, shortLabel);
    }

} // end CvModificationType




