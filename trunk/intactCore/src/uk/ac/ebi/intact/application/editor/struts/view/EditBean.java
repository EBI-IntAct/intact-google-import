/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view;

import java.io.Serializable;

/**
 * Generic edit bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditBean implements Serializable {

    /**
     * The editing state.
     */
    public static final String VIEW = "editing";

    /**
     * The saving state.
     */
    public static final String SAVE = "saving";

    /**
     * Keeps track of the edit state; default is in view mode.
     */
    private String myEditState = VIEW;

    /**
     * Returns the status of the editing state.
     * @return {@link #VIEW} if this instance is currently in view mode;
     * for all other instances {@link #SAVE} is returned.
     */
    public String getEditState() {
        return myEditState;
    }

    /**
     * Sets this bean's edit state.
     * @param state the bean's edit state.
     *
     * <pre>
     * post: getEditState() = state
     * </pre>
     */
    public void setEditState(String state) {
        myEditState = state;
    }
}
