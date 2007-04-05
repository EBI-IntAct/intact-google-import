/**
 * Generated by Agitar build: Agitator Version 1.0.4.000276 (Build date: Mar 27, 2007) [1.0.4.000276]
 * JDK Version: 1.5.0_09
 *
 * Generated on 04-Apr-2007 08:27:55
 * Time to generate: 00:46.276 seconds
 *
 */

package uk.ac.ebi.intact.model;

import agitar.test.uk.ac.ebi.intact.model.AgitarTestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class ComponentAliasAgitarTest extends AgitarTestCase {

    static Class TARGET_CLASS = ComponentAlias.class;

//    public void testConstructor() throws Throwable {
//        Institution anOwner = ( Institution ) Mockingbird.getProxyObject( Institution.class );
//        Component component = ( Component ) Mockingbird.getProxyObject( Component.class );
//        CvAliasType cvAliasType = ( CvAliasType ) Mockingbird.getProxyObject( CvAliasType.class );
//        Mockingbird.enterRecordingMode();
//        Mockingbird.setReturnValue( component.getAc(), "testString" );
//        Mockingbird.enterTestMode();
//        ComponentAlias componentAlias = new ComponentAlias( anOwner, component, cvAliasType, "\t X X    \rXXXXXXXXX\tXXXXXXXXXXX \t\t   \n " );
//        assertEquals( "componentAlias.getEvidences().size()", 0, componentAlias.getEvidences().size() );
//        assertSame( "componentAlias.getCvAliasType()", cvAliasType, componentAlias.getCvAliasType() );
//        assertEquals( "componentAlias.getName()", "X X    \rXXXXXXXXX\tXXXXXXXXXXX", componentAlias.getName() );
//        assertSame( "componentAlias.getOwner()", anOwner, componentAlias.getOwner() );
//        assertEquals( "componentAlias.parentAc", "testString", getPrivateField( componentAlias, "parentAc" ) );
//        assertInvoked( component, "getAc" );
//    }

    public void testConstructor1() throws Throwable {
        ComponentAlias componentAlias = new ComponentAlias( null, new Component( new Institution( "testComponentAliasShortLabel2" ), new InteractionImpl( new ArrayList( 100 ), new ArrayList( 1000 ), null, new CvInteractorType( new Institution( "testComponentAliasShortLabel1" ), "testComponentAliasShortLabel" ), "testComponentAliasShortLabel", null ), new Complex(), new CvComponentRole( new Institution( "testComponentAliasShortLabel" ), "testComponentAliasShortLabel" ) ), null, "31CharactersXXXXXXXXXXXXXXXXXXX" );
        assertEquals( "componentAlias.getEvidences().size()", 0, componentAlias.getEvidences().size() );
        assertNull( "componentAlias.getCvAliasType()", componentAlias.getCvAliasType() );
        assertEquals( "componentAlias.getName()", "31CharactersXXXXXXXXXXXXXXXXXX", componentAlias.getName() );
        assertNull( "componentAlias.getOwner()", componentAlias.getOwner() );
        assertNull( "componentAlias.parentAc", getPrivateField( componentAlias, "parentAc" ) );
    }

//    public void testConstructor2() throws Throwable {
//        Institution anOwner = ( Institution ) Mockingbird.getProxyObject( Institution.class );
//        Component component = ( Component ) Mockingbird.getProxyObject( Component.class );
//        CvAliasType cvAliasType = ( CvAliasType ) Mockingbird.getProxyObject( CvAliasType.class );
//        Mockingbird.enterRecordingMode();
//        Mockingbird.setReturnValue( component.getAc(), "testString" );
//        Mockingbird.enterTestMode();
//        ComponentAlias componentAlias = new ComponentAlias( anOwner, component, cvAliasType, "30CharactersXXXXXXXXXXXXXXXXXX" );
//        assertEquals( "componentAlias.getEvidences().size()", 0, componentAlias.getEvidences().size() );
//        assertSame( "componentAlias.getCvAliasType()", cvAliasType, componentAlias.getCvAliasType() );
//        assertEquals( "componentAlias.getName()", "30CharactersXXXXXXXXXXXXXXXXXX", componentAlias.getName() );
//        assertSame( "componentAlias.getOwner()", anOwner, componentAlias.getOwner() );
//        assertEquals( "componentAlias.parentAc", "testString", getPrivateField( componentAlias, "parentAc" ) );
//        assertInvoked( component, "getAc" );
//    }

    public void testGetParent() throws Throwable {
        Institution owner = new Institution( "testComponentAliasShortLabel1" );
        CvComponentRole role = new CvComponentRole( owner, "testComponentAliasShortLabel" );
        CvAliasType cvAliasType = new CvAliasType( null, "testComponentAliasShortLabel" );
        Object[] objects = new Object[0];
        CvComponentRole role2 = new CvComponentRole( null, "testComponentAliasShortLabel1" );
        AnnotatedObjectImpl component = new Component( null, "testComponentAliasShortLabel", new InteractionImpl( Arrays.asList( objects ), new CvInteractionType( new Institution( "testComponentAliasShortLabel" ), "testComponentAliasShortLabel" ), "testComponentAliasShortLabel", owner ), new SmallMoleculeImpl( "testComponentAliasShortLabel", new Institution( "testComponentAlias\tShortLabel" ), null ), role2 );
        ComponentAlias alias = new ComponentAlias( new Institution( "testComponentAlias\nShortLabel" ), new Component( new Institution( "testComponentAlias\rShortLabel" ), new InteractionImpl( new ArrayList( 100 ), null, "testComponentAliasShortLabel1", new Institution( "testComponentAliasShortLabel2" ) ), new Complex(), role ), cvAliasType, "testComponentAliasName" );
        component.addAlias( alias );
        AnnotatedObjectImpl result = ( AnnotatedObjectImpl ) alias.getParent();
        assertSame( "result", component, result );
    }

    public void testGetParent1() throws Throwable {
        Institution owner = new Institution( "testComponentAliasShortLabel" );
        CvInteractionType type = new CvInteractionType( owner, "testComponentAliasShortLabel" );
        Institution owner2 = new Institution( "testComponentAliasShortLabel1" );
        AnnotatedObject result = new ComponentAlias( new Institution( "testComponentAlias\nShortLabel" ), new Component( owner2, new InteractionImpl( new ArrayList(), type, "testComponentAliasShortLabel", null ), new SmallMoleculeImpl( "testComponentAliasShortLabel", owner2, new CvInteractorType( owner, "testComponentAliasShortLabel" ) ), new CvComponentRole( new Institution( "testComponentAlias\rShortLabel" ), "testComponentAliasShortLabel" ) ), new CvAliasType( new Institution( "testComponentAliasShortLabel2" ), "testComponentAliasShortLabel" ), "testComponentAliasName" ).getParent();
        assertNull( "result", result );
    }

    public void testGetParentAc() throws Throwable {
        ComponentAlias componentAlias = new ComponentAlias( new Institution( "testComponentAlias\nShortLabel" ), new Component( new Institution( "testComponentAlias\rShortLabel" ), new InteractionImpl( new ArrayList( 100 ), null, "testComponentAliasShortLabel", new Institution( "testComponentAliasShortLabel2" ) ), new Complex(), new CvComponentRole( new Institution( "testComponentAliasShortLabel" ), "testComponentAliasShortLabel" ) ), new CvAliasType( new Institution( "testComponentAliasShortLabel1" ), "testComponentAliasShortLabel" ), "testComponentAliasName" );
        String result = componentAlias.getParentAc();
        assertNull( "result", result );
        assertNull( "componentAlias.getParent()", componentAlias.getParent() );
        assertNull( "componentAlias.parentAc", getPrivateField( componentAlias, "parentAc" ) );
    }

    public void testGetParentAc1() throws Throwable {
        Institution owner = new Institution( "testComponentAliasShortLabel" );
        Component component = new Component( new Institution( "testComponentAlias\rShortLabel" ), new InteractionImpl( new ArrayList( 100 ), new ArrayList( 1000 ), null, new CvInteractorType( new Institution( "testComponentAliasShortLabel2" ), "testComponentAliasShortLabel" ), "testComponentAliasShortLabel", null ), new Complex(), new CvComponentRole( new Institution( "testComponentAliasShortLabel1" ), "testComponentAliasShortLabel" ) );
        component.setAc( "testComponentAliasAc" );
        ComponentAlias componentAlias = new ComponentAlias( owner, component, new CvAliasType( owner, "testComponentAliasShortLabel" ), "testComponentAliasName" );
        String result = componentAlias.getParentAc();
        assertEquals( "result", "testComponentAliasAc", result );
        assertNull( "componentAlias.getParent()", componentAlias.getParent() );
        assertEquals( "componentAlias.parentAc", "testComponentAliasAc", getPrivateField( componentAlias, "parentAc" ) );
    }

    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ComponentAlias( new Institution( "testComponentAliasShortLabel1" ), null, new CvAliasType( new Institution( "testComponentAliasShortLabel" ), "testComponentAliasShortLabel" ), "testComponentAliasName" );
            fail( "Expected NullPointerException to be thrown" );
        } catch ( NullPointerException ex ) {
            assertNull( "ex.getMessage()", ex.getMessage() );
            assertThrownBy( Alias.class, ex );
        }
    }
}

