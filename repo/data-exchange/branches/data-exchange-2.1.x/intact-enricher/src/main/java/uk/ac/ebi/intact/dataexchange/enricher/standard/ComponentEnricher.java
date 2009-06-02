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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.CvExperimentalPreparation;
import uk.ac.ebi.intact.model.CvExperimentalRole;
import uk.ac.ebi.intact.model.CvIdentification;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class ComponentEnricher extends AnnotatedObjectEnricher<Component>{

    @Autowired
    private BioSourceEnricher bioSourceEnricher;

    @Autowired
    private InteractorEnricher interactorEnricher;

    @Autowired
    private CvObjectEnricher cvObjectEnricher;

    public ComponentEnricher() {
    }

    public void enrich(Component objectToEnrich) {
        if (objectToEnrich.getExpressedIn() != null) {
            bioSourceEnricher.enrich(objectToEnrich.getExpressedIn());
        }

        interactorEnricher.enrich(objectToEnrich.getInteractor());

        if (objectToEnrich.getCvBiologicalRole() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvBiologicalRole());
        }
        for (CvExperimentalRole expRole : objectToEnrich.getExperimentalRoles()) {
            cvObjectEnricher.enrich(expRole);
        }
        for (CvExperimentalPreparation experimentalPreparation : objectToEnrich.getExperimentalPreparations()) {
            cvObjectEnricher.enrich(experimentalPreparation);
        }
        for (CvIdentification participantDetectionMethods : objectToEnrich.getParticipantDetectionMethods()) {
            cvObjectEnricher.enrich(participantDetectionMethods);
        }

        super.enrich(objectToEnrich);
    }
}
