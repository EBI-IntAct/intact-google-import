/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.plugins.psimitab.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.search.engine.SearchEngine;
import psidev.psi.mi.search.engine.SearchEngineException;
import uk.ac.ebi.intact.psimitab.search.IntActFastSearchEngine;
import uk.ac.ebi.intact.psimitab.search.IntActPsimiTabIndexWriter;

public class IndexerMojoTest extends AbstractMojoTestCase {

    @Test
    public void testIndex() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "src/test/plugin-configs/index-config.xml");

        IndexerMojo mojo = (IndexerMojo) lookupMojo("index", pluginXmlFile);
        mojo.setLog(new SystemStreamLog());

        mojo.execute();

        SearchResult result = Searcher.search("P38974", FSDirectory.getDirectory(mojo.getIndexDirectory()));
        assertEquals(1, result.getInteractions().size());
    }
    
    
    @Test
    public void testIndex2() throws Exception {
        File pluginXmlFile = new File(getBasedir(), "src/test/plugin-configs/index-config-extra.xml");

        IndexerMojo mojo = (IndexerMojo) lookupMojo("index", pluginXmlFile);
        
        mojo.setLog(new SystemStreamLog());
        mojo.setIndexWriter(new IntActPsimiTabIndexWriter());

        mojo.execute();
        
        Directory indexDirectory = FSDirectory.getDirectory(mojo.getIndexDirectory());
        
        SearchEngine engine;
        try
        {
            engine = new IntActFastSearchEngine(indexDirectory);
        }
        catch (IOException e)
        {
            throw new SearchEngineException(e);
        }
        
        SearchResult id_result = Searcher.search("P35568", FSDirectory.getDirectory(mojo.getIndexDirectory()), engine);
        assertEquals(1, id_result.getInteractions().size());
        
        SearchResult go_result = Searcher.search("GO0005069", FSDirectory.getDirectory(mojo.getIndexDirectory()), engine);
        assertEquals(1, go_result.getInteractions().size());
    }
}
