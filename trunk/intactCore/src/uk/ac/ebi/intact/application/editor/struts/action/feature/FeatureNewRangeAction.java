/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.RangeBean;
import uk.ac.ebi.intact.model.Range;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is invoked when the user wants to add a new range to a feature.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureNewRangeAction extends AbstractEditorAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The form to extract values.
        FeatureActionForm featureForm = ((FeatureActionForm) form);

        // Handler to the current user.
        EditUserI user = getIntactUser(request);

        // The current view of the edit session.
        FeatureViewBean view = (FeatureViewBean) user.getView();

        // The bean to extract values.
        RangeBean rbnew = featureForm.getNewRange();

        // Does the range exist in the current ranges?
        if (view.rangeExists(rbnew)) {
            ActionErrors errors = new ActionErrors();
            errors.add("new.range",
                    new ActionError("error.feature.range.exists"));
            saveErrors(request, errors);
            // Incorrect values for ranges. Display the error in the input page.
            return mapping.getInputForward();
        }
        // From and To ranges as strings
        String fromRange = rbnew.getFromRange();
        String toRange = rbnew.getToRange();

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

        // Back to the input form.
        return mapping.getInputForward();
    }
}