package uk.ac.ebi.intact.util.uniprotExport.miscore;

import org.apache.commons.collections.CollectionUtils;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.CcLine;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineCreatedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.CcLineEventListener;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import javax.swing.event.EventListenerList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Write CC lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/01/11</pre>
 */

public class CCLineWriter {

    private IntActInteractionClusterScore clusterScore;
    private final static String PUBMED = "pubmed";
    private final static String UNIPROT = "uniprotkb";
    protected static final String NEW_LINE = System.getProperty("line.separator");
    protected EventListenerList listenerList = new EventListenerList();
    private final static String TAXID = "taxid";

    /**
     * Use to out the CC lines in a file.
     */
    private Writer ccWriter;

    public CCLineWriter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        this.clusterScore = clusterScore;
        ccWriter = new FileWriter(fileName);
    }

    public IntActInteractionClusterScore getClusterScore() {
        return clusterScore;
    }

    public void setClusterScore(IntActInteractionClusterScore clusterScore) {
        this.clusterScore = clusterScore;
    }

    private void writeCCLineTitle(StringBuffer sb){
        sb.append("CC   -!- INTERACTION:");
        sb.append(NEW_LINE);
    }

    private void writeCCLines(List<CcLine> ccLines, String uniprotAc) throws IOException {
        if (!ccLines.isEmpty()){
            Collections.sort(ccLines);

            StringBuffer sb = new StringBuffer(128 * ccLines.size());

            writeCCLineTitle(sb);

            for ( CcLine ccLine : ccLines ) {
                sb.append(ccLine.getCcLine());
            }

            sb.append("//");
            sb.append(NEW_LINE);

            String ccs = sb.toString();

            // write the content in the output file.
            ccWriter.write(ccs);
            ccWriter.flush();
            // fire the event
            fireCcLineCreatedEvent(new CcLineCreatedEvent(this, uniprotAc, ccLines));
        }
    }

    private List<CcLine> convertInteractionsIntoCCLines(Map.Entry<String, List<Integer>> interactor){
        String uniprotAc = interactor.getKey();
        List<Integer> interactions = interactor.getValue();

        List<CcLine> ccLines = new ArrayList<CcLine>();

        for (Integer interactionId : interactions){
            EncoreInteraction interaction = this.clusterScore.getInteractionMapping().get(interactionId);

            if (interaction != null){
                CcLine line = createCCLine(interaction, uniprotAc);

                if (line != null){
                    ccLines.add(line);
                }
            }
        }

        return ccLines;
    }

    public void write() throws IOException {

        for (Map.Entry<String, List<Integer>> interactor : this.clusterScore.getInteractorMapping().entrySet()){

            List<CcLine> ccLines = convertInteractionsIntoCCLines(interactor);

            writeCCLines(ccLines, interactor.getKey());
        }
    }

    /**
     * create the output of a CC line for a set of exportable interactions.
     *
     * @param interaction
     */
    private CcLine createCCLine( EncoreInteraction interaction, String firstInteractor) {

        String uniprot1 = interaction.getInteractorA(UNIPROT);
        String uniprot2 = interaction.getInteractorB(UNIPROT);

        if (uniprot1 != null && uniprot2 != null && firstInteractor != null){
            // produce the CC lines for the 1st protein
            CcLine cc1 = null;

            if (firstInteractor.equals(uniprot1)){
                formatCCLines(interaction, uniprot1, uniprot2);
            }
            else {
                formatCCLines(interaction, uniprot2, uniprot1);
            }

            return cc1;
        }

        return null;
    }

    private String [] extractOrganismFrom(Collection<CrossReference> references){

        String taxId = "-";
        String organismName = "-";

        for (CrossReference ref : references){
            if (TAXID.equalsIgnoreCase(ref.getDatabase())){
                taxId = ref.getIdentifier();
                if (ref.getText() != null){
                    organismName = ref.getText();
                }
            }
        }

        return new String [] {taxId, organismName};
    }

    private Map<String, Map<String, String>> collectPubmedAffectedBySpokeExpansion(EncoreInteraction interaction){
        Map<String, String> interactionToPubmed = interaction.getExperimentToPubmed();
        Map<String, Map<String, String>> pubmedHavingSpokeExpansion = new HashMap<String, Map<String, String>>();

        for (Map.Entry<String, String> ip : interactionToPubmed.entrySet()){
            String ac = ip.getKey();
            String pubmed = ip.getValue();

            if (this.clusterScore.getSpokeExpandedInteractions().containsKey(ac)){
                Map.Entry<String, String> methodType = this.clusterScore.getSpokeExpandedInteractions().get(ac);

                if (pubmedHavingSpokeExpansion.containsKey(pubmed)){
                    Map<String, String> map = pubmedHavingSpokeExpansion.get(pubmed);
                    map.put(methodType.getKey(), methodType.getValue());
                }
                else {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put(methodType.getKey(), methodType.getValue());
                    pubmedHavingSpokeExpansion.put(pubmed, map);
                }
            }
        }

        return pubmedHavingSpokeExpansion;
    }

    private Collection<String> collectSpokeExpandedInteractions(Map<String, Map<String, String>> pubmedsHavingSpokeExpansion, Collection<String> associatedPubmed, String detectionMi, String typeMi){
        Collection<String> currentSpokeExpandedInteractions = new ArrayList<String>();

        for (String pubmed : associatedPubmed){
            if (pubmedsHavingSpokeExpansion.containsKey(pubmed)){
                Map<String, String> methodToTypeMap = pubmedsHavingSpokeExpansion.get(pubmed);

                if (methodToTypeMap.containsKey(detectionMi)){
                    if (methodToTypeMap.get(detectionMi).equals(typeMi)){
                        currentSpokeExpandedInteractions.add(pubmed);
                    }
                }
            }
        }

        return currentSpokeExpandedInteractions;
    }

    /**
     * Format introduced on July 29th 2009.
     *
     * Generate the CC line content based on the Interaction and its two interactor.
     * <br> protein1 is the entry in which that CC content will appear.
     * <p/>
     * <pre>
     *          <font color=gray>ID   rr44_HUMAN     STANDARD;      PRT;   123 AA.</font>
     *          <font color=gray>AC   P01232</font>
     *          <font color=gray>GN   rr44.</font>
     *          CC   -!- INTERACTION:
     *          CC       Interact=Yes (PubMed:12344567); Xref=IntAct:EBI-375446,EBI-389883;
     *          CC         Protein1=rr44 [P01232];
     *          CC         Protein2=tsr [P10981];
     * </pre>
     * @param interaction : the interaction to convert
     *
     * @return a CCLine
     */
    private CcLine formatCCLines(EncoreInteraction interaction, String uniprot1, String uniprot2) {

        StringBuffer buffer = new StringBuffer(256); // average size is 160 char

        // extract gene names
        String geneName1 = this.clusterScore.getGeneNames().get(uniprot1);
        String geneName2 = this.clusterScore.getGeneNames().get(uniprot2);

        // extract organisms
        String [] organismsA;
        String [] organismsB;
        if (interaction.getInteractorA().equals(uniprot1)){
            organismsA = extractOrganismFrom(interaction.getOrganismsA());
            organismsB = extractOrganismFrom(interaction.getOrganismsB());
        }
        else {
            organismsA = extractOrganismFrom(interaction.getOrganismsB());
            organismsB = extractOrganismFrom(interaction.getOrganismsA());
        }
        String taxId1 = organismsA[0];
        String taxId2 = organismsB[0];

        String organism2 = organismsB[1];

        // collect all pubmeds and spoke expanded information
        Map<String, List<String>> typeToPubmed = interaction.getTypeToPubmed();
        Map<String, List<String>> methodToPubmed = interaction.getMethodToPubmed();

        Map<String, Map<String, String>> pubmedsHavingSpokeExpansion = collectPubmedAffectedBySpokeExpansion(interaction);

        // write introduction
        writeInteractionIntroduction(true, uniprot1, uniprot2, buffer);

        // write first protein
        writeFirstProtein(uniprot1, geneName1, buffer);

        // write second protein
        writeSecondProtein(uniprot2, geneName2, taxId1, taxId2, organism2, buffer);

        // write the deatils of the interaction
        writeInteractionDetails(buffer, typeToPubmed, methodToPubmed, pubmedsHavingSpokeExpansion);

        return new CcLine(buffer.toString(), geneName1, uniprot2);
    }

    private void writeInteractionDetails(StringBuffer buffer, Map<String, List<String>> typeToPubmed, Map<String, List<String>> methodToPubmed, Map<String, Map<String, String>> pubmedsHavingSpokeExpansion) {

        for (Map.Entry<String, List<String>> typeEntry : typeToPubmed.entrySet()){
            List<String> pubmeds1 = typeEntry.getValue();
            String type = this.clusterScore.getMiTerms().get(typeEntry.getKey());

            for (Map.Entry<String, List<String>> methodEntry : methodToPubmed.entrySet()){
                List<String> pubmeds2 = methodEntry.getValue();
                String method = this.clusterScore.getMiTerms().get(methodEntry.getKey());

                Collection<String> associatedPubmeds = CollectionUtils.intersection(pubmeds1, pubmeds2);

                if (!associatedPubmeds.isEmpty()){

                    Collection<String> spokeExpandedPubmeds = collectSpokeExpandedInteractions(pubmedsHavingSpokeExpansion, associatedPubmeds, method, type);
                    Collection<String> binaryInteractions = CollectionUtils.subtract(associatedPubmeds, spokeExpandedPubmeds);

                    if (!spokeExpandedPubmeds.isEmpty()){
                        writeSpokeExpandedInteractions(buffer, type, method, spokeExpandedPubmeds);
                    }
                    writeBinaryInteraction(buffer, type, method, binaryInteractions);
                }
            }
        }
    }

    private void writeBinaryInteraction(StringBuffer buffer, String type, String method, Collection<String> binaryInteractions) {
        buffer.append("CC         InteractionType="+type+"; Method="+method+"; Source=");

        for (String pid : binaryInteractions){
            buffer.append("Pubmed:"+pid+", ");
        }
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(";");
        buffer.append(NEW_LINE);
    }

    private void writeSpokeExpandedInteractions(StringBuffer buffer, String type, String method, Collection<String> spokeExpandedPubmeds) {
        buffer.append("CC         InteractionType="+type+"; Method="+method+"; Expansion=Spoke; Source=");

        for (String pid : spokeExpandedPubmeds){
            buffer.append("Pubmed:"+pid+", ");
        }

        buffer.deleteCharAt(buffer.length() - 1);
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append(";");
        buffer.append(NEW_LINE);
    }

    private void writeSecondProtein(String uniprot2, String geneName2, String taxId1, String taxId2, String organism2, StringBuffer buffer) {
        buffer.append("CC         Protein2=");
        buffer.append( geneName2 ).append(' ').append( '[' ).append( uniprot2 ).append( ']' ).append( ';' );

        if (!taxId1.equalsIgnoreCase(taxId2)) {
            buffer.append(" Organism=");
            buffer.append( organism2 ).append( " [NCBI_TaxID=" ).append( taxId2 ).append( "]" );
            buffer.append(';');
        }

        buffer.append(NEW_LINE);
    }

    private void writeFirstProtein(String uniprot1, String geneName1, StringBuffer buffer) {
        buffer.append("CC         Protein1=");
        buffer.append( geneName1 ).append(' ').append( '[' ).append( uniprot1 ).append( ']' ).append( ';' );
        buffer.append(NEW_LINE);
    }

    private void writeInteractionIntroduction(boolean doesInteract, String uniprot1, String uniprot2, StringBuffer buffer) {
        buffer.append("CC       Interact="+ (doesInteract ? "yes" : "no") +"; ");

        buffer.append(" Xref=IntAct:").append( uniprot1 ).append(',').append(uniprot2).append(';');
        buffer.append(NEW_LINE);
    }

    void fireCcLineCreatedEvent(CcLineCreatedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == CcLineEventListener.class) {
                ((CcLineEventListener) listeners[i + 1]).ccLineCreated(evt);
            }
        }
    }

    public void addCcLineExportListener(CcLineEventListener eventListener) {
        listenerList.add(CcLineEventListener.class, eventListener);
    }

    // This methods allows classes to unregister for MyEvents
    public void removeCcLineExportListener(CcLineEventListener eventListener) {
        listenerList.remove(CcLineEventListener.class, eventListener);
    }
}
