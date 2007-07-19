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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import uk.ac.ebi.intact.model.Component;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvObjectEnricher implements Enricher<Component> {

    private static ThreadLocal<CvObjectEnricher> instance = new ThreadLocal<CvObjectEnricher>() {
        @Override
        protected CvObjectEnricher initialValue() {
            return new CvObjectEnricher();
        }
    };

    public static CvObjectEnricher getInstance() {
        return instance.get();
    }

    protected CvObjectEnricher() {
    }

    public void enrich(Component objectToEnrich) {

    }

    public void close() {
    }
}