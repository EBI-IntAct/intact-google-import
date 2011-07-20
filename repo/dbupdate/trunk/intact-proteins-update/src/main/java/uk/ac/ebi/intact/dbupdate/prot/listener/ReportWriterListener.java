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
package uk.ac.ebi.intact.dbupdate.prot.listener;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import uk.ac.ebi.intact.commons.util.Crc64;
import uk.ac.ebi.intact.dbupdate.prot.ProcessorException;
import uk.ac.ebi.intact.dbupdate.prot.ProteinTranscript;
import uk.ac.ebi.intact.dbupdate.prot.RangeUpdateReport;
import uk.ac.ebi.intact.dbupdate.prot.event.*;
import uk.ac.ebi.intact.dbupdate.prot.rangefix.InvalidRange;
import uk.ac.ebi.intact.dbupdate.prot.rangefix.UpdatedRange;
import uk.ac.ebi.intact.dbupdate.prot.report.ReportWriter;
import uk.ac.ebi.intact.dbupdate.prot.report.UpdateReportHandler;
import uk.ac.ebi.intact.dbupdate.prot.util.AdditionalInfoMap;
import uk.ac.ebi.intact.dbupdate.prot.util.ProteinTools;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.protein.mapping.model.actionReport.BlastReport;
import uk.ac.ebi.intact.protein.mapping.model.actionReport.MappingReport;
import uk.ac.ebi.intact.protein.mapping.model.actionReport.PICRReport;
import uk.ac.ebi.intact.protein.mapping.results.BlastResults;
import uk.ac.ebi.intact.protein.mapping.results.PICRCrossReferences;
import uk.ac.ebi.intact.util.protein.utils.AliasUpdateReport;
import uk.ac.ebi.intact.util.protein.utils.AnnotationUpdateReport;
import uk.ac.ebi.intact.util.protein.utils.ProteinNameUpdateReport;
import uk.ac.ebi.intact.util.protein.utils.XrefUpdaterReport;

import java.io.IOException;
import java.util.*;

