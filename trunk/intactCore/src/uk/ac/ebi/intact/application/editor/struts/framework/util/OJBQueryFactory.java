/*
 Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;

/**
 * This factory class builds queries for the editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class OJBQueryFactory {

    /** Only instance of this class */
    private static final OJBQueryFactory OUR_INSTANCE = new OJBQueryFactory();

    /**
     * @return returns the only instance of this class.
     */
    public static final OJBQueryFactory getInstance() {
        return OUR_INSTANCE;
    }

    /**
     * Returns a query to count search result
     * @param clazz the class to construct menus. Eg., CvTopic.class
     * @return a query to count search result.
     */
    public Query getSearchCountQuery(Class clazz, String value) {
        // Replace * with % for SQL
        String sqlValue = value.replaceAll("\\*", "%");

        Criteria crit1 = new Criteria();
        crit1.addLike("ac", sqlValue);
        Criteria crit2 = new Criteria();
        crit2.addLike("shortLabel", sqlValue);

        // Looking for either AC or shortlabel
        crit1.addOrCriteria(crit2);

        ReportQueryByCriteria query = QueryFactory.newReportQuery(clazz, crit1);
        query.setAttributes(new String[] {"count(ac)"});
        return query;
    }

    /**
     * Returns a query to search for given class and value.
     * @param clazz the class to search. Eg., CvTopic.class
     * @return a query to search. The result set is sorted in ascending order.
     */
    public Query getSearchQuery(Class clazz, String value) {
        // Replace * with % for SQL
        String sqlValue = value.replaceAll("\\*", "%");

        Criteria crit1 = new Criteria();
        // Need all records for given class.
        crit1.addLike("ac", sqlValue);
        Criteria crit2 = new Criteria();
        crit2.addLike("shortLabel", sqlValue);

        // Looking for both ac and shortlabel
        crit1.addOrCriteria(crit2);

        ReportQueryByCriteria query = QueryFactory.newReportQuery(clazz, crit1);
        // Limit to ac and shortlabel
        query.setAttributes(new String[] {"ac", "shortLabel", "fullName"});
        // Sorts on shortlabel
        query.addOrderByAscending("shortLabel");
        return query;
    }

    /**
     * Returns a query to build menus
     * @param clazz the class to construct menus. Eg., CvTopic.class
     * @return a query to build menus. The menus are sorted in ascending order.
     */
    public Query getMenuBuildQuery(Class clazz) {
        Criteria crit = new Criteria();
        // Need all records for given class.
        crit.addLike("ac", "%");
        ReportQueryByCriteria query = QueryFactory.newReportQuery(clazz, crit);
        // Limit to shortlabel
        query.setAttributes(new String[] {"shortLabel"});
        query.addOrderByAscending("shortLabel");
        return query;
    }
}
