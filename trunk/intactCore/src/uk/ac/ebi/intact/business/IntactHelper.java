/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.business;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.VirtualProxy;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.query.Query;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.proxy.IntactObjectProxy;
import uk.ac.ebi.intact.persistence.*;
import uk.ac.ebi.intact.util.PropertyLoader;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


/**
 * <p>This class implements the business logic for intact. The requests to be
 * processed are usually obtained (in a webapp environment) from a struts
 * action class and then the business operations are carried out, via the DAO
 * interface to the data source. </p>
 *
 * @author Chris Lewington
 */
public class IntactHelper implements SearchI, Externalizable {

    // Inner static class ------------------------------------------------------

    public static final class PersistenceType {
        private final int myType;

        private PersistenceType(int type) {
            myType = type;
        }
    }

    // End of Inner class ------------------------------------------------------

    /** The Persistence Broker */
    public static final PersistenceType PB = new PersistenceType(0);

    /** The ODMG */
    public static final PersistenceType ODMG = new PersistenceType(1);

    /**
     * Path of the configuration file which allow to retrieve the
     * inforamtion related to the IntAct node we are running on.
     */
    private static final String INSTITUTION_CONFIG_FILE = "/config/Institution.properties";

    //initialise variables used for persistence
    private DAO dao;
    private DAOSource dataSource;

    //Logger is not serializable - but transient on its own doesn't work!
    private transient Logger pr;

    public void addCachedClass(Class clazz) {
        if (dao != null) {
            dao.addCachedClass(clazz);
        }
    }

    /**
     * Wipe the whole cache.
     */
    public void clearCache() {
        if (dao != null) dao.clearCache();
    }

    /**
     * True if an object of given class and AC exists in the cache.
     * @param clazz the object type.
     * @param ac the AC
     * @return true if an object is found in the cache.
     */
    public boolean isInCache(Class clazz, String ac) {
        return dao.isInCache(clazz, ac);
    }

    /**
     * Removes given object from the cache.
     * @param obj the object to clear from the OJB cache.
     */
    public void removeFromCache(Object obj) {
        dao.removeFromCache(obj);
    }

    /**
     * Removes given object from the cache.
     * @param clazz the object type.
     * @param ac the AC
     */
    public void removeFromCache(Class clazz, String ac) {
        dao.removeFromCache(clazz, ac);
    }

    /**
     * Method required for writing to disk via Serializable. Needed because
     * basic serialization does not work due to the use of the Log4J logger.
     * @param out The object output stream.
     * @throws IOException thrown if there were problems writing to disk.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        pr = null;
        out.writeObject(this);
    }

    /**
     * Used for serialization via Externalizable. Needed because basic serialization
     * does not work with the Log4J logger.
     * @param in The object input stream.
     * @throws IOException Thrwon if there were problems reading from disk.
     * @throws ClassNotFoundException thrown if the class definition needed to create
     * an instance is not available.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readObject();
        if (dataSource != null) pr = dataSource.getLogger();

    }

    public Logger getLogger() {
        return pr;
    }

    /**
     *  Constructor - requires a datasource to be provided.
     *
     * @param source - a datasource object from which a connection could be obtained. Note
     * that use of this constructor prevents optimisation of reflection and may result
     * in slower response times for eg initial searches.
     *
     * @exception IntactException - thrown if either the type map or source are invalid
     *
     */
    public IntactHelper(DAOSource source) throws IntactException {
        this(source, source.getUser(), source.getPassword());
    }

    /**
     *  Shortcut constructor
     *
     * @exception IntactException - thrown if either the type map or source are invalid
     *
     */
    public IntactHelper() throws IntactException {
        try {
            DAOSource ds = DAOFactory.getDAOSource("uk.ac.ebi.intact.persistence.ObjectBridgeDAOSource");
            initialize(ds, ds.getUser(), ds.getPassword(), PB);
        }
        catch (DataSourceException de) {
            String msg = "intact helper: There was a problem accessing a data store";
            throw new IntactException(msg, de);
        }
    }


    /**
     * Constructor allowing a helper instance to be created with a given
     * username and password.
     *
     * @param user - the username to make a connection with
     * @param password - the user's password (null values allowed)
     */
    public IntactHelper(String user, String password) throws IntactException {
        try {
            DAOSource ds = DAOFactory.getDAOSource("uk.ac.ebi.intact.persistence.ObjectBridgeDAOSource");
            initialize(ds, user, password, PB);
        }
        catch (DataSourceException de) {
            String msg = "intact helper: There was a problem accessing a data store";
            throw new IntactException(msg, de);
        }
    }

    /**
     * Constructor allowing a helper instance to be created with a given
     * data source, username and password.
     *
     * @param source - the data source to be connected to
     * @param user - the username to make a connection with
     * @param password - the user's password (null values allowed)
     */
    public IntactHelper(DAOSource source, String user, String password) throws IntactException {
        this(source, user, password, PB);
    }

    /**
     * Constructor allowing a helper instance to be created with a given
     * username and password.
     *
     * @param source - the data source to be connected to
     * @param user - the username to make a connection with
     * @param password - the user's password (null values allowed)
     * @param type the persistence type (e.g., ODMG or PB)
     */
    public IntactHelper(DAOSource source, String user, String password,
                        PersistenceType type) throws IntactException {
        initialize(source, user, password, type);
    }

    /**
     * validates a user's credentials against the data store.
     *
     * @param user the username
     * @param password the user password (may be null)
     *
     * @return boolean true if valid details, false if not (including a null username).
     */
//    public boolean isUserVerified(String user, String password) {
//        return dao.isUserValid(user, password);
//    }

