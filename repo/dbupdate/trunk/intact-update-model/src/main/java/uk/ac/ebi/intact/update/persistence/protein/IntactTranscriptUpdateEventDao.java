package uk.ac.ebi.intact.update.persistence.protein;

import uk.ac.ebi.intact.annotation.Mockable;
import uk.ac.ebi.intact.update.model.protein.update.events.IntactTranscriptUpdateEvent;

/**
 * Dao for intact transcript update events
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/08/11</pre>
 */
 @Mockable
public interface IntactTranscriptUpdateEventDao extends ProteinEventDao<IntactTranscriptUpdateEvent> {
}
