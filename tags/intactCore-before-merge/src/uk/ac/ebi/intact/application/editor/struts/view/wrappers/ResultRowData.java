/*
Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.wrappers;

import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.InteractionRowData;

import java.sql.Timestamp;
import java.sql.Date;

/**
 * An instance of this class contains information for the display library to display
 * results from a search page.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ResultRowData {

    private String myAc;
    private String myShortLabel;
    private String myFullName;
    private String myType;
    private String myCreator;
    private String myUpdator;
    private Timestamp myCreated;
    private Timestamp myUpdated;

    public ResultRowData() {
    }

    /**
     * @param data an array of data. [0]. ac, [1], shortlabel and [2] full name
     * @param clazz the edit class type.
     */
    public ResultRowData(Object[] data, Class clazz) {
       this((String) data[0], (String) data[1], (String) data[2], (String) data[3], (String) data[4],
            new Timestamp(((Date) data[5]).getTime()), new Timestamp(((Date) data[6]).getTime()));
        myType = IntactHelper.getDisplayableClassName(clazz);
    }
//     public ResultRowData(Object[] data, Class clazz) {
//        this((String) data[0], (String) data[1], (String) data[2], (String) data[3], (String) data[4]);
//        myType = IntactHelper.getDisplayableClassName(clazz);
//    }

    /**
     * Constructs with an annotated object
     * @param annobj the Annotated object to represent a row
     */
    public ResultRowData(AnnotatedObject annobj) {

        this(annobj.getAc(), annobj.getShortLabel(), annobj.getFullName(), annobj.getCreator(), annobj.getUpdator(), annobj.getCreated(), annobj.getUpdated());
        myType = IntactHelper.getDisplayableClassName(annobj);
    }

//    public ResultRowData(String ac, String shortlabel, String fullname, String creator, String updator) {
//        myAc = ac;
//        myShortLabel = shortlabel;
//        myFullName = fullname;
//        myCreator = creator;
//        myUpdator = updator;
//   }

    public ResultRowData(String ac, String shortlabel, String fullname, String creator, String updator, Timestamp created, Timestamp updated) {
            myAc = ac;
            myShortLabel = shortlabel;
            myFullName = fullname;
            myCreator = creator;
            myUpdator = updator;
            myCreated = created;
            myUpdated = updated;
    }


    // Override equals method.

    /**
     * True if ACs match
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj != null) && (getClass() == obj.getClass())) {
            // Same class; can cast safely.
            ResultRowData other = (ResultRowData) obj;
            return myAc.equals(other.myAc);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return myAc.hashCode();
    }

    // Getter methods.

    public String getAc() {
        return myAc;
    }

    public String getShortLabel() {
        return myShortLabel;
    }

    public String getFullName() {
        return myFullName;
    }

    public String getType() {
        return myType;
    }

    public String getCreator() {
        return myCreator;
    }

    public String getUpdator() {
        return myUpdator;
    }

    public Timestamp getCreated() {
        return myCreated;
    }

    public Timestamp getUpdated() {
        return myUpdated;
    }
}
