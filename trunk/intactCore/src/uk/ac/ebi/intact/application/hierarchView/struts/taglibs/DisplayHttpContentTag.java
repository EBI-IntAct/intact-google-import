/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchView.struts.taglibs;

import uk.ac.ebi.intact.application.hierarchView.business.Constants;
import uk.ac.ebi.intact.application.hierarchView.business.IntactUserI;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * That class allow to initialize properly the HTTPSession object
 * with what will be neededlater by the user of the web application.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */
public class DisplayHttpContentTag extends TagSupport {

    /**
     * Evaluate the tag's body content.
     */
    public int doStartTag() throws JspTagException {
        // evaluate the tag's body content and any sub-tag
        return EVAL_BODY_INCLUDE;
    } // doStartTag


    /**
     * Called when the JSP encounters the end of a tag. This will create the
     * option list.
     */
    public int doEndTag() throws JspException {
        HttpSession session = pageContext.getSession();

        try {
            IntactUserI user = (IntactUserI) session.getAttribute (Constants.USER_KEY);
            String urlStr = user.getSourceURL();

            if (urlStr == null) {
                // nothing to display
                return EVAL_PAGE;
            }

            URL url = null;
            try {
                url = new URL (urlStr);
            } catch (MalformedURLException me) {
                String decodedUrl = URLDecoder.decode (urlStr, "UTF-8");
                pageContext.getOut().write ("The source is malformed : <a href=\"" + decodedUrl +
                                             "\" target=\"_blank\">" + decodedUrl + "</a>" );
                return EVAL_PAGE;
            }

            // Retrieve the content of the URL
            StringBuffer httpContent = new StringBuffer();
            String tmpLine;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((tmpLine = reader.readLine()) != null) {
                    httpContent.append(tmpLine);
                }
                reader.close();
            } catch (IOException ioe) {
                 user.resetSourceURL();
                String decodedUrl = URLDecoder.decode (urlStr, "UTF-8");
                 pageContext.getOut().write ("Unable to display the source at : <a href=\"" + decodedUrl +
                                             "\" target=\"_blank\">" + decodedUrl + "</a>" );
                 return EVAL_PAGE;
            }

            // return the content to the browser
            pageContext.getOut().write (httpContent.toString());
            return EVAL_PAGE;

        } catch (Exception e) {
            e.printStackTrace();
            throw new JspException ("Error when trying to get HTTP content");
        }

    } // doEndTag

} // DisplaySourceTag