    /**
     * close the data source. NOTE: performing this operation will
     * invalidate any outstanding transactions, and a call to open()
     * must be made before further store access can be made. Do not use this method
     * if you wish to continue data store operations in the same connection!
     * Note that any previous data may be lost, and a subsequent call to open()
     * will result in a new connection being established to the persitent store.
     *
     * @exception IntactException if the store was unable to be closed.
     */
    public void closeStore() throws IntactException {
        try {
            dao.close();
        }
        catch (DataSourceException dse) {
            throw new IntactException("failed to close data source!", dse);
        }
    }

    /**
     * Open the data source which was closed previously by {@link #closeStore()}
     * @throws IntactException for any errors in opening the data source.
     */
    public void openStore() throws IntactException {
        try {
            dao.open();
        }
        catch (DataSourceException dse) {
            throw new IntactException("failed to reopen the data source!", dse);
        }
    }

    /**
     * @return boolean true if a client-initiated transaction is in progress, false if not
     * or if there is no valid connection to the datastore.
     */
    public boolean isInTransaction() {
        return dao.isActive();
    }

    /**
     * Wrapper for uk.ac.ebi.intact.persistence#DAO(Object)
     * @param obj the object to check for.
     * @return <code>true</code> if <code>obj</code> is persisted or it has non
     * null primary key value (shouldn't do it).
     */
    public boolean isPersistent(Object obj) {
        return dao.isPersistent(obj);
    }

    /**
     * starts a business level transaction. This allows finer grained
     * transaction management of business operations (eg for performing
     * a number of creates/deletes within one unit of work). You can choose, by
     * specifying the appropriate parameter, which level of transaction you want:
     * either object level or JDBC (ie relational) level. If an unkown type is supplied
     * then the transaction type defaults to JDBC.
     * @param transactionType The type of transaction you want (relational or object).
     * Use either BusinessConstants.OBJECT_TX or BusinessConstants.JDBC_TX.
     *
     * @exception IntactException thrown usually if a transaction is already running
     */
    public void startTransaction(int transactionType) throws IntactException {
        // The default transaction type is JDBC.
        int txType = BusinessConstants.JDBC_TX;
        if (transactionType == BusinessConstants.OBJECT_TX) {
            txType = BusinessConstants.OBJECT_TX;
        }
        try {
            dao.begin(txType);
        }
        catch (TransactionException e) {
            throw new IntactException("unable to start an intact transaction!", e);
        }
    }

    /**
     * Locks the given object for <b>write</b> access.
     * <b>{@link #startTransaction(int)} must be called to prior to this method.</b>
     * @param cvobj the object to lock for <b>write</b> access.
     */
    public void lock(CvObject cvobj) {
        dao.lock(cvobj);
    }

    /**
     * ends a busines transaction. This method should be used if it is required
     * to manage business transactionas across multiple operations (eg for
     * performing a number of creates/deletes in one unit of work).
     *
     * @exception IntactException thrown usually if there is no transaction in progress
     */
    public void finishTransaction() throws IntactException {
        try {
            dao.commit();
        }
        catch (Exception e) {
            throw new IntactException("unable to complete an intact transaction!", e);
        }
    }

    /**
     * unwraps a transaction. This method is of use when a "business unit of work"
     * fails for some reason and the operations within it should not affect the
     * persistent store.
     *
     * @exception IntactException thrown usually if a transaction is not in progress
     */
    public void undoTransaction() throws IntactException {
        try {
            dao.rollback();
        }
        catch (Exception e) {
            throw new IntactException("unable to undo an intact transaction!", e);
        }
    }

    /**
     * Provides the database name that is being connected to.
     * @return String the database name, or an empty String if the query fails
     */
    public String getDbName() {
        return dao.getDbName();
    }

    /**
     * Provides the user name that is connecting to the DB.
     * @return String the user name, or an empty String if the query fails
     * @exception org.apache.ojb.broker.accesslayer.LookupException thrown on error
     * getting the Connection
     * @exception SQLException thrown if the metatdata can't be obtained
     */
    public String getDbUserName() throws LookupException, SQLException {
        return dao.getDbUserName();
    }


    /**
     *  This method provides a create operation for intact objects.
     *
     * @param objects - a collection of intact objects to be created
     *
     * @exception IntactException - thrown if a problem arises during the creation process
     *
     */
    public void create(Collection objects) throws IntactException {
        try {
            dao.makePersistent(objects);
        }
        catch (CreateException ce) {
            String msg = "intact helper: object creation failed.. \n";
            throw new IntactException(msg, ce);
        }
        catch (TransactionException te) {
            String msg = "intact helper: transaction problem during object creation.. \n";
            throw new IntactException(msg, te);
        }
    }

    /**
     *  This method provides a delete operation.
     *
     * @param obj -obj to be deleted
     *
     * @exception IntactException - thrown if a problem arises during the deletion
     *
     */
    public void delete(Object obj) throws IntactException {
        // Only delete it if it is persistent.
        if (!isPersistent(obj)) {
            return;
        }
        try {
            dao.remove(obj);
        }
        catch (TransactionException de) {
            String msg = "intact helper: failed to delete object of type " + obj.getClass().getName();
            throw new IntactException(msg, de);
        }
    }

