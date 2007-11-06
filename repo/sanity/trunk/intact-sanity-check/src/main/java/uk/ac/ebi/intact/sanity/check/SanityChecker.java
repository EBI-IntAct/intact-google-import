/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.sanity.check;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.sanity.check.config.SanityCheckConfig;
import uk.ac.ebi.intact.sanity.commons.*;
import uk.ac.ebi.intact.sanity.commons.rules.RuleRunner;
import uk.ac.ebi.intact.sanity.commons.rules.RuleRunnerReport;
import uk.ac.ebi.intact.util.ElapsedTime;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Checks potential annotation error on IntAct objects.
 * <p/>
 * Reports are send to the curators.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id: SanityChecker.java,v 1.33 2006/06/01 08:16:45 catherineleroy Exp $
 */
public class SanityChecker {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog( SanityChecker.class );

    /**
     * Executes a sanity check for the whole database, including all rule groups.
     * Does not send mails, nor provides editor URLs.
     *
     * @return
     * @throws SanityCheckerException
     */
    public static SanityReport executeSanityCheck() throws SanityCheckerException {
        return executeSanityCheck( ( SanityCheckConfig ) null );
    }

    /**
     * Executes a sanity check for the whole database, for the rule groups specified.
     * Does not send mails, nor provides editor URLs.
     *
     * @return
     * @throws SanityCheckerException
     */
    public static SanityReport executeSanityCheck( String... groupNames ) throws SanityCheckerException {
        return executeSanityCheck( ( SanityCheckConfig ) null, groupNames );
    }

    /**
     * Executes a sanity check for the whole database, including all rule groups.
     * Can send mails depending on the SanityCheckConfiguration provided.
     *
     * @param sanityConfig
     * @return
     * @throws SanityCheckerException
     */
    public static SanityReport executeSanityCheck( SanityCheckConfig sanityConfig ) throws SanityCheckerException {
        final Set<String> availableGroups = DeclaredRuleManager.getInstance().getAvailableGroups();
        return executeSanityCheck( sanityConfig, availableGroups.toArray( new String[availableGroups.size()] ) );
    }

    /**
     * Executes a sanity check for the whole database, for the rule groups specified.
     * Can send mails depending on the SanityCheckConfiguration provided.
     *
     * @param sanityConfig
     * @return
     * @throws SanityCheckerException
     */
    public static SanityReport executeSanityCheck( SanityCheckConfig sanityConfig, String... groupNames ) throws SanityCheckerException {
        RuleRunnerReport.getInstance().clear();

        if ( log.isDebugEnabled() ) log.debug( "Executing Sanity Check" );

        long startTime = System.currentTimeMillis();
        checkAllAnnotatedObjects( groupNames );
        long elapsedTime = System.currentTimeMillis() - startTime;

        SanityReport report = RuleRunnerReport.getInstance().toSanityReport( true );

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        addReportAttributes( report, elapsedTime );

        if ( sanityConfig != null ) {

            if ( sanityConfig.getEditorUrl() != null ) {
                EditorUrlBuilder editorUrlBuilder = new EditorUrlBuilder( sanityConfig );

                for ( SanityResult sanityResult : report.getSanityResult() ) {
                    for ( InsaneObject insaneObject : sanityResult.getInsaneObject() ) {
                        editorUrlBuilder.addEditorUrl( insaneObject );
                    }
                }
            }

            try {
                SanityReportMailer reportMailer = new SanityReportMailer( sanityConfig );
                reportMailer.mailReports( report );
            } catch ( Exception e ) {
                throw new SanityCheckerException( "Exception sending mails", e );
            }
        } else {
            if ( log.isDebugEnabled() ) log.debug( "No sanity configuration provided." );
        }

        return report;
    }

    /**
     * Executes a sanity check for the provided objects. Does not send mails.
     *
     * @param annotatedObjects
     */
    public static SanityReport executeSanityCheck( Collection<? extends AnnotatedObject> annotatedObjects ) {
        final Set<String> availableGroups = DeclaredRuleManager.getInstance().getAvailableGroups();
        return executeSanityCheck( annotatedObjects, availableGroups.toArray( new String[availableGroups.size()] ) );
    }

    /**
     * Executes a sanity check for the provided objects. Does not send mails.
     *
     * @param annotatedObjects
     */
    public static SanityReport executeSanityCheck( Collection<? extends AnnotatedObject> annotatedObjects, String... groupNames ) {
        RuleRunnerReport.getInstance().clear();

        if ( log.isDebugEnabled() ) log.debug( "Executing Sanity Check" );

        long startTime = System.currentTimeMillis();
        checkAnnotatedObjects( annotatedObjects, groupNames );
        long elapsedTime = System.currentTimeMillis() - startTime;

        SanityReport report = RuleRunnerReport.getInstance().toSanityReport();
        RuleRunnerReport.getInstance().clear();

        addReportAttributes( report, elapsedTime );

        return report;
    }

