/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.protein;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;
import uk.ac.ebi.intact.persistence.dao.XrefDao;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.model.UniprotProteinType;
import uk.ac.ebi.intact.uniprot.model.UniprotSpliceVariant;
import uk.ac.ebi.intact.uniprot.service.UniprotService;
import uk.ac.ebi.intact.util.Crc64;
import uk.ac.ebi.intact.util.biosource.BioSourceService;
import uk.ac.ebi.intact.util.biosource.BioSourceServiceException;
import uk.ac.ebi.intact.util.protein.alarm.AlarmProcessor;
import uk.ac.ebi.intact.util.protein.utils.AliasUpdaterUtils;
import uk.ac.ebi.intact.util.protein.utils.AnnotationUpdaterUtils;
import uk.ac.ebi.intact.util.protein.utils.XrefUpdaterUtils;
import uk.ac.ebi.intact.util.protein.utils.UniprotServiceResult;

import java.util.*;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08-Feb-2007</pre>
 */
public class ProteinServiceImpl implements ProteinService {

    private UniprotServiceResult uniprotServiceResult;

    public static final Log log = LogFactory.getLog( ProteinServiceImpl.class );

    // TODO not public Constructor but a Factory that decides whether to use Remote API or Yasp

    // TODO Factory could coordinate a shared cache between multiple instances of the service (eg. multiple services running in threads)

    // TODO when running tests using this service, the implementation of UniprotBridgeAdapter could be DummyUniprotBridgeAdapter
    // TODO that creates proteins without relying on the network. Spring configuration might come in handy to configure the tests.

    // TODO upon global protein update, update the table IA_DB_INFO and store last-global-protein-update : YYYY-MMM-DD

    public static final String NEW_LINE = "\n";

    /**
     * UniProt Data Source.
     */
    private UniprotService uniprotService;

    /**
     * BioSource service allowing to create new BioSource in the database.
     */
    private BioSourceService bioSourceService;

    /**
     * Strategy dealing with unrecoverable error.
     */
    private AlarmProcessor alarmProcessor;

    /**
     * Mapping allowing to specify which database shortlabel correspond to which MI reference.
     */
    private Map<String, String> databaseName2mi = new HashMap<String, String>();

    //////////////////////////
    // Constructor

    ProteinServiceImpl( UniprotService uniprotService ) {
        if ( uniprotService == null ) {
            throw new IllegalArgumentException( "You must give a non null implementation of a UniProt Service." );
        }
        this.uniprotService = uniprotService;
    }

    /////////////////////////
    // Getters and Setters

    public BioSourceService getBioSourceService() {
        return bioSourceService;
    }

    public void setBioSourceService( BioSourceService bioSourceService ) {
        if ( bioSourceService == null ) {
            throw new NullPointerException( "bioSourceService must not be null." );
        }
        this.bioSourceService = bioSourceService;
    }

    public UniprotService getUniprotService() {
        return uniprotService;
    }

    public void setUniprotService( UniprotService uniprotService ) {
        if ( uniprotService == null ) {
            throw new NullPointerException( "uniprotService must not be null." );
        }
        this.uniprotService = uniprotService;
    }

    public AlarmProcessor getAlarmProcessor() {
        return alarmProcessor;
    }

    public void setAlarmProcessor( AlarmProcessor alarmProcessor ) {
        this.alarmProcessor = alarmProcessor;
    }

    public void addDbMapping( String databaseName, String miRef ) {
        // TODO log overwriting !!
        databaseName2mi.put( databaseName, miRef );
    }

    //////////////////////////
    // ProteinLoaderService