    /**
     * Convenience method to create a single object in persistent store.
     *
     * @param obj The object to be created
     *
     * @exception IntactException thrown if the creation failed
     */
    public void create(Object obj) throws IntactException {
        try {
            dao.create(obj);
        }
        catch (CreateException ce) {
            ce.printStackTrace();
            String msg = "intact helper: single object creation failed for class " + obj.getClass().getName();
            throw new IntactException(msg, ce);
        }
    }

    /**
     *  This method provides an update operation.
     *
     * @param obj -obj to be updated
     *
     * @exception IntactException - thrown if a problem arises during the update
     *
     */
    public void update(Object obj) throws IntactException {
        // No force updating.
        update(obj, false);
    }

    /**
     * This method forces the given object to update. This is needed because
     * OJB does not seem to set the dirty marker properly (e.g., if a
     * collection size remains unchanged and the OJJB uses the size to work out
     * if the object is marked as dirty).
     * @param obj the object to update.
     * @exception IntactException - thrown if a problem arises during the update
     */
     public void forceUpdate(Object obj) throws IntactException {
        // Force the update
        update(obj, true);
    }

    /**
     * Cancels the update for the given object.
     * @param obj the object to cancel the update for.
     */
    public void cancelUpdate(Object obj) {
        dao.removeFromCache(obj);
    }

    /*------------------------ search facilities ---------------------------------------

    //methods implemented from the SearchI interface...
    /**
    *  Not Yet Fully Implemented.
     *  @see SearchI
     */
    public Collection paramSearch(String objectType, String searchParam, String searchValue,
                                  boolean includeSubClass,
                                  boolean matchSubString) throws IntactException {
        throw new IntactException("Not Fully Implemented yet");
    }


    /**
     * Not Yet Fully Implemented.
     * @see SearchI
     */
    public List stringSearch(String objectType, String searchString,
                             boolean includeSubClass,
                             boolean matchSubString) throws IntactException {
        throw new IntactException("Not Fully Implemented yet");
    }

    //other various search methods......

    /**
     * Search which provides an Iterator rather than a Collection. This is particularly
     * useful for applications which may need to close result data for DB resource
     * reasons (the iterator from this method can be passed to <code>closeResults</code>).
     * Note that this will NOT close oracle cursors on the oracle server, as it seems the
     * only guranteed way to do that is to 'cose' the connection (preferably via an oracle
     * connection pool supplied with the driver).
     * @param objectType - the object type to be searched
     * @param searchParam - the parameter to search on (eg field)
     * @param searchValue - the search value to match with the parameter
     *
     * @return Iterator - a "DB aware" Iterator over the results - null if no match found
     *
     * @exception IntactException - thrown if problems are encountered during the search process
     */
//    public Iterator iterSearch(String objectType, String searchParam, String searchValue)
//            throws IntactException {
//        try {
//            return dao.iteratorFind(objectType, searchParam, searchValue);
//        }
//        catch (SearchException se) {
//            //return to action servlet witha forward to error page command
//            String msg = "intact helper: query by Iterator failed.. \n";
//            throw new IntactException(msg + "reason: " + se.getNestedMessage(), se.getRootCause());
//        }
//    }

    /**
     * Allows access to column data rather than whole objects. This is more of a convenience
     * method if you want to get at some data without retrieving complete objects.
     * @param type The name of the object you want data from (fully qualified java name) - must be specified
     * @param cols The columns (NOT attribute names) that you are interested in - null returns all
     * @return Iterator an Iterator over all column values, returned as Objects (null if nothing found),
     * NB if you do not exhaust this Iterator you should make sure datastore resources are
     * cleaned up by passing it to the <code>closeData</code> method.
     * @throws IntactException
     */
//    public Iterator getColumnData(String type, String[] cols) throws IntactException {
//        try {
//            return dao.findColumnValues(type, cols);
//        }
//        catch (SearchException se) {
//            throw new IntactException("failed to get column data!", se);
//        }
//    }

    /**
     * Allows search by raw SQL String. This should only be used if you are confident
     * with SQL syntaxt and you know what you are doing - no gurantees are made on the
     * results (garbage in, garbage out).
     * @param type The object you are interested in - must be specified
     * @param sqlString The SQL string you wish to search with
     * @return Collection The search results, empty if none found
     * @throws IntactException
     */
//    public Collection searchBySQL(String type, String sqlString) throws IntactException {
//        try {
//            return dao.findBySQL(type, sqlString);
//        }
//        catch (SearchException se) {
//            throw new IntactException("failed to execute SQL string " + sqlString, se);
//        }
//    }

    /**
     * Closes result data.
     * @param items The Iterator used to retrieve the data originally.
     * @exception IllegalArgumentException thrown if the Iterator is an invalid type
     */
//    public void closeData(Iterator items) {
//        dao.closeResults(items);
//    }

    /**
     *  This method provides a means of searching intact objects, within the constraints
     * provided by the parameters to the method. NB this will probably become private, and replaced
     * for public access by paramSearch...
     *
     * @param objectType - the object type to be searched
     * @param searchParam - the parameter to search on (eg field)
     * @param searchValue - the search value to match with the parameter
     *
     * @return Collection - the results of the search (empty Collection if no matches found)
     *
     * @exception IntactException - thrown if problems are encountered during the search process
     */
    public Collection search(String objectType, String searchParam, String searchValue) throws IntactException {
        //now retrieve an object...
        try {

            long timer = System.currentTimeMillis();

            Collection resultList = dao.find(objectType, searchParam, searchValue);

            long tmp = System.currentTimeMillis();
            timer = tmp - timer;
            pr.info("**************************************************");
            pr.info("intact helper: time spent in DAO find (ms): " + timer);
            pr.info("**************************************************");
            return resultList;
        }
        catch (SearchException se) {
            se.printStackTrace();
            //return to action servlet witha forward to error page command
            String msg = "intact helper: unable to perform search operation.. \n";
            throw new IntactException(msg + "reason: " + se.getNestedMessage(), se.getRootCause());

        }
    }

