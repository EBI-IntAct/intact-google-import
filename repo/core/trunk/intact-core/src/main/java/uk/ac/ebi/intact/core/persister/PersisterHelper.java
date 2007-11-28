/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.core.persister;

import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

/**
 * Helper class to reduce the code needed to save or update an Annotated object.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PersisterHelper {
    private PersisterHelper() {}

    public static void saveOrUpdate( IntactEntry... intactEntries ) throws PersisterException {
        for ( IntactEntry intactEntry : intactEntries ) {
            for ( Interaction interaction : intactEntry.getInteractions() ) {
                saveOrUpdate( interaction );
            }
        }
    }

    public static void saveOrUpdate( AnnotatedObject... annotatedObjects ) throws PersisterException {
        boolean inTransaction = IntactContext.getCurrentInstance().getDataContext().isTransactionActive();

        if ( !inTransaction ) IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        CorePersister corePersister = new CorePersister();

        for ( AnnotatedObject ao : annotatedObjects ) {
            corePersister.synchronize( ao );
        }
        corePersister.commit();

        for ( AnnotatedObject ao : annotatedObjects ) {
            corePersister.reload( ao );
        }

        if ( !inTransaction ) corePersister.commitTransactionAndRollbackIfNecessary();

    }
}