    protected static void addReportAttributes( SanityReport report, long elapsedTime ) {
        // instance name
        try {
            String database = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                    .getBaseDao().getDbName();
            report.setDatabase( database );
        }
        catch ( SQLException e ) {
            throw new RuntimeException( e );
        }

        ReportAttribute executionDateAtt = new ReportAttribute();
        executionDateAtt.setName( "Date" );
        executionDateAtt.setValue( new Date().toString() );
        report.getReportAttribute().add( executionDateAtt );

        ReportAttribute elapsedTimeAtt = new ReportAttribute();
        elapsedTimeAtt.setName( "Elapsed time" );
        elapsedTimeAtt.setValue( new ElapsedTime( ( int ) elapsedTime / 1000 ).toString() );
        report.getReportAttribute().add( elapsedTimeAtt );
    }

    protected static void checkAllCvObjects( String... groupNames ) {
        CvObjectDao cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
        Collection<CvObject> allCvObjects = cvObjectDao.getAll();

        if ( log.isInfoEnabled() ) log.info( "\tProcessing " + allCvObjects.size() + " CvObjects" );

        checkAnnotatedObjects( cvObjectDao.getAll(), groupNames );
    }

    protected static void checkAllAnnotatedObjects( String... groupNames ) {
        checkAllAnnotatedObjectsOfType( CvObject.class, groupNames );
        checkAllAnnotatedObjectsOfType( Experiment.class, groupNames );
        checkAllAnnotatedObjectsOfType( InteractionImpl.class, groupNames );
        checkAllAnnotatedObjectsOfType( ProteinImpl.class, groupNames );
        checkAllAnnotatedObjectsOfType( NucleicAcidImpl.class, groupNames );
        checkAllAnnotatedObjectsOfType( SmallMoleculeImpl.class, groupNames );
        checkAllAnnotatedObjectsOfType( BioSource.class, groupNames );
        checkAllAnnotatedObjectsOfType( Feature.class, groupNames );
        checkAllAnnotatedObjectsOfType( Publication.class, groupNames );
    }

    protected static <T extends AnnotatedObject> void checkAllAnnotatedObjectsOfType( Class<T> aoClass, String... groupNames ) {
        if ( DeclaredRuleManager.getInstance().getDeclaredRulesForTarget( aoClass, groupNames ).isEmpty() ) {
            if ( log.isDebugEnabled() ) log.debug( "No declared rules for: " + aoClass.getName() );
            return;
        }

        if ( IntactContext.getCurrentInstance().getDataContext().isTransactionActive() ) {
            throw new IntactException( "To execute this method the transaction must not be active" );
        }

        AnnotatedObjectDao<T> annotatedObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotatedObjectDao( aoClass );

        if ( log.isInfoEnabled() ) {
            beginTransaction();
            int total = annotatedObjectDao.countAll();
            commitTransaction();

            log.info( "\tGoing to process: " + total + " " + aoClass.getSimpleName() + "s" );
        }

        int firstResult = 0;
        final int maxResults = 200;

        Collection<T> annotatedObjects = null;
        do {
            beginTransaction();
            annotatedObjects = annotatedObjectDao.getAll( firstResult, maxResults );

            checkAnnotatedObjects( annotatedObjects, groupNames );

            commitTransaction();

            if ( log.isDebugEnabled() ) {
                int processed = firstResult + annotatedObjects.size();

                if ( firstResult != processed ) {
                    log.debug( "\t\tProcessed " + ( firstResult + annotatedObjects.size() ) );
                }
            }

            firstResult = firstResult + maxResults;

        } while ( !annotatedObjects.isEmpty() );
    }

    protected static void checkAnnotatedObjects( Collection<? extends AnnotatedObject> annotatedObjectsToCheck, String... groupNames ) {
        if ( groupNames.length == 0 ) {
            throw new IllegalArgumentException( "No rule groups provided" );
        }

        RuleRunner.runAvailableRules( annotatedObjectsToCheck, groupNames );
    }


    private static void beginTransaction() {
        if ( !IntactContext.getCurrentInstance().getDataContext().isTransactionActive() ) {
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        }
    }

    private static void commitTransaction() {
        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch ( IntactTransactionException e ) {
            throw new RuntimeException( e );
        }
    }
}
