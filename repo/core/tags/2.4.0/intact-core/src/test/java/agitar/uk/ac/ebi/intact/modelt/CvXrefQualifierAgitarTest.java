/**
 * Generated by Agitar build: Agitator Version 1.0.4.000276 (Build date: Mar 27, 2007) [1.0.4.000276]
 * JDK Version: 1.5.0_09
 *
 * Generated on 04-Apr-2007 08:28:51
 * Time to generate: 00:07.380 seconds
 *
 */

package agitar.uk.ac.ebi.intact.modelt; import uk.ac.ebi.intact.model.*;

import com.agitar.lib.junit.AgitarTestCase;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

public class CvXrefQualifierAgitarTest extends AgitarTestCase {

    static Class TARGET_CLASS = CvXrefQualifier.class;

    public void testConstructor() throws Throwable {
        Institution owner = new Institution( "testCvXrefQualLab" );
        CvXrefQualifier cvXrefQualifier = new CvXrefQualifier( owner, "testCvXrefQualLab" );
        assertEquals( "cvXrefQualifier.xrefs.size()", 0, cvXrefQualifier.xrefs.size() );
        assertEquals( "cvXrefQualifier.getAliases().size()", 0, cvXrefQualifier.getAliases().size() );
        assertEquals( "cvXrefQualifier.shortLabel", "testCvXrefQualLab", cvXrefQualifier.getShortLabel() );
        assertEquals( "cvXrefQualifier.annotations.size()", 0, cvXrefQualifier.annotations.size() );
        assertSame( "cvXrefQualifier.getOwner()", owner, cvXrefQualifier.getOwner() );
    }

    public void testConstructorThrowsIllegalArgumentException() throws Throwable {
        try {
            new CvXrefQualifier( new Institution( "testCvXrefQualLab" ), "" );
            fail( "Expected IllegalArgumentException to be thrown" );
        } catch ( IllegalArgumentException ex ) {
            assertEquals( "ex.getMessage()", "Must define a non empty short label", ex.getMessage() );
            assertThrownBy( AnnotatedObjectUtils.class, ex );
        }
    }

    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new CvXrefQualifier( new Institution( "testCvXrefQualLab" ), null );
            fail( "Expected NullPointerException to be thrown" );
        } catch ( NullPointerException ex ) {
            assertEquals( "ex.getMessage()", "Must define a non null short label", ex.getMessage() );
            assertThrownBy( AnnotatedObjectUtils.class, ex );
        }
    }
}

