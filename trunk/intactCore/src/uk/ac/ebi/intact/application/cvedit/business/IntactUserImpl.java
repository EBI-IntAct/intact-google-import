/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.cvedit.business;

import java.util.*;

import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionBindingEvent;

import uk.ac.ebi.intact.persistence.*;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.application.cvedit.exception.InvalidLoginException;
import uk.ac.ebi.intact.application.cvedit.struts.view.CvViewBean;

/**
 * This class stores information about an Intact Web user session. Instead of
 * binding multiple objects, only an object of this class is bound to a session,
 * thus serving a single access point for multiple information.
 * <p>
 * This class implements the <tt>ttpSessionBindingListener</tt> interface for it
 * can be notified of session time outs.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class IntactUserImpl implements IntactUserIF, HttpSessionBindingListener {

    // Beginning of Inner classes

    // ------------------------------------------------------------------------

    /**
     * Inner class to generate unique ids to use primary keys for CommentBean
     * class.
     */
    private static class UniqueID {

        /**
         * The initial value. All the unique ids are based on this value for any
         * (all) user(s).
         */
        private static long theirCurrentTime = System.currentTimeMillis();

        /**
         * Returns a unique id using the initial seed value.
         */
        private static synchronized long get() {
            return theirCurrentTime++;
        }
    }

    // ------------------------------------------------------------------------

    // End of Inner classes

    // Static data

    /**
     * The name of the topic list.
     */
    private static final String theirTopicNames = "TopicNames";

    /**
     * The name of the database list.
     */
    private static final String theirDBNames = "DatabaseNames";

    /**
     * The name of qualifier list.
     */
    private static final String theirQualifierNames = "QualifierNames";

    /**
     * An empty list only contains this item.
     */
    private static final String theirEmptyListItem = "-------------";

    /**
     * Maps: List Name -> List type. Common to all the users and it is immutable.
     */
    private static final Map theirNameToType = new HashMap();

    // End of static data

    /**
     * The user ID.
     */
    private String myUser;

    /**
     * Reference to the Intact Helper.
     */
    private IntactHelper myHelper;

    /**
     * The session start time.
     */
    private Date myStartTime;

    /**
     * The session end time.
     */
    private Date myEndTime;

    /**
     * The selected topic.
     */