    public UniprotServiceResult retrieve( String uniprotId ) {
        if ( uniprotId == null ) {
            throw new IllegalArgumentException( "You must give a non null UniProt AC" );
        }

        uniprotId = uniprotId.trim();

        if ( uniprotId.length() == 0 ) {
            throw new IllegalArgumentException( "You must give a non empty UniProt AC" );
        }
        // Instanciate the uniprotServiceResult that is going to hold the proteins collection, the information messages
        // and the error message.
        uniprotServiceResult = new UniprotServiceResult(uniprotId);

        Collection<UniprotProtein> proteins = retrieveFromUniprot( uniprotId );

        Collection<Protein> intactProteins = new ArrayList<Protein>( proteins.size() );
        try{
        if ( proteins.size() > 1 ) {
            if ( eachSpeciesSeenOnlyOnce( proteins ) ) {
                intactProteins.addAll( createOrUpdate( proteins ) );
            } else {
                uniprotServiceResult.addError("eachSpecieisSeenOnlyOnce( Proteins(" + uniprotId + " ) ): false");
//                raiseAlarm( "eachSpecieisSeenOnlyOnce( Proteins(" + uniprotId + " ) ): false" );
            }
        } else {
            intactProteins.addAll( createOrUpdate( proteins ) );
        }
        }catch(ProteinServiceException e){
            uniprotServiceResult.addError(e.getMessage());
        }

        uniprotServiceResult.addAllToProteins(intactProteins);
        return uniprotServiceResult;
    }

    /**
     * Process the alarm.
     */
    private void raiseAlarm( String message ) {


        if ( alarmProcessor != null ) {
            alarmProcessor.processAlarm( message );
        } else {
            log.error( "No alarm processor available - Could not keep processing going on." );
            log.error( "Alarm Message was: " + message );
        }
    }

    private boolean eachSpeciesSeenOnlyOnce( Collection<UniprotProtein> proteins ) {

        Collection<Integer> species = new ArrayList<Integer>( proteins.size() );

        for ( UniprotProtein protein : proteins ) {
            int taxid = protein.getOrganism().getTaxid();
            if ( species.contains( taxid ) ) {
                return false;
            }
            species.add( taxid );
        }

        return true;
    }

    public UniprotServiceResult retrieve( String uniprotId, int taxidFilter )  {
        throw new UnsupportedOperationException();
    }

    public UniprotServiceResult retrieve( String uniprotId, Collection<Integer> taxidFilters ) {
        throw new UnsupportedOperationException();
    }

    public UniprotServiceResult retrieve( Collection<String> uniprotIds ) {
        throw new UnsupportedOperationException();
    }

    public UniprotServiceResult retrieve( Collection<String> uniprotIds, int taxidFilter )  {
        throw new UnsupportedOperationException();
    }

    public UniprotServiceResult retrieve( Collection<String> uniprotIds, Collection<Integer> taxidFilters )  {
        throw new UnsupportedOperationException();
    }

    ///////////////////////////
    // Private methods

