/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.view.biosrc;

import org.apache.struts.tiles.ComponentContext;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.exception.SearchException;
import uk.ac.ebi.intact.application.editor.struts.framework.util.AbstractEditViewBean;
import uk.ac.ebi.intact.model.BioSource;

/**
 * BioSource edit view bean.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceViewBean extends AbstractEditViewBean {

    /**
     * The tax id.
     */
    private String myTaxId;

    // Override the super method to initialize this class specific resetting.
    public void reset(Class clazz) {
        super.reset(clazz);
        // Set fields to null.
        setTaxId(null);
    }

    // Override the super method to set the tax id.
    public void reset(BioSource biosrc) {
        super.reset(biosrc);
        setTaxId(biosrc.getTaxId());
    }

    // Implements abstract methods

    protected void updateAnnotatedObject(EditUserI user) throws SearchException {
        // The current biosource.
        BioSource bs = (BioSource) getAnnotatedObject();

        // Have we set the annotated object for the view?
        if (bs == null) {
            // Not persisted; create a new biosource object.
            bs = new BioSource(user.getInstitution(), getShortLabel(), getTaxId());
            setAnnotatedObject(bs);
        }
        else {
            bs.setTaxId(getTaxId());
        }
    }

    // Override to provide BioSource layout.
    public void setLayout(ComponentContext context) {
        context.putAttribute("content", "edit.biosrc.layout");
    }

    // Override to provide BioSource help tag.
    public String getHelpTag() {
        return "editor.biosource";
    }

    // Getter/Setter methods for tax id.
    public String getTaxId() {
        return myTaxId;
    }

    public void setTaxId(String taxid) {
        myTaxId = taxid;
    }
}