    /**
     *  This method provides a means of searching intact objects, within the constraints
     * provided by the parameters to the method. NB this will probably become private, and replaced
     * for public access by paramSearch...
     *
     * @param searchClass - the class to search
     * @param searchParam - the parameter to search on (eg field)
     * @param searchValue - the search value to match with the parameter
     *
     * @return Collection - the results of the search (empty Collection if no matches found)
     *
     * @exception IntactException - thrown if problems are encountered during the search process
     */
    public Collection search(Class searchClass, String searchParam, String searchValue) throws IntactException {
        //now retrieve an object...
        try {
            return dao.find(searchClass, searchParam, searchValue);
        }
        catch (SearchException se) {
            se.printStackTrace();
            //return to action servlet witha forward to error page command
            String msg = "intact helper: unable to perform search operation.. \n";
            throw new IntactException(msg + "reason: " + se.getNestedMessage(), se.getRootCause());
        }
    }

    /**
     * Searches for objects by classname and Xref (primaryId).
     *
     * @param clazz the class we are looking for
     * @param aPrimaryId the primaryId of the Xref
     *
     * @return a Collection of object of type clazz for which a Xref having the given primaryId has been found.
     */
    public Collection getObjectsByXref(Class clazz,
                                       String aPrimaryId) throws IntactException {

        // get the Xref from the database
        Collection xrefs = this.search(Xref.class.getName(), "primaryId", aPrimaryId);
        Collection results = new ArrayList();

        // add all referenced objects of the searched class
        for (Iterator iterator = xrefs.iterator(); iterator.hasNext();) {
            Xref xref = (Xref) iterator.next();
            results.addAll(this.search(clazz.getName(), "ac", xref.getParentAc()));
        }
        return results;
    }

    /**
     * Searches for objects by classname and Xref.
     *
     * @param clazz the class we are looking for
     * @param database the CvDatabase of the Xref that links back to the object of type clazz
     * @param aPrimaryId the primaryId of the Xref
     *
     * @return a Collection of object of type clazz for which a Xref having the given primaryId
     *         and CvDatabase has been found.
     */
    public Collection getObjectsByXref( Class clazz,
                                        CvDatabase database,
                                        String aPrimaryId ) throws IntactException {

        // get the Xref from the database
        Collection xrefs = this.search( Xref.class.getName(), "primaryId", aPrimaryId );
        Collection results = new ArrayList();

        // add all referenced objects of the searched class
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();
            // if the CvDatabase are the same (null or not), we add the parent.
            if( ( null != database && database.equals( xref.getCvDatabase() ) )
                ||
                ( null == database && null == xref.getCvDatabase() ) ) {
                results.addAll( this.search( clazz.getName(), "ac", xref.getParentAc() ) );
            }
        }
        return results;
    }

    /**
     * Searches for objects by classname and Xref.
     *
     * @param clazz the class we are looking for
     * @param database the CvDatabase of the Xref that links back to the object of type clazz
     * @param qualifier the CvXrefQualifier of the Xref that links back to the object of type clazz
     * @param aPrimaryId the primaryId of the Xref
     *
     * @return a Collection of object of type clazz for which a Xref having the given primaryId
     *         and CvDatabase has been found.
     */
    public Collection getObjectsByXref( Class clazz,
                                        CvDatabase database,
                                        CvXrefQualifier qualifier,
                                        String aPrimaryId ) throws IntactException {

        // get the Xref from the database
        Collection xrefs = this.search( Xref.class.getName(), "primaryId", aPrimaryId );
        Collection results = new ArrayList();

        // add all referenced objects of the searched class
        for ( Iterator iterator = xrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();
            // if the CvDatabase are the same (null or not), we add the parent.
            if( ( null != database && database.equals( xref.getCvDatabase() ) )
                ||
                ( null == database && null == xref.getCvDatabase() ) ) {

                if( ( null != qualifier && qualifier.equals( xref.getCvXrefQualifier() ) )
                    ||
                    ( null == qualifier && null == xref.getCvXrefQualifier() ) ) {

                    results.addAll( this.search( clazz.getName(), "ac", xref.getParentAc() ) );
                }
            }
        }
        return results;
    }

    /** Searches for a unique Object by classname and Xref.
     *  Currently this searches only by primaryId.
     *  Should search by database and primaryId.
     */
    public Object getObjectByXref(Class clazz,
                                  String aPrimaryId) throws IntactException {

        Collection results = getObjectsByXref(clazz, aPrimaryId);

        //should be unique...
        if (results.size() > 1) {
            throw new IntactException("error - more than one result returned with query by "
                    + aPrimaryId + " ");
        } else {
            if (results.isEmpty()) {
                return null;
            }
            Iterator it = results.iterator();
            return it.next();
        }
    }


    /**
     * Search for objects by any alias with the specified name.
     *
     * @param clazz the class filter.
     *              All objects in the returned collection will be instance of that class.
     * @param aliasName the name of the alias to look for.
     * @return the collection of objects of type <code>clazz</code> who have an alias
     *         named <code>aliasName</code>
     * @throws IntactException when an error occurs when searching.
     * @throws IllegalArgumentException if the name of the Alias is not specified or empty.
     * @see uk.ac.ebi.intact.model.Alias
     */
