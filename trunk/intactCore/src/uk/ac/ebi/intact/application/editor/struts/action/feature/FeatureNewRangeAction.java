/*
Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.feature;

import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.action.CommonDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.feature.RangeBean;
import uk.ac.ebi.intact.application.editor.struts.view.feature.FeatureViewBean;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interaction;
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

        // The feature must exist before adding a new range.
        if (view.getAnnotatedObject() == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("feature.range.interval",
                    new ActionError("error.feature.range.nullfeature"));
            saveErrors(request, errors);
            // Feature must exist. Display the error in the input page.
            return mapping.getInputForward();
        }
        // Can we create a Range instance from the user input? validate
        // method only confirms ranges are valid.
        RangeBean rb = featureForm.getNewRange();

        // The range to construct from the bean.
        Range range;

        // Capture the interval errors in creating a range.
        try {
            range = rb.makeRange((Feature) view.getAnnotatedObject(), user);
        }
        catch (IllegalArgumentException iae) {
            ActionErrors errors = new ActionErrors();
            errors.add("feature.range.interval",
                    new ActionError("error.feature.range.interval.invalid"));
            saveErrors(request, errors);
            // Incorrect values for ranges. Display the error in the input page.
            return mapping.getInputForward();
        }
        // Add the new range
        view.addRange(range);

        // Back to the input form.
        return mapping.getInputForward();
    }


}