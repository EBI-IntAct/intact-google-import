/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.hierarchView.business.tulip;

import org.apache.axis.utils.Options;
import org.apache.axis.client.AdminClient;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.io.InputStream;
import java.util.Properties;

import uk.ac.ebi.intact.application.hierarchView.business.PropertyLoader;
import uk.ac.ebi.intact.application.hierarchView.struts.Constants;

/**
 * Allows to deploy and undeploy the web service
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 */

public class WebServiceManager implements ServletContextListener {

    private String deploymentFile   = null;
    private String undeploymentFile = null;

    private boolean started = false;


    public String getDeploymentFile() {
        return deploymentFile;
    }

    public void setDeploymentFile(String deploymentFile) {
        this.deploymentFile = deploymentFile;
    }

    public String getUndeploymentFile() {
        return undeploymentFile;
    }

    public void setUndeploymentFile(String undeploymentFile) {
        this.undeploymentFile = undeploymentFile;
    }


    /**
     * Tells is the Manager is properly initialized
     * @return true is the manager is initialized, false in the other way round.
     */
    public boolean isInitialized () {
        return ((null != deploymentFile) && (null != undeploymentFile));
    }

    /**
     * Initialize the web service manager by reading the properties file
     * and setting needed files names.
     * @throws Exception
     */
    public void init () throws Exception {

        // The configuration file.
        String configFile = Constants.WEB_SERVICE_PROPERTY_FILE;

        System.out.println ("Loading web service's properties");
        Properties props = PropertyLoader.load (configFile);

        if (null != props) {
            String deploymentFile   = props.getProperty ("webService.deployment");
            String undeploymentFile = props.getProperty ("webService.undeployment");

            System.out.println ("Properties Loaded :" +
                                "\nwebService.deployment = "   + deploymentFile +
                                "\nwebService.undeployment = " + undeploymentFile);

            if ((null == deploymentFile) || (null == undeploymentFile)) {
                String msg = "Error, can't initialize WebServiceManager : unable to read properties file.";
                throw new Exception (msg);
            }

            this.setDeploymentFile (deploymentFile);
            this.setUndeploymentFile (undeploymentFile);

            System.out.println ("Properties loaded.");
        }
    } // init


    /**
     * Tells is the web service is running.
     * @return true is the web service is running, or false in the other way round.
     */
    public boolean isRunning () {
        return started;
    }

    /**
     * Undeploy the web service according to collected data
     * (i.e. undeployment file name)
     */
    public void undeploy () throws Exception {

        try {
            if (false == isInitialized()) init();
            processWsddFile (this.undeploymentFile);
            // web service stopped
            started = false;
        } catch (Exception e) {
          throw e;
        }
    } // undeploy


    /**
     * deploy the web service according to collected data
     * (i.e. deployment file name)
     */
    public void deploy () throws Exception {

        try {
            if (false == isInitialized()) init();
            processWsddFile (this.deploymentFile);
            // web service started
            started = true;
        } catch (Exception e) {
          throw e;
        }
    } // deploy


    /**
     * process the WSDD file given in parameter
     *
     * @param wsddFile can be a deployment or undeployment file
     * @throws Exception
     */
    public void processWsddFile (String wsddFile) throws Exception {
        String opts[] = new String[1];
        opts[0] = "";
        Options serviceOptions;

        try {
            serviceOptions = new Options(opts);
        }
        catch (Exception e) {
            serviceOptions = null;
        }

        AdminClient axisAdmin = new AdminClient();
        InputStream wsddStream = this.getClass().getResourceAsStream (wsddFile);

        if (wsddStream != null) {
            try {
                String reg = axisAdmin.process(serviceOptions, wsddStream);
                if (reg != null) {
                    // SUCCESS
                    return;
                }
                else {
                    // FAILURE
                    throw (new Exception ("Unable to process the WSDD file: " + wsddFile));
                }
            }
            catch (Exception e) {
                // REGISTRATION ERROR;
                throw (e);
            }
        }
        else
            throw (new Exception ("Unable to open the WSDD file: " + wsddFile));
    }  // processWsddFile


    // ****************************************
    // Implementation of ServletContextListener
    public void contextInitialized (ServletContextEvent event) {
    }


    public void contextDestroyed (ServletContextEvent event) {

        // UNDEPLOY THE WEB SERVICE
        // Could throw an exception if Axis is running on the same Tomcat server,
        // seems that connection with Axis are already closed when we try to stop
        // the web service.
        try {
            this.undeploy();
            System.out.println ("Tulip web service undeployed successfully");
        } catch (Exception e) {
            // TODO : log it
            System.out.println ("Unable to undeploy the web service\n" + e.getMessage());
            e.printStackTrace();
        }
    } // contextDestroyed

}  // WebServiceManager
