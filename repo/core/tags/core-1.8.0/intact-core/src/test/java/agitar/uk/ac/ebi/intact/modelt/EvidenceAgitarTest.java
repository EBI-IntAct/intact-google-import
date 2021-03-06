/**
 * Generated by Agitar build: Agitator Version 1.0.4.000276 (Build date: Mar 27, 2007) [1.0.4.000276]
 * JDK Version: 1.5.0_09
 *
 * Generated on 04-Apr-2007 08:24:23
 * Time to generate: 00:07.320 seconds
 *
 */

package agitar.uk.ac.ebi.intact.modelt; import uk.ac.ebi.intact.model.*;

import com.agitar.lib.junit.AgitarTestCase;

public class EvidenceAgitarTest extends AgitarTestCase {

    static Class TARGET_CLASS = Evidence.class;

    public void testConstructor() throws Throwable {
        Evidence evidence = new Evidence();
        assertNull( "evidence.getCvEvidenceTypeAc()", evidence.getCvEvidenceTypeAc() );
    }

    public void testGetCvEvidenceTypeAc() throws Throwable {
        Evidence evidence = new Evidence();
        evidence.setCvEvidenceTypeAc( "testEvidenceAc" );
        String result = evidence.getCvEvidenceTypeAc();
        assertEquals( "result", "testEvidenceAc", result );
    }

    public void testGetCvEvidenceTypeAc1() throws Throwable {
        String result = new Evidence().getCvEvidenceTypeAc();
        assertNull( "result", result );
    }

    public void testSetCvEvidenceType() throws Throwable {
        Evidence evidence = new Evidence();
        CvEvidenceType cvEvidenceType = new CvEvidenceType( null, "testEvidenceShortLabel" );
        evidence.setCvEvidenceType( cvEvidenceType );
        assertSame( "evidence.getCvEvidenceType()", cvEvidenceType, evidence.getCvEvidenceType() );
    }

    public void testSetCvEvidenceTypeAc() throws Throwable {
        Evidence evidence = new Evidence();
        evidence.setCvEvidenceTypeAc( "testEvidenceAc" );
        assertEquals( "evidence.cvEvidenceTypeAc", "testEvidenceAc", evidence.getCvEvidenceTypeAc() );
    }

    public void testSetParameters() throws Throwable {
        Evidence evidence = new Evidence();
        evidence.setParameters( "testEvidenceParameters" );
        assertEquals( "evidence.getParameters()", "testEvidenceParameters", evidence.getParameters() );
    }
}

