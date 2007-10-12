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
package uk.ac.ebi.intact.core.persister.standard;

import uk.ac.ebi.intact.model.BioSource;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourcePersister extends AbstractAnnotatedObjectPersister<BioSource>{

    private static ThreadLocal<BioSourcePersister> instance = new ThreadLocal<BioSourcePersister>() {
        @Override
        protected BioSourcePersister initialValue() {
            return new BioSourcePersister();
        }
    };

    public static BioSourcePersister getInstance() {
        return instance.get();
    }

    protected BioSourcePersister() {
        super();
    }

    /**
     * TODO: uniqueness - taxid,celltype,tissue
     */
    protected BioSource fetchFromDataSource(BioSource intactObject) {
        if (intactObject.getTaxId() == null) {
            throw new IllegalArgumentException("Organism without a tax id");
        }

        // TODO: this will fail with specific celltype
        return getIntactContext().getDataContext().getDaoFactory()
                .getBioSourceDao().getByTaxonIdUnique(intactObject.getTaxId());
    }

}