//    public Collection getObjectsByAlias(Class clazz,
//                                        String aliasName) throws IntactException {
//
//        if (aliasName == null || "".equals(aliasName))
//            throw new IllegalArgumentException("You have requested a search by alias, but the specified alias name is null.");
//
//        // get the Alias from the database
//        Collection aliases = search(Alias.class.getName(), "name", aliasName);
//        Collection results = new ArrayList();
//
//        // add all referenced objects of the searched class
//        for (Iterator iterator = aliases.iterator(); iterator.hasNext();) {
//            Alias alias = (Alias) iterator.next();
//            results.addAll(search(clazz.getName(), "ac", alias.getParentAc()));
//        }
//        return results;
//    }

    /**
     * Search for objects by any alias with the specified name.
     *
     * @param clazz the class filter.
     *              All objects in the returned collection will be instance of that class.
     * @param aliasName the name of the alias to look for.
     * @param aliasTypeShortLabel the shortLabel of the alias type we are interrested in.
     * @return the collection of objects of type <code>clazz</code> who have an alias
     *         named <code>aliasName</code>
     * @throws IntactException When the requested aliasType is not found or an error occurs when searching.
     * @throws IllegalArgumentException if the name of the Alias is not specified or empty.
     * @see uk.ac.ebi.intact.model.Alias
     */
//    public Collection getObjectsByAlias(Class clazz,
//                                        String aliasName,
//                                        String aliasTypeShortLabel) throws IntactException {
//
//        if (aliasName == null || "".equals(aliasName))
//            throw new IllegalArgumentException("You have requested a search by alias, but the specified alias name is null.");
//
//        if (aliasTypeShortLabel == null)
//            throw new IllegalArgumentException("You have requested a search by alias (" + aliasName +
//                    ") but the specified alias type is null.");
//
//        CvAliasType cvAliasType = (CvAliasType) getObjectByLabel(CvAliasType.class, aliasTypeShortLabel);
//        if (cvAliasType == null)
//            throw new IntactException("The requested CvAliasType (" + aliasTypeShortLabel + ") could not be found in the database.");
//
//        return getObjectsByAlias(clazz, aliasName, cvAliasType);
//    }

    /**
     * Search for objects by any alias with the specified name.
     *
     * @param clazz the class filter.
     *              All objects in the returned collection will be instance of that class.
     * @param aliasName the name of the alias to look for.
     * @param cvAliasType the type of the alias we are interrested in.
     * @return the collection of objects of type <code>clazz</code> who have an alias
     *         named <code>aliasName</code>
     * @throws IntactException When the requested aliasType is not found or an error occurs when searching.
     * @throws IllegalArgumentException if the name of the Alias is not specified or empty.
     * @see uk.ac.ebi.intact.model.Alias
     */
//    public Collection getObjectsByAlias(Class clazz,
//                                        String aliasName,
//                                        CvAliasType cvAliasType) throws IntactException {
//
//        // get the Xref from the database
//        Collection aliases = this.search(Alias.class.getName(), "name", aliasName);
//        Collection results = new ArrayList();
//
//        // add all referenced objects of the searched class
//        for (Iterator iterator = aliases.iterator(); iterator.hasNext();) {
//            Alias alias = (Alias) iterator.next();
//            if (!alias.getCvAliasType().equals(cvAliasType))
//                continue; // we use only aliases mathing with the requested CvAliasType
//            results.addAll(this.search(clazz.getName(), "ac", alias.getParentAc()));
//        }
//        return results;
//    }


    /** Return an Object by classname and shortLabel.
     *  For efficiency, classes which are subclasses of CvObject are cached
     *  if the label is unique.
     *
     */
    public Object getObjectByLabel(Class clazz,
                                   String label) throws IntactException {

        /** Algorithm sketch:
         *  if (clazz is a controlled vocabulary class){
         *     if (not (clazz is cached)){
         *        load clazz into cache
         *     }
         *     return element from cache;
         *  return element from search;
         */

        Object result = null;

        /*
        if (isCachedClass(clazz)){
            result = cache.get(clazz + "-" + label);
             if (null != result){
                 return result;
             }
         }
         */

        Collection resultList = null;

        resultList = this.search(clazz, "shortLabel", label);

        if (resultList.isEmpty()) {
            result = null;
        } else {
            Iterator i = resultList.iterator();
            result = i.next();
            if (i.hasNext()) {
                IntactException ie = new DuplicateLabelException(label, clazz.getName());
                throw(ie);
            }
        }

        /*
        if (isCachedClass(clazz)){
            cache.put(clazz + "-" + label,result);
        }
        */

        return result;
    }

    /** Return an Object by classname and ac.
     *
     */
    public Object getObjectByAc(Class clazz,
                                String ac) throws IntactException {

        Object result = null;

        Collection resultList = null;

        resultList = this.search(clazz.getName(), "ac", ac);

        if (resultList.isEmpty()) {
            result = null;
        } else {
            Iterator i = resultList.iterator();
            result = i.next();
            if (i.hasNext()) {
                IntactException ie = new DuplicateLabelException(ac, clazz.getName());
                throw(ie);
            }
        }

        return result;
    }

    /**
     *  This method retrieves an intact object, given a class and the object's full
     * name (provided that name is unique).
     *
     * @param clazz the class to search on
     * @param name the name of the object required
     *
     * @return Object the result of the search, or null if not found
     *
     * @exception IntactException thrown if a search problem occurs
     *
     */
