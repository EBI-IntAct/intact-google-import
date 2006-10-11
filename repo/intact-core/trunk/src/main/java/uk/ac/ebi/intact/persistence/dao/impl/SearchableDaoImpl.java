/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.persistence.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.model.AnnotatedObjectImpl;
import uk.ac.ebi.intact.model.Searchable;
import uk.ac.ebi.intact.persistence.dao.SearchableDao;
import uk.ac.ebi.intact.persistence.dao.query.SearchableQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Searches
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10-Oct-2006</pre>
 */
public class SearchableDaoImpl extends HibernateBaseDaoImpl<AnnotatedObjectImpl> implements SearchableDao
{
    public SearchableDaoImpl(Session session, IntactSession intactSession)
    {
        super(AnnotatedObjectImpl.class, session, intactSession);
    }

    public Integer countByQuery(Class<? extends Searchable> searchableClass, SearchableQuery query)
    {
        DetachedCriteria criteria = new SearchableCriteriaBuilder(query)
                .createCriteria(searchableClass);
        criteria.setProjection(Projections.countDistinct("ac"));

        List<Integer> list = criteria.getExecutableCriteria(getSession()).list();

        int count = 0;

        for (Integer entityCount : list)
        {
            count += entityCount;
        }

        return count;
    }

    public Map<Class<? extends Searchable>, Integer> countByQuery(SearchableQuery query)
    {
        return countByQuery(STANDARD_SEARCHABLES, query);
    }

    public Map<Class<? extends Searchable>, Integer> countByQuery(Class<? extends Searchable>[] searchableClasses, SearchableQuery query)
    {
        Map<Class<? extends Searchable>, Integer> counts = new HashMap<Class<? extends Searchable>, Integer>();

        for (Class<? extends Searchable> searchable : searchableClasses)
        {
            counts.put(searchable, countByQuery(searchable, query));
        }

        return counts;
    }

    public List<? extends Searchable> getByQuery(SearchableQuery query, Integer firstResult, Integer maxResults)
    {
        return getByQuery(STANDARD_SEARCHABLES, query, firstResult, maxResults);
    }

    public List<? extends Searchable> getByQuery(Class<? extends Searchable>[] searchableClasses, SearchableQuery query, Integer firstResult, Integer maxResults)
    {
        List<? extends Searchable> results = new ArrayList<Searchable>();

        for (Class searchable : searchableClasses)
        {
            int resultsFoundSoFar = results.size();
            int resultsToFetch = maxResults-resultsFoundSoFar;
            
            results.addAll(getByQuery(searchable, query, firstResult, resultsToFetch));

            if (results.size() == maxResults)
            {
                break;
            }
        }

        return results;
    }

    public <T extends Searchable> List<T> getByQuery(Class<T> searchableClass, SearchableQuery query, Integer firstResult, Integer maxResults)
    {
        DetachedCriteria criteria = new SearchableCriteriaBuilder(query)
                .createCriteria(searchableClass);

        Criteria crit = criteria.getExecutableCriteria(getSession());

        if (firstResult != null)
        {
            crit.setFirstResult(firstResult);
        }

        if (maxResults != null)
        {
            crit.setMaxResults(maxResults);
        }

        return crit.list();
    }

    public List<String> getAcsByQuery(Class<? extends Searchable> searchableClass, SearchableQuery query, Integer firstResult, Integer maxResults)
    {
        DetachedCriteria criteria = new SearchableCriteriaBuilder(query)
                .createCriteria(searchableClass)
                .setProjection(Projections.property("ac"));

        Criteria crit = criteria.getExecutableCriteria(getSession());

        if (firstResult != null)
        {
            crit.setFirstResult(firstResult);
        }

        if (maxResults != null)
        {
            crit.setMaxResults(maxResults);
        }

        return crit.list();
    }
}
