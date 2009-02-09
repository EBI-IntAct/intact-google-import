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
package uk.ac.ebi.intact.dataexchange.psimi.solr.converter;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.*;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.term.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.bridges.ontologies.OntologyIndexSearcher;

import java.util.*;

/**
 * Converts from Row to SolrDocument and viceversa.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrDocumentConverter {

    private static final String DB_GO = "go";
    private static final String DB_INTERPRO = "interpro";
    private static final String DB_PSIMI = "psi-mi";

    private DocumentDefinition documentDefintion;

    /**
     * Access to the Ontology index.
     */
    private OntologyIndexSearcher ontologySearcher;

    public SolrDocumentConverter(DocumentDefinition documentDefintion) {
        this.documentDefintion = documentDefintion;
    }

    public SolrDocumentConverter(DocumentDefinition documentDefintion,
                                 OntologyIndexSearcher ontologySearcher) {
        this.documentDefintion = documentDefintion;
        this.ontologySearcher = ontologySearcher;
    }

    public SolrInputDocument toSolrDocument(String mitabLine) {
        Row row = documentDefintion.createRowBuilder().createRow(mitabLine);
        return toSolrDocument(row, mitabLine);
    }

    public SolrInputDocument toSolrDocument(BinaryInteraction binaryInteraction) {
        Row row = documentDefintion.createInteractionRowConverter().createRow(binaryInteraction);
        return toSolrDocument(row);
    }

    public SolrInputDocument toSolrDocument(Row row) {
        return toSolrDocument(row, row.toString());
    }

    protected SolrInputDocument toSolrDocument(Row row, String mitabLine) {
        SolrInputDocument doc = new SolrInputDocument();

        // store the mitab line
        doc.addField("line", mitabLine);

        addColumnToDoc(doc, row, "idA", IntactDocumentDefinition.ID_INTERACTOR_A);
        addColumnToDoc(doc, row, "idB", IntactDocumentDefinition.ID_INTERACTOR_B);
        addColumnToDoc(doc, row, "altidA", IntactDocumentDefinition.ALTID_INTERACTOR_A);
        addColumnToDoc(doc, row, "altidB", IntactDocumentDefinition.ALTID_INTERACTOR_B);
        addColumnToDoc(doc, row, "aliasA", IntactDocumentDefinition.ALIAS_INTERACTOR_A);
        addColumnToDoc(doc, row, "aliasB", IntactDocumentDefinition.ALIAS_INTERACTOR_B);
        addColumnToDoc(doc, row, "detmethod_exact", IntactDocumentDefinition.INT_DET_METHOD);
        addColumnToDoc(doc, row, "pubauth", IntactDocumentDefinition.PUB_AUTH);
        addColumnToDoc(doc, row, "pubid", IntactDocumentDefinition.PUB_ID);
        addColumnToDoc(doc, row, "taxidA", IntactDocumentDefinition.TAXID_A);
        addColumnToDoc(doc, row, "taxidB", IntactDocumentDefinition.TAXID_B);
        addColumnToDoc(doc, row, "type_exact", IntactDocumentDefinition.INT_TYPE);
        addColumnToDoc(doc, row, "source", IntactDocumentDefinition.SOURCE);
        addColumnToDoc(doc, row, "interaction_id", IntactDocumentDefinition.INTERACTION_ID);
        addColumnToDoc(doc, row, "confidence", IntactDocumentDefinition.CONFIDENCE);

        // extended
        if (documentDefintion instanceof IntactDocumentDefinition) {
            addColumnToDoc(doc, row, "experimentalRoleA", IntactDocumentDefinition.EXPERIMENTAL_ROLE_A);
            addColumnToDoc(doc, row, "experimentalRoleB", IntactDocumentDefinition.EXPERIMENTAL_ROLE_B);
            addColumnToDoc(doc, row, "biologicalRoleA", IntactDocumentDefinition.BIOLOGICAL_ROLE_A);
            addColumnToDoc(doc, row, "biologicalRoleB", IntactDocumentDefinition.BIOLOGICAL_ROLE_B);
            addColumnToDoc(doc, row, "propertiesA", IntactDocumentDefinition.PROPERTIES_A);
            addColumnToDoc(doc, row, "propertiesB", IntactDocumentDefinition.PROPERTIES_B);
            addColumnToDoc(doc, row, "typeA", IntactDocumentDefinition.INTERACTOR_TYPE_A);
            addColumnToDoc(doc, row, "typeB", IntactDocumentDefinition.INTERACTOR_TYPE_B);
            addColumnToDoc(doc, row, "hostOrganism", IntactDocumentDefinition.HOST_ORGANISM);
            addColumnToDoc(doc, row, "expansion", IntactDocumentDefinition.EXPANSION_METHOD);
            addColumnToDoc(doc, row, "dataset", IntactDocumentDefinition.DATASET);
            addColumnToDoc(doc, row, "annotationA", IntactDocumentDefinition.ANNOTATIONS_A);
            addColumnToDoc(doc, row, "annotationB", IntactDocumentDefinition.ANNOTATIONS_B);
            addColumnToDoc(doc, row, "parameterA", IntactDocumentDefinition.PARAMETERS_A);
            addColumnToDoc(doc, row, "parameterB", IntactDocumentDefinition.PARAMETERS_B);
            addColumnToDoc(doc, row, "parameterInteraction", IntactDocumentDefinition.PARAMETERS_INTERACTION);
        }

        // ac
        doc.addField("pkey", mitabLine);

        return doc;
    }

    public BinaryInteraction toBinaryInteraction(SolrDocument doc) {
        return documentDefintion.interactionFromString(toMitabLine(doc));
    }

    public Row toRow(SolrDocument doc) {
        return documentDefintion.createRowBuilder().createRow(toMitabLine(doc));
    }

    public String toMitabLine(SolrDocument doc) {
        return (String) doc.getFieldValue("line");
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex) {
        // do not process columns not found in the row
        if (row.getColumnCount() <= columnIndex) {
            return;
        }

        Column column = row.getColumnByIndex( columnIndex );

        for (Field field : column.getFields()) {
            doc.addField(fieldName, field.toString());

            if (field.getType() != null) {
                doc.addField(field.getType()+"_xref", field.getValue());
            }

            if (isExpandableOntology(field.getType())) {
                doc.addField(field.getType(), field.getValue());

                if (field.getDescription() != null) {
                    doc.addField("spell", field.getDescription());
                }

                for (Field parentField : getAllParents(field)) {
                    doc.addField(parentField.getType()+"_expanded", parentField.toString());
                    doc.addField(parentField.getType()+"_expanded_id", parentField.getValue());
                }
            }

        }
    }

    private boolean isExpandableOntology( String name ) {
        return (DB_GO.equals(name) ||
                DB_INTERPRO.equals(name) ||
                DB_PSIMI.equals(name));
    }

     /**
     * @param field the field for which we want to get the parents
     * @return list of cv terms with parents and itself
     */
    private List<Field> getAllParents( psidev.psi.mi.tab.model.builder.Field field ) {
        if (ontologySearcher == null) {
            return Collections.EMPTY_LIST;
        }

        List<psidev.psi.mi.tab.model.builder.Field> allParents = null;

        final String type = field.getType();

        if ( isExpandableOntology( type ) ) {
            String identifier = field.getValue();

            // fetch parents and fill the field list
            final OntologyTerm ontologyTerm = new LazyLoadedOntologyTerm( ontologySearcher, identifier );
            final Set<OntologyTerm> parents = ontologyTerm.getAllParentsToRoot();

            allParents = convertTermsToFields( type, parents );
        }

        return ( allParents != null ? allParents : Collections.EMPTY_LIST );
    }

    private List<psidev.psi.mi.tab.model.builder.Field> convertTermsToFields( String type, Set<OntologyTerm> terms ) {
        List<psidev.psi.mi.tab.model.builder.Field> fields =
                new ArrayList<psidev.psi.mi.tab.model.builder.Field>( terms.size());

        for ( OntologyTerm term : terms ) {
            psidev.psi.mi.tab.model.builder.Field field =
                    new psidev.psi.mi.tab.model.builder.Field( type, term.getId(), term.getName() );
            fields.add( field );
        }

        return fields;
    }
}