    /**
     * Create or update a protein.
     *
     * @param uniprotProtein the UniProt protein we want to create in IntAct.
     *
     * @return an up-to-date IntAct protein.
     *
     * @throws ProteinServiceException
     */
    private Collection<Protein> createOrUpdate( UniprotProtein uniprotProtein ) throws ProteinServiceException {

        Collection<Protein> proteins = new ArrayList<Protein>( 1 );

        Collection<Protein> nonUniprotProteins = new ArrayList<Protein>( 1 );
        String taxid = String.valueOf( uniprotProtein.getOrganism().getTaxid() );

        // Collection IntAct protein based on UniProt primary AC
        Collection<Protein> primaryProteins = searchIntactByPrimaryAc( uniprotProtein );
        if ( !primaryProteins.isEmpty() ) {
            primaryProteins = filterByTaxid( primaryProteins, taxid );
            nonUniprotProteins.addAll( removeAndGetNonUniprotProteins( primaryProteins ) );
        }
        int countPrimary = primaryProteins.size();

        // Collection IntAct protein based on UniProt secondary ACs
        Collection<Protein> secondaryProteins = searchIntactBySecondaryAc( uniprotProtein );
        if ( !secondaryProteins.isEmpty() ) {
            secondaryProteins = filterByTaxid( secondaryProteins, taxid );
            nonUniprotProteins.addAll( removeAndGetNonUniprotProteins( secondaryProteins ) );
        }
        int countSecondary = secondaryProteins.size();

        if ( countPrimary == 0 && countSecondary == 0 ) {

            log.debug( "Could not find IntAct protein by UniProt primary or secondary AC." );
            Protein protein = createMinimalisticProtein( uniprotProtein );
            proteins.add( protein );
            updateProtein( protein, uniprotProtein );

        } else if ( countPrimary == 0 && countSecondary == 1 ) {
            //Corresponding test : ProteinServiceImplTest.testRetrieve_primaryCount0_secondaryCount1()
            log.debug( "Found a single IntAct protein by UniProt secondary AC (hint: could be a TrEMBL moved to SP)." );
            Protein protein = secondaryProteins.iterator().next();
            proteins.add( protein );

            // update UniProt Xrefs
            XrefUpdaterUtils.updateUniprotXrefs( protein, uniprotProtein );

            // Update protein
            updateProtein( protein, uniprotProtein );

        } else if ( countPrimary == 1 && countSecondary == 0 ) {
            // Corresponding test : ProteinServiceImplTest.testRetrieve_sequenceUpdate()
            //                      ProteinServiceImplTest.testRetrieve_update_CDC42_CANFA()
            log.debug( "Found a single IntAct protein by UniProt primary AC." );
            Protein protein = primaryProteins.iterator().next();
            proteins.add( protein );
            updateProtein( protein, uniprotProtein );

        } else {

            // Error cases


            String pCount = "Count of protein in Intact for the Uniprot entry primary ac(" + countPrimary + ") for the Uniprot entry secondary ac(s)(" + countSecondary + ")";
            log.error( "Could not update that protein, number of protein found in IntAct: " + pCount );

            if ( countPrimary > 1 && countSecondary == 0 ) {
            //corresponding test : testRetrieve_primaryCount2_secondaryCount1()
                StringBuilder sb = new StringBuilder();
                sb.append( "More than one IntAct protein is matching Primary AC: " + uniprotProtein.getPrimaryAc() );
                sb.append( NEW_LINE ).append( "Matches were:" ).append( NEW_LINE );
                int i = 1;
                for ( Protein pp : primaryProteins ) {
                    sb.append( i++ ).append( ". " );
                    sb.append( pp.getAc() );
                    sb.append( "  " );
                    sb.append( pp.getShortLabel() );
                    sb.append( NEW_LINE );
                }
                uniprotServiceResult.addError(sb.toString());
//                raiseAlarm( sb.toString() );

            } else if ( countPrimary == 0 && countSecondary > 1 ) {
            // corresponding test ProteinServiceImplTest.testRetrieve_primaryCount0_secondaryCount2()

                StringBuilder sb = new StringBuilder();
                sb.append( "More than one IntAct protein is matching secondary AC(s): " + uniprotProtein.getSecondaryAcs() );
                sb.append( NEW_LINE ).append( "Matches were:" ).append( NEW_LINE );
                int i = 1;
                for ( Protein pp : secondaryProteins ) {
                    sb.append( i++ ).append( ". " );
                    sb.append( pp.getAc() );
                    sb.append( "  " );
                    sb.append( pp.getShortLabel() );
                    sb.append( NEW_LINE );
                }
                uniprotServiceResult.addError(sb.toString());
//                raiseAlarm( sb.toString() );

            } else {

                // corresponding test ProteinServiceImplTest.testRetrieve_primaryCount1_secondaryCount1()
                uniprotServiceResult.addError( "Unexpected number of protein found in IntAct for UniprotEntry("+ uniprotProtein.getPrimaryAc() + ") " + pCount + NEW_LINE +
                            "Please fix this problem manualy.");
//                raiseAlarm( "Unexpected number of protein found in IntAct for UniprotEntry("+ uniprotProtein.getPrimaryAc() + ") " + pCount + NEW_LINE +
//                            "Please fix this problem manualy." );

            }
        }

        proteins.addAll( nonUniprotProteins );

        uniprotServiceResult.addAllToProteins(nonUniprotProteins);

        return proteins;
    }

