/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactConfigurator;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.context.impl.WebappSession;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * TODO: comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/08/2006</pre>
 */
public class StartupIntactListener implements ServletContextListener, HttpSessionListener
{
    private static final Log log = LogFactory.getLog(StartupIntactListener.class);

    private static final String FACES_INIT_DONE
            = StartupIntactListener.class.getName() + ".INTACT_INIT_DONE";

    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        IntactSession intactSession = new WebappSession(servletContextEvent.getServletContext(), null);

        boolean initDone = (intactSession.getApplicationAttribute(FACES_INIT_DONE) != null);

        if (!initDone) {
            // start the intact application (e.g. load Institution, etc)
            IntactConfigurator.initIntact(intactSession);

            intactSession.setApplicationAttribute(FACES_INIT_DONE, true);
        }

    }

    public void sessionCreated(HttpSessionEvent httpSessionEvent)
    {
        HttpSession session = httpSessionEvent.getSession();

        log.debug("Session started: "+session.getId());

        IntactSession intactSession = new WebappSession(session.getServletContext(), session);

        // start a intactContext for this session
        IntactConfigurator.createIntactContext(intactSession);

    }

    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        log.debug("ServletContext destroyed.");
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent)
    {
        log.debug("Session destroyed: "+httpSessionEvent.getSession().getId());
    }
}
