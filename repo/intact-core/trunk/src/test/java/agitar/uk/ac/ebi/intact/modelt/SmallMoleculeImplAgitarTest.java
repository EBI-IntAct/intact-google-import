/**
 * Generated by Agitar build: Agitator Version 1.0.4.000276 (Build date: Mar 27, 2007) [1.0.4.000276]
 * JDK Version: 1.5.0_09
 *
 * Generated on 04-Apr-2007 08:32:34
 * Time to generate: 00:09.644 seconds
 *
 */

package agitar.uk.ac.ebi.intact.modelt; import uk.ac.ebi.intact.model.*;

import com.agitar.lib.junit.AgitarTestCase;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

public class SmallMoleculeImplAgitarTest extends AgitarTestCase {

    static Class TARGET_CLASS = SmallMoleculeImpl.class;

    public void testConstructor() throws Throwable {
        Institution owner = new Institution( "testSmallMoleculeImplShortLabel" );
        CvInteractorType type = new CvInteractorType( owner, "testSmallMoleculeImplShortLabel" );
        SmallMoleculeImpl smallMoleculeImpl = new SmallMoleculeImpl( "testSmallMoleculeImplShortLabel", owner, type );
        assertEquals( "smallMoleculeImpl.xrefs.size()", 0, smallMoleculeImpl.xrefs.size() );
        assertEquals( "smallMoleculeImpl.getAliases().size()", 0, smallMoleculeImpl.getAliases().size() );
        assertEquals( "smallMoleculeImpl.getEvidences().size()", 0, smallMoleculeImpl.getEvidences().size() );
        assertEquals( "smallMoleculeImpl.shortLabel", "testSmallMoleculeImp", smallMoleculeImpl.getShortLabel() );
        assertEquals( "smallMoleculeImpl.annotations.size()", 0, smallMoleculeImpl.annotations.size() );
        assertSame( "smallMoleculeImpl.getOwner()", owner, smallMoleculeImpl.getOwner() );
        assertEquals( "smallMoleculeImpl.references.size()", 0, smallMoleculeImpl.references.size() );
        assertEquals( "smallMoleculeImpl.getActiveInstances().size()", 0, smallMoleculeImpl.getActiveInstances().size() );
        assertSame( "smallMoleculeImpl.getCvInteractorType()", type, smallMoleculeImpl.getCvInteractorType() );
    }

    public void testConstructorThrowsIllegalArgumentException() throws Throwable {
        try {
            new SmallMoleculeImpl( "", new Institution( "testSmallMoleculeImplShortLabel" ), new CvInteractorType( null, "testSmallMoleculeImplShortLabel" ) );
            fail( "Expected IllegalArgumentException to be thrown" );
        } catch ( IllegalArgumentException ex ) {
            assertEquals( "ex.getMessage()", "Must define a non empty short label", ex.getMessage() );
            assertThrownBy( AnnotatedObjectUtils.class, ex );
        }
    }

    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SmallMoleculeImpl( null, new Institution( "testSmallMoleculeImplShortLabel" ), new CvInteractorType( new Institution( "testSmallMoleculeImplShortLabel1" ), "testSmallMoleculeImplShortLabel" ) );
            fail( "Expected NullPointerException to be thrown" );
        } catch ( NullPointerException ex ) {
            assertEquals( "ex.getMessage()", "Must define a non null short label", ex.getMessage() );
            assertThrownBy( AnnotatedObjectUtils.class, ex );
        }
    }
}

