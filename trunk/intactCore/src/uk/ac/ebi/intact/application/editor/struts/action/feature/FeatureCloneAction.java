/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.DefinedFeatureBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.RangeBean;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Range;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is invoked when Clone button is selected. As a pre condition,
 * an AC must be provided.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureCloneAction extends AbstractEditorAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Handler to the user.
        EditUserI user = getIntactUser(request);

        // The current view.
        FeatureViewBean view = (FeatureViewBean) user.getView();

        // Reset the view.
        view.reset(Feature.class);

        // The defined feature from the view.
        DefinedFeatureBean dfb = view.getDefinedFeature();

        // Set the defined feature values to the bean.
        view.setShortLabel(dfb.getShortLabel());
        view.setFullName(dfb.getFullName());

        // The undetermined range.
        RangeBean rb = dfb.getDefinedRange();
        String fromRange = rb.getFromRange();
        String toRange = rb.getToRange();

        // Create a new range.
        int[] ranges = RangeBean.parseRange(fromRange, toRange);

        // The new range created from the new range bean.
        Range range = new Range(user.getInstitution(), ranges[0],
                ranges[1], ranges[2], ranges[3], null);

        // Sets the fuzzy types.
        range.setFromCvFuzzyType(RangeBean.parseFuzzyType(fromRange, user));
        range.setToCvFuzzyType(RangeBean.parseFuzzyType(toRange, user));

        // Add a copy of the new range
        view.addRange(new RangeBean(range));

        // Update the form for the display.
        view.copyPropertiesTo((EditorActionForm) form);

        // Update the form and display.
        return mapping.findForward(SUCCESS);
    }
}