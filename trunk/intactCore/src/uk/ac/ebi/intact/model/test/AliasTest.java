// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.model.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junitx.framework.Assert;
import junitx.framework.ObjectFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.test.util.TestableProtein;

/**
 * Test the basic methods of an Alias.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class AliasTest extends TestCase {

    /**
     * Returns this test suite. Reflection is used here to add all
     * the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( AliasTest.class );
    }

    private Institution owner;
    private BioSource yeast;
    private CvAliasType type1;
    private CvAliasType type2;

    protected void setUp() throws Exception {
        super.setUp();

        // initialize required objects

        owner = new Institution( "owner" );
        assertNotNull( owner );

        yeast = new BioSource( owner, "yeast", "4932" );

        type1 = new CvAliasType( owner, "type1" );
        assertNotNull( type1 );

        type2 = new CvAliasType( owner, "type2" );
        assertNotNull( type2 );
    }

    public void testConstructor_ok() {

        try {
            Protein protein = new TestableProtein( "", owner, yeast, "proteinTest", "AAAAAAAAAAAAAAAAAA" );
            Alias alias = new Alias( owner, protein, type1, "anAlias" );
            assertNotNull( alias );
            assertEquals( protein.getAc(), alias.getParentAc() );

        } catch ( Exception e ) {

            e.printStackTrace();
            fail( "That constructor call should have succeeded." );
        }
    }

    public void testConstructor3_missingInstitution() {

        try {
            Protein protein = new TestableProtein( "", owner, yeast, "proteinTest", "AAAAAAAAAAAAAAAAAA" );
            new Alias( null, protein, type1, "anAlias" );
            fail( "Null Institution should not be allowed." );

        } catch ( Exception e ) {

            // ok.
        }
    }

    public void testSetName() {

        Protein protein = new TestableProtein( "", owner, yeast, "proteinTest", "AAAAAAAAAAAAAAAAAA" );
        String name = "anAlias";
        Alias alias = new Alias( owner, protein, type1, name );
        assertEquals( name.trim(), alias.getName() );

        name = "  anAlias  ";
        alias = new Alias( owner, protein, type1, name );
        assertEquals( name.trim(), alias.getName() );

        name = null;
        alias = new Alias( owner, protein, type1, name );
        assertNull( alias.getName() );
    }

    public void testEqualsAndHashCode() {

        final Protein protein = new TestableProtein( "EBI-xxx", owner, yeast, "proteinTest", "AAAAAAAAAAAAAAAAAAAA" );

        // names are not equals
        ObjectFactory factory = new ObjectFactory() {
            public Object createInstanceX() {
                return new Alias( owner, protein, null, "name 1" );
            }

            public Object createInstanceY() {
                return new Alias( owner, protein, null, "name 2" );
            }
        };

        // Make sure the object factory meets its contract for testing.
        // This contract is specified in the API documentation.
        Assert.assertObjectFactoryContract( factory );
        // Assert equals(Object) contract.
        Assert.assertEqualsContract( factory );
        // Assert hashCode() contract.
        Assert.assertHashCodeContract( factory );

        // alias type are not equals
        factory = new ObjectFactory() {
            public Object createInstanceX() {
                return new Alias( owner, protein, type1, "name" );
            }

            public Object createInstanceY() {
                return new Alias( owner, protein, null, "name" );
            }
        };

        Assert.assertObjectFactoryContract( factory );
        Assert.assertEqualsContract( factory );
        Assert.assertHashCodeContract( factory );
    }
}