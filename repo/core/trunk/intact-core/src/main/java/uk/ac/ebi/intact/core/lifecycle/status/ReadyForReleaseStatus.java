/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.core.lifecycle.status;

import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.lifecycle.LifecycleEventListener;
import uk.ac.ebi.intact.core.lifecycle.LifecycleTransition;
import uk.ac.ebi.intact.model.CvLifecycleEventType;
import uk.ac.ebi.intact.model.CvPublicationStatusType;
import uk.ac.ebi.intact.model.Publication;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class ReadyForReleaseStatus extends GlobalStatus {

    public ReadyForReleaseStatus() {
        setStatusType( CvPublicationStatusType.READY_FOR_RELEASE );
    }

    /**
     * The publication is made public.
     *
     * @param publication the publication
     * @param releaseId mandatory release process ID
     */
    @LifecycleTransition(fromStatus = CvPublicationStatusType.READY_FOR_RELEASE, toStatus = CvPublicationStatusType.RELEASED)
    public void release(Publication publication, String releaseId) {
        enfoceMandatory(releaseId);
        changeStatus(publication, CvPublicationStatusType.RELEASED, CvLifecycleEventType.RELEASED, releaseId);

        // Notify listeners
        for ( LifecycleEventListener listener : getListeners() ) {
            listener.fireReleased( publication );
        }
    }
}