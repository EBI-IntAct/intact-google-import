/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.protein.mock;

import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.service.AbstractUniprotService;

import java.util.*;

/**
 * Mock service serving mock proteins.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.1.2
 */
public class MockUniprotService extends AbstractUniprotService {


    private static Map<String, Collection<UniprotProtein>> proteins = new HashMap<String, Collection<UniprotProtein>>( );

    static {

        // This static block initialize the mock proteins and how they can be searched on (ACs and variant ACs).

        UniprotProtein cdc42canfa = MockUniprotProtein.build_CDC42_CANFA();
        UniprotProtein cdc42human = MockUniprotProtein.build_CDC42_HUMAN();

        proteins.put( "P60952", Arrays.asList( cdc42canfa ) );
        proteins.put( "P60952-1", Arrays.asList( cdc42canfa ) );
        proteins.put( "P60952-2", Arrays.asList( cdc42canfa ) );

        proteins.put( "P60953", Arrays.asList( cdc42human ) );
        proteins.put( "Q7L8R5", Arrays.asList( cdc42human ) ); /* (!) secondary AC not shared by CANFA */
        proteins.put( "P60953-1", Arrays.asList( cdc42human ) );
        proteins.put( "P60953-2", Arrays.asList( cdc42human ) );

        proteins.put( "P21181", Arrays.asList( cdc42canfa, cdc42human ) );
        proteins.put( "P25763", Arrays.asList( cdc42canfa, cdc42human ) );
        proteins.put( "P21181-1", Arrays.asList( cdc42canfa, cdc42human ) );
        proteins.put( "P21181-4", Arrays.asList( cdc42canfa, cdc42human ) );
    }



    ////////////////////////////
    // AbstractUniprotService

    public Collection<UniprotProtein> retreive( String ac ) {
        Collection<UniprotProtein> myProteins = new ArrayList<UniprotProtein>( 2 );
        myProteins.addAll( proteins.get( ac ) );
        return myProteins;
    }

    public Map<String, Collection<UniprotProtein>> retreive( Collection<String> acs ) {
        Map<String, Collection<UniprotProtein>> results = new HashMap<String, Collection<UniprotProtein>>( acs.size() );
        for ( String ac : acs ) {
            results.put( ac, retreive( ac ) );
        }
        return results;
    }
}