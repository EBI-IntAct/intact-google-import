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
package uk.ac.ebi.intact.task.mitab;

import org.springframework.batch.item.file.LineMapper;
import psidev.psi.mi.tab.io.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MitabLineMapper implements LineMapper<BinaryInteraction>{

    private PsimiTabReader psimitabReader;

    public MitabLineMapper() {
        this.psimitabReader = new PsimiTabReader();
    }

    public BinaryInteraction mapLine(String line, int lineNumber) throws Exception {
        
        try {
            return psimitabReader.readLine(line);
        } catch (Exception e) {
            throw new Exception("Problem converting to binary interaction line "+lineNumber+": "+line);
        }
    }
}
