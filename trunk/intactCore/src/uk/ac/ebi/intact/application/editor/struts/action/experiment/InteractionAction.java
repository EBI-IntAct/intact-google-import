/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.experiment;

import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.InteractionBean;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.application.editor.util.LockManager;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Interaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Invoked when Delete/Edit Interaction button is pressed to delete/edit
 * an Interaction.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionAction extends AbstractEditorAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The dyna form.
        DynaActionForm dynaform = (DynaActionForm) form;

        // The index position of the annotation.
        int idx = ((Integer) dynaform.get("idx")).intValue();

        // The command associated with the index.
        String cmd = ((String[]) dynaform.get("intCmd"))[idx];

        // Message resources to access button labels.
        MessageResources msgres = getResources(request);

        if (cmd.equals(msgres.getMessage("exp.int.button.edit"))) {
            return edit(mapping, form, request, response);
        }
        // default is delete.
        return delete(mapping, form, request, response);
    }

    public ActionForward edit(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws Exception {
        // The dyna form.
        DynaActionForm dynaform = (DynaActionForm) form;

        // The index position of the annotation.
        int idx = ((Integer) dynaform.get("idx")).intValue();

        // The protein we are editing at the moment.
        InteractionBean ib = ((InteractionBean[]) dynaform.get("ints"))[idx];

        // We must have the protein bean.
        assert ib != null;

        // Check the lock.
        LockManager lmr = LockManager.getInstance();

        // Handler to the Intact User.
        EditUserI user = getIntactUser(request);

        // Cache the this object's intAc because we need to set it later.
        String expAc = user.getView().getAc();

        // The intAc to search
        String intAc = ib.getAc();

        // Try to acuire the lock.
        if (!lmr.acquire(intAc, user.getUserName())) {
            ActionErrors errors = new ActionErrors();
            // The owner of the lock (not the current user).
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.lock", intAc, lmr.getOwner(intAc)));
            saveErrors(request, errors);
            return mapping.findForward(FAILURE);
        }
        // The selected Annotated object.
        AnnotatedObject annobj = (AnnotatedObject) user.getObjectByAc(
                Interaction.class, intAc);
        // The object we are editing presently.
        user.setView(annobj);

        // Set the experiment AC, so we can come back to this experiment again.
        ((InteractionViewBean) user.getView()).setSourceExperimentAc(expAc);

        dynaform.set("dispatch", "save");

        // Update the form.
        return mapping.findForward("intEdit");
    }

    public ActionForward delete(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws Exception {
        // The dyna form.
        DynaActionForm dynaform = (DynaActionForm) form;

        // The index position of the annotation.
        int idx = ((Integer) dynaform.get("idx")).intValue();

        // The current view of the edit session.
        ExperimentViewBean view =
                (ExperimentViewBean) getIntactUser(request).getView();

        // The bean associated with the current action.
        InteractionBean ib = ((InteractionBean[]) dynaform.get("ints"))[idx];

        // We must have the bean.
        assert ib != null;

        // Wants to delete the selected interaction.
        view.delInteraction(ib);

        return mapping.findForward(SUCCESS);
    }
}