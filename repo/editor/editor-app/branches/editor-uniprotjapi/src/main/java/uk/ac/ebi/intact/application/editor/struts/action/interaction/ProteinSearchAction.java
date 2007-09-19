/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.action.interaction;

import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorAction;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionActionForm;
import uk.ac.ebi.intact.application.editor.struts.view.interaction.InteractionViewBean;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.searchengine.ResultWrapper;
import uk.ac.ebi.intact.util.protein.ProteinService;
import uk.ac.ebi.intact.util.protein.ProteinServiceFactory;
import uk.ac.ebi.intact.util.protein.utils.UniprotServiceResult;
import uk.ac.ebi.intact.util.MailSender;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The action class to search a Protein.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 *
 * @struts.action
 *      path="/int/prot/search"
 *      name="intForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 */
public class ProteinSearchAction extends AbstractEditorAction {

    /**
     * SP AC. 10 characters allowed.
     */
    private static final Pattern ourSpAcPat = Pattern.compile("\\w{1,10}$");

    /**
     * Intact AC. Must start with either uppercase characters, followed by
     * '-' and a number.
     */
    private static final Pattern ourIntactAcPat = Pattern.compile("[A-Z]+\\-[0-9]+$");

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
     * @return - represents a destination to which the action servlet,
     * <code>ActionServlet</code>, might be directed to perform a RequestDispatcher.forward()
     * or HttpServletResponse.sendRedirect() to, as a result of processing
     * activities of an <code>Action</code> class
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        // The form.
        InteractionActionForm intform = (InteractionActionForm) form;

        MailSender mailSender = new MailSender();
        String emails[] = {"rodrigue@ebi.ac.uk"};//,"cleroy@ebi.ac.uk"};
                mailSender.postMail(emails,"Catherine : la plus belle pour toi.", "Dans ma generosite que tout le monde sait infinie, je t'ai " +
                        "donne la copine la plus divine qui puisse exister sur cette terre\n Dieu","dieu@ciel.univers");
                String email[] = {"cleroy@ebi.ac.uk"};//,"cleroy@ebi.ac.uk"};
                        mailSender.postMail(email,"Nicolas : le plus beau pour toi.", "Dans ma generosite que tout le monde sait infinie, je t'ai " +
                                "donne le copain le plus divin qui puisse exister sur cette terre\n Dieu","dieu@ciel.univers");


        String ac = intform.getProtSearchAC();
        String spAc = intform.getProtSearchSpAC();
        String shortLabel = intform.getProtSearchLabel();

        // Cache string lengths.
        int acLen = ac.length();
        int spAcLen = spAc.length();
        int shortLabelLen = shortLabel.length();

        // Error if all three fields are empty.
        if ((acLen == 0) && (spAcLen == 0) && (shortLabelLen == 0)) {
            ActionMessages errors = new ActionMessages();
            errors.add("int.interact.search",
                    new ActionMessage("error.int.interact.search.input"));
            saveErrors(request, errors);
            setAnchor(request, intform);
            return mapping.getInputForward();
        }
        // The default values for search.
        String value = shortLabel;
        String param = "shortLabel";

        if (acLen != 0) {
            Matcher matcher = ourIntactAcPat.matcher(ac);
            if (!matcher.matches()) {
                ActionMessages errors = new ActionMessages();
                errors.add("int.interact.search",
                        new ActionMessage("error.int.interact.search.ac"));
                saveErrors(request, errors);
                setAnchor(request, intform);
                return mapping.getInputForward();
            }
            value = ac;
            param = "ac";
        }
        else if (spAcLen != 0) {
            Matcher matcher = ourSpAcPat.matcher(spAc);
            if (!matcher.matches()) {
                ActionMessages errors = new ActionMessages();
                errors.add("int.interact.search",
                        new ActionMessage("error.int.interact.search.sp"));
                saveErrors(request, errors);
                setAnchor(request, intform);
                return mapping.getInputForward();
            }
            value = spAc;
            param = "spAc";
        }
        // Handler to the current user.
        EditUserI user = getIntactUser(request);

        // The maximum proteins allowed.
        int max = getService().getInteger("protein.search.limit");

        UniprotRemoteService uniprotRemoteService = new UniprotRemoteService();
        // The wrapper to hold lookup result.
        ProteinService proteinService = ProteinServiceFactory.getInstance().buildProteinService(uniprotRemoteService);

