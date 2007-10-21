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
package uk.ac.ebi.intact.config.impl;

import uk.ac.ebi.intact.config.IntactPersistence;
import uk.ac.ebi.intact.context.IntactSession;

import javax.persistence.EntityManagerFactory;

/**
 * This configuration uses a memory database (H2)
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InMemoryDataConfig extends AbstractJpaDataConfig {

    public static final String NAME = "uk.ac.ebi.intact.config.IN_MEMORY";

    private EntityManagerFactory entityManagerFactory;

    public InMemoryDataConfig(IntactSession session) {
        super(session);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory = IntactPersistence.createEntityManagerFactory("intact-core-mem");
        }
        return entityManagerFactory;
    }

    @Override
    public String getName() {
        return NAME;
    }
}