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
package uk.ac.ebi.intact.psixml.persister.shared;

import org.junit.Test;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.psixml.commons.model.IntactEntry;
import uk.ac.ebi.intact.psixml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.psixml.persister.PersisterReport;

import java.io.InputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EntryPersisterTest {

    private static final String INTACT_FILE = "/xml/intact_2006-07-19.xml";
    private static final String MINT_FILE = "/xml/mint_2006-07-18.xml";

    private static final boolean DRY_RUN = false;

    @Test
    public void entryToIntactDefault() throws Exception {

        InputStream is = EntryPersisterTest.class.getResourceAsStream(INTACT_FILE);
        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = reader.read(is);

        EntryConverter entryConverter = new EntryConverter(IntactContext.getCurrentInstance().getInstitution());

        for (Entry psiEntry : entrySet.getEntries()) {

            IntactEntry intactEntry = entryConverter.psiToIntact(psiEntry);
            IntactEntryPersister persister = new IntactEntryPersister(IntactContext.getCurrentInstance(), DRY_RUN);

            IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            persister.saveOrUpdate(intactEntry);
            PersisterReport report = persister.getReport();

            System.out.println("Report: created: " + report.getCreated().size() + " / updated: " + report.getUpdated().size());

            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        }


    }

}