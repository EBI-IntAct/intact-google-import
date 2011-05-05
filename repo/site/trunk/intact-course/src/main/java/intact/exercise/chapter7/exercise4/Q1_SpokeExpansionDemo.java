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

package intact.exercise.chapter7.exercise4;

import psidev.psi.mi.tab.expansion.ExpansionStrategy;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;
import psidev.psi.mi.xml.model.EntrySet;

import java.io.File;

/**
 * Question 1: Write some code that expands the interactions in a file by using the Spoke expansion using
 * the provided XML file in the exercise. How many interactions are present in the file and how many binary
 * interactions would be generated with the expansion?
 *
 * @see psidev.psi.mi.tab.expansion.ExpansionStrategy
 * @see psidev.psi.mi.tab.expansion.SpokeExpansion
 * @see psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion
 */
public class Q1_SpokeExpansionDemo {
    public static void main( String[] args ) throws PsimiXmlReaderException {

        // The data file to be parsed by the API
        File inputFile = new File( Q1_SpokeExpansionDemo.class.getResource( "/samples/psixml25/16705748.xml" ).getFile() );

        // Create a reader
        PsimiXmlReader reader = new PsimiXmlReader();

        // Read the whole file into an EntrySet
        final EntrySet entrySet = reader.read( inputFile );

        int interactionCount = 0;
        int expandedBinaryInteractionCount = 0;

        // TODO start here
        ExpansionStrategy strategy = null; // change this!

        // iterate through the Interactions inside the Entries inside the EntrySet and
    }
}
