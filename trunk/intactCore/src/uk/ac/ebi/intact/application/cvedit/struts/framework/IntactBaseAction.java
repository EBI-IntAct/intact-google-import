/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.cvedit.struts.framework;

import org.apache.struts.action.*;

import uk.ac.ebi.intact.application.cvedit.business.IntactUserIF;
import uk.ac.ebi.intact.application.cvedit.business.IntactServiceIF;
import uk.ac.ebi.intact.application.cvedit.struts.framework.util.WebIntactConstants;
import uk.ac.ebi.intact.application.cvedit.exception.SessionExpiredException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Super class for all Intact related action classes.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public abstract class IntactBaseAction extends Action {

    /** The global Intact error key. */
    public static final String INTACT_ERROR = "IntactError";

    /** Error container */
    private ActionErrors myErrors = new ActionErrors();

    /**
     * Returns the only instance of Intact Service instance.
     * @return only instance of the <code>IntactServiceImpl</code> class.
     */
    protected IntactServiceIF getIntactService() {
        IntactServiceIF service = (IntactServiceIF)
            getApplicationObject(WebIntactConstants.INTACT_SERVICE);
        return service;
    }

    /**
     * Returns the Intact User instance saved in a session for given
     * Http request.
     *
     * @param request the Http request to access the Intact user object.
     * @return an instance of <code>IntactUserImpl</code> stored in a session.
     * No new session is created.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected IntactUserIF getIntactUser(HttpServletRequest request)
            throws SessionExpiredException {
        IntactUserIF user = (IntactUserIF)
            getSessionObject(request,WebIntactConstants.INTACT_USER);
        if (user == null) {
            throw new SessionExpiredException();
        }
        return user;
    }

    /**
     * Returns the Intact User instance saved in a session.
     *
     * @param session the session to access the Intact user object.
     * @return an instance of <code>IntactUserImpl</code> stored in
     * <code>session</code>
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    protected IntactUserIF getIntactUser(HttpSession session)
            throws SessionExpiredException {
        IntactUserIF user = (IntactUserIF)
            session.getAttribute(WebIntactConstants.INTACT_USER);
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
     * Returns the session from given request. No new session is created.
     * @param request the request to get the session from.
     * @return session associated with given request.
     * @exception SessionExpiredException for an expired session.
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
     * Removes the obsolete form bean.
     * @param mapping the ActionMapping used to select this instance.
     * @param request the HTTP request we are processing.
     */
    protected void removeFormBean(ActionMapping mapping,
                                  HttpServletRequest request) {
       // Remove the obsolete form bean
       if (mapping.getAttribute() != null) {
           if ("request".equals(mapping.getScope())) {
               request.removeAttribute(mapping.getAttribute());
           } else {
              HttpSession session = request.getSession();
              session.removeAttribute(mapping.getAttribute());
           }
       }
    }

    /**
     * Clear error container.
     */
    protected void clearErrors() {
        if (!myErrors.empty()) {
            myErrors.clear();
        }
    }

    /**
     * Adds an error with given key.
     *
     * @param key the error key. This value is looked up in the
     * IntactResources.properties bundle.
     */
    protected void addError(String key) {
        myErrors.add(INTACT_ERROR, new ActionError(key));
    }

    /**
     * Adds an error with given key and value.
     *
     * @param key the error key. This value is looked up in the
     * IntactResources.properties bundle.
     * @param value the value to substitute for the first place holder in the
     * IntactResources.properties bundle.
     */
    protected void addError(String key, String value) {
        myErrors.add(INTACT_ERROR, new ActionError(key, value));
    }

    /**
     * Saves the errors in given request for <struts:errors> tag.
     *
     * @param request the request to save errors.
     */
    protected void saveErrors(HttpServletRequest request) {
        super.saveErrors(request, myErrors);
    }

    /**
     * True if there are errors.
     */
    protected boolean hasErrors() {
        return !myErrors.empty();
    }

    /**
     * Returns the course of action depending on the value in
     * <code>WebIntactConstants.SINGLE_MATCH</code>. The default action
     * is to forward to search page.
     *
     * @param request the HTTP request to access the
     * <code>WebIntactConstants.SINGLE_MATCH</code>.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return = WebIntactConstants.SINGLE_MATCH or
     *                WebIntactConstants.FORWARD_RESULTS
     * post: return <> Undefined
     * </pre>
     */
    protected String fwdResultsOrSearch(HttpServletRequest request)
            throws SessionExpiredException {
        // The default is to search again.
        String retval = WebIntactConstants.FORWARD_SEARCH;

        // If we have multiple search results then we go to the results page
        // otherwise go to search page.
        Boolean singleCase = (Boolean) getSessionObject(request,
            WebIntactConstants.SINGLE_MATCH);

        // Ensure that we have this attribute or else NullPointerException.
        if ((singleCase != null) && !singleCase.booleanValue()) {
            // Multiple objects; go to results page.
            retval = WebIntactConstants.FORWARD_RESULTS;
        }
        return retval;
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

    /**
     * Retrieve a session object based on the request and attribute name.
     *
     * @param request the HTTP request to retrieve a session object stored
     * under <tt>attrName</tt>.
     * @param attrName the name of the attribute.
     * @return the session object stored in <tt>request</tt> under
     * <tt>attrName</tt>.
     * @exception SessionExpiredException for an expired session.
     *
     * <pre>
     * post: return <> Undefined
     * </pre>
     */
    private Object getSessionObject(HttpServletRequest request,
            String attrName) throws SessionExpiredException {
        return getSession(request).getAttribute(attrName);
    }
}
