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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.IntactEnvironment;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.enricher.PsiEnricher;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Component;

import java.io.*;
import java.util.Properties;
import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        File fileToImport = new File("/ebi/sp/pro6/intact/local/data/curation-material/whitworth-2007/whitworth2007.xml");

        Properties props = new Properties();
        props.put(IntactEnvironment.INSTITUTION_LABEL.getFqn(), "ebi");
        props.put(IntactEnvironment.AC_PREFIX_PARAM_NAME.getFqn(), "EBI");

        File hibernateFile = new File(Playground.class.getResource("/d003-hibernate.cfg.xml").getFile());

        IntactContext.initStandaloneContext(hibernateFile);
        IntactContext.getCurrentInstance().getUserContext().setUserId("BARANDA");

        EnricherConfig enricherConfig = new EnricherConfig();
        enricherConfig.setUpdateInteractionShortLabels(true);

        EnricherContext.getInstance().getConfig().setUpdateInteractionShortLabels(true);

        System.out.println("Importing: " + fileToImport);

        InputStream is = new FileInputStream(fileToImport);

        File tempFile = File.createTempFile("psiToImport-", ".xml");
        Writer writer = new FileWriter(tempFile);

        PsiEnricher.enrichPsiXml(is, writer, enricherConfig);

        writer.flush();

        InputStream enricherInput = new FileInputStream(tempFile);

        int interactionsBefore = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().countAll();
        int experimentsBefore = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getExperimentDao().countAll();

        PsiExchange.importIntoIntact(enricherInput);

        int interactionsAfter = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().countAll();
        int experimentsAfter = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getExperimentDao().countAll();

        System.out.println("Interactions Created: " + (interactionsAfter-interactionsBefore));
        System.out.println("Experiments Created: " + (experimentsAfter-experimentsBefore));


    }


}