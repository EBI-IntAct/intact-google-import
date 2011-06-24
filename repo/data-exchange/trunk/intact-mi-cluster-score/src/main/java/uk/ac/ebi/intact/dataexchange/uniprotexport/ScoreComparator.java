package uk.ac.ebi.intact.dataexchange.uniprotexport;

import org.apache.commons.collections.CollectionUtils;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The ScoreComparator will take as arguments :
 * - the text file generated by the uniprot export A which contains the exported binary interactions and their scores
 * - the text file generated by the uniprot export B which contains the exported binary interactions and their scores
 * - the text file generated by the uniprot export A which contains the excluded binary interactions and their scores
 * - the text file generated by the uniprot export B which contains the excluded binary interactions and their scores
 *
 * It will retrieve a specific binary interaction and collect the score it had during uniprot export A and the score it had during uniprot export B
 * It will return the results of each exported interactions (exported in A and/or B) in another file (we can specifiy its name)
 * Format for input files : id-uniprot1-uniprot2:score
 *
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/06/11</pre>
 */

public class ScoreComparator {

    private static final String SCORE_SEPARATOR = ":";
    private static final String INTERACTOR_SEPARATOR = "-";

    public static void main( String[] args ) throws IOException {
        // Five possible arguments
        if( args.length != 5 ) {
            System.err.println( "Usage: ExportFilter <exportedA> <exportedB> <excludedA> <excludedB> <results>" );
            System.err.println( "Usage: <exportedA> is the file containing the exported interactions of a first export (list of id-uniprot1-uniprot2-score)" );
            System.err.println( "Usage: <exportedB> is the file containing the exported interactions of a second export (list of id-uniprot1-uniprot2-score)" );
            System.err.println( "Usage: <excludedA> is the file containing the excluded interactions of the first export (list of id-uniprot1-uniprot2-score)" );
            System.err.println( "Usage: <excludedB> is the file containing the excluded interactions of the second export (list of id-uniprot1-uniprot2-score)" );
            System.err.println( "Usage: <results> is the file where to write the scores of both exports for the binary interaction exported in first export and/or second export" );

            System.exit( 1 );
        }
        System.out.println("Reading arguments...");
        File exportedA = new File(args[0]);
        File exportedB = new File(args[1]);
        File excludedA = new File(args[2]);
        File excludedB = new File(args[3]);
        File results = new File(args[4]);

        System.out.println("Extracting exported interactions in "+exportedA.getName()+"...");
        Map<String, String> exportResultsA = extractScoreResultsFor(exportedA);
        System.out.println("Extracting exported interactions in "+exportedB.getName()+"...");
        Map<String, String> exportResultsB = extractScoreResultsFor(exportedB);
        System.out.println("Extracting excluded interactions in "+excludedA.getName()+"...");
        Map<String, String> excludedResultsA = extractScoreResultsFor(excludedA);
        System.out.println("Extracting excluded interactions in "+excludedB.getName()+"...");
        Map<String, String> excludedResultsB = extractScoreResultsFor(excludedB);

        System.out.println("Collect exported interactions and scores ...");
        Collection<String> exportedBothAAndB = CollectionUtils.intersection(exportResultsA.keySet(), exportResultsB.keySet());
        System.out.println(exportedBothAAndB.size() + " interactions exported in both exports");
        Collection<String> exportedANotB = CollectionUtils.subtract(exportResultsA.keySet(), exportedBothAAndB);
        System.out.println(exportedANotB.size() + " interactions exported in first export but not second");
        Collection<String> exportedBNotA = CollectionUtils.subtract(exportResultsB.keySet(), exportedBothAAndB);
        System.out.println(exportedANotB.size() + " interactions exported in second export but not first");


        FileWriter resultsWriter = new FileWriter(results);
        resultsWriter.write("Interaction");
        resultsWriter.write("\t");
        resultsWriter.write("Score for export A");
        resultsWriter.write("\t");
        resultsWriter.write("Score for export B");
        resultsWriter.write("\n");

        System.out.println("Write scores of exported interactions in both exports...");
        for (String interaction : exportedBothAAndB){
            resultsWriter.write(interaction);
            resultsWriter.write("\t");
            resultsWriter.write(exportResultsA.get(interaction));
            resultsWriter.write("\t");
            resultsWriter.write(exportResultsB.get(interaction));
            resultsWriter.write("\n");
        }

        System.out.println("Write scores of exported interactions in first export but not second...");
        for (String interaction : exportedANotB){
            resultsWriter.write(interaction);
            resultsWriter.write("\t");
            resultsWriter.write(exportResultsA.get(interaction));
            resultsWriter.write("\t");
            resultsWriter.write(excludedResultsB.get(interaction));
            resultsWriter.write("\n");
        }

        System.out.println("Write scores of exported interactions in second export but not first...");
        for (String interaction : exportedBNotA){
            resultsWriter.write(interaction);
            resultsWriter.write("\t");
            resultsWriter.write(excludedResultsA.get(interaction));
            resultsWriter.write("\t");
            resultsWriter.write(exportResultsB.get(interaction));
            resultsWriter.write("\n");
        }

        resultsWriter.close();
    }

    private static Map<String, String> extractScoreResultsFor(File file) throws FileNotFoundException, IOException{
        Map<String, String> exportScores = new HashMap<String, String> ();

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();

        while (line != null){

            if (line.contains(INTERACTOR_SEPARATOR)){
                String[] values = line.split(INTERACTOR_SEPARATOR);

                if (values.length == 3){
                    String firstInteractor = values[1];
                    String secondValue = values[2];

                    if (secondValue.contains(SCORE_SEPARATOR)){
                        String [] scores = secondValue.split(SCORE_SEPARATOR);
                        String secondInteractor = scores[0];
                        String score = scores[1];

                        System.out.println("Extracting" + line + "...'");
                        exportScores.put(firstInteractor+"-"+secondInteractor, score);
                    }
                    else {
                        System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                    }
                }
                else {
                    System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
                }
            }
            else {
                System.err.println("the line " + line + " cannot be loaded because is not of the form 'id-interactorA-interactorB:score'");
            }
            line = reader.readLine();
        }

        reader.close();

        return exportScores;
    }

}
