/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.persistence.dao;

import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Disjunction;
import org.apache.ojb.broker.accesslayer.LookupException;

import java.sql.SQLException;
import java.util.Collection;

import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.model.NotAnEntityException;

import javax.persistence.Entity;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24-Apr-2006</pre>
 */
public abstract class HibernateDao<T>
{
    private Class<T> entityClass;
    private Session session;

    public HibernateDao(Class<T> entityClass, Session session)
    {
        this.entityClass = entityClass;
       this.session = session;
    }

    protected Session getSession()
    {
        return session;
    }

    /**
     * Provides the database name that is being connected to.
     *
     * @return String the database name, or an empty String if the query fails
     */
    public String getDbName() throws SQLException
    {
        return session.connection().getMetaData().getDatabaseProductName();
    }

    /**
     * Provides the user name that is connecting to the DB.
     *
     * @return String the user name, or an empty String if the query fails
     *
     * @throws SQLException thrown if the metatdata can't be obtained
     */
    public String getDbUserName() throws SQLException {
        return session.connection().getMetaData().getUserName();
    }

    /**
     * Checks if the class passed as an argument has the annotation <code>@javax.persistence.Entity</code>.
     * If not, this methods throws a <code>NotAnEntityException</code>
     * @param entity The entity to validate
     */
    public static void validateEntity(Class<? extends IntactObject> entity)
    {
        if (entity.getAnnotation(Entity.class) == null)
        {
            throw new NotAnEntityException(entity);
        }
    }

     protected T getByPropertyName(String propertyName, String value)
    {
       return getByPropertyName(propertyName, value, true);
    }

    protected T getByPropertyName(String propertyName, String value, boolean ignoreCase)
    {
       Criteria criteria = getSession().createCriteria(entityClass);

        SimpleExpression restriction = Restrictions.eq(propertyName, value);

        if (ignoreCase)
        {
           restriction.ignoreCase();
        }

        criteria.add(restriction);

        return (T) criteria.uniqueResult();
    }

    protected <T> Collection<T> getByPropertyNameLike(String propertyName, String value)
    {
        Criteria criteria = getSession().createCriteria(entityClass)
                .add(Restrictions.like(propertyName, value).ignoreCase());

        return criteria.list();
    }

    protected <T> Collection<T> getByPropertyNameLike(String propertyName, String value,  boolean ignoreCase, MatchMode matchMode)
    {
       Criteria criteria = getSession().createCriteria(entityClass);

        SimpleExpression restriction = Restrictions.like(propertyName, value, matchMode);

        if (ignoreCase)
        {
           restriction.ignoreCase();
        }

        criteria.add(restriction);

        return criteria.list();
    }

    protected Disjunction disjunctionForArray(String propertyName, String[] values)
    {
        Disjunction disj = Restrictions.disjunction();

        for (String value : values)
        {
            if (value.contains("%"))
            {
                disj.add(Restrictions.like(propertyName, value));
            }
            else
            {
                disj.add(Restrictions.eq(propertyName, value));
            }
        }

        return disj;
    }

    protected MatchMode matchModeForValue(String value, boolean removeWildcardFromValue)
    {
        MatchMode mode;

        if (value.startsWith("%") && value.endsWith("%"))
        {
            mode = MatchMode.ANYWHERE;
        }
        else if (value.startsWith("%") && !value.endsWith("%"))
        {
            mode = MatchMode.START;
        }
        else if (!value.startsWith("%") && value.endsWith("%"))
        {
            mode = MatchMode.END;
        }
        else
        {
            mode = MatchMode.EXACT;
        }

        if (removeWildcardFromValue)
        {
            value = value.replaceAll("%", "");
        }

        return mode;
    }

    public Class<T> getEntityClass()
    {
        return entityClass;
    }
}