    /**
     * Create or update a collection of proteins.
     *
     * @param uniprotProtein the collection of UniProt protein we want to create in IntAct.
     *
     * @return a Collection of up-to-date IntAct proteins.
     *
     * @throws ProteinServiceException
     */
    private Collection<Protein> createOrUpdate( Collection<UniprotProtein> uniprotProtein ) throws ProteinServiceException {
        // TODO Collection or Set ???
        Collection<Protein> proteins = new ArrayList<Protein>( uniprotProtein.size() );

        for ( UniprotProtein protein : uniprotProtein ) {
            proteins.addAll( createOrUpdate( protein ) );
        }

        return proteins;
    }

    /**
     * Update an existing intact protein's annotations.
     * <p/>
     * That includes, all Xrefs, Aliases, splice variants.
     *
     * @param protein        the intact protein to update.
     * @param uniprotProtein the uniprot protein used for data input.
     */
    private void updateProtein( Protein protein, UniprotProtein uniprotProtein ) throws ProteinServiceException {

        // check that both protein carry the same organism information
        String t1 = protein.getBioSource().getTaxId();
        int t2 = uniprotProtein.getOrganism().getTaxid();
        if ( !String.valueOf( t2 ).equals( t1 ) ) {
            String msg = "UpdateProteins is trying to modify the BioSource(" + t1 + ") of the following protein:" +
                         protein.getShortLabel() + protein.getAc() + " by BioSource( " + t2 + " )" +
                         "\nChanging the taxid of an existing protein is a forbidden operation.";

            throw new ProteinServiceException( msg );
        }

        // Fullname
        String fullname = uniprotProtein.getDescription();
        if ( fullname != null && fullname.length() > 250 ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Truncating fullname to the first 250 first chars." );
            }
            fullname = fullname.substring( 0, 250 );
        }
        protein.setFullName( fullname );

        // Shortlabel
        protein.setShortLabel( generateProteinShortlabel( uniprotProtein ) );

        // Xrefs -- but UniProt's as they are supposed to be up-to-date at this stage.
        XrefUpdaterUtils.updateAllXrefs( protein, uniprotProtein, databaseName2mi );

        // Aliases
        AliasUpdaterUtils.updateAllAliases( protein, uniprotProtein );

        // Sequence
        boolean sequenceUpdated = false;
        String sequence = uniprotProtein.getSequence();
        if ( sequence == null || !sequence.equals( protein.getSequence() ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Sequence requires update." );
            }
            protein.setSequence( sequence );
            sequenceUpdated = true;
        }

        // CRC64
        String crc64 = uniprotProtein.getCrc64();
        if ( protein.getCrc64() == null || !protein.getCrc64().equals( crc64 ) ) {
            log.debug( "CRC64 requires update." );
            protein.setCrc64( crc64 );
        }

