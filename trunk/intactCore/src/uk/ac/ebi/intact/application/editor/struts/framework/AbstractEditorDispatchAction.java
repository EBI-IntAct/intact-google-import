/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework;

import org.apache.struts.actions.LookupDispatchAction;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import uk.ac.ebi.intact.application.editor.exception.SessionExpiredException;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.business.EditorService;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;

/**
 * The super class for all the CV edit actions.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $id$$
 */
public abstract class AbstractEditorDispatchAction extends LookupDispatchAction {

    /**
     * Returns the only instance of Intact Service instance.
     * @return only instance of the <code>EditorService</code> class.
     */
    protected EditorService getIntactService() {
        EditorService service = (EditorService)
                getApplicationObject(EditorConstants.EDITOR_SERVICE);
        return service;
    }

    /**
     * Returns the session from given request. No new session is created.
     * @param request the request to get the session from.
     * @return session associated with given request.
     * @exception uk.ac.ebi.intact.application.editor.exception.SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected HttpSession getSession(HttpServletRequest request)
            throws SessionExpiredException {
        // Don't create a new session.
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionExpiredException();
        }
        return session;
    }

    /**
     * Returns the Intact User instance saved in a session for given
     * Http request.
     *
     * @param request the Http request to access the Intact user object.
     * @return an instance of <code>EditUser</code> stored in a session.
     * No new session is created.
     * @exception uk.ac.ebi.intact.application.editor.exception.SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected EditUserI getIntactUser(HttpServletRequest request)
            throws SessionExpiredException {
        EditUserI user = (EditUserI)
                getSession(request).getAttribute(EditorConstants.INTACT_USER);
        if (user == null) {
            throw new SessionExpiredException();
        }
        return user;
    }

    /**
     * Convenience method that logs for agiven message.
     * @param message string that describes the error or exception
     */
    protected void log(String message) {
        if (super.servlet.getDebug() >= 1)
            super.servlet.log(message);
    }

    /**
     * Returns the course of action based on the last search result. If the
     * last search produced multiple entries then this method returns the
     * path to multiple results page. For a single result, the method
     * returns the path to the search page.
     *
     * @param user the user to determine where to go.
     *
     * <pre>
     * post: return = EditorConstants.FORWARD_SEARCH or
     *                EditorConstants.FORWARD_RESULTS
     * post: return <> Undefined
     * </pre>
     */
    protected String getForwardAction(EditUserI user) {
        return user.hasSingleSearchResult() ? EditorConstants.FORWARD_SEARCH :
                EditorConstants.FORWARD_RESULTS;
    }

    // Helper Methods

    /**
     * A convenient method to retrieve an application object from a session.
     * @param attrName the attribute name.
     * @return an application object stored in a session under <tt>attrName</tt>.
     */
    private Object getApplicationObject(String attrName) {
        return super.servlet.getServletContext().getAttribute(attrName);
    }
}