/**
 * Listener for logging the actions during the update
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ReportWriterListener extends AbstractProteinUpdateProcessorListener {

    private static final String EMPTY_VALUE = "-";
    private static final String NEW_LINE = System.getProperty("line.separator");

    private UpdateReportHandler reportHandler;

    public ReportWriterListener(UpdateReportHandler reportHandler) {
        this.reportHandler = reportHandler;
    }

    @Override
    public void onProteinDuplicationFound(DuplicatesFoundEvent evt) throws ProcessorException {
        try {
            ReportWriter duplicatedWriter = reportHandler.getDuplicatedWriter();
            duplicatedWriter.writeHeaderIfNecessary("Kept", "Uniprot Primary ac", "Total Active instances", "Duplicates", "Updated isoforms/feature chains", "moved interactions", "Added xrefs", "Added annotations");
            String protAc = evt.getReferenceProtein() != null ? evt.getReferenceProtein().getAc() : "All. Impossible to merge";
            int activeInstanceNumber = evt.getReferenceProtein() != null ? evt.getReferenceProtein().getActiveInstances().size() : 0;

            duplicatedWriter.writeColumnValues(protAc,
                    evt.getPrimaryUniprotAc(),
                    String.valueOf(activeInstanceNumber),
                    protCollectionToString(evt.getProteins(), false, evt.getOriginalActiveInstancesCount()),
                    mapCollectionStringToString(evt.getUpdatedTranscripts()),
                    mapCollectionStringToString(evt.getMovedInteractions()),
                    mapCollectionXrefsToString(evt.getAddedXRefs()),
                    mapCollectionAnnotationsToString(evt.getAddedAnnotations())
            );

            duplicatedWriter.flush();

            if (!evt.getUpdatedRanges().isEmpty()){
                for (RangeUpdateReport rangeReport : evt.getUpdatedRanges().values()){
                    logFeatureChanged(rangeReport);

                    for (Collection<UpdatedRange> updatedRanges : rangeReport.getShiftedRanges().values()){
                        for (UpdatedRange updatedRange : updatedRanges){
                            logRangeChanged(updatedRange);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new ProcessorException("Problem writing protein to stream", e);
        }
    }

    private String mapCollectionStringToString(Map<String, Collection<String>> mapInfo){
        StringBuffer buffer = new StringBuffer();

        int i = 1;

        for (Map.Entry<String, Collection<String>> entry : mapInfo.entrySet()){
            buffer.append(entry.getKey());
            buffer.append("[");
            buffer.append(entry.getValue().size());
            buffer.append("]");

            if (i < mapInfo.size()){
                buffer.append(", ");
            }
            i++;
        }

        return buffer.toString();
    }

    private String mapCollectionXrefsToString(Map<String, Collection<InteractorXref>> mapInfo){
        StringBuffer buffer = new StringBuffer();

        int i = 1;

        for (Map.Entry<String, Collection<InteractorXref>> entry : mapInfo.entrySet()){
            buffer.append(entry.getKey());
            buffer.append("[");
            buffer.append(entry.getValue().size());
            buffer.append("]");

            if (i < mapInfo.size()){
                buffer.append(", ");
            }
            i++;
        }

        return buffer.toString();
    }

    private String mapCollectionAnnotationsToString(Map<String, Collection<Annotation>> mapInfo){
        StringBuffer buffer = new StringBuffer();

        int i = 1;

        for (Map.Entry<String, Collection<Annotation>> entry : mapInfo.entrySet()){
            buffer.append(entry.getKey());
            buffer.append("[");
            buffer.append(entry.getValue().size());
            buffer.append("]");

            if (i < mapInfo.size()){
                buffer.append(", ");
            }
            i++;
        }

        return buffer.toString();
    }

    @Override
    public void onDelete(ProteinEvent evt) throws ProcessorException {
        try {
            ReportWriter deleteReportWriter = reportHandler.getDeletedWriter();

            writeDefaultLine(deleteReportWriter, evt.getProtein(), evt.getMessage());

            deleteReportWriter.flush();
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    @Override
    public void onProteinTranscriptWithSameSequence(ProteinTranscriptWithSameSequenceEvent evt) throws ProcessorException {
        try {
            ReportWriter transcriptWithSameSequenceWriter = reportHandler.getTranscriptWithSameSequenceWriter();
            String uniprotAc = evt.getUniprotProtein() != null ? evt.getUniprotProtein().getPrimaryAc() : "-";
            transcriptWithSameSequenceWriter.writeHeaderIfNecessary("protein ac", "protein shortlabel", "Uniprot ac", "Transcript ac", "sequence");
            transcriptWithSameSequenceWriter.writeColumnValues(evt.getProtein().getAc(),
                    evt.getProtein().getShortLabel(),
                    uniprotAc,
                    evt.getUniprotTranscriptAc(),
                    evt.getProtein().getSequence());
            transcriptWithSameSequenceWriter.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing protein to stream", e);
        }
    }

    @Override
    public void onProteinCreated(ProteinEvent evt) throws ProcessorException {
        try {
            ReportWriter createdWriter = reportHandler.getCreatedWriter();

            writeDefaultLine(reportHandler.getCreatedWriter(), evt.getProtein(), evt.getMessage());

            createdWriter.flush();
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    @Override
    public void onProteinSequenceChanged(ProteinSequenceChangeEvent evt) throws ProcessorException {
        final Protein protein = evt.getProtein();
        try {
            final ReportWriter writer = reportHandler.getSequenceChangedWriter();
            if (evt.getOldSequence() != null) {
                writer.writeLine(">"+ protein.getAc()+"|OLD|"+
                        protein.getShortLabel()+"|"+
                        getPrimaryIdString(protein)
                        +"|CRC:"+ Crc64.getCrc64(evt.getOldSequence())+
                        "|Length:"+evt.getOldSequence().length());
                writer.writeLine(insertNewLinesIfNecessary(evt.getOldSequence(), 80));
            }

            String state;
            int seqDiff;
            int levenshtein;
            double conservation;

            if (evt.getOldSequence() != null) {
                state = "UPDATE";
                seqDiff = evt.getNewSequence().length()-evt.getOldSequence().length();
                levenshtein = StringUtils.getLevenshteinDistance(protein.getSequence(), evt.getOldSequence());
                conservation = ProteinTools.calculateSequenceConservation(evt.getOldSequence(), evt.getNewSequence());
            } else {
                state = "NEW";
                seqDiff = evt.getNewSequence().length();
                levenshtein = seqDiff;
                conservation = 0;
            }
            int sequenceLength = evt.getNewSequence().length();
            writer.writeLine(">"+ protein.getAc()+"|"+state+"|"+
                    protein.getShortLabel()+"|"+
                    getPrimaryIdString(protein)+
                    "|CRC:"+evt.getUniprotCrc64()+
                    "|Length:"+Integer.toString(sequenceLength)+
                    "|Diff:"+seqDiff+
                    "|Levenshtein:"+ levenshtein+
                    "|Conservation:"+conservation);
            writer.writeLine(insertNewLinesIfNecessary(evt.getNewSequence(), 80));
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing to sequence changed writer", e);
        }
    }

    @Override
    public void onDeadProteinFound(DeadUniprotEvent evt) throws ProcessorException {
        final Protein protein = evt.getProtein();
        try {
            final ReportWriter writer = reportHandler.getDeadProteinWriter();

            writer.writeHeaderIfNecessary("Protein accession",
                    "Protein shortlabel",
                    "Protein taxId",
                    "Updated primary annotation",
                    "Removed xref",
                    "Added annotations");

            String primaryRef = evt.getUniprotIdentityXref() != null ? evt.getUniprotIdentityXref().getPrimaryId() : "-";
            StringBuilder xRefs = new StringBuilder();
            StringBuilder annotations = new StringBuilder();

            if (evt.getDeletedXrefs() != null && !evt.getDeletedXrefs().isEmpty()){
                for (Xref ref : evt.getDeletedXrefs()) {

                    String qual = (ref.getCvXrefQualifier() != null)? "("+ref.getCvXrefQualifier().getShortLabel()+")" : "";

                    xRefs.append(ref.getCvDatabase().getShortLabel()+":"+ref.getPrimaryId()+qual);
                }
            }
            else {
                xRefs.append("-");
            }

            if (evt.getAddedAnnotations() != null && !evt.getAddedAnnotations().isEmpty()){
                for (Annotation annotation : evt.getAddedAnnotations()) {

                    String qual = (annotation.getCvTopic()!= null)? "("+ annotation.getCvTopic().getShortLabel()+")" : "";

                    annotations.append(qual+":"+ (annotation.getAnnotationText() != null ? annotation.getAnnotationText() : "-"));
                }
            }
            else {
                annotations.append("-");
            }

            String taxId = protein.getBioSource() != null ? protein.getBioSource().getTaxId() : "-";
            writer.writeColumnValues(protein.getAc(),
                    protein.getShortLabel(),
                    taxId,
                    primaryRef,
                    xRefs.toString(),
                    annotations.toString());
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing to dead protein writer", e);
        }
    }

    @Override
    public void onNonUniprotProteinFound(ProteinEvent evt) throws ProcessorException {
        try {
            ReportWriter noUniprotWriter = reportHandler.getNonUniprotProteinWriter();

            writeDefaultLine(noUniprotWriter, evt.getProtein());

            noUniprotWriter.flush();
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    @Override
    public void onProcessErrorFound(UpdateErrorEvent evt) throws ProcessorException{
        try {
            ReportWriter writer = reportHandler.getPreProcessErrorWriter();

            String proteinAc = "-";
            String proteinShortlablel = "-";
            String uniprotAc = evt.getUniprotAc() != null ? evt.getUniprotAc() : "-";

            if (evt.getProtein() != null){
                proteinAc = evt.getProtein().getAc();
                proteinShortlablel = evt.getProtein().getShortLabel();
            }

            writer.writeHeaderIfNecessary("Protein ac",
                    "Protein shortlabel",
                    "Uniprot ac",
                    "error type",
                    "error description");
            writer.writeColumnValues(proteinAc,
                    proteinShortlablel,
                    uniprotAc,
                    evt.getError().toString(),
                    evt.getMessage());
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }

    @Override
    public void onSecondaryAcsFound(UpdateCaseEvent evt) throws ProcessorException {
        try {
            ReportWriter writer = reportHandler.getSecondaryProteinsWriter();
            writer.writeHeaderIfNecessary("UniProt ID",
                    "IA secondary c.",
                    "IA secondary",
                    "IA isoform secondary c.",
                    "IA isoform secondary",
                    "Xrefs added",
                    "Xrefs removed");
            String primaryId = evt.getProtein().getPrimaryAc();
            writer.writeColumnValues(primaryId,
                    String.valueOf(evt.getSecondaryProteins().size()),
                    protCollectionToString(evt.getSecondaryProteins(), true),
                    String.valueOf(evt.getSecondaryIsoforms().size()),
                    protTranscriptCollectionToString(evt.getSecondaryIsoforms(), true, null),
                    xrefReportsAddedToString(evt.getXrefUpdaterReports()),
                    xrefReportsRemovedToString(evt.getXrefUpdaterReports()));
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing secondary acs found to stream", e);
        }
    }

    @Override
    public void onUpdateCase(UpdateCaseEvent evt) throws ProcessorException {
        try {
            ReportWriter writer = reportHandler.getUpdateCasesWriter();
            writer.writeHeaderIfNecessary("UniProt ID",
                    "Total prots (updated or not for this uniprot protein)",
                    "IA primary c.",
                    "IA primary",
                    "IA isoform primary c.",
                    "IA isoform primary",
                    "IA feature chain primary c.",
                    "IA feature chain primary",
                    "Xrefs added",
                    "Xrefs removed",
                    "Aliases added",
                    "Aliases removed",
                    "Names updated",
                    "New annotations");
            String primaryId = evt.getProtein().getPrimaryAc();

            StringBuffer buffer = new StringBuffer();
            for (Map.Entry<String, Collection<Annotation>> entry : evt.getNewAnnotations().entrySet()) {

                buffer.append(entry.getKey()).append(" [");

                for (Annotation annotation : entry.getValue()){
                    String qual = (annotation.getCvTopic()!= null)? "("+ annotation.getCvTopic().getShortLabel()+")" : "";

                    buffer.append(qual+":"+ (annotation.getAnnotationText() != null ? annotation.getAnnotationText() : "-"));
                    buffer.append(" ");
                }
                buffer.append(entry.getKey()).append("] ");
            }

            writer.writeColumnValues(primaryId,
                    collectionToString(evt.getProteins()),
                    String.valueOf(evt.getPrimaryProteins().size()),
                    protCollectionToString(evt.getPrimaryProteins(), true),
                    String.valueOf(evt.getPrimaryIsoforms().size()),
                    protTranscriptCollectionToString(evt.getPrimaryIsoforms(), true, null),
                    String.valueOf(evt.getPrimaryFeatureChains().size()),
                    protTranscriptCollectionToString(evt.getPrimaryFeatureChains(), true, null),
                    xrefReportsAddedToString(evt.getXrefUpdaterReports()),
                    xrefReportsRemovedToString(evt.getXrefUpdaterReports()),
                    addedAliasReportsToString(evt.getAliasUpdaterReports()),
                    removedAliasReportsToString(evt.getAliasUpdaterReports()),
                    nameReportsToString(evt.getNameUpdaterReports()),
                    buffer.toString()
            );
            writer.flush();

            if (!evt.getUpdatedRanges().isEmpty()){
                for (RangeUpdateReport rangeReport : evt.getUpdatedRanges().values()){
                    logFeatureChanged(rangeReport);

                    for (Collection<UpdatedRange> updatedRanges : rangeReport.getShiftedRanges().values()){
                        for (UpdatedRange updatedRange : updatedRanges){
                            logRangeChanged(updatedRange);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ProcessorException("Problem writing update case to stream", e);
        }
    }

    public void onOutOfDateParticipantFound(OutOfDateParticipantFoundEvent evt) throws ProcessorException {
        try {
            ReportWriter writer = reportHandler.getOutOfDateParticipantWriter();
            writer.writeHeaderIfNecessary("UniProt ID",
                    "IA primary c.",
                    "parent ac to remap",
                    "remapped intact ac",
                    "Components",
                    "sequence");
            String uniprotId = evt.getProtein() != null ? evt.getProtein().getPrimaryAc() : "-";
            writer.writeColumnValues(uniprotId,
                    evt.getProteinWithConflicts().getAc(),
                    dashIfNull(evt.getValidParentAc()),
                    dashIfNull(evt.getRemappedProteinAc()),
                    compCollectionToString(evt.getComponentsToFix()),
                    evt.getProteinWithConflicts().getSequence());
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing update case to stream", e);
        }
    }

    @Override
    public void onRangeChanged(RangeChangedEvent evt) throws ProcessorException {
        UpdatedRange updatedRange = evt.getUpdatedRange();

        String uniprotAc = EMPTY_VALUE;
        String rangeAc = updatedRange.getRangeAc();
        String featureAc = updatedRange.getFeatureAc();
        String componentAc = updatedRange.getComponentAc();
        String proteinAc = updatedRange.getProteinAc();
        String interactionAc = updatedRange.getInteractionAc();
        String featureLabel = EMPTY_VALUE;
        String proteinLabel = EMPTY_VALUE;

        String oldRange = EMPTY_VALUE;
        String newRange = EMPTY_VALUE;

        if (updatedRange.getNewRange() != null){
            Feature feature = updatedRange.getNewRange().getFeature();
            Component component = feature.getComponent();
            Interactor interactor = component.getInteractor();

            final InteractorXref xref = ProteinUtils.getUniprotXref(interactor);
            uniprotAc = (xref != null)? xref.getPrimaryId() : EMPTY_VALUE;
            newRange = updatedRange.getNewRange().toString();
            featureLabel = feature.getShortLabel();
            proteinLabel = interactor.getShortLabel();
        }

        if (updatedRange.getOldRange() != null){
            oldRange = updatedRange.getOldRange().toString();
        }

        try {
            ReportWriter writer = reportHandler.getRangeChangedWriter();
            writer.writeHeaderIfNecessary("Range AC",
                    "Old Pos.",
                    "New Pos.",
                    "Length Changed",
                    "Seq. Changed",
                    "Feature AC",
                    "Feature Label",
                    "Comp. AC",
                    "Prot. AC",
                    "Prot. Label",
                    "Prot. Uniprot",
                    "Interaction ac");
            writer.writeColumnValues(dashIfNull(rangeAc),
                    dashIfNull(oldRange),
                    dashIfNull(newRange),
                    booleanToYesNo(updatedRange.isRangeLengthChanged()),
                    booleanToYesNo(updatedRange.isSequenceChanged()),
                    dashIfNull(featureAc),
                    dashIfNull(featureLabel),
                    dashIfNull(componentAc),
                    dashIfNull(proteinAc),
                    dashIfNull(proteinLabel),
                    dashIfNull(uniprotAc),
                    dashIfNull(interactionAc));
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing update case to stream", e);
        }
    }

    private void logRangeChanged(UpdatedRange updatedRange) throws ProcessorException {

        String uniprotAc = EMPTY_VALUE;
        String rangeAc = updatedRange.getRangeAc();
        String featureAc = updatedRange.getFeatureAc();
        String componentAc = updatedRange.getComponentAc();
        String proteinAc = updatedRange.getProteinAc();
        String interactionAc = updatedRange.getInteractionAc();
        String featureLabel = EMPTY_VALUE;
        String proteinLabel = EMPTY_VALUE;

        String oldRange = EMPTY_VALUE;
        String newRange = EMPTY_VALUE;

        if (updatedRange.getNewRange() != null){
            Feature feature = updatedRange.getNewRange().getFeature();
            Component component = feature.getComponent();
            Interactor interactor = component.getInteractor();

            final InteractorXref xref = ProteinUtils.getUniprotXref(interactor);
            uniprotAc = (xref != null)? xref.getPrimaryId() : EMPTY_VALUE;
            newRange = updatedRange.getNewRange().toString();
            featureLabel = feature.getShortLabel();
            proteinLabel = interactor.getShortLabel();
        }

        if (updatedRange.getOldRange() != null){
            oldRange = updatedRange.getOldRange().toString();
        }

        try {
            ReportWriter writer = reportHandler.getRangeChangedWriter();
            writer.writeHeaderIfNecessary("Range AC",
                    "Old Pos.",
                    "New Pos.",
                    "Length Changed",
                    "Seq. Changed",
                    "Feature AC",
                    "Feature Label",
                    "Comp. AC",
                    "Prot. AC",
                    "Prot. Label",
                    "Prot. Uniprot",
                    "Interaction ac");
            writer.writeColumnValues(dashIfNull(rangeAc),
                    dashIfNull(oldRange),
                    dashIfNull(newRange),
                    booleanToYesNo(updatedRange.isRangeLengthChanged()),
                    booleanToYesNo(updatedRange.isSequenceChanged()),
                    dashIfNull(featureAc),
                    dashIfNull(featureLabel),
                    dashIfNull(componentAc),
                    dashIfNull(proteinAc),
                    dashIfNull(proteinLabel),
                    dashIfNull(uniprotAc),
                    dashIfNull(interactionAc));
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing update case to stream", e);
        }
    }

    private void logFeatureChanged(RangeUpdateReport rangeReport) throws ProcessorException {

        Map<String, AnnotationUpdateReport> featureReport = rangeReport.getUpdatedFeatureAnnotations();

        for (Map.Entry<String, AnnotationUpdateReport> entry : featureReport.entrySet()){
            StringBuffer added = new StringBuffer();
            StringBuffer removed = new StringBuffer();
            StringBuffer updated = new StringBuffer();

            AnnotationUpdateReport a = entry.getValue();

            if (a != null){
                if (a.getAddedAnnotations().isEmpty()){
                    added.append(EMPTY_VALUE);
                }
                else{
                    added.append(AnnotationUpdateReport.annotationsToString(a.getAddedAnnotations()));
                }
                if (a.getAddedAnnotations().isEmpty()){
                    added.append(EMPTY_VALUE);
                }
                else{
                    removed.append(AnnotationUpdateReport.annotationsToString(a.getRemovedAnnotations()));
                }
                if (a.getAddedAnnotations().isEmpty()){
                    added.append(EMPTY_VALUE);
                }
                else{
                    updated.append(AnnotationUpdateReport.annotationsToString(a.getUpdatedAnnotations()));
                }
            }

            try {
                ReportWriter writer = reportHandler.getFeatureChangedWriter();
                writer.writeHeaderIfNecessary("Feature AC",
                        "Added annotations",
                        "Removed annotations",
                        "Updated annotations");

                writer.writeColumnValues(dashIfNull(entry.getKey()),
                        added.toString(),
                        removed.toString(),
                        updated.toString());
                writer.flush();
            } catch (IOException e) {
                throw new ProcessorException("Problem writing update case to stream", e);
            }
        }
    }

    @Override
    public void onInvalidRange(InvalidRangeEvent evt) throws ProcessorException {
        InvalidRange updatedRange = evt.getInvalidRange();

        String uniprotAc = updatedRange.getUniprotAc();
        String rangeAc = updatedRange.getRangeAc();
        String featureAc = updatedRange.getFeatureAc();
        String componentAc = updatedRange.getComponentAc();
        String proteinAc = updatedRange.getProteinAc();
        String interactionAc = updatedRange.getInteractionAc();
        String featureLabel = EMPTY_VALUE;
        String proteinLabel = EMPTY_VALUE;

        String oldRange = EMPTY_VALUE;

        if (updatedRange.getOldRange() != null){
            Feature feature = updatedRange.getOldRange().getFeature();
            Component component = feature.getComponent();
            Interactor interactor = component.getInteractor();

            final InteractorXref xref = ProteinUtils.getUniprotXref(interactor);
            uniprotAc = (xref != null)? xref.getPrimaryId() : EMPTY_VALUE;
            oldRange = updatedRange.getOldRange().toString();
            featureLabel = feature.getShortLabel();
            proteinLabel = interactor.getShortLabel();
        }

        try {
            ReportWriter writer = reportHandler.getInvalidRangeWriter();
            writer.writeHeaderIfNecessary("Range AC",
                    "Pos.",
                    "Sequence length.",
                    "Feature AC",
                    "Feature Label",
                    "Comp. AC",
                    "Prot. AC",
                    "Prot. Label",
                    "Prot. Uniprot",
                    "Interaction ac",
                    "Message",
                    "Added annotations",
                    "Removed annotations");
            writer.writeColumnValues(dashIfNull(rangeAc),
                    dashIfNull(oldRange),
                    Integer.toString(updatedRange.getSequence().length()),
                    dashIfNull(featureAc),
                    dashIfNull(featureLabel),
                    dashIfNull(componentAc),
                    dashIfNull(proteinAc),
                    dashIfNull(proteinLabel),
                    dashIfNull(uniprotAc),
                    dashIfNull(interactionAc),
                    dashIfNull(updatedRange.getMessage()));
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing update case to stream", e);
        }
    }

    @Override
    public void onOutOfDateRange(InvalidRangeEvent evt) throws ProcessorException {
        InvalidRange updatedRange = evt.getInvalidRange();

        String uniprotAc = updatedRange.getUniprotAc();
        String rangeAc = updatedRange.getRangeAc();
        String featureAc = updatedRange.getFeatureAc();
        String componentAc = updatedRange.getComponentAc();
        String proteinAc = updatedRange.getProteinAc();
        String interactionAc = updatedRange.getInteractionAc();
        String featureLabel = EMPTY_VALUE;
        String proteinLabel = EMPTY_VALUE;

        String oldRange = EMPTY_VALUE;
        String newRange = EMPTY_VALUE;

        if (updatedRange.getOldRange() != null){
            Feature feature = updatedRange.getOldRange().getFeature();
            Component component = feature.getComponent();
            Interactor interactor = component.getInteractor();

            final InteractorXref xref = ProteinUtils.getUniprotXref(interactor);
            uniprotAc = (xref != null)? xref.getPrimaryId() : EMPTY_VALUE;
            oldRange = updatedRange.getOldRange().toString();
            featureLabel = feature.getShortLabel();
            proteinLabel = interactor.getShortLabel();
        }

        if (updatedRange.getNewRange() != null){
            newRange = updatedRange.getNewRange().toString();
        }

        String sequenceLength = updatedRange.getSequence() != null ? Integer.toString(updatedRange.getSequence().length()) : EMPTY_VALUE;

        try {
            ReportWriter writer = reportHandler.getOutOfDateRangeWriter();
            writer.writeHeaderIfNecessary("Range AC",
                    "Pos.",
                    "Computed Pos.",
                    "Sequence length.",
                    "Uniprot ac",
                    "Valid sequence version",
                    "Feature AC",
                    "Feature Label",
                    "Comp. AC",
                    "Prot. AC",
                    "Prot. Label",
                    "Interaction Ac",
                    "Message");
            writer.writeColumnValues(dashIfNull(rangeAc),
                    dashIfNull(oldRange),
                    dashIfNull(newRange),
                    dashIfNull(sequenceLength),
                    dashIfNull(uniprotAc),
                    dashIfNull(Integer.toString(updatedRange.getValidSequenceVersion())),
                    dashIfNull(featureAc),
                    dashIfNull(featureLabel),
                    dashIfNull(componentAc),
                    dashIfNull(proteinAc),
                    dashIfNull(proteinLabel),
                    dashIfNull(interactionAc),
                    dashIfNull(updatedRange.getMessage()));
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing update case to stream", e);
        }
    }

    public void onInvalidIntactParent(InvalidIntactParentFoundEvent evt) throws ProcessorException{
        ReportWriter writer = null;
        try {
            writer = reportHandler.getIntactParentWriter();
            writer.writeHeaderIfNecessary("Protein Ac",
                    "Old parent Ac",
                    "Number of parents",
                    "parents xrefs");
            int numberParent = evt.getParents() != null ? evt.getParents().size() : 0;
            StringBuilder xRefs = new StringBuilder();

            for (String ref : evt.getParents()) {

                String qual = (ref != null)? "("+ref+")" : "";

                xRefs.append(qual);
            }

            writer.writeColumnValues(evt.getProtein().getAc(),
                    dashIfNull(evt.getOldParentAc()),
                    Integer.toString(numberParent),
                    xRefs.toString());
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing invalid intact parents to stream", e);
        }
    }

    public void onProteinRemapping(ProteinRemappingEvent evt) throws ProcessorException{
        ReportWriter writer = null;
        try {
            writer = reportHandler.getProteinMappingWriter();
            writer.writeHeaderIfNecessary("Protein Ac",
                    "Sequence",
                    "Protein identifiers",
                    "Message",
                    "Uniprot id found",
                    "Actions");

            StringBuffer identifiers = new StringBuffer(1064);

            for (Map.Entry<String, String> entry : evt.getContext().getIdentifiers().entrySet()){
                identifiers.append(entry.getKey() + " : " + entry.getValue() + ",");
            }

            String uniprotId = "-";
            StringBuffer actions = new StringBuffer(1064);
            if (evt.getResult() != null){
                uniprotId = dashIfNull(evt.getResult().getFinalUniprotId());
                List<MappingReport> reports = evt.getResult().getListOfActions();

                for (MappingReport report : reports){
                    actions.append("[");
                    actions.append(report.getName().toString() + " : " + report.getStatus().getLabel() + " : " + report.getStatus().getDescription());

                    if (!report.getWarnings().isEmpty()){
                        actions.append(" (");
                        for (String warn : report.getWarnings()){
                            actions.append(warn + ";");
                        }
                        actions.append(")");
                    }

                    if (!report.getPossibleAccessions().isEmpty()){
                        actions.append(", possible accessions : ");
                        for (String ac : report.getPossibleAccessions()){
                            actions.append(ac + ";");
                        }
                    }

                    if (report instanceof PICRReport){
                        actions.append(", ");

                        PICRReport<PICRCrossReferences> picr = (PICRReport) report;
                        actions.append("Is a Swissprot entry : " + picr.isASwissprotEntry());

                        if (!picr.getCrossReferences().isEmpty()){
                            actions.append(", other cross references : ");
                            for (PICRCrossReferences xrefs : picr.getCrossReferences()){
                                actions.append(xrefs.getDatabase() + " : " + xrefs.getAccessions() + ";");
                            }
                        }
                    }
                    else if (report instanceof BlastReport){

                        BlastReport<BlastResults> blast = (BlastReport) report;

                        if (!blast.getBlastMatchingProteins().isEmpty()){
                            actions.append(", Blast Results : ");
                            for (BlastResults prot : blast.getBlastMatchingProteins()){
                                actions.append("BLAST Protein " + prot.getAccession() + " : identity = " + prot.getIdentity() + ";");
                                actions.append("Query start = " + prot.getStartQuery() + ": end = " + prot.getEndQuery() + ";");
                                actions.append("Match start = " + prot.getStartMatch() + ": end = " + prot.getEndMatch() + ",");
                            }
                        }
                    }
                    actions.append("], ");
                }
            }

            writer.writeColumnValues(evt.getContext().getIntactAccession(),
                    dashIfNull(evt.getContext().getSequence()),
                    dashIfNull(identifiers.toString()),
                    evt.getMessage(),
                    dashIfNull(uniprotId),
                    actions.toString());
            writer.flush();
        } catch (IOException e) {
            throw new ProcessorException("Problem writing results of protein mapping", e);
        }
    }

    @Override
    public void onProteinSequenceCaution(ProteinSequenceChangeEvent evt) throws ProcessorException {
        ReportWriter writer = null;
        try {
            writer = reportHandler.getSequenceChangedCautionWriter();
            writer.writeHeaderIfNecessary("Protein Ac",
                    "Relative conservation",
                    "Old sequence",
                    "New sequence");

            if (evt.getProtein() != null){
                writer.writeColumnValues(dashIfNull(evt.getProtein().getAc()),
                        Double.toString(evt.getRelativeConservation()),
                        dashIfNull(evt.getOldSequence()),
                        dashIfNull(evt.getNewSequence()));
                writer.flush();
            }

        } catch (IOException e) {
            throw new ProcessorException("Problem writing sequence changed cautions to stream", e);
        }
    }

    @Override
    public void onDeletedComponent(DeletedComponentEvent evt) throws ProcessorException {
        ReportWriter writer = null;
        try {
            writer = reportHandler.getDeletedComponentWriter();
            writer.writeHeaderIfNecessary("Protein Ac",
                    "Uniprot Ac",
                    "Component AC",
                    "Interaction Ac");

            if (evt.getProtein() != null && evt.getComponent() != null){
                String interactionAc = evt.getComponent().getInteraction() != null ? evt.getComponent().getInteraction().getAc() : "-";
                writer.writeColumnValues(dashIfNull(evt.getProtein().getAc()),
                        dashIfNull(evt.getUniprotIdentity()),
                        dashIfNull(evt.getComponent().getAc()),
                        dashIfNull(interactionAc));
                writer.flush();
            }

        } catch (IOException e) {
            throw new ProcessorException("Problem writing deleted components to stream", e);
        }
    }

    private static String insertNewLinesIfNecessary(String oldSequence, int maxLineLength) {
        StringBuilder sb = new StringBuilder(oldSequence);

        int startIndex = oldSequence.length() - (oldSequence.length() % maxLineLength);

        if (startIndex >= maxLineLength) {
            for (int i = startIndex; i>0; i=i-maxLineLength) {
                sb.insert(i, NEW_LINE);
            }
        }

        return sb.toString();
    }

    private static String compCollectionToString(Collection<Component> components) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<Component> iterator = components.iterator(); iterator.hasNext();) {
            Component component = iterator.next();

            sb.append(component.getShortLabel()).append("(").append(component.getAc()).append(")");
            sb.append("[interaction:").append(component.getInteraction().getAc()).append(" ,interactor:").append(component.getInteractor().getAc()).append("]");

            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        if (components.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String protTranscriptCollectionToString(Collection<ProteinTranscript> protCollection,
                                                           boolean showInteractionsCount,
                                                           AdditionalInfoMap<?> additionalInfo) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<ProteinTranscript> iterator = protCollection.iterator(); iterator.hasNext();) {
            Protein protein = iterator.next().getProtein();

            sb.append(protein.getShortLabel()).append("(").append(protein.getAc()).append(")");

            if (showInteractionsCount) {
                sb.append("[").append(protein.getActiveInstances().size()).append("]");
            }

            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                sb.append("[").append(additionalInfo.get(protein.getAc())).append("]");
            }

            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        if (protCollection.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String protCollectionToString(Collection<? extends Protein> protCollection,
                                                 boolean showActiveInstancesCount) {
        return protCollectionToString(protCollection, showActiveInstancesCount, Collections.EMPTY_MAP);
    }

    private static String protCollectionToString(Collection<? extends Protein> protCollection,
                                                 boolean showInteractionsCount,
                                                 AdditionalInfoMap<?> additionalInfo) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<? extends Protein> iterator = protCollection.iterator(); iterator.hasNext();) {
            Protein protein = iterator.next();

            sb.append(protein.getShortLabel()).append("(").append(protein.getAc()).append(")");

            if (showInteractionsCount) {
                sb.append("[").append(protein.getActiveInstances().size()).append("]");
            }

            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                sb.append("[").append(additionalInfo.get(protein.getAc())).append("]");
            }

            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    private static String protCollectionToString(Collection<? extends Protein> protCollection,
                                                 boolean showInteractionsCount,
                                                 Map<String,Collection<?>> additionalInfo) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<? extends Protein> iterator = protCollection.iterator(); iterator.hasNext();) {
            Protein protein = iterator.next();

            sb.append(protein.getShortLabel()).append("(").append(protein.getAc()).append(")");

            if (showInteractionsCount) {
                sb.append("[").append(protein.getActiveInstances().size()).append("]");
            }

            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                sb.append("[").append(additionalInfo.get(protein.getAc()).size()).append("]");
            }

            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        if (protCollection.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String collectionToString(Collection<String> protCollection) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<String> iterator = protCollection.iterator(); iterator.hasNext();) {
            String protein = iterator.next();

            sb.append(protein);

            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        if (protCollection.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String xrefReportsAddedToString(Collection<XrefUpdaterReport> xrefReports) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<XrefUpdaterReport> iterator = xrefReports.iterator(); iterator.hasNext();) {
            XrefUpdaterReport report = iterator.next();

            sb.append(report.getProtein()).append("[").append(report.addedXrefsToString()).append("]");

            if (iterator.hasNext()) {
                sb.append("|");
            }
        }

        if (xrefReports.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String nameReportsToString(Collection<ProteinNameUpdateReport> nameReports) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<ProteinNameUpdateReport> iterator = nameReports.iterator(); iterator.hasNext();) {
            ProteinNameUpdateReport report = iterator.next();

            String shortlabel = report.getShortLabel() != null ? report.getShortLabel() : "not updated";
            String fullName = report.getFullName() != null ? report.getFullName() : "not updated";

            sb.append(report.getProtein()).append("[").append("ShortLabel : ").append(shortlabel).append("FullName : ").append(fullName).append("]");

            if (iterator.hasNext()) {
                sb.append("|");
            }
        }

        if (nameReports.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String addedAliasReportsToString(Collection<AliasUpdateReport> aliasReports) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<AliasUpdateReport> iterator = aliasReports.iterator(); iterator.hasNext();) {
            AliasUpdateReport report = iterator.next();

            sb.append(report.getProtein()).append("[").append(AliasUpdateReport.aliasesToString(report.getAddedAliases())).append("]");

            if (iterator.hasNext()) {
                sb.append("|");
            }
        }

        if (aliasReports.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String removedAliasReportsToString(Collection<AliasUpdateReport> aliasReports) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<AliasUpdateReport> iterator = aliasReports.iterator(); iterator.hasNext();) {
            AliasUpdateReport report = iterator.next();

            sb.append(report.getProtein()).append("[").append(AliasUpdateReport.aliasesToString(report.getRemovedAliases())).append("]");

            if (iterator.hasNext()) {
                sb.append("|");
            }
        }

        if (aliasReports.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private static String xrefReportsRemovedToString(Collection<XrefUpdaterReport> xrefReports) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<XrefUpdaterReport> iterator = xrefReports.iterator(); iterator.hasNext();) {
            XrefUpdaterReport report = iterator.next();

            sb.append(report.getProtein()).append("[").append(report.removedXrefsToString()).append("]");

            if (iterator.hasNext()) {
                sb.append("|");
            }
        }

        if (xrefReports.isEmpty()) {
            sb.append(EMPTY_VALUE);
        }

        return sb.toString();
    }

    private void writeDefaultHeaderIfNecessary(ReportWriter writer) throws IOException {
        if (writer != null) {
            writer.writeHeaderIfNecessary("datetime", "ac", "shortLabel", "primary ID", "message");
            writer.flush();
        }
    }

    private void writeDefaultLine(ReportWriter writer, Protein protein) throws IOException {
        writeDefaultLine(writer, protein, null);
    }

    private void writeDefaultLine(ReportWriter writer, Protein protein, String message) throws IOException {
        writeDefaultHeaderIfNecessary(writer);
        if (writer != null) {
            String primaryId = getPrimaryIdString(protein);
            message = dashIfNull(message);

            writer.writeColumnValues(new DateTime().toString(), protein.getAc(), protein.getShortLabel(), primaryId, message);
            writer.flush();
        }
    }

    private String dashIfNull(String str) {str = (str != null)? str : EMPTY_VALUE;
        return str;
    }

    private String booleanToYesNo(boolean bool) {
        return bool? "Y" : "N";
    }

    private String getPrimaryIdString(Protein protein) {
        InteractorXref uniprotXref = ProteinUtils.getUniprotXref(protein);

        String primaryId = null;

        if (uniprotXref != null) {
            primaryId = uniprotXref.getPrimaryId();
        } else {
            StringBuilder sb = new StringBuilder(64);
            List<InteractorXref> xrefs = ProteinUtils.getIdentityXrefs(protein);

            Iterator<InteractorXref> iterator = xrefs.iterator();
            while (iterator.hasNext()) {
                InteractorXref xref =  iterator.next();
                sb.append(xref.getCvDatabase().getShortLabel()).append(":").append(xref.getPrimaryId());
                if (iterator.hasNext()) sb.append("|");
                primaryId = sb.toString();
            }

            if (xrefs.isEmpty()) {
                primaryId = EMPTY_VALUE;
            }
        }
        return primaryId;
    }

    public UpdateReportHandler getReportHandler() {
        return reportHandler;
    }
}