        LOGGER.debug("ProteinSearchAction.execute 1");
        ResultWrapper rw = null;
        UniprotServiceResult uniprotServiceResult = null;

        if (param.equals("spAc")) {
            try{
                LOGGER.debug("ProteinSearchAction.execute 2");
                uniprotServiceResult = proteinService.retrieve(value);
            }catch(Exception e){
                LOGGER.error(e.getMessage() + e.getStackTrace() + e.getCause());
                // This can only happen when problems with creating an internal helper
                // This error is already logged from the User class.
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.intact"));
                saveErrors(request, errors);
                return mapping.findForward(FAILURE);
            }
        }
        else {
            LOGGER.debug("ProteinSearchAction.execute 3");
            try {
                rw = user.lookup(ProteinImpl.class, param, value, max);
                if(rw.isEmpty()){
                    rw = user.lookup(NucleicAcidImpl.class, param, value,max);
                    LOGGER.debug("ProteinSearchAction.execute 4");
                }
                if(rw.isEmpty()){
                    rw = user.lookup(SmallMoleculeImpl.class, param, value,max);
                    LOGGER.debug("ProteinSearchAction.execute 5");
                }
            }
            catch (IntactException ie) {
                LOGGER.error(ie);
                // This can only happen when problems with creating an internal helper
                // This error is already logged from the User class.
                ActionMessages errors = new ActionMessages();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.intact"));
                saveErrors(request, errors);
                return mapping.findForward(FAILURE);
            }
        }
        // Check the size
        if ((rw != null && rw.isTooLarge()) || (uniprotServiceResult != null && uniprotServiceResult.getProteins().size() > max )) {
            LOGGER.debug("ProteinSearchAction.execute 6");
            ActionMessages errors = new ActionMessages();
            errors.add("int.interact.search", new ActionMessage("error.int.interact.search.many",
                    Integer.toString(rw.getPossibleResultSize()), param, Integer.toString(max)));
            saveErrors(request, errors);
            setAnchor(request, intform);
            // Report back to the form.
            return mapping.getInputForward();
        }

        // Search found any results?
        if ((rw != null && rw.isEmpty()) || (uniprotServiceResult != null && uniprotServiceResult.getProteins().isEmpty())) {
            // The error to display on the web page.
            ActionMessages errors = new ActionMessages();
            // Log the error if we have one.
//            Exception exp = user.getProteinParseException();
//            if (exp != null) {
//                LOGGER.error("", exp);
//                errors.add("int.interact.search",
//                        new ActionMessage("error.int.interact.search.empty.parse", param + " : " + ac));
//            }else
            if(uniprotServiceResult != null && !uniprotServiceResult.getErrors().isEmpty() ){
                Map<String,String> map = uniprotServiceResult.getErrors();
                Set<Map.Entry<String,String>> set = map.entrySet();
                Iterator<Map.Entry<String,String>> iterator = set.iterator();
                while(iterator.hasNext()){
                    Map.Entry<String,String> entry = iterator.next();
                    LOGGER.error("An error occured while searching for protein : " + value + " : \n" + entry.getKey() +
                    "\n" + entry.getValue());
                }
                errors.add("int.interact.search",
                        new ActionMessage("error.int.interact.search.empty.parse", param + " : " + value));
            } else {
                errors.add("int.interact.search",
                        new ActionMessage("error.int.interact.search.empty", param + " : " + ac));
            }
            saveErrors(request, errors);
            setAnchor(request, intform);
            return mapping.getInputForward();
        }
        // Can safely cast it as we have the correct editor view bean.
        InteractionViewBean view = (InteractionViewBean) user.getView();

        if( rw != null && !rw.getResult().isEmpty()) {
            for (Iterator iter = rw.getResult().iterator(); iter.hasNext();) {
                Interactor interactor = (Interactor) iter.next();
                view.addInteractor(interactor);
            }
        }else if(uniprotServiceResult != null && !uniprotServiceResult.getProteins().isEmpty()){
            for(Protein protein : uniprotServiceResult.getProteins()){
                view.addInteractor(protein);
            }
        }else{
            throw new IntactException("If we have reach this line, it means that rw.getResult() and uniprotServiceResult.getProteins" +
                    " were empty which is normally not possible as we check it previously.");
        }


        // The anchor is set via the Search protein button.
        setAnchor(request, intform);
        return mapping.getInputForward();
    }
}