        // TODO if sequence  was updated, run a range check. Use the AlarmProcessor to log messages.
        if ( sequenceUpdated || !protein.getActiveInstances().isEmpty() ) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Request a Range check on Protein " + protein.getShortLabel() + " " + protein.getAc() );
            }
        }

        // Persist changes
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ProteinDao pdao = daoFactory.getProteinDao();
        pdao.update( ( ProteinImpl ) protein );

        ///////////////////////////////
        // Update Splice Variants

        // search intact
        Collection<ProteinImpl> variants = getSpliceVariants( protein );

        Collection<SpliceVariantMatch> matches = findMatches( variants, uniprotProtein.getSpliceVariants() );
        for ( SpliceVariantMatch match : matches ) {

            if ( match.isSuccessful() ) {

                // update
                updateSpliceVariant( match.getIntactProtein(), protein, match.getUniprotSpliceVariant(), uniprotProtein );

            } else if ( match.hasNoIntact() ) {

                // create shallow
                Protein intactSpliceVariant = createMinimalisticSpliceVariant( match.getUniprotSpliceVariant(),
                                                                               protein,
                                                                               uniprotProtein );
                // update
                updateSpliceVariant( intactSpliceVariant, protein, match.getUniprotSpliceVariant(), uniprotProtein );

            } else {

//                raiseAlarm( "Could not find a corresponding" );
                throw new ProteinServiceException( "Could not find a corresponding" );
            }
        }
    }

    /**
     * Compares the two given collections and try to associate intact proteins to uniprot splice variants based on their AC.
     *
     * @param intactProteins        collection of intact protein (splice variant).
     * @param uniprotSpliceVariants collection of uniprot splice variants.
     *
     * @return a non null collection of matches.
     *
     * @throws ProteinServiceException when an intact protein doesn't have an identity.
     */
    private Collection<SpliceVariantMatch> findMatches( Collection<ProteinImpl> intactProteins,
                                                        Collection<UniprotSpliceVariant> uniprotSpliceVariants
    ) throws ProteinServiceException {

        int max = intactProteins.size() + uniprotSpliceVariants.size();
        Collection<SpliceVariantMatch> matches = new ArrayList<SpliceVariantMatch>( max );

        // copy the intact collection
        Collection<Protein> proteins = new ArrayList<Protein>( intactProteins );

        CvDatabase uniprot = CvHelper.getDatabaseByMi( CvDatabase.UNIPROT_MI_REF );
        CvXrefQualifier identity = CvHelper.getQualifierByMi( CvXrefQualifier.IDENTITY_MI_REF );

        for ( Iterator<UniprotSpliceVariant> itsv = uniprotSpliceVariants.iterator(); itsv.hasNext(); ) {
            UniprotSpliceVariant usv = itsv.next();

            boolean found = false;
            for ( Iterator<Protein> itp = proteins.iterator(); itp.hasNext() && false == found; ) {
                Protein protein = itp.next();

                Collection<Xref> xrefs = AnnotatedObjectUtils.searchXrefs( protein, uniprot, identity );
                String upac = null;
                if ( xrefs.size() == 1 ) {
                    upac = xrefs.iterator().next().getPrimaryId();
                    xrefs = null;
                } else {
                    throw new ProteinServiceException( "Could not find a unique UniProt identity for splice variant: " + protein.getAc() );
                }

                if ( usv.getPrimaryAc().equals( upac ) ) {
                    // found it
                    matches.add( new SpliceVariantMatch( protein, usv ) );
                    itp.remove(); // that protein was matched !
                    itsv.remove(); // that splice variant was matched !
                    found = true;
                }

                if ( !found ) {
                    // Search by secondary AC
                    for ( Iterator<String> iterator = usv.getSecondaryAcs().iterator(); iterator.hasNext() && false == found; )
                    {
                        String ac = iterator.next();

                        if ( ac.equals( upac ) ) {
                            // found it
                            matches.add( new SpliceVariantMatch( protein, usv ) );
                            itp.remove(); // that protein was matched !
                            itsv.remove(); // that splice variant was matched !
                            found = true;
                        }
                    }
                }
            } // for - intact proteins

            if ( !found ) {
                matches.add( new SpliceVariantMatch( null, usv ) ); // no mapping found
            }

        } // for - uniprot splice variants

        for ( Protein protein : proteins ) {
            matches.add( new SpliceVariantMatch( protein, null ) ); // no mapping found
        }

        return matches;
    }

    /**
     * Update an existing splice variant.
     *
     * @param spliceVariant
     * @param uniprotSpliceVariant
     */
    private void updateSpliceVariant( Protein spliceVariant, Protein master,
                                      UniprotSpliceVariant uniprotSpliceVariant,
                                      UniprotProtein uniprotProtein
    ) throws ProteinServiceException {

        String shorltabel = uniprotSpliceVariant.getPrimaryAc();
        spliceVariant.setShortLabel( shorltabel );

        spliceVariant.setFullName( master.getFullName() );

        if ( uniprotSpliceVariant.getSequence() == null ) {
            if ( log.isDebugEnabled() ) {
                log.error( "Splice variant " + uniprotSpliceVariant.getPrimaryAc() + " has no sequence" );
            }
            return;
        }

        boolean sequenceUpdated = false;
        if ( !uniprotSpliceVariant.getSequence().equals( spliceVariant.getSequence() ) ) {
            spliceVariant.setSequence( uniprotSpliceVariant.getSequence() );
            sequenceUpdated = true;
        }

        spliceVariant.setCrc64( Crc64.getCrc64( spliceVariant.getSequence() ) );

        // Add IntAct Xref - done in the shallow creation ??

        // update UniProt Xrefs
        XrefUpdaterUtils.updateSpliceVariantUniprotXrefs( spliceVariant, uniprotSpliceVariant, uniprotProtein );

        // Update Aliases
        AliasUpdaterUtils.updateAllAliases( spliceVariant, uniprotSpliceVariant );

        // Update Note
        String note = uniprotSpliceVariant.getNote();
        if ( ( note != null ) && ( !note.trim().equals( "" ) ) ) {
            Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            CvObjectDao<CvTopic> cvDao = daoFactory.getCvObjectDao( CvTopic.class );
            CvTopic comment = cvDao.getByShortLabel( CvTopic.ISOFORM_COMMENT );

            Annotation annotation = new Annotation( owner, comment );
            annotation.setAnnotationText( note );
            AnnotationUpdaterUtils.addNewAnnotation( spliceVariant, annotation );
        }

        // Persist changed
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ProteinDao pdao = daoFactory.getProteinDao();
        pdao.update( ( ProteinImpl ) spliceVariant );
    }

    /**
     * Create a simple protein in view of updating it.
     * <p/>
     * It should contain the following elements: Shorltabel, Biosource and UniProt Xrefs.
     *
     * @param uniprotSpliceVariant the Uniprot splice variant we are going to build the intact on from.
     *
     * @return a non null, persisted intact protein.
     */
    private Protein createMinimalisticSpliceVariant( UniprotSpliceVariant uniprotSpliceVariant,
                                                     Protein master,
                                                     UniprotProtein uniprotProtein
    ) {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ProteinDao pdao = daoFactory.getProteinDao();

        Protein variant = new ProteinImpl( CvHelper.getInstitution(),
                                           master.getBioSource(),
                                           uniprotSpliceVariant.getPrimaryAc(),
                                           CvHelper.getProteinType() );

        pdao.persist( ( ProteinImpl ) variant );

        // Create isoform-parent Xref
        CvXrefQualifier isoformParent = CvHelper.getQualifierByMi( CvXrefQualifier.ISOFORM_PARENT_MI_REF );
        CvDatabase intact = CvHelper.getDatabaseByMi( CvDatabase.INTACT_MI_REF );
        InteractorXref xref = new InteractorXref( CvHelper.getInstitution(), intact, master.getAc(), isoformParent );
        variant.addXref( xref );
        XrefDao xdao = daoFactory.getXrefDao();
        xdao.persist( xref );

        // Create UniProt Xrefs
        XrefUpdaterUtils.updateSpliceVariantUniprotXrefs( variant, uniprotSpliceVariant, uniprotProtein );

        pdao.update( ( ProteinImpl ) variant );

        return variant;
    }

    /**
     * Create a simple protein in view of updating it.
     * <p/>
     * It should contain the following elements: Shortlabel, Biosource and UniProt Xrefs.
     *
     * @param uniprotProtein the Uniprot protein we are going to build the intact on from.
     *
     * @return a non null, persisted intact protein.
     */
    private Protein createMinimalisticProtein( UniprotProtein uniprotProtein ) throws ProteinServiceException {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ProteinDao pdao = daoFactory.getProteinDao();

        BioSource biosource = null;
        try {
            biosource = bioSourceService.getBiosourceByTaxid( String.valueOf( uniprotProtein.getOrganism().getTaxid() ) );
        } catch ( BioSourceServiceException e ) {
            throw new ProteinServiceException(e);
        }

        Protein protein = new ProteinImpl( CvHelper.getInstitution(),
                                           biosource,
                                           generateProteinShortlabel( uniprotProtein ),
                                           CvHelper.getProteinType() );

        pdao.persist( ( ProteinImpl ) protein );

        // Create UniProt Xrefs
        XrefUpdaterUtils.updateUniprotXrefs( protein, uniprotProtein );

        pdao.update( ( ProteinImpl ) protein );

        return protein;
    }

    private Collection<Protein> searchIntact( String uniprotAc ) {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        ProteinDao pdao = daoFactory.getProteinDao();
        List<ProteinImpl> proteins = pdao.getByUniprotId( uniprotAc );

        Collection<Protein> p = new ArrayList<Protein>( proteins.size() );
        for ( ProteinImpl protein : proteins ) {
            p.add( protein );
        }

        return p;
    }

    private Collection<Protein> searchIntactByPrimaryAc( UniprotProtein uniprotProtein ) {
        Collection<Protein> proteins = searchIntact( uniprotProtein.getPrimaryAc() );

        if ( log.isDebugEnabled() ) {
            log.debug( "Searching by Primary Ac yielded " + proteins.size() + " proteins." );
        }

        return proteins;
    }

    private Collection<Protein> searchIntactBySecondaryAc( UniprotProtein uniprotProtein ) {
        Collection<Protein> proteins = new ArrayList<Protein>( 2 );

        for ( String ac : uniprotProtein.getSecondaryAcs() ) {

            Collection<Protein> ps = searchIntact( ac );
            if ( log.isDebugEnabled() ) {
                log.debug( "Searching by secondary AC[ " + ac + " ] yielded " + ps.size() + " proteins." );
            }
            proteins.addAll( ps );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Search by secondary AC yielded overall " + proteins.size() + " proteins." );
        }

        return proteins;
    }

    private Collection<UniprotProtein> retrieveFromUniprot( String uniprotId ) {
        return uniprotService.retreive( uniprotId );
    }

    /**
     * Remove from the given IntAct proteins all of those that are not original UniProt proteins.
     * Removed proteins are returned to the user. The given collection may be altered.
     *
     * @param proteins the collection to be updated.
     *
     * @return a non null colection of IntAct protein that are not from UniProt.
     */
    private Collection<Protein> removeAndGetNonUniprotProteins( Collection<Protein> proteins ) {
        Collection<Protein> selected = new ArrayList<Protein>( proteins.size() );

        for ( Iterator<Protein> iterator = proteins.iterator(); iterator.hasNext(); ) {
            Protein protein = iterator.next();

            if ( !ProteinUtils.isFromUniprot( protein ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Protein " + protein.getShortLabel() + " (" + protein.getAc() + ") is not from UniProt." );
                }
                iterator.remove();
                selected.add( protein );
            }
        }

        return selected;
    }

    private String generateProteinShortlabel( UniprotProtein uniprotProtein ) {

        String name = null;

        if ( uniprotProtein == null ) {
            throw new NullPointerException( "uniprotProtein must not be null." );
        }

        // if this is a TrEMBL protein, we need to add _SPECIES to it !!
        UniprotProteinType type = uniprotProtein.getSource();
        if ( log.isDebugEnabled() ) {
            log.debug( "Protein type: " + type );
        }
        if ( type != null && type.equals( UniprotProteinType.TREMBL ) ) {
            name = uniprotProtein.getId() + "_" + uniprotProtein.getOrganism().getName().toUpperCase();
        } else {
            name = uniprotProtein.getId();
        }

        return name.toLowerCase();
    }

    private Collection<Protein> filterByTaxid( Collection<Protein> proteins, String taxid ) {

        if ( log.isDebugEnabled() ) {
            log.debug( "Filtering protein collection (" + proteins.size() + ") by taxid: " + taxid );
        }
        Collection<Protein> filtered = new ArrayList<Protein>( proteins.size() );

        for ( Protein protein : proteins ) {
            if ( protein.getBioSource().getTaxId().equals( taxid ) ) {
                filtered.add( protein );
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "After filtering, " + filtered.size() + " protein(s) remain." );
        }

        return filtered;
    }

    /**
     * Get existing splice variant from the master protein given. <br>
     *
     * @param master The master protein of the splice variant
     *
     * @return the created splice variants
     */
    private Collection<ProteinImpl> getSpliceVariants( Protein master ) {

        if ( master == null ) {
            throw new IllegalArgumentException( "You must give a non null protein." );
        }

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        return daoFactory.getProteinDao().getSpliceVariants( master );
    }
}