//    public Object getObjectByName(Class clazz,
//                                  String name) throws IntactException {
//
//        Object result = null;
//
//        Collection resultList = null;
//
//        resultList = this.search(clazz.getName(), "fullName", name);
//
//        if (resultList.isEmpty()) {
//            result = null;
//        } else {
//            Iterator i = resultList.iterator();
//            result = i.next();
//            if (i.hasNext()) {
//                IntactException ie = new IntactException("More than one object returned by search by name.");
//                throw(ie);
//            }
//        }
//
//        return result;
//    }

    /**
     *  This method is used for searching by short label (provided that name is unique).
     *
     * @param name the name of the BioSource required
     *
     * @return BioSource the result of the search, or null if not found
     *
     * @exception IntactException thrown if a search problem occurs, or more than
     * one BioSource found
     *
     */
//    public BioSource getBioSourceByName(String name) throws IntactException {
//
//        Collection resultList = null;
//        resultList = this.search("uk.ac.ebi.intact.model.BioSource", "shortLabel", name);
//
//        if (resultList.isEmpty()) return null;
//        if (resultList.size() > 1) throw new IntactException("More than one BioSource returned by name search");
//        return (BioSource) resultList.iterator().next();
//    }

    /**
     * Searches for a BioSource given a tax ID. Only a single BioSource is found
     * for given tax id and null values for cell type and tissue.
     *
     * @param taxId The tax ID to search on - should be unique
     * @return BioSource The matching BioSource object, or null if none found (for the
     *         combination of tax id, cell and tissue)
     * @throws IntactException thrown if there was a search problem.
     */
    public BioSource getBioSourceByTaxId( String taxId ) throws IntactException {

        //List of biosurce objects for given tax id
        Collection results = search( BioSource.class.getName(), "taxId", taxId );

        // Get the biosource with null values for cell type and tisse
        //  (there is only one of them exists).
        for ( Iterator iter = results.iterator(); iter.hasNext(); ) {
            BioSource biosrc = (BioSource) iter.next();
            if( ( biosrc.getCvCellType() == null ) && ( biosrc.getCvTissue() == null ) ) {
                return biosrc;
            }
        }
        // None found.
        return null;
    }


    /**
     *  This method is used for obtaining an interactor given a specific BioSource and
     * the subclass of Interactor that is to be searched for. NB is it true that a match would
     * be unique??
     *
     * @param clazz the subclass of Interactor to search on
     * @param source the BioSource to search with - must be fully defined or at least AC set
     *
     * @return Collection the list of Interactors that have the given BioSource, or empty if none found
     *
     * @exception IntactException thrown if a search problem occurs
     * @exception NullPointerException if source or class is null
     * @exception IllegalArgumentException if the class parameter is not assignable from Interactor
     *
     * NB Not tested yet - BioSource data in DB required
     *
     */
    public Collection getInteractorBySource(Class clazz, BioSource source) throws IntactException {

        if (source == null) throw new NullPointerException("Need a BioSource to search by BioSource!");
        if (clazz == null) throw new NullPointerException("Class is null for Interactor/BioSource search!");
        if (!Interactor.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Cannot do Interactor search - Class "
                    + clazz.getName() + "is not a subclass of Interactor");

        return (this.search(Interactor.class.getName(), "bioSource_ac", source.getAc()));

    }


    /**
     *  Search for an experiment given a specific BioSource. NB is it true that a match would
     * be unique?? Object model suggests a 1-1 match...
     *
     * @param source the BioSource to search with - must be fully defined or at least AC set
     *
     * @return Experiment the result of the search, or empty if none found
     *
     * @exception IntactException thrown if a search problem occurs
     *
     * NB not tested yet!! (requires data in the DB)
     *
     */
//    public Experiment getExperimentBySource(BioSource source) throws IntactException {
//
//        Experiment result = null;
//        Collection resultList = null;
//        String bioAc = source.getAc();
//        resultList = this.search(Experiment.class.getName(), "bioSource_ac", bioAc);
//        if (resultList.isEmpty()) {
//            result = null;
//        } else {
//            Iterator i = resultList.iterator();
//            result = (Experiment) i.next();
//            if (i.hasNext()) {
//                IntactException ie = new IntactException("More than one object returned by search by bioSource.");
//                throw(ie);
//            }
//        }
//
//        return result;
//    }

    /**
     *  Search for components given a role. Assumed this is unlikely to be unique.
     *
     * @param role the role to search with - must be fully defined or at least AC set
     *
     * @return Collection the result of the search, or an empty Collection if not found
     *
     * @exception IntactException thrown if a search problem occurs
     *
     * NB Not tested yet!!
     *
     */
//    public Collection getComponentsByRole(CvComponentRole role) throws IntactException {
//
//        return (this.search(Component.class.getName(), "role", role.getAc()));
//
//    }

    /**
     *  Search for interactions given a type. Assumed this is unlikely to be unique.
     *
     * @param type the type to search with - must be fully defined or at least AC set
     *
     * @return Collection the result of the search, or an empty Collection if not found
     *
     * @exception IntactException thrown if a search problem occurs
     *
     * NB Not tested yet!!
     *
     */
//    public Collection getInteractionsByType(CvInteractionType type) throws IntactException {
//
//        return (this.search(Interaction.class.getName(), "interactionType_ac", type.getAc()));
//    }


    /**
     *  Search for Experiments given an Institution. Assumed this is unlikely to be unique.
     * Note: If the Institution AC is not set, a search will first be carried out for it
     * using its shortLabel - thus only Institutions that are persistent will provide
     * a non-empty search result. If more than one Institution is returned,
     * the first one's AC will be used.
     *
     * @param institution the institution to search with
     *
     * @return Collection the result of the search, or an empty Collection if not found
     *
     * @exception IntactException thrown if a search problem occurs
     *
     */
