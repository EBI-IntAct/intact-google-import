/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.cvedit.struts.taglibs;

import uk.ac.ebi.intact.application.cvedit.struts.framework.util.WebIntactConstants;
import uk.ac.ebi.intact.application.cvedit.business.IntactUserIF;
import uk.ac.ebi.intact.persistence.TransactionException;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.http.HttpSession;

/**
 * This tag class resets the transaction only if it has already been activated
 * by a call to begin() method. There is no output generated by this tag class.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class ResetTransactionTag extends TagSupport {

    /**
     * Evaluate the tag's body content.
     */
    public int doStartTag() throws JspTagException {
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Called when the JSP encounters the end of a tag. This will reset any
     * open transactions.
     */
    public int doEndTag() throws JspException {
        HttpSession session = super.pageContext.getSession();
        IntactUserIF user = (IntactUserIF) session.getAttribute(
            WebIntactConstants.INTACT_USER);
        try {
            super.pageContext.getServletContext().log(" In ResetTransaction ABOUT to ROLLBACK ");
            //super.pageContext.getServletContext().log(" Autocommit FLAG=" + service.getDAO().getConnection().getAutoCommit());
            super.pageContext.getServletContext().log(" Transaction= " + user.isActive());
            user.rollback();
        }
        catch (TransactionException te) {
            throw new JspException(te.toString());
        }
        return EVAL_PAGE;
    }
}
