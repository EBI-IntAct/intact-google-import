/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchView.struts.taglibs;

import uk.ac.ebi.intact.application.hierarchView.business.PropertyLoader;
import uk.ac.ebi.intact.application.hierarchView.business.Constants;
import uk.ac.ebi.intact.application.hierarchView.business.IntactUserI;
import uk.ac.ebi.intact.application.hierarchView.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.application.hierarchView.business.image.ImageBean;
import uk.ac.ebi.intact.application.hierarchView.highlightment.HighlightProteins;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Properties;
import java.util.Collection;

import org.apache.log4j.Logger;



/**
 * That class allows to display in the browser the current interaction network
 * and the associated HTML MAP.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */
public class DisplayInteractionNetworkTag extends TagSupport {

    static Logger logger = Logger.getLogger (Constants.LOGGER_NAME);

    /**
     * Evaluate the tag's body content.
     */
    public int doStartTag() throws JspTagException {
        // evaluate the tag's body content and any sub-tag
        return EVAL_BODY_INCLUDE;
    } // doStartTag


    /**
     * Called when the JSP encounters the end of a tag.
     */
    public int doEndTag() throws JspException {
        HttpSession session = pageContext.getSession();

        try {
            IntactUserI user = (IntactUserI) session.getAttribute (Constants.USER_KEY);
            ImageBean imageBean   = user.getImageBean();
            String behaviour      = user.getBehaviour();
            InteractionNetwork in = user.getInteractionNetwork();

            /**
             * Apply an highlight if needed data are available
             */
            if (user.InteractionNetworkReadyToBeHighlighted()) {
                String methodClass = user.getMethodClass();
                HighlightProteins.perform (methodClass, behaviour, session, in) ;
            }

            /**
             *  Display only the picture if needed data are available
             */
            if (user.InteractionNetworkReadyToBeDisplayed()) {

                // Display the HTML code map
                pageContext.getOut().write (imageBean.getMapCode());

                // read the Graph.properties file
                String mapName = null;
                String format = null;

                Properties properties = PropertyLoader.load (uk.ac.ebi.intact.application.hierarchView.business.Constants.PROPERTY_FILE);

                if (null != properties) {
                    mapName = properties.getProperty ("hierarchView.image.map.name");
                    format = properties.getProperty ("hierarchView.image.format.name");
                } else {
                    logger.error("Unable to load properties from " +
                                 uk.ac.ebi.intact.application.hierarchView.business.Constants.PROPERTY_FILE);
                }


                // Prepare an identifier unique to the generated image
                String AC = user.getAC();
                int depth = user.getCurrentDepth();
                String method = user.getMethodClass();
                Collection keys = user.getKeys();
                String highlightContext = "";
                if (keys != null) {
                    // a highlight has been requested
                    highlightContext = (String) keys.iterator().next();
                    // only relevant to add the behaviour if one is applied
                    highlightContext += behaviour;
                }

                String userContext = AC + depth + method + highlightContext;

                /* the context parameter in the URL is given to prevent some browser
                 * (eg. Netscape 4.7) to cache image wrongly.
                 * If the image link were /hierarchView/GenerateImage, netscape don't even
                 * call the servlet and display cached image.
                 */
                String msg = "<p align=\"left\">\n"
                        + "  <center>"
                        + "     <img src=\"/hierarchView/GenerateImage?format=" + format
                        +        "&context="+ userContext +"\" "
                        + "      USEMAP=\"#" + mapName +"\" border =\"0\">"
                        + "     <br>"
                        + "  </center>"
                        + "</p>";

                pageContext.getOut().write (msg);
            }

        } catch (Exception ioe) {
            throw new JspException ("Error: could not display interaction network.");
        }
        return EVAL_PAGE;
    } // doEndTag

} // DisplayInteractionNetworkTag