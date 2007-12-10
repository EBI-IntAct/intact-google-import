package uk.ac.ebi.intact.application.hierarchview; /**
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * TODO comment that class header
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public abstract class TestHelper {


    public static File getTargetDirectory() throws UnsupportedEncodingException {
        String outputDirPath = getFileByResources( "/", TestHelper.class ).getAbsolutePath();

        assertNotNull( outputDirPath );
        File outputDir = new File( outputDirPath );
        // we are in test-classes, move one up
        outputDir = outputDir.getParentFile();
        assertNotNull( outputDir );
        assertTrue( outputDir.isDirectory() );
        assertEquals( "target", outputDir.getName() );

        return outputDir;
    }

    public static File getFileByResources( String fileName, Class clazz ) throws UnsupportedEncodingException {
        String strFile = clazz.getResource( fileName ).getFile();
        return new File( URLDecoder.decode( strFile, "utf-8" ) );
    }
}
