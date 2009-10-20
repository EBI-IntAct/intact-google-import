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
package uk.ac.ebi.intact.util.cdb;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PubmedIdCheckerTest {

    @Test
    public void isPubmedId() throws Exception {
        Assert.assertTrue(PubmedIdChecker.isPubmedId("1234567"));
        Assert.assertFalse(PubmedIdChecker.isPubmedId("unassigned4"));
    }

    @Test
    public void ensureValidFormat_valid() throws Exception {
        PubmedIdChecker.ensureValidFormat("1234567");

        Assert.assertTrue(true);
    }

    @Test (expected = InvalidPubmedException.class )
    public void ensureValidFormat_invalid() throws Exception {
        PubmedIdChecker.ensureValidFormat("unassigned3");
    }
}