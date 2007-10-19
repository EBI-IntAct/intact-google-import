/**
 * 
 */
package uk.ac.ebi.intact.psimitab.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import psidev.psi.mi.search.column.PsimiTabColumn;
import psidev.psi.mi.search.util.DefaultDocumentBuilder;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineParser;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.psimitab.IntActBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntActColumnHandler;

/**
 * TODO comment this!
 *
 * @author Nadin Neuhauser (nneuhaus@ebi.ac.uk)
 * @version $Id: IntActDocumentBuilder.java 
 */
public class IntActDocumentBuilder extends DefaultDocumentBuilder {

	private static final String DEFAULT_COL_SEPARATOR = "\t";
	
	
	public static Document createDocumentFromPsimiTabLine(String psiMiTabLine) throws MitabLineException
	{
		String[] tokens = psiMiTabLine.split(DEFAULT_COL_SEPARATOR);
			
		// raw fields
		String roleA = tokens[IntActColumnSet.EXPERIMENTAL_ROLE_A.getOrder()];
		String roleB = tokens[IntActColumnSet.EXPERIMENTAL_ROLE_B.getOrder()];
		String propertiesA = tokens[IntActColumnSet.PROPERTIES_A.getOrder()];
		String propertiesB = tokens[IntActColumnSet.PROPERTIES_B.getOrder()];
		String typeA = tokens[IntActColumnSet.INTERACTOR_TYPE_A.getOrder()];
		String typeB = tokens[IntActColumnSet.INTERACTOR_TYPE_B.getOrder()];
		String hostOrganism = tokens[IntActColumnSet.HOSTORGANISM.getOrder()];
		String expansion = tokens[IntActColumnSet.EXPANSION_METHOD.getOrder()];
		
		Document doc = DefaultDocumentBuilder.createDocumentFromPsimiTabLine(psiMiTabLine);
		
		doc.add(new Field("roles", isolateBracket(roleA) + " " + isolateBracket(roleB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, IntActColumnSet.EXPERIMENTAL_ROLE_A, roleA);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, IntActColumnSet.EXPERIMENTAL_ROLE_B, roleB);

		doc.add(new Field("properties", isolateValue(propertiesA) + " " + isolateValue(propertiesB),
				Field.Store.NO,
				Field.Index.TOKENIZED));

		addTokenizedAndSortableField(doc, IntActColumnSet.PROPERTIES_A, propertiesA);
		addTokenizedAndSortableField(doc, IntActColumnSet.PROPERTIES_B, propertiesB);
		
		doc.add(new Field("interactor_types", isolateBracket(typeA) + " " + isolateBracket(typeB),
				Field.Store.NO,
				Field.Index.TOKENIZED));
		
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, IntActColumnSet.INTERACTOR_TYPE_A, typeA);
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, IntActColumnSet.INTERACTOR_TYPE_B, typeB);
		
		createHostOrganismField(doc, psiMiTabLine);
		
		DefaultDocumentBuilder.addTokenizedAndSortableField(doc, IntActColumnSet.EXPANSION_METHOD, expansion);

		return doc;
	}
	
	public static String createPsimiTabLine(Document doc)
	{
    	if (doc == null)
    	{
    		throw new NullPointerException("Document is null");
    	}
    	
		StringBuffer sb = new StringBuffer(256);
		sb.append(DefaultDocumentBuilder.createPsimiTabLine(doc));
		sb.append(doc.get(IntActColumnSet.EXPERIMENTAL_ROLE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.EXPERIMENTAL_ROLE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.PROPERTIES_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.PROPERTIES_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.INTERACTOR_TYPE_A.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.INTERACTOR_TYPE_B.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.HOSTORGANISM.getShortName())).append(DEFAULT_COL_SEPARATOR);
		sb.append(doc.get(IntActColumnSet.EXPANSION_METHOD.getShortName())).append(DEFAULT_COL_SEPARATOR);
		
		return  sb.toString();
	}
	
    /**
     * Creates a BinaryInteraction from a lucene document using the new version of psimitab with extra columns
     * @param doc the Document to use
     * @return the binary interaction
     * @throws MitabLineException thrown if there are syntax or other problems parsing the document/line
     */
    public static BinaryInteraction createBinaryInteraction(Document doc) throws MitabLineException
    {
        String line = createPsimiTabLine(doc);

        MitabLineParser parser = new MitabLineParser();
        parser.setBinaryInteractionClass(IntActBinaryInteraction.class);
        parser.setColumnHandler(new IntActColumnHandler());
  
        return parser.parse(line);
    }
    
    /**
     * Gets only the value part of a column
     * @param column
     * @return
     */
    public static String isolateValue(String column)
    {
        String[] values = column.split("\\|");

        StringBuilder sb = new StringBuilder();

        for (String v : values) {
            if (v.contains("go")) {
                v = "GO".concat(v.split("\\:")[1]);
            	//v = v.toUpperCase();
            }
            
            if (v.contains(":") && !v.contains("GO") && !v.contains("go")) {
                v = v.split("\\:")[1];
            }
            
            if (v.contains("(")) {
                v = v.split("\\(")[0];
            }

            sb.append(v+" ");
        }

        sb.trimToSize();

        return sb.toString();
    }

    public static String isolateBracket(String column)
    {
        String[] values = column.split("\\|");

        StringBuilder sb = new StringBuilder();

        for (String v : values) {

            if (v.contains("(")) {
                v = v.split("\\(")[1];
                v = v.split("\\)")[0];
            }

            sb.append(v+" ");
        }

        sb.trimToSize();

        return sb.toString();
    }
    
    public static void addTokenizedAndSortableField(Document doc, PsimiTabColumn column, String columnValue)
    {
         doc.add(new Field(column.getShortName(),
                columnValue,
                Field.Store.YES,
                Field.Index.TOKENIZED));
        
        doc.add(new Field(column.getSortableColumnName(),
                isolateValue(columnValue),
                Field.Store.NO,
                Field.Index.UN_TOKENIZED));
    }

    /**
     * Creates a List of HostOrganism (as String) 
     * @param organism 
     * @return the list with the hostorganism
     */
    private static List<String> hostOrganism(Collection<CrossReference> organism)
    {
        List<String> hostOrganisms = new ArrayList<String>(organism.size());

        for (CrossReference o : organism)
        {
        	hostOrganisms.add(o.getDatabase());
        }

        return hostOrganisms;
    }
    
    private static void createHostOrganismField(Document doc, String line) throws MitabLineException {
    	MitabLineParser parser = new MitabLineParser();
    	
    	parser.setBinaryInteractionClass(IntActBinaryInteraction.class);
    	parser.setColumnHandler(new IntActColumnHandler());
    	
        IntActBinaryInteraction binaryInteraction = (IntActBinaryInteraction)parser.parse(line);

        List<String> hostOrganism = hostOrganism(binaryInteraction.getHostOrganism());

        doc.add(new Field("hostOrganisms",
                listToString(hostOrganism),
                Field.Store.NO,
                Field.Index.TOKENIZED));
        
        String[] tokens = line.split(DEFAULT_COL_SEPARATOR);
        String organism = tokens[IntActColumnSet.HOSTORGANISM.getOrder()];
        
        doc.add(new Field(IntActColumnSet.HOSTORGANISM.getShortName(),
        		organism,
                Field.Store.YES,
                Field.Index.TOKENIZED));
        
        doc.add(new Field(IntActColumnSet.HOSTORGANISM.getSortableColumnName(),
                organism,
                Field.Store.NO,
                Field.Index.UN_TOKENIZED));
    }

	
}
