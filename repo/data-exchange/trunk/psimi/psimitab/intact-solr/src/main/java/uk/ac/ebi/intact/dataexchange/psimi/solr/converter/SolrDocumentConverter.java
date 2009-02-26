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

import org.apache.commons.collections.map.LRUMap;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.Column;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import psidev.psi.mi.tab.model.builder.Field;
import psidev.psi.mi.tab.model.builder.Row;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.ByInteractorTypeRowDataAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.GeneNameSelectiveAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.IdSelectiveAdder;
import uk.ac.ebi.intact.dataexchange.psimi.solr.converter.impl.TypeFieldFilter;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.LazyLoadedOntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;

import java.util.*;

/**
 * Converts from Row to SolrDocument and viceversa.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class SolrDocumentConverter {

    private DocumentDefinition documentDefintion;

    private Map<String,Collection<Field>> cvCache;

    private Set<String> expandableOntologies;

    /**
     * Access to the Ontology index.
     */
    private OntologySearcher ontologySearcher;

    public SolrDocumentConverter() {
        this(new IntactDocumentDefinition());
    }

    public SolrDocumentConverter(DocumentDefinition documentDefintion) {
        expandableOntologies = new HashSet<String>( );
        createDefaultExpandableOntologies();

        this.documentDefintion = documentDefintion;
        cvCache = new LRUMap(10000);
    }

    public SolrDocumentConverter(DocumentDefinition documentDefintion,
                                 OntologySearcher ontologySearcher) {
        this(documentDefintion);
        this.ontologySearcher = ontologySearcher;
    }

    private void createDefaultExpandableOntologies() {
        expandableOntologies.add( FieldNames.DB_GO );
        expandableOntologies.add( FieldNames.DB_INTERPRO );
        expandableOntologies.add( FieldNames.DB_CHEBI );
        expandableOntologies.add( FieldNames.DB_PSIMI );
    }

    public Set<String> getExpandableOntologies() {
        return expandableOntologies;
    }

    public void setExpandableOntologies( Set<String> expandableOntologies ) {
        this.expandableOntologies = expandableOntologies;
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
        doc.addField(FieldNames.LINE, mitabLine);

        addColumnToDoc(doc, row, FieldNames.ID_A, IntactDocumentDefinition.ID_INTERACTOR_A, true);
        addColumnToDoc(doc, row, FieldNames.ID_B, IntactDocumentDefinition.ID_INTERACTOR_B, true);
        addColumnToDoc(doc, row, FieldNames.ALTID_A, IntactDocumentDefinition.ALTID_INTERACTOR_A);
        addColumnToDoc(doc, row, FieldNames.ALTID_B, IntactDocumentDefinition.ALTID_INTERACTOR_B);
        addColumnToDoc(doc, row, FieldNames.ALIAS_A, IntactDocumentDefinition.ALIAS_INTERACTOR_A);
        addColumnToDoc(doc, row, FieldNames.ALIAS_B, IntactDocumentDefinition.ALIAS_INTERACTOR_B);
        addColumnToDoc(doc, row, FieldNames.DETMETHOD, IntactDocumentDefinition.INT_DET_METHOD, true);
        addColumnToDoc(doc, row, FieldNames.PUBAUTH, IntactDocumentDefinition.PUB_AUTH);
        addColumnToDoc(doc, row, FieldNames.PUBID, IntactDocumentDefinition.PUB_ID);
        addColumnToDoc(doc, row, FieldNames.TAXID_A, IntactDocumentDefinition.TAXID_A);
        addColumnToDoc(doc, row, FieldNames.TAXID_B, IntactDocumentDefinition.TAXID_B);
        addColumnToDoc(doc, row, FieldNames.TYPE, IntactDocumentDefinition.INT_TYPE, true);
        addColumnToDoc(doc, row, FieldNames.SOURCE, IntactDocumentDefinition.SOURCE);
        addColumnToDoc(doc, row, FieldNames.INTERACTION_ID, IntactDocumentDefinition.INTERACTION_ID);
        addColumnToDoc(doc, row, FieldNames.CONFIDENCE, IntactDocumentDefinition.CONFIDENCE);

        // extended
        if (documentDefintion instanceof IntactDocumentDefinition) {
            addColumnToDoc(doc, row, FieldNames.EXPERIMENTAL_ROLE_A, IntactDocumentDefinition.EXPERIMENTAL_ROLE_A);
            addColumnToDoc(doc, row, FieldNames.EXPERIMENTAL_ROLE_B, IntactDocumentDefinition.EXPERIMENTAL_ROLE_B);
            addColumnToDoc(doc, row, FieldNames.BIOLOGICAL_ROLE_A, IntactDocumentDefinition.BIOLOGICAL_ROLE_A);
            addColumnToDoc(doc, row, FieldNames.BIOLOGICAL_ROLE_B, IntactDocumentDefinition.BIOLOGICAL_ROLE_B);
            addColumnToDoc(doc, row, FieldNames.PROPERTIES_A, IntactDocumentDefinition.PROPERTIES_A, true);
            addColumnToDoc(doc, row, FieldNames.PROPERTIES_B, IntactDocumentDefinition.PROPERTIES_B, true);
            addColumnToDoc(doc, row, FieldNames.TYPE_A, IntactDocumentDefinition.INTERACTOR_TYPE_A);
            addColumnToDoc(doc, row, FieldNames.TYPE_B, IntactDocumentDefinition.INTERACTOR_TYPE_B);
            addColumnToDoc(doc, row, FieldNames.HOST_ORGANISM, IntactDocumentDefinition.HOST_ORGANISM);
            addColumnToDoc(doc, row, FieldNames.EXPANSION, IntactDocumentDefinition.EXPANSION_METHOD);
            addColumnToDoc(doc, row, FieldNames.DATASET, IntactDocumentDefinition.DATASET);
            addColumnToDoc(doc, row, FieldNames.ANNOTATION_A, IntactDocumentDefinition.ANNOTATIONS_A);
            addColumnToDoc(doc, row, FieldNames.ANNOTATION_B, IntactDocumentDefinition.ANNOTATIONS_B);
            addColumnToDoc(doc, row, FieldNames.PARAMETER_A, IntactDocumentDefinition.PARAMETERS_A);
            addColumnToDoc(doc, row, FieldNames.PARAMETER_B, IntactDocumentDefinition.PARAMETERS_B);
            addColumnToDoc(doc, row, FieldNames.PARAMETER_INTERACTION, IntactDocumentDefinition.PARAMETERS_INTERACTION);

            addCustomFields(row, doc, new ByInteractorTypeRowDataAdder(IntactDocumentDefinition.ID_INTERACTOR_A,
                                                                      IntactDocumentDefinition.INTERACTOR_TYPE_A));
            addCustomFields(row, doc, new ByInteractorTypeRowDataAdder(IntactDocumentDefinition.ID_INTERACTOR_B,
                                                                      IntactDocumentDefinition.INTERACTOR_TYPE_B));
        }

        // ac
        //doc.addField(FieldNames.PKEY, "NEW"); // pkey is generated automatically and using UUID

        // add the iRefIndex field from the interaction_id column to the rig field (there should be zero or one)
        addFilteredField(row, doc, FieldNames.RIGID, IntactDocumentDefinition.INTERACTION_ID, new TypeFieldFilter("irefindex"));

        // ids
        addCustomFields(row, doc, new IdSelectiveAdder());

        // gene names
        addCustomFields(row, doc, new GeneNameSelectiveAdder());

        return doc;
    }

    public BinaryInteraction toBinaryInteraction(SolrDocument doc) {
        return documentDefintion.interactionFromString(toMitabLine(doc));
    }

    public BinaryInteraction toBinaryInteraction(SolrInputDocument doc) {
        return documentDefintion.interactionFromString(toMitabLine(doc));
    }

    public Row toRow(SolrDocument doc) {
        return documentDefintion.createRowBuilder().createRow(toMitabLine(doc));
    }

    public String toMitabLine(SolrDocument doc) {
        return (String) doc.getFieldValue(FieldNames.LINE);
    }

    public String toMitabLine(SolrInputDocument doc) {
        return (String) doc.getFieldValue(FieldNames.LINE);
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex) {
        addColumnToDoc(doc, row, fieldName, columnIndex, false);
    }

    private void addColumnToDoc(SolrInputDocument doc, Row row, String fieldName, int columnIndex, boolean expandableColumn) {
        // do not process columns not found in the row
        if (row.getColumnCount() <= columnIndex) {
            return;
        }

        Column column = row.getColumnByIndex( columnIndex );

        for (Field field : column.getFields()) {
            if (expandableColumn) {
                doc.addField(fieldName+"_exact", field.toString());
            }

            doc.addField(fieldName, field.toString());
            doc.addField(fieldName+"_ms", field.toString());

            if (field.getType() != null) {
                doc.addField(field.getType()+"_xref", field.getValue());
                doc.addField(field.getType()+"_xref_ms", field.toString());
            }

            addDescriptionField(doc, field.getType(), field);
            addDescriptionField(doc, fieldName, field);

            if (isExpandableOntology(field.getType())) {
                doc.addField(field.getType(), field.getValue());

                if (field.getDescription() != null) {
                    doc.addField("spell", field.getDescription());
                }

                boolean includeItself = true;

                for (Field parentField : getAllParents(field, includeItself)) {
                    addExpandedFields(doc, fieldName, parentField);
                }
            }
        }
    }

    private void addFilteredField(Row row, SolrInputDocument doc, String fieldName, int columnIndex, FieldFilter filter) {
        Collection<Field> fields = getFieldsFromColumn(row, columnIndex, filter);

        for (Field field : fields) {
            doc.addField(fieldName, field.getValue());
        }
    }

    private void addCustomFields(Row row, SolrInputDocument doc, RowDataSelectiveAdder selectiveAdder) {
        selectiveAdder.addToDoc(doc, row);
    }

    private Collection<Field> getFieldsFromColumn(Row row, int columnIndex, FieldFilter filter) {
        List<Field> fields = new ArrayList<Field>();

        // do not process columns not found in the row
        if (row.getColumnCount() <= columnIndex) {
            return null;
        }

        Column column = row.getColumnByIndex( columnIndex );

        for (Field field : column.getFields()) {
            if (field != null && filter.acceptField(field)) {
                fields.add(field);
            }
        }

        return fields;
    }

    private void addExpandedFields(SolrInputDocument doc, String fieldName, Field field) {
        addExpandedField(doc, field, fieldName);
        addExpandedField(doc, field, field.getType());
    }

    private void addExpandedField(SolrInputDocument doc, Field field, String fieldPrefix) {
        doc.addField(fieldPrefix+"_expanded", field.toString());
        doc.addField(fieldPrefix+"_expanded_id", field.getValue());
        doc.addField(fieldPrefix+"_expanded_ms", field.toString());

        addDescriptionField(doc, fieldPrefix+"_expanded", field);
    }

    private void addDescriptionField(SolrInputDocument doc, String fieldPrefix, Field field) {
        if (field.getDescription() != null) {

            doc.addField(fieldPrefix+"_desc", field.getDescription());
            doc.addField(fieldPrefix+"_desc_s", field.getDescription());
        }
    }

    private boolean isExpandableOntology( String name ) {
        return ( expandableOntologies.contains( name ) );
    }

    /**
     * @param field         the field for which we want to get the parents
     * @param includeItself if true, the passed field will be part of the collection (its description updated from the index)
     * @return list of cv terms with parents and itself
     */
    private Collection<Field> getAllParents(psidev.psi.mi.tab.model.builder.Field field, boolean includeItself) {
        if (ontologySearcher == null) {
            return Collections.EMPTY_LIST;
        }

        List<psidev.psi.mi.tab.model.builder.Field> allParents = null;

        final String type = field.getType();

        String identifier = field.getValue();

        if (cvCache.containsKey(identifier)) {
            return cvCache.get(identifier);
        }

        // fetch parents and fill the field list
        final OntologyTerm ontologyTerm = new LazyLoadedOntologyTerm(ontologySearcher, identifier);
        final Set<OntologyTerm> parents = ontologyTerm.getAllParentsToRoot();

        allParents = convertTermsToFields(type, parents);

        if (includeItself) {
            Field updatedItself = convertTermToField(type, ontologyTerm);
            allParents.add(updatedItself);
        }

        cvCache.put(identifier, allParents);

        return (allParents != null ? allParents : Collections.EMPTY_LIST);
    }

    private List<psidev.psi.mi.tab.model.builder.Field> convertTermsToFields( String type, Set<OntologyTerm> terms ) {
        List<psidev.psi.mi.tab.model.builder.Field> fields =
                new ArrayList<psidev.psi.mi.tab.model.builder.Field>( terms.size());

        for ( OntologyTerm term : terms ) {
            Field field = convertTermToField(type, term);
            fields.add( field );
        }

        return fields;
    }

    private Field convertTermToField(String type, OntologyTerm term) {
        Field field =
                new Field( type, term.getId(), term.getName() );
        return field;
    }
}