//    private String mySelectedTopic;

    /**
     * Maps list name -> list of items. Made it transient
     */
    private transient Map myNameToItems = new HashMap();

    /**
     * The current Cv object we are editing.
     */
    private CvObject myEditCvObject;

    /**
     * The current view of the user. Not saving the state of the view yet.
     */
    private transient CvViewBean myView;

    // Static initializer.

    // Fill the maps with list names and their associated classes.
    static {
        theirNameToType.put(theirTopicNames, CvTopic.class);
        theirNameToType.put(theirDBNames, CvDatabase.class);
        theirNameToType.put(theirQualifierNames, CvXrefQualifier.class);
    }

    // Static Methods.

    /**
     * Returns the unique id based on the current time; the ids are unique
     * for a session.
     */
    public static long getId() {
        return UniqueID.get();
    }

    // Constructors.

    /**
     * Constructs an instance of this class with given mapping file and
     * the name of the data source class.
     *
     * @param mapping the name of the mapping file.
     * @param dsClass the class name of the Data Source.
     * @param user the user
     * @param password the password of <code>user</code>.
     *
     * @exception DataSourceException for error in getting the data source; this
     *  could be due to the errors in repository files.
     * @exception IntactException for errors in creating IntactHelper.
     * @exception InvalidLoginException the underlying persistent mechanism
     *  rejected <code>user</code> and <code>password</code> combination.
     * @exception SearchException for error in creating lists such as topics,
     *  database names etc.
     */
    public IntactUserImpl(String mapping, String dsClass, String user,
            String password) throws DataSourceException, IntactException,
            InvalidLoginException, SearchException {
        myUser = user;
        DAOSource ds = DAOFactory.getDAOSource(dsClass);

        // Pass config details to data source - don't need fast keys as only
        // accessed once
        Map fileMap = new HashMap();
        fileMap.put(Constants.MAPPING_FILE_KEY, mapping);
        ds.setConfig(fileMap);

        // Initialize the helper.
        myHelper = new IntactHelper(ds, user, password);

        // Cache the list names.
        for (Iterator iter = theirNameToType.entrySet().iterator();
             iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            myNameToItems.put(entry.getKey(), makeList((Class) entry.getValue()));
        }
        // Record the time started.
        myStartTime = Calendar.getInstance().getTime();
    }

    // Implements HttpSessionBindingListener

    /**
     * Will call this method when an object is bound to a session.
     * Not doing anything.
     */
    public void valueBound(HttpSessionBindingEvent event) {
        // Create my initial view.
        myView = new CvViewBean();
    }

    /**
     * Will call this method when an object is unbound from a session. This
     * method sets the logout time.
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        try {
            logoff();
        }
        catch (IntactException ie) {
            // Just ignore this exception. Where to log this?
        }
    }

    // Implementation of IntactUserIF interface.

    public CvViewBean getView() {
        return myView;
    }

    public String getUser() {
        return myUser;
    }

    public void setSelectedTopic(String topic) {
        myView.setTopic(topic);
//        mySelectedTopic = topic;
    }

    public String getSelectedTopic() {
        return myView.getTopic();
//        return mySelectedTopic;
    }

    public Institution getInstitution() throws SearchException {
        return (Institution) getObjectByLabel(Institution.class, "EBI");
    }

    public void updateList(Class clazz) throws SearchException {
        if (!theirNameToType.containsValue(clazz)) {
            return;
        }
        // Valid type; must update the list. First get the name of the list to
        // update.
        for (Iterator iter = theirNameToType.entrySet().iterator();
             iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (clazz.equals(entry.getValue())) {
                String name = (String) entry.getKey();
                // Remove this from the existing map.
                myNameToItems.remove(name);
                myNameToItems.put(name, makeList((Class) entry.getValue()));
            }
        }
    }

    public Collection getTopicList() {
//        // The topic list.
//        Collection list = (Collection) myNameToItems.get(theirTopicNames);
//
//        // The class of the current edit object.
//        Class clazz = myEditCvObject.getClass();
//
//        // Remove the short label from the list (only for CvTopics).
//        if (clazz.equals(CvTopic.class)) {
//            // The short label of the CV object we are editing at the moment.
//            String label = myEditCvObject.getShortLabel();
//
//            // New collection because we are modifying the list.
//            Collection topics = new ArrayList(list);
//
//            // Remove the short label from the drop down list.
//            topics.remove(label);
//            return topics;
//        }
//        // No modifcations to the list; jsut return the cache list.
//        return list;
        //return getList((Collection) myNameToItems.get(theirTopicNames));
        return getList(theirTopicNames);
    }

    public Collection getDatabaseList() {
        return getList(theirDBNames);
    }

    public Collection getQualifierList() {
        return getList(theirQualifierNames);
    }

    public boolean isQualifierListEmpty() {
        return isListEmpty(theirQualifierNames);
    }

    public void begin() throws IntactException {
        myHelper.startTransaction();
    }

    public void commit() throws IntactException {
        myHelper.finishTransaction();
    }

    public void rollback() throws IntactException {
        myHelper.undoTransaction();
    }

    public void create(Object object) throws IntactException {
        myHelper.create(object);
    }

    public void update(Object object) throws IntactException {
        myHelper.update(object);
    }

    public void delete(Object object) throws IntactException {
        myHelper.delete(object);
    }

    public void setCurrentEditObject(CvObject cvobj) {
        myEditCvObject = cvobj;

        // Update the view to using the new CV edit object.
        myView.initialise(cvobj);
    }

    public CvObject getCurrentEditObject() {
        return myEditCvObject;
    }

    public Object getObjectByLabel(Class clazz, String label)
        throws SearchException {

        Object result = null;

        Collection resultList = search(clazz.getName(), "shortLabel", label);

        if (resultList.isEmpty()) {
            return null;
        }
        Iterator i = resultList.iterator();
        result = i.next();
        if (i.hasNext()) {
            throw new SearchException(
                "More than one object returned by search by label.");
        }
        return result;
    }

    public Collection search(String objectType, String searchParam,
                              String searchValue) throws SearchException {
        try {
            return myHelper.search(objectType, searchParam, searchValue);
        }
        catch (IntactException ie) {
            throw new SearchException("Search failed: " + ie.getNestedMessage());
        }
    }

    public void logoff() throws IntactException {
        myEndTime = Calendar.getInstance().getTime();
        myHelper.closeStore();
    }

    public Date loginTime() {
        return myStartTime;
    }

    public Date logoffTime() {
        return myEndTime;
    }

    // Helper methods.

    /**
     * This method creates a list for given class.
     *
     * @param clazz the class type to create the list.
     *
     * @return list made of short labels for given class type. A special
     * list with <code>theirEmptyListItem</code> is returned if there
     * are no items found for <code>clazz</code>.
     */
    private Collection makeList(Class clazz) throws SearchException {
        // The collection to return.
        Collection list = new ArrayList();

        // Interested in all the records for 'clazz'.
        Collection results = search(clazz.getName(), "ac", "*");

        if (results.isEmpty()) {
            // Special list when we don't have any names.
            list.add(theirEmptyListItem);
            return list;
        }
        for (Iterator iter = results.iterator(); iter.hasNext();) {
            list.add(((CvObject) iter.next()).getShortLabel());
        }
        return list;
    }

    /**
     * Returns <code>true</code> only if the list for given name contains
     * a single item and that item equals to the empty list item identifier.
     * @param name the name of the list.
     */
    private boolean isListEmpty(String name) {
        Collection list = (Collection) myNameToItems.get(name);
        Iterator iter = list.iterator();
        return (iter.next()).equals(theirEmptyListItem) && !iter.hasNext();
    }

    /**
     * Returns the collection for given list name.
     * @param name the name of the list to return the list.
     * @return the list for <code>name</code>. If the current editable object is
     * as same as <code>name/code>'s class, then the cuurent editable's name (short
     * label) wouldn't be included. For example, if the short label for a CvTopic is
     * Function, then the list wouldn't have 'Function' if the current editable object
     * is of CvTopic and its short label is 'Function'.
     */
    private Collection getList(String name) {
        Collection list = (Collection) myNameToItems.get(name);
        Class clazz = (Class) theirNameToType.get(name);

        // Remove the short label only when the current editable object's
        // class and the given class match.
        if (myEditCvObject.getClass().equals(clazz)) {
            // The short label of the CV object we are editing at the moment.
            String label = myEditCvObject.getShortLabel();

            // New collection because we are modifying the list.
            Collection topics = new ArrayList(list);

            // Remove the short label from the drop down list.
            topics.remove(label);
            return topics;
        }
        // No modifcations to the list; just return the cache list.
        return list;
    }
}
