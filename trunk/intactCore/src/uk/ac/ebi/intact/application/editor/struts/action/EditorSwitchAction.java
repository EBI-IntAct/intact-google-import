/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action;

import org.apache.struts.tiles.actions.TilesAction;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.exception.SessionExpiredException;

/**
 * This action class is responsible for selecting which to editor to display. The
 * selection is based on the current selected class. For example, if the user has
 * selected a CvObject, then the CV editor is presented.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditorSwitchAction extends TilesAction {

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it),
     * with provision for handling exceptions thrown by the business logic.
     * Override this method to provide functionality
     *
     * @param context the current Tile context, containing Tile attributes.
     * @param mapping the ActionMapping used to select this instance.
     * @param form the optional ActionForm bean for this request (if any).
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @return null because there is no forward associated with this action.
     * @throws Exception if the application business logic throws an exception.
     */
    public ActionForward execute(ComponentContext context,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Don't create a new session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionExpiredException();
        }
        EditUserI user = (EditUserI) session.getAttribute(
                EditorConstants.INTACT_USER);
        // The topic user has selected.
        String topic = user.getSelectedTopic();
        if (topic.equals("BioSource")) {
            // Switch over to biosource layout.
            context.putAttribute("content", "edit.biosrc.layout");
        }
        return null;
    }
}
