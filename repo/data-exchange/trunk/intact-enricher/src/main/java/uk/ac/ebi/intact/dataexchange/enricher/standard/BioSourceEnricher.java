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
package uk.ac.ebi.intact.dataexchange.enricher.standard;

import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyTerm;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.intact.dataexchange.enricher.fetch.BioSourceFetcher;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.BioSourceXref;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.model.util.CvObjectBuilder;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BioSourceEnricher extends AnnotatedObjectEnricher<BioSource> {

     private static ThreadLocal<BioSourceEnricher> instance = new ThreadLocal<BioSourceEnricher>() {
        @Override
        protected BioSourceEnricher initialValue() {
            return new BioSourceEnricher();
        }
    };

    public static BioSourceEnricher getInstance() {
        return instance.get();
    }

    protected BioSourceEnricher() {
    }

    public void enrich(BioSource objectToEnrich) {

        // get the taxonomy term from newt
        int taxId = Integer.valueOf(objectToEnrich.getTaxId());

        if (taxId == 0) {
            throw new EnricherException("Biosource has an invalid taxid: "+taxId+" ("+objectToEnrich.getFullName()+")");
        }

        TaxonomyTerm term = BioSourceFetcher.getInstance().fetchByTaxId(taxId);

        String label = term.getCommonName();
        String fullName = term.getScientificName();

        if (label == null || label.length() == 0) {
            label = fullName;
        }

        if (label != null) {
            label = AnnotatedObjectUtils.prepareShortLabel(label.toLowerCase());
            objectToEnrich.setShortLabel(label);
        }

        if (fullName != null) {
            objectToEnrich.setFullName(fullName);
        }

        CvObjectEnricher cvObjectEnricher = CvObjectEnricher.getInstance();
        if (objectToEnrich.getCvCellType() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvCellType());
        }
        if (objectToEnrich.getCvTissue() != null) {
            cvObjectEnricher.enrich(objectToEnrich.getCvTissue());
        }

        // check if it has a newt xref
        checkNewtXref(objectToEnrich);

        super.enrich(objectToEnrich);
    }

    protected void checkNewtXref(BioSource organism) {
        boolean hasNewt = false;

        for (BioSourceXref xref : organism.getXrefs()) {
            if (CvObjectUtils.getPsiMiIdentityXref(xref.getCvDatabase()).getPrimaryId()
                    .equals(CvDatabase.NEWT_MI_REF)) {
                hasNewt = true;
                break;
            }
        }

        if (!hasNewt) {
            CvObjectBuilder cvObjectBuilder = new CvObjectBuilder();
            CvXrefQualifier identityQual = cvObjectBuilder.createIdentityCvXrefQualifier(organism.getOwner());
            CvDatabase newtDb = CvObjectUtils.createCvObject(organism.getOwner(), CvDatabase.class, CvDatabase.NEWT_MI_REF, CvDatabase.UNIPROT);

            BioSourceXref newtXref = XrefUtils.createIdentityXref(organism, organism.getTaxId(), identityQual, newtDb);
            organism.getXrefs().add(newtXref);
        }
    }

    public void close() {
    }
}