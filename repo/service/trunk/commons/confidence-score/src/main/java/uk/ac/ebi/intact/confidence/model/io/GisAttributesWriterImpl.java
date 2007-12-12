/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.model.io;

import uk.ac.ebi.intact.confidence.model.BinaryInteractionAttributes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *        12-Dec-2007
 *        </pre>
 */
public class GisAttributesWriterImpl implements BinaryInteractionAttributesWriter {

    public void append( BinaryInteractionAttributes binaryInteractionAttributes, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile, true);
        writer.append( gisLine(binaryInteractionAttributes) + "\n" );
        writer.close();
    }

    public void write( List<BinaryInteractionAttributes> binaryInteractionsWithAttributes, File outFile ) throws IOException {
        Writer writer = new FileWriter(outFile);
        for ( Iterator<BinaryInteractionAttributes> attrIter = binaryInteractionsWithAttributes.iterator(); attrIter.hasNext(); )
        {
            BinaryInteractionAttributes binAttr = attrIter.next();
            writer.write( gisLine(binAttr) + "\n");
        }
        writer.close();
    }

    private String gisLine(BinaryInteractionAttributes binAttr){
        String line = binAttr.convertToString();
        int i = line.indexOf( ",");
        line = line.substring( i+1);
        line = line.replace( ",", " ");
        line += " " + binAttr.getConfidence().toString();
        return line;
    }
}
