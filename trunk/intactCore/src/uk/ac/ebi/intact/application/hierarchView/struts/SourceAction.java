package uk.ac.ebi.intact.application.hierarchView.struts;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.hierarchView.highlightment.source.HighlightmentSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

/**
 * Implementation of <strong>Action</strong> that validates an highlightment submisson.
 *
 * @author Samuel Kerrien
 * @version $Id$
 */
public final class SourceAction extends Action {

    // --------------------------------------------------------- Public Methods

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward perform (ActionMapping mapping,
				                  ActionForm form,
				                  HttpServletRequest request,
				                  HttpServletResponse response)
	throws IOException, ServletException {

	// read form values from the bean
	ActionErrors errors = new ActionErrors();

	String someKeys = request.getParameter(Constants.ATTRIBUTE_KEYS);

	if ((null == someKeys) || (someKeys.length() < 1))
	  errors.add("keys", new ActionError("error.keys.required"));

	// Report any errors we have discovered back to the original form
	if (!errors.empty()) {
	    saveErrors(request, errors);
	    return (new ActionForward(mapping.getInput()));
	}

	// Save our data in the session
	HttpSession session = request.getSession();

	// get the class method name to create an instance
	String source = (String) session.getAttribute (Constants.ATTRIBUTE_METHOD_CLASS);

	HighlightmentSource highlightmentSource = HighlightmentSource.getHighlightmentSource(source);
	Collection keys = highlightmentSource.parseKeys(someKeys);

	session.setAttribute(Constants.ATTRIBUTE_KEYS, keys);

	// Print debug in the log file
	if (servlet.getDebug() >= 1)
	    servlet.log("SourceAction: keys=" + keys +
	                "\nlogged on in session " + session.getId());

        // Remove the obsolete form bean
	if (mapping.getAttribute() != null) {
            if ("request".equals(mapping.getScope()))
                request.removeAttribute(mapping.getAttribute());
            else
                session.removeAttribute(mapping.getAttribute());
        }

	// Forward control to the specified success URI
	return (mapping.findForward("success"));

    }

} // SourceAction
