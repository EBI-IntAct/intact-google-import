/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.config.DataConfig;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;
import uk.ac.ebi.intact.config.impl.AbstractJpaDataConfig;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import java.io.Serializable;
import java.util.Collection;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>07-Aug-2006</pre>
 */
public class DataContext implements Serializable {

    private static final Log log = LogFactory.getLog( DataContext.class );

    private IntactSession session;

    public DataContext( IntactSession session ) {
        this.session = session;
    }

    public void beginTransaction() {
        beginTransaction( getDefaultDataConfig().getName() );
    }

    public void beginTransactionManualFlush() {
        getDefaultDataConfig().setAutoFlush(true);
        beginTransaction( getDefaultDataConfig().getName() );
    }

    public void beginTransaction( String dataConfigName ) {
        DaoFactory daoFactory = getDaoFactory( dataConfigName );

        if ( !daoFactory.isTransactionActive() ) {
            log.debug( "Creating new transaction for: " + dataConfigName );
            daoFactory.beginTransaction();
        } else {
            log.debug( "Using existing transaction for: " + dataConfigName );
        }
    }

    public boolean isTransactionActive() {
        return isTransactionActive( getDefaultDataConfig().getName() );
    }

    public boolean isTransactionActive( String dataConfigName ) {
        return getDaoFactory( dataConfigName ).isTransactionActive();
    }

    public void commitTransaction() throws IntactTransactionException {
        try {
            commitTransaction( getDefaultDataConfig().getName() );
        } catch ( Exception e ) {
            throw new IntactTransactionException( e );
        }
    }

    public void commitTransaction( String dataConfigName ) throws IntactTransactionException {
        DaoFactory daoFactory = getDaoFactory( dataConfigName );

        daoFactory.commitTransaction();
        /*
        if ( daoFactory.isTransactionActive() ) {
            try {
                if (getSession().getFlushMode() == FlushMode.MANUAL)
                {
                    flushSession();
                }

                daoFactory.getCurrentTransaction().commit();
            } catch ( IntactTransactionException e ) {
                log.debug( "An Exception occured commiting" + e.getMessage() );
                throw new IntactTransactionException( e );
            }
            if ( log.isDebugEnabled() ) {
                log.debug( "Committed transaction: " + dataConfigName );
            }
        }
        */
        assert ( daoFactory.isTransactionActive() == false );

        // flush the CvContext in to avoid lazy initialization errors
        clearCvContext();
    }

    private void clearCvContext() {
       CvContext.getCurrentInstance(session).clearCache();
    }

    public Session getSession() {
        return getSessionFromSessionFactory(IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig());
    }

     protected Session getSessionFromSessionFactory(DataConfig dataConfig) {
        Session session;
        if (dataConfig instanceof AbstractHibernateDataConfig) {
            AbstractHibernateDataConfig hibernateDataConfig = (AbstractHibernateDataConfig) dataConfig;
            session = hibernateDataConfig.getSessionFactory().getCurrentSession();
        } else if (dataConfig instanceof AbstractJpaDataConfig) {
            AbstractJpaDataConfig jpaDataConfig = (AbstractJpaDataConfig) dataConfig;
            HibernateEntityManager hibernateEntityManager = (HibernateEntityManager) jpaDataConfig.getSessionFactory().createEntityManager();
            session = hibernateEntityManager.getSession();
        } else {
            throw new IllegalStateException("Wrong DataConfig found: "+dataConfig);
        }
        return session;
    }

    public void commitAllActiveTransactions() throws IntactTransactionException {
        Collection<DataConfig> dataConfigs = RuntimeConfig.getCurrentInstance( session ).getDataConfigs();

        for ( DataConfig dataConfig : dataConfigs ) {
            DaoFactory daoFactory = getDaoFactory( dataConfig );

            if ( daoFactory.isTransactionActive() ) {
                daoFactory.getCurrentTransaction().commit();
            }
        }
    }

    public DaoFactory getDaoFactory() {
        DataConfig dataConfig = RuntimeConfig.getCurrentInstance( session ).getDefaultDataConfig();
        return getDaoFactory( dataConfig );
    }

    public DaoFactory getDaoFactory( String dataConfigName ) {
        DataConfig dataConfig = RuntimeConfig.getCurrentInstance( session ).getDataConfig( dataConfigName );
        return getDaoFactory( dataConfig );
    }

    public boolean isReadOnly() {
        return RuntimeConfig.getCurrentInstance( session ).isReadOnlyApp();
    }

    public void flushSession() {
        DataConfig dataConfig = RuntimeConfig.getCurrentInstance( session ).getDefaultDataConfig();
        dataConfig.flushSession();

        DaoFactory daoFactory = DaoFactory.getCurrentInstance(session, dataConfig);
        daoFactory.getEntityManager().flush();

        // flush the CvContext in to avoid lazy initialization errors
        clearCvContext();
    }

    private DaoFactory getDaoFactory( DataConfig dataConfig ) {
        return DaoFactory.getCurrentInstance( session, dataConfig );
    }

    private DataConfig getDefaultDataConfig() {
        DataConfig dataConfig = RuntimeConfig.getCurrentInstance( session ).getDefaultDataConfig();

        if (dataConfig == null) {
            dataConfig = IntactContext.calculateDefaultDataConfig( session );
        }

        return dataConfig;
    }
}