//    public Collection getExperimentsByInstitution(Institution institution) throws IntactException {
//
//        String ownerAc = institution.getAc();
//        if (ownerAc == null) {
//            //get it first via shortLabel
//            Collection tmpResults = this.search(Institution.class.getName(), "shortLabel", institution.getShortLabel());
//            if (!tmpResults.isEmpty()) {
//                //take the first one
//                Institution inst = (Institution) tmpResults.iterator().next();
//                ownerAc = inst.getAc();
//            }
//        }
//        return (this.search(Experiment.class.getName(), "owner_ac", ownerAc));
//    }

    /**
     *  Search for Protein given a cross reference. Assumed this is unique.
     *
     * @param xref the cross reference to search with - must be fully defined, or
     * at least have its parent AC field set
     *
     * @return Protein the result of the search, or null if not found
     *
     * @exception IntactException thrown if a search problem occurs, the Xref does
     * not match a Protein, or we get more than one Protein back
     *
     * NB not yet tested....
     *
     */
//    public Protein getProteinByXref(Xref xref) throws IntactException {
//
//        Collection resultList = null;
//        resultList = this.search(Protein.class.getName(), "ac", xref.getParentAc());
//        if (resultList.isEmpty()) return null;
//        if (resultList.size() > 1) throw new IntactException("Got more than one Protein with Xref " + xref.getAc());
//
//        Object obj = resultList.iterator().next();
//        if (!(obj instanceof Protein))
//            throw new IntactException("Xref refers to an instance of "
//                    + obj.getClass().getName() + ", NOT a Protein!!");
//
//        return (Protein) obj;
//    }

    /**
     * @see uk.ac.ebi.intact.persistence.DAO#getTableName(Class)
     */
//    public String getTableName(Class clazz) {
//        return dao.getTableName(clazz);
//    }

    /**
     * Delete all elements in a collection.
     */
    public void deleteAllElements(Collection aCollection) throws IntactException {
        try {
            for (Iterator i = aCollection.iterator(); i.hasNext();) {
                dao.remove(i.next());
            }
        }
        catch (TransactionException te) {
            String msg = "intact helper: error deleting collection elements";
            throw new IntactException(msg, te);
        }
    }

    /**
     *  Used to obtain the Experiments related to a search object. For example, if the object is a
     * Protein then we are interested in the Interactions and also the Experiments that the Protein
     * is related to. In practice this therefore means that we only need the Experiments, since
     * the Interactions can be obtained from those.
     *
     * @param item A search result item (though anything can be tried!)
     * @return Collection The Experiments related to the parameter item
     * (empty if none found)
     */
