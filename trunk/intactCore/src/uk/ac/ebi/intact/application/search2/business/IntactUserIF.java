/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.search2.business;

import uk.ac.ebi.intact.business.IntactException;

import java.util.Collection;

/**
 * This interface represents an Intact user.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk), modified by Chris Lewington
 * @version $Id$
 */
public interface IntactUserIF
        extends uk.ac.ebi.intact.application.commons.business.IntactUserI {

    /**
     * Return the search criteria.
     * @return the search criteria as a <code>String</code> object.
     */
    public String getSearchCritera();

    /**
     * This method provides a means of searching intact objects, within the constraints
     * provided by the parameters to the method.
     *
     * @param objectType the object type to be searched
     * @param searchParam the parameter to search on (eg field)
     * @param searchValue the search value to match with the parameter
     *
     * @return the results of the search (empty if no matches were found).
     *
     * @exception IntactException thrown if problems are encountered during the
     * search process.
     */
    public Collection search(String objectType, String searchParam,
                              String searchValue) throws IntactException;

    public void setHelpLink(String link);
    public String getHelpLink();

    public void setSearchValue(String value);
    public String getSearchValue();

    public void setSearchClass(String searchClass);
    public String getSearchClass();
}
