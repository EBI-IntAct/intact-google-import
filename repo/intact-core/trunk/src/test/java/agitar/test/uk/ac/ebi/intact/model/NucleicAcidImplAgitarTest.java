/**
 * Generated by Agitar build: Agitator Version 1.0.4.000276 (Build date: Mar 27, 2007) [1.0.4.000276]
 * JDK Version: 1.5.0_09
 *
 * Generated on 04-Apr-2007 08:31:37
 * Time to generate: 00:10.306 seconds
 *
 */

package uk.ac.ebi.intact.model;

import com.agitar.lib.junit.AgitarTestCase;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

public class NucleicAcidImplAgitarTest extends AgitarTestCase {

    static Class TARGET_CLASS = NucleicAcidImpl.class;

    public void testConstructor() throws Throwable {
        Institution owner = new Institution( "test" );
        BioSource source = new BioSource( owner, "test", "1" );
        CvInteractorType type = new CvInteractorType( owner, "type" );
        NucleicAcidImpl nucleicAcidImpl = new NucleicAcidImpl( owner, source, "testNucleicAcidImplShortLabel", type );
        assertEquals( "nucleicAcidImpl.xrefs.size()", 0, nucleicAcidImpl.xrefs.size() );
        assertSame( "nucleicAcidImpl.getBioSource()", source, nucleicAcidImpl.getBioSource() );
        assertEquals( "nucleicAcidImpl.getAliases().size()", 0, nucleicAcidImpl.getAliases().size() );
        assertEquals( "nucleicAcidImpl.getEvidences().size()", 0, nucleicAcidImpl.getEvidences().size() );
        assertEquals( "nucleicAcidImpl.shortLabel", "testNucleicAcidImplS", nucleicAcidImpl.shortLabel );
        assertEquals( "nucleicAcidImpl.getSequenceChunks().size()", 0, nucleicAcidImpl.getSequenceChunks().size() );
        assertEquals( "nucleicAcidImpl.annotations.size()", 0, nucleicAcidImpl.annotations.size() );
        assertSame( "nucleicAcidImpl.getOwner()", owner, nucleicAcidImpl.getOwner() );
        assertEquals( "nucleicAcidImpl.references.size()", 0, nucleicAcidImpl.references.size() );
        assertSame( "nucleicAcidImpl.getCvInteractorType()", type, nucleicAcidImpl.getCvInteractorType() );
        assertEquals( "nucleicAcidImpl.getActiveInstances().size()", 0, nucleicAcidImpl.getActiveInstances().size() );
    }

    public void testConstructorThrowsIllegalArgumentException() throws Throwable {
        Institution owner = new Institution( "test" );
        BioSource source = new BioSource( owner, "test", "1" );
        CvInteractorType type = new CvInteractorType( owner, "type" );
        try {
            new NucleicAcidImpl( owner, source, "", type );
            fail( "Expected IllegalArgumentException to be thrown" );
        } catch ( IllegalArgumentException ex ) {
            assertEquals( "ex.getMessage()", "Must define a non empty short label", ex.getMessage() );
            assertThrownBy( AnnotatedObjectUtils.class, ex );
        }
    }

    public void testConstructorThrowsNullPointerException() throws Throwable {
        Institution owner = new Institution( "test" );
        BioSource source = new BioSource( owner, "test", "1" );
        CvInteractorType type = new CvInteractorType( owner, "type" );
        try {
            new NucleicAcidImpl( owner, source, null, type );
            fail( "Expected NullPointerException to be thrown" );
        } catch ( NullPointerException ex ) {
            assertEquals( "ex.getMessage()", "Must define a non null short label", ex.getMessage() );
            assertThrownBy( AnnotatedObjectUtils.class, ex );
        }
    }
}