//    public Collection getRelatedExperiments(Object item) {
//
//        Collection results = new ArrayList();
//        if (item instanceof Protein) {
//            //collect all the related experiments (interactions can be displayed from them)
//            Protein protein = (Protein) item;
//            Collection components = protein.getActiveInstances();
//            Iterator iter1 = components.iterator();
//            while (iter1.hasNext()) {
//                Component component = (Component) iter1.next();
//                Interaction interaction = component.getInteraction();
//                Collection experiments = interaction.getExperiments();
//                Iterator iter2 = experiments.iterator();
//                while (iter2.hasNext()) {
//                    results.add(iter2.next());
//                }
//            }
//        }
//
//        if (item instanceof Interaction) {
//            //collect all experiments....
//            Collection experiments = ((Interaction) item).getExperiments();
//            Iterator iter3 = experiments.iterator();
//            while (iter3.hasNext()) {
//                results.add(iter3.next());
//            }
//        }
//
//        return results;
//    }

    /**
     * Gets the underlying JDBC connection. This is a 'useful method' rather than
     * a good practice one as it returns the underlying DB connection (and assumes there is one).
     * No guarantees - if you screw up the Connection you are in trouble!.
     *
     * @return Connection a JDBC Connection, or null if the DAO you are using is not
     * an OJB one.
     * @throws IntactException thrown if there was a problem getting the connection
     */
    public Connection getJDBCConnection() throws IntactException {

        if (dao instanceof ObjectBridgeDAO) {
            try {
                return ((ObjectBridgeDAO) dao).getJDBCConnection();
            } catch (LookupException le) {
                throw new IntactException("Failed to get JDBC Connection!", le);
            }
        }
        return null;
    }


    /**
     * Allow the user not to know about the it's Institution, it has to be configured once
     * in the properties file: ${INTACTCORE_HOME}/config/Institution.properties and then
     * when calling that method, the Institution is either retreived or created according
     * to its shortlabel.
     *
     * @return the Institution to which all created object will be linked.
     */
    public Institution getInstitution() throws IntactException {
        Institution institution = null;

        Properties props = PropertyLoader.load(INSTITUTION_CONFIG_FILE);
        if (props != null) {
            String shortlabel = props.getProperty("Institution.shortLabel");
            if (shortlabel == null || shortlabel.trim().equals(""))
                throw new IntactException("Your institution is not properly configured, check out the configuration file:" +
                        INSTITUTION_CONFIG_FILE + " and set 'Institution.shortLabel' correctly");

            // search for it (force it for LC as short labels must be in LC).
            shortlabel = shortlabel.trim();
            Collection result = search(Institution.class.getName(), "shortLabel", shortlabel);

            if (result.size() == 0) {
                // doesn't exist, create it
                institution = new Institution(shortlabel);

                String fullname = props.getProperty("Institution.fullName");
                if (fullname != null) {
                    fullname = fullname.trim();
                    if (!fullname.equals(""))
                        institution.setFullName(fullname);
                }


                String lineBreak = System.getProperty("line.separator");
                StringBuffer address = new StringBuffer(128);
                String line = props.getProperty("Institution.postalAddress.line1");
                if (line != null) {
                    line = line.trim();
                    if (!line.equals("")) {
                        address.append(line).append(lineBreak);
                    }
                }

                line = props.getProperty("Institution.postalAddress.line2");
                if (line != null) {
                    line = line.trim();
                    if (!line.equals(""))
                        address.append(line).append(lineBreak);
                }

                line = props.getProperty("Institution.postalAddress.line3");
                if (line != null) {
                    line = line.trim();
                    if (!line.equals(""))
                        address.append(line).append(lineBreak);
                }

                line = props.getProperty("Institution.postalAddress.line4");
                if (line != null) {
                    line = line.trim();
                    if (!line.equals(""))
                        address.append(line).append(lineBreak);
                }

                line = props.getProperty("Institution.postalAddress.line5");
                if (line != null) {
                    line = line.trim();
                    if (!line.equals(""))
                        address.append(line).append(lineBreak);
                }

                if (address.length() > 0) {
                    address.deleteCharAt(address.length() - 1); // delete the last line break;
                    institution.setPostalAddress(address.toString());
                }

                String url = props.getProperty("Institution.url");
                if (url != null) {
                    url = url.trim();
                    if (!url.equals(""))
                        institution.setUrl(url);
                }

                this.create(institution);

            } else {
                // return the object found
                institution = (Institution) result.iterator().next();
            }

        } else {
            throw new IntactException("Unable to read the properties from " + INSTITUTION_CONFIG_FILE);
        }

        return institution;
    }

    public int getCountByQuery(Query query) {
        return dao.getCountByQuery(query);
    }

    public Collection getCollectionByQuery(Query query) {
        return dao.getCollectionByQuery(query);
    }

    public Iterator getIteratorByReportQuery(Query query) {
        return dao.getIteratorByReportQuery(query);
    }
    
    /**
     * Returns the real object wrapped around the proxy for given object of
     * IntactObjectProxy type.
     * @param obj the object to return the real object from.
     * @return the real object wrapped around the proxy for given object of
     * IntactObjectProxy type; otherwise, <code>obj</code> is returned.
     */
    public static IntactObject getRealIntactObject(IntactObject obj) {
        if (IntactObjectProxy.class.isAssignableFrom(obj.getClass())) {
            return (IntactObject) ((IntactObjectProxy) obj).getRealSubject();
        }
        return obj;
    }

    /**
     * Gives the Object classname, give the real object class name if this is a VirtualProxy class
     *
     * @param obj the object for which we request the real class name.
     * @return the real class name.
     *
     * @see org.apache.ojb.broker.VirtualProxy
     */
    public static Class getRealClassName(Object obj) {
        Class name = null;

        if (obj instanceof VirtualProxy) {
            name = ((IntactObjectProxy) obj).getRealClassName();
        } else {
            name = obj.getClass();
        }

        return name;
    }

    /**
     * From the real className of an object, gets a displayable name.
     *
     * @param obj the object for which we want the class name to display - the object must not be null
     * @return the classname to display in the view.
     */
    public static String getDisplayableClassName(Object obj) {

        return getDisplayableClassName(getRealClassName(obj));
    }

    /**
     * From the real className of className, gets a displayable name.
     *
     * @param clazz the class for which we want the class name to display - the class must not be null
     * @return the classname to display in the view.
     */
    public static String getDisplayableClassName(Class clazz) {

        return getDisplayableClassName(clazz.getName());
    }

    /**
     * From the real className of className, gets a displayable name.
     * <br>
     * 1. get the real class name.
     * 2. Removes the package name
     * 3. try to remove an eventual Impl suffix
     *
     * @param name the class name for which we want the class name to display - the class must not be null
     * @return the classname to display in the view.
     */
    public static String getDisplayableClassName(String name) {

        int indexDot = name.lastIndexOf(".");
        int indexImpl = name.lastIndexOf("Impl");
        if (indexImpl != -1)
            name = name.substring(indexDot + 1, indexImpl);
        else
            name = name.substring(indexDot + 1);

        return name;
    }

    //---------------- private helper methods ------------------------------------

    private void initialize(DAOSource source, String user, String password,
                            PersistenceType type) throws IntactException {
        dataSource = source;

        if (source == null) {
            //couldn't get a mapping from the context, so can't search!!
            String msg = "intact helper: unable to search for any objects - data source required";
            throw new IntactException(msg);
        }

        //set up a logger
        pr = dataSource.getLogger();

        //get a DAO using the supplied user details
        try {
            dao = dataSource.getDAO(user, password, type);
        }
        catch (DataSourceException dse) {
            String msg = "intact helper: There was a problem accessing a data store";
            throw new IntactException(msg, dse);
        }
    }

    private void update(Object obj, boolean force) throws IntactException {

        try {
            if (force) {
                dao.forceUpdate(obj);
            }
            else {
                dao.update(obj);
            }
        } catch (UpdateException ue) {
            ue.printStackTrace();
            String msg = "intact helper: failed to perform update on class " + obj.getClass().getName();
            throw new IntactException(msg, ue);

        }

    }
}