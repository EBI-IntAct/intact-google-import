/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.biosrc;

import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.framework.util.EditorConstants;
import uk.ac.ebi.intact.application.editor.struts.view.biosrc.BioSourceViewBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.exception.SearchException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.NewtServerProxy;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.*;
import java.net.URL;

/**
 * Handles when the user enters a value for tax id. The short label and the full
 * name of the BioSource object is replaced with the values retrieved from the
 * Newt server using the tax id.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceAction extends AbstractEditorAction {

    /**
     * The tax id database.
     */
    private static final String TAX_DB = "newt";

    /**
     * Action for submitting the CV info form.
     * @param mapping the <code>ActionMapping</code> used to select this instance
     * @param form the optional <code>ActionForm</code> bean for this request
     * (if any).
     * @param request the HTTP request we are processing
     * @param response the HTTP response we are creating
     * @return failure mapping for any errors in searching the database; refresh
     * mapping if the screen needs to be updated (this will only happen if the
     * short label on the screen is different to the short label returned by
     * getUnqiueShortLabel method. For all other instances, success mapping is
     * returned.
     * @throws java.lang.Exception for any uncaught errors.
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // Extract the tax id from the form.
        DynaActionForm theForm = (DynaActionForm) form;
        String taxid = (String) theForm.get("taxId");

        // Handler to the user.
        EditUserI user = getIntactUser(request);

        // Validate the tax id; it should be unique.
        if (!validateTaxId(user, taxid, request)) {
            return mapping.getInputForward();
        }
        // To report errors.
        ActionErrors errors;

        // This shouldn't crash the application as we had
        // already created the correct editor view bean.
        BioSourceViewBean bioview = (BioSourceViewBean) user.getView();

        // The URL to access the Newt proxy.
        URL url = getService().getNewtServerUrl();
        // URL is null for an incorrect URL set for NewtProxy.
        if (url == null) {
            // Error in communcating with the server.
            errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.newt.url"));
            saveErrors(request, errors);
            return mapping.getInputForward();
        }

        // The response from the Newt server.
        NewtServerProxy.NewtResponse newtResponse =
                getNewtResponse(user, url, taxid, request);

        // Any errors?
        if (hasErrors(request)) {
            return mapping.getInputForward();
        }
        // Values from newt.
        String newtLabel = newtResponse.getShortLabel();
        String newtName = newtResponse.getFullName();

        // Validate the short label; compute the new name using
        // scientific name and tax id for an empty short label.
        newtLabel = newtResponse.hasShortLabel()
                ? user.getUniqueShortLabel(newtLabel, taxid)
                : this.getUniqueShortLabel(newtName, taxid, user);

        // Covert to lowercase as we only allow LC characters for a short label.
        newtLabel = newtLabel.toLowerCase();

        // Validate the scientific name.
        if (newtName.length() == 0) {
            errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.newt.name", taxid));
            // Display the error and continue on.
            saveErrors(request, errors);
        }
        // Retrieve the xref bean for previous tax id.
        XreferenceBean taxXref = findXref(TAX_DB, bioview);

        // Set the values for existing tax ref.
        if (taxXref != null) {
            // We have a xref bean for previous tax id; set it with inputs from
            // the form.
            taxXref.setPrimaryId(taxid);
            taxXref.setSecondaryId(newtLabel);
            // Need to update this xref.
            bioview.addXrefToUpdate(taxXref);
        }
        else {
            // We don't have an xref for tax id yet; create xref with data
            // from the form.
            Xref xref = createTaxXref(user, taxid, newtLabel);
            bioview.addXref(xref);
        }
        // Set the view with the new inputs.
        bioview.setShortLabel(newtLabel);
        bioview.setFullName(newtName);
        bioview.setTaxId(taxid);

        return mapping.findForward(EditorConstants.FORWARD_SUCCESS);
    }

    /**
     * Validates the tax id.
     * @param user the handler to user to search the database.
     * @param taxid the tax id to search in the database for.
     * @param request the HTTP request to save errors
     * @return <code>false</code> if the search fails or a BioSource
     * instance found  with the same <code>taxid</code>.
     * @exception SearchException for errors in acccessing the database.
     */
    private boolean validateTaxId(EditUserI user,
                                  String taxid,
                                  HttpServletRequest request)
            throws SearchException {
        // Holds the results from the search.
        Collection results = user.search(BioSource.class.getName(), "taxId", taxid);
        if (results.isEmpty()) {
            // Don't have this tax id on the database.
            return true;
        }
        // If we found a single record then it must be the current record.
        if (results.size() == 1) {
            // Found a BioSource with similar tax id; is it as same as the
            // current record?
            BioSourceViewBean view = (BioSourceViewBean) user.getView();
            String currentAc = view.getAnnotatedObject().getAc();
            // Check for null here as it could be null for a new biosource.
            if (currentAc != null) {
                String resultAc = ((BioSource) results.iterator().next()).getAc();
                if (currentAc.equals(resultAc)) {
                    // We have retrieved the same record from the DB.
                    return true;
                }
            }
        }
        // Found a tax id which belongs to another biosource.
        ActionErrors errors = new ActionErrors();
        errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.newt.taxid", taxid));
        saveErrors(request, errors);
        return false;
    }

    private NewtServerProxy.NewtResponse getNewtResponse(EditUserI user,
                                                         URL url,
                                                         String taxid,
                                                         HttpServletRequest request) {
        // To report errors.
        ActionErrors errors;

        // Handler to the Newt server.
        NewtServerProxy newtServer = user.getNewtProxy(url);

        // Query the server.
        NewtServerProxy.NewtResponse newtResponse = null;
        try {
            newtResponse = newtServer.query(Integer.parseInt(taxid));
        }
        catch (IOException ioe) {
            // Error in communcating with the server.
            errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.newt.connection", ioe.getMessage()));
            saveErrors(request, errors);
        }
        catch (NewtServerProxy.TaxIdNotFoundException ex) {
            errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.newt.search", taxid));
            saveErrors(request, errors);
        }
        return newtResponse;
    }

    /**
     * Returns a unique short label.
     * @param name the scientific nsme returned by the Newt server.
     * @param taxid the tax id to use as the extrnal ac when short label is
     * not unique
     * @param user handler to the user to access the method to get a
     * unique short label.
     * @return tax id if the computed short label is empty; otherwise
     * it could be either new computed label or the tax id.
     * @throws SearchException for errors in searching the database.
     */
    private String getUniqueShortLabel(String name, String taxid, EditUserI user)
            throws SearchException {
        // Save the computed a short label.
        String newlabel = computeShortLabel(name);
        // new label is empty if a label cannot be constructed.
        if (newlabel.length() == 0) {
            // Try the tax id as an external ac.
            return user.getUniqueShortLabel(taxid);
        }
        // Check for uniqueness of the computed label.
        return user.getUniqueShortLabel(newlabel, taxid);
    }

    private Xref createTaxXref(EditUserI user, String taxid, String label)
            throws SearchException {
        // The owner of the object we are editing.
        Institution owner = user.getInstitution();

        // The database the new xref belong to.
        CvDatabase db = (CvDatabase) user.getObjectByLabel(
                CvDatabase.class, TAX_DB);

        CvXrefQualifier xqual = (CvXrefQualifier) user.getObjectByLabel(
                CvXrefQualifier.class, "identity");
        return new Xref(owner, db, taxid, label, null, xqual);
    }

    /**
     * Returns the Xreference bean for given primary id and the database name.
     * @param dbname the name of the database to match.
     * @param bioview the view bean to access the xrefs and the tax id.
     * @return <code>XreferenceBean</code> whose database name and primary id
     * are as same as <code>dbname</code> and the primar id of
     * <code>bioview</code> respectively; <code>null</code> is returned for
     * otherwise.
     */
    private XreferenceBean findXref(String dbname, BioSourceViewBean bioview) {
        // The xrefs to loop.
        List xrefs = bioview.getXrefs();
        // The primary id to match.
        String primaryId = bioview.getTaxId();

        // Return as soon as a match is found.
        for (Iterator iter = xrefs.iterator(); iter.hasNext();) {
            XreferenceBean xrefbean = (XreferenceBean) iter.next();
            if (xrefbean.getDatabase().equals(dbname) &&
                    xrefbean.getPrimaryId().equals(primaryId)) {
                return xrefbean;
            }
        }
        // No mtach found.
        return null;
    }

    /**
     * Returns the short label computed using the scientific name.
     * @param sciName the scientific name to compute the short label.
     * @return this is computed as follows: concatenation of the first three
     * chars of the first word and the first two characters of the second
     * word. An empty string is returned for all other instances.
     */
    private String computeShortLabel(String sciName) {
        // Split the scientific name to extract first two awards.
        StringTokenizer stk = new StringTokenizer(sciName);
        String firstWord = stk.nextToken();
        String firstThreeChars = "";
        if (firstWord.length() >= 3) {
            firstThreeChars = firstWord.substring(0, 3);
        }
        String firstTwoChars = "";
        if (stk.hasMoreTokens()) {
            String secondWord = stk.nextToken();
            if (secondWord.length() >= 2) {
                firstTwoChars = secondWord.substring(0, 2);
            }
        }
        return firstThreeChars + firstTwoChars;
    }
}
