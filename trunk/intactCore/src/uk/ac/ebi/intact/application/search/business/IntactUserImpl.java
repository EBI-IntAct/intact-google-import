/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.search.business;

import java.util.*;

import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionBindingEvent;

import uk.ac.ebi.intact.persistence.*;
import uk.ac.ebi.intact.business.*;
import uk.ac.ebi.intact.model.*;

/**
 * This class stores information about an Intact Web user session. Instead of
 * binding multiple objects, only an object of this class is bound to a session,
 * thus serving a single access point for multiple information.
 * <p>
 * This class implements the <tt>ttpSessionBindingListener</tt> interface for it
 * can be notified of session time outs.
 *
 * @author Chris Lewington, Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class IntactUserImpl implements IntactUserIF, HttpSessionBindingListener {

    /**
     * Reference to the DAO.
     */
    private IntactHelper helper;

    /**
     * The search criteria.
     */
    private String searchCriteria;

    /**
     * Constructs an instance of this class with given mapping file and
     * the name of the data source class.
     *
     * @param mapping the name of the mapping file.
     * @param dsClass the class name of the Data Source.
     *
     * @exception DataSourceException for error in getting the data source; this
     *  could be due to the errors in repository files or the underlying
     *  persistent mechanism rejected <code>user</code> and
     *  <code>password</code> combination.
     * @exception IntactException thrown for any error in creating lists such
     *  as topics, database names etc.
     */
    public IntactUserImpl(String mapping, String dsClass)
        throws DataSourceException, IntactException {
        DAOSource ds = DAOFactory.getDAOSource(dsClass);

        // Pass config details to data source - don't need fast keys as only
        // accessed once
        Map fileMap = new HashMap();
        fileMap.put(Constants.MAPPING_FILE_KEY, mapping);
        ds.setConfig(fileMap);

        // build a helper for use throughout a session
        this.helper = new IntactHelper(ds);
    }

    // Implements HttpSessionBindingListener

    /**
     * Will call this method when an object is bound to a session.
     * Not doing anything.
     */
    public void valueBound(HttpSessionBindingEvent event) {
    }

    /**
     * Will call this method when an object is unbound from a session.
     */
    public void valueUnbound(HttpSessionBindingEvent event) {

        try {
            this.helper.closeStore();
        }
        catch(IntactException ie) {
            //failed to close the store - not sure what to do here yet....
        }
    }

    public String getSearchCritera() {
        return this.searchCriteria;
    }

    public Collection search(String objectType, String searchParam,
                              String searchValue) throws IntactException {
        // Set the search criteria.
        this.searchCriteria = searchParam;

        //now retrieve an object...
        return helper.search(objectType, searchParam, searchValue);
    }
}
