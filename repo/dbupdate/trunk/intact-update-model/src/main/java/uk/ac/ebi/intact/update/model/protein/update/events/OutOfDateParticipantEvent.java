package uk.ac.ebi.intact.update.model.protein.update.events;

import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.update.model.protein.update.UpdateProcess;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Event for participants having feature conflicts when trying to update the proteinAc sequence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25-Nov-2010</pre>
 */
@Entity
@DiscriminatorValue("OutOfDateParticipantEvent")
public class OutOfDateParticipantEvent extends ProteinEvent{

    private Collection<String> componentsWithFeatureConflicts;
    private String remapped_protein;

    public OutOfDateParticipantEvent(){
        super();
        this.componentsWithFeatureConflicts = new ArrayList<String>();
        this.remapped_protein = null;

    }

    public OutOfDateParticipantEvent(UpdateProcess updateProcess, Protein protein, int index, Protein fixedProtein){
        super(updateProcess, EventName.participant_with_feature_conflicts, protein, index);
        this.componentsWithFeatureConflicts = new ArrayList<String>();
        this.remapped_protein = fixedProtein != null ? fixedProtein.getAc() : null;
    }

    @ElementCollection
    @CollectionTable(name="ia_component_conflicts", joinColumns=@JoinColumn(name="event_id"))
    @Column(name="component_ac")
    public Collection<String> getComponentsWithFeatureConflicts() {
        return componentsWithFeatureConflicts;
    }

    public void setComponentsWithFeatureConflicts(Collection<String> componentsWithFeatureConflicts) {
        if (componentsWithFeatureConflicts != null){
            this.componentsWithFeatureConflicts = componentsWithFeatureConflicts;
        }
    }

    @Column(name="remapped_protein_ac", nullable = true)
    public String getRemapped_protein() {
        return remapped_protein;
    }

    public void setRemapped_protein(String remapped_protein) {
        this.remapped_protein = remapped_protein;
    }
}
