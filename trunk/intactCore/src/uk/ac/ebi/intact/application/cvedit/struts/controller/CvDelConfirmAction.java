/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.cvedit.struts.controller;

import uk.ac.ebi.intact.application.cvedit.struts.framework.IntactBaseAction;
import uk.ac.ebi.intact.application.cvedit.struts.framework.IntactBaseForm;
import uk.ac.ebi.intact.application.cvedit.struts.framework.util.WebIntactConstants;
import uk.ac.ebi.intact.application.cvedit.struts.view.ListObject;
import uk.ac.ebi.intact.application.cvedit.business.IntactUserIF;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.CvObject;
import org.apache.struts.action.*;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Iterator;

/**
 * Called by struts framework when the user is prompted to confirm to delete a
 * CV object. A positive reponse (submit) will delete the selected CV object from
 * the database and Cancel will take back to the previous page (view/edit CV).
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class CvDelConfirmAction extends IntactBaseAction {

    /**
     * Process the specified HTTP request, and create the corresponding
     * HTTP response (or forward to another web component that will create
     * it). Return an ActionForward instance describing where and how
     * control should be forwarded, or null if the response has
     * already been completed.
     *
     * @param mapping - The <code>ActionMapping</code> used to select this instance
     * @param form - The optional <code>ActionForm</code> bean for this request (if any)
     * @param request - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     *
     * @return - represents a destination to which the controller servlet,
     * <code>ActionServlet</code>, might be directed to perform a RequestDispatcher.forward()
     * or HttpServletResponse.sendRedirect() to, as a result of processing
     * activities of an <code>Action</code> class
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // Determine the action taken by the user.
        IntactBaseForm theForm = (IntactBaseForm) form;

        // Handler to the Intact User.
        IntactUserIF user = super.getIntactUser(request);

        // The object to delete.
        CvObject cvobj = user.getCurrentEditObject();

        if (theForm.isSubmitted()) {
            try {
                // Begin the transaction.
                user.begin();
                // Delete the object we are editing at the moment.
                user.delete(cvobj);
                // Commit all the changes.
                user.commit();
            }
            catch (IntactException ie1) {
                try {
                    user.rollback();
                }
                catch (IntactException ie2) {
                    // Oops! Problems with rollback; ignore this as this
                    // error is reported via the main exception (ie1).
                }
                // Log the stack trace.
                super.log(ExceptionUtils.getStackTrace(ie1));
                // Error with deleting the object.
                super.addError("error.delete", ie1.getMessage());
                super.saveErrors(request);
                return mapping.findForward(WebIntactConstants.FORWARD_FAILURE);
            }
            // Delete successful; back to the results or search.
            if (user.hasSingleSearchResult()) {
                return mapping.findForward(WebIntactConstants.FORWARD_SEARCH);
            }
            else {
                user.removeFromSearchCache(cvobj.getAc());
                return mapping.findForward(WebIntactConstants.FORWARD_RESULTS);
            }
        }
        // Cancel chosen.
        return mapping.findForward(WebIntactConstants.FORWARD_EDIT);
    }
}