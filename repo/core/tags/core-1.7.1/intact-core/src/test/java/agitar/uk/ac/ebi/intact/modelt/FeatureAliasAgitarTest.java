/**
 * Generated by Agitar build: Agitator Version 1.0.4.000276 (Build date: Mar 27, 2007) [1.0.4.000276]
 * JDK Version: 1.5.0_09
 *
 * Generated on 04-Apr-2007 08:28:17
 * Time to generate: 00:58.425 seconds
 *
 */

package agitar.uk.ac.ebi.intact.modelt; import uk.ac.ebi.intact.model.*;

import com.agitar.lib.junit.AgitarTestCase;

public class FeatureAliasAgitarTest extends AgitarTestCase {

    static Class TARGET_CLASS = FeatureAlias.class;

    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new FeatureAlias( new Institution( "testFeatureAliasShortLabel1" ), null, new CvAliasType( new Institution( "testFeatureAliasShortLabel" ), "testFeatureAliasShortLabel" ), "testFeatureAliasName" );
            fail( "Expected NullPointerException to be thrown" );
        } catch ( NullPointerException ex ) {
            assertNull( "ex.getMessage()", ex.getMessage() );
            assertThrownBy( Alias.class, ex );
        }
    }
}

