/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.biosource;

//import org.springframework.beans.factory.BeanFactory;
//import org.springframework.beans.factory.xml.XmlBeanFactory;
//import org.springframework.core.io.ClassPathResource;

import uk.ac.ebi.intact.util.taxonomy.TaxonomyService;

/**
 * Factory building BioSourceServices.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class BioSourceServiceFactory {

    public static final String SPRING_CONFIG_FILE = "/taxonomy-service.xml";

    /////////////////////////////
    // Singleton

    private static BioSourceServiceFactory ourInstance = new BioSourceServiceFactory();

    public static BioSourceServiceFactory getInstance() {
        return ourInstance;
    }

    private BioSourceServiceFactory() {
    }

    ////////////////////////////
    // Instance methods

    public TaxonomyService buildBioSourceService() {

        // TODO why is Spring not available in the Classpath ????

//        ClassPathResource resource = new ClassPathResource( SPRING_CONFIG_FILE );
//        BeanFactory factory = new XmlBeanFactory( resource );
//        TaxonomyService service = ( TaxonomyService ) factory.getBean( "proteinLoaderBean" );
//        return service;
        throw new UnsupportedOperationException( "Spring Bean not configured yet." );
    }

    public BioSourceService buildBioSourceService( TaxonomyService taxonomyService ) {
        if ( taxonomyService == null ) {
            throw new IllegalArgumentException( "You must give a non null TaxonomyBridgeAdapter." );
        }
        return new BioSourceServiceImpl( taxonomyService );
    }
}
