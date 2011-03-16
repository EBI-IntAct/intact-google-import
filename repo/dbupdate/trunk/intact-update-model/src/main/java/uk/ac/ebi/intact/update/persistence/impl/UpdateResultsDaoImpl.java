package uk.ac.ebi.intact.update.persistence.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.update.model.protein.mapping.actions.ActionName;
import uk.ac.ebi.intact.update.model.protein.mapping.actions.status.StatusLabel;
import uk.ac.ebi.intact.update.model.protein.mapping.results.UpdateMappingResults;
import uk.ac.ebi.intact.update.persistence.UpdateResultsDao;

import javax.persistence.Query;
import java.util.List;

/**
 * The basic implementation of UpdateResultsDao
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21-May-2010</pre>
 */
@Repository
@Transactional(readOnly = true)
@Lazy
public class UpdateResultsDaoImpl extends UpdateBaseDaoImpl<UpdateMappingResults> implements UpdateResultsDao {
    /**
     * Create a new UpdateResultsDaoImpl
     */
    public UpdateResultsDaoImpl() {
        super(UpdateMappingResults.class);
    }

    /**
     *
     * @param id
     * @return
     */
    public UpdateMappingResults getUpdateResultsWithId(long id) {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u where u.id = :id" );
        query.setParameter( "id", id);

        if (query.getResultList().isEmpty()){
            return null;
        }
        return (UpdateMappingResults) query.getResultList().iterator().next();
    }

    /**
     *
     * @param proteinAc
     * @return
     */
    public UpdateMappingResults getUpdateResultsForProteinAc(String proteinAc) {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u where u.intactAccession = :proteinAc" );
        query.setParameter( "proteinAc", proteinAc);

        if (query.getResultList().isEmpty()){
            return null;
        }
        return (UpdateMappingResults) query.getResultList().iterator().next();
    }

    /**
     *
     * @param name
     * @return
     */
    public List<UpdateMappingResults> getResultsContainingAction(ActionName name) {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.name = :name" );
        query.setParameter( "name", name);

        return query.getResultList();
    }

    /**
     *
     * @param label
     * @return
     */
    public List<UpdateMappingResults> getResultsContainingActionWithLabel(StatusLabel label) {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.statusLabel = :status" );
        query.setParameter( "status", label);

        return query.getResultList();
    }

    /**
     *
     * @return
     */
    public List<UpdateMappingResults> getUpdateResultsWithSwissprotRemapping() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.name = :name" );
        query.setParameter( "name", ActionName.BLAST_Swissprot_Remapping);

        return query.getResultList();
    }

    /**
     *
     * @return
     */
    public List<UpdateMappingResults> getSuccessfulUpdateResults() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u where u.finalUniprotId <> null" );

        return query.getResultList();
    }

    /**
     *
     * @return
     */
    public List<UpdateMappingResults> getUpdateResultsToBeReviewedByACurator() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.statusLabel = :status or" +
                " (a.statusLabel = :status2 and u.finalUniprotId = null and a.name <> :name)" );
        query.setParameter( "status", StatusLabel.TO_BE_REVIEWED);
        query.setParameter( "status2", StatusLabel.COMPLETED);
        query.setParameter( "name", ActionName.update_checking);

        return query.getResultList();
    }

    /**
     *
     * @return
     */
    public List<UpdateMappingResults> getProteinNotUpdatedBecauseNoSequenceAndNoIdentityXrefs() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.statusLabel = :status and a.name = :name" );
        query.setParameter( "status", StatusLabel.FAILED);
        query.setParameter( "name", ActionName.update_checking);

        return query.getResultList();
    }

    /**
     *
     * @return
     */
    public List<UpdateMappingResults> getUnsuccessfulUpdateResults() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where u.finalUniprotId = null and " +
                "u not in ( select u2 from UpdateMappingResults as u2 join u2.listOfActions as a2 where a2.statusLabel <> :status and a2.name <> :name) " +
                "and a.name <> :name" );
        query.setParameter( "status", StatusLabel.FAILED);
        query.setParameter( "name", ActionName.update_checking);

        return query.getResultList();
    }

    /**
     *
     * @return
     */
    public List<UpdateMappingResults> getUpdateResultsWithConflictsBetweenActions() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.statusLabel = :status and a.name = :name" );
        query.setParameter( "status", StatusLabel.TO_BE_REVIEWED);
        query.setParameter( "name", ActionName.update_checking);

        return query.getResultList();
    }

    /**
     * 
     * @return
     */
    public List<UpdateMappingResults> getUpdateResultsWithConflictBetweenSwissprotSequenceAndFeatureRanges() {
        final Query query = getEntityManager().createQuery( "select u from UpdateMappingResults as u join u.listOfActions as a where a.statusLabel = :status and a.name = :name" );
        query.setParameter( "status", StatusLabel.FAILED);
        query.setParameter( "name", ActionName.feature_range_checking);

        return query.getResultList();
    }
}
