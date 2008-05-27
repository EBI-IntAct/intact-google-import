/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.dbupdate.prot.event.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyService;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dbupdate.prot.ProcessorException;
import uk.ac.ebi.intact.dbupdate.prot.ProteinProcessor;
import uk.ac.ebi.intact.dbupdate.prot.ProteinUpdateProcessor;
import uk.ac.ebi.intact.dbupdate.prot.event.*;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.uniprot.service.UniprotService;
import uk.ac.ebi.intact.util.biosource.BioSourceServiceFactory;
import uk.ac.ebi.intact.util.protein.ProteinServiceImpl;

/**
 * Updates the current protein in the database, using information from uniprot
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UniprotProteinUpdater extends ProteinServiceImpl implements ProteinUpdateProcessorListener, ProteinProcessorListener {
    
    private static final Log log = LogFactory.getLog( UniprotProteinUpdater.class );

    private ProteinUpdateProcessor proteinProcessor;

    public UniprotProteinUpdater(UniprotService uniprotService, TaxonomyService taxonomyService) {
        super(uniprotService);
        setBioSourceService(BioSourceServiceFactory.getInstance().buildBioSourceService(taxonomyService));
    }

    public void onPreProcess(ProteinEvent evt) throws ProcessorException {
        Protein protein = evt.getProtein();

        if (log.isTraceEnabled()) log.trace("Checking if the protein can be updated using UniProt information: "+ protein.getShortLabel()+" ("+protein.getAc()+")");

        if (!ProteinUtils.isFromUniprot(protein)) {
            if (log.isTraceEnabled()) log.trace("Request finalization, as this protein cannot be updated using UniProt");
            ((ProteinProcessor)evt.getSource()).finalizeAfterCurrentPhase();
        }
    }

    public void onProcess(ProteinEvent evt) throws ProcessorException {
        this.proteinProcessor = (ProteinUpdateProcessor) evt.getSource();

        Protein protToUpdate = evt.getProtein();

        InteractorXref uniprotXref = ProteinUtils.getUniprotXref(protToUpdate);
        try {
            retrieve(uniprotXref.getPrimaryId());
        } catch (Exception e) {
            throw new ProcessorException(e);
        }

    }

    public void onDelete(ProteinEvent evt) throws ProcessorException {}

    public void onProteinDuplicationFound(MultiProteinEvent evt) throws ProcessorException {}

    public void onDeadProteinFound(ProteinEvent evt) throws ProcessorException {}

    public void onProteinSequenceChanged(ProteinSequenceChangeEvent evt) throws ProcessorException {}

    public void onProteinCreated(ProteinEvent evt) throws ProcessorException {}

    @Override
    protected void deleteProtein(Protein protein) {
        proteinProcessor.fireOnDelete(new ProteinEvent(proteinProcessor, IntactContext.getCurrentInstance().getDataContext(), protein));
    }

    @Override
    protected void sequenceChanged(Protein protein, String oldSequence) {
        proteinProcessor.fireOnProteinSequenceChanged(new ProteinSequenceChangeEvent(proteinProcessor, IntactContext.getCurrentInstance().getDataContext(), protein, oldSequence));
    }

    @Override
    protected void proteinCreated(Protein protein) {
         proteinProcessor.fireOnProteinCreated(new ProteinEvent(proteinProcessor, IntactContext.getCurrentInstance().getDataContext(), protein));
    }
}
