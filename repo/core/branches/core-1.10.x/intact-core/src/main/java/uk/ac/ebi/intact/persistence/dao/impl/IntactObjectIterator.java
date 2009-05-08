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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.persistence.dao.IntactObjectDao;

import javax.persistence.EntityTransaction;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Iterator;
import java.util.List;

/**
 * Allow to iterate over the object stored in the database withough loading them all at once.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 1.5
 */
@Deprecated
public class IntactObjectIterator<T extends IntactObject> implements Iterator<T> {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntactObjectIterator.class );

    /**
     * Maximum size of a chunk of data.
     */
    public static final int DEFAULT_CHUNK_SIZE = 50;

    public static final int NOT_INITIALISED = -1;

    ////////////////////////
    // Instance variables

    // Data access.
    private EntityManagerFactory emf;

    // chunk of data.
    private List<T> chunk;

    // iterator on the current chunk.
    private Iterator<T> chunkIterator;

    // current count of object read
    private int index = 0;

    // count of object to be read.
    //private int objectCount = NOT_INITIALISED;

    //
    private int batchSize = DEFAULT_CHUNK_SIZE;

    private Class intactObjectClass;

    private DetachedCriteria criteria;

    private EntityManager em;

    //////////////////////////
    // Constructor

    private IntactObjectDao<T> buildDao(EntityManager em) {
        return new IntactObjectDaoImpl( intactObjectClass, em );
    }

    public IntactObjectIterator( Class<T> intactObjectClass, DetachedCriteria criteria, EntityManagerFactory emf ) {

        if ( intactObjectClass == null ) {
            throw new IllegalArgumentException( "You must give a non Class that extends IntactObject." );
        }

        this.intactObjectClass = intactObjectClass;
        this.criteria = criteria;
        this.emf = emf;

        // initialisation
        /*
        objectCount = dao.countAll();

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();

        dao = null;

        log.debug( objectCount + " object to be read from the iterator." );
        */

    }

    public IntactObjectIterator( Class intactObjectClass, DetachedCriteria criteria, EntityManagerFactory emf, int batchSize ) {

        this( intactObjectClass, criteria, emf );

        if ( batchSize < 1 ) {
            throw new IllegalArgumentException( "Batch size must be greater or equal to 1." );
        }
        this.batchSize = batchSize;
    }

    ////////////////////////////
    // implements Iterator

    public boolean hasNext() {
        /*
        if ( objectCount == NOT_INITIALISED) {
            throw new IllegalStateException( "" );
        }
        return index < objectCount;     */


        if ( chunkIterator != null && chunkIterator.hasNext() ) {
            return true;
        }

        if ( chunkIterator == null ) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.commit();
            } catch ( Exception ie ) {
                log.error( "Exception commiting " + ie );
                try {
                    tx.rollback();
                } catch (Exception e) {
                    log.error(e);
                }
            } finally {
                if ( em.isOpen() ) {
                    em.close();
                }
            }

            em = emf.createEntityManager();

            //if (log.isTraceEnabled()) log.trace( "Retreiving " + batchSize + " objects." );

            chunk = ( List<T> )
                    ( ( HibernateBaseDaoImpl<T> ) buildDao(em) )
                            .executeDetachedCriteria( criteria, index, batchSize );
            //if (log.isTraceEnabled()) log.trace(  "Retreived " + chunk.size() + " object(s)." );

            chunkIterator = chunk.iterator();
        }

        return chunkIterator.hasNext();
    }

    public T next() {

        T object = null;
        /*
     if ( ! hasNext() ) {
         throw new NoSuchElementException();
     }   */


        if ( chunkIterator != null ) {

            object = chunkIterator.next();
            index++;

            if ( !chunkIterator.hasNext() ) {
                chunkIterator = null;
                chunk = null;
            }
        }

        return object;
    }


    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize( int batchSize ) {
        this.batchSize = batchSize;
    }

    public int getIndex() {
        return index;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
