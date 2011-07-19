package uk.ac.ebi.intact.update.persistence.protein;

import uk.ac.ebi.intact.update.model.protein.update.ProteinEventName;
import uk.ac.ebi.intact.update.model.protein.update.events.PersistentProteinEvent;
import uk.ac.ebi.intact.update.persistence.UpdateEventDao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * This interface allows to query the database to get protein events
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02-Dec-2010</pre>
 */

public interface ProteinEventDao<T extends PersistentProteinEvent> extends UpdateEventDao<T>, Serializable {

    List<T> getAllProteinEventsByName(ProteinEventName name);
    List<T> getAllProteinEventsByNameAndProteinAc(ProteinEventName name, String proteinAc);

    List<T> getProteinEventsByNameAndProcessId(ProteinEventName name, long processId);
    List<T> getProteinEventsByNameAndProteinAc(ProteinEventName name, String proteinAc, long processId);

    List<T> getProteinEventsByNameAndDate(ProteinEventName name, Date updatedDate);
    List<T> getProteinEventsByNameAndProteinAc(ProteinEventName name, String proteinAc, Date date);

    List<T> getProteinEventsByNameBeforeDate(ProteinEventName name, Date updatedDate);
    List<T> getProteinEventsByNameAndProteinAcBefore(ProteinEventName name, String proteinAc, Date date);

    List<T> getProteinEventsByNameAfterDate(ProteinEventName name, Date updatedDate);
    List<T> getProteinEventsByNameAndProteinAcAfter(ProteinEventName name, String proteinAc, Date date);
}
