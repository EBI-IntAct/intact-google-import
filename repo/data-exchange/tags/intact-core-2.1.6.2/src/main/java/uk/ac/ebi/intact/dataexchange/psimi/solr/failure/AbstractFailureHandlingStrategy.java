/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dataexchange.psimi.solr.failure;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.3
 */
public class AbstractFailureHandlingStrategy implements FailureHandlingStrategy {

    private int failureCount;

    public AbstractFailureHandlingStrategy() {
        this.failureCount = 0;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount( int failureCount ) {
        this.failureCount = failureCount;
    }

    ////////////////////////////
    // FailureHandlingStrategy

    public void handleFailure( Throwable t, String mitabLine, int lineCount ) {
    }
}
