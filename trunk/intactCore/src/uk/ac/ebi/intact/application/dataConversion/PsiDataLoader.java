/*
* Copyright (c) 2002 The European Bioinformatics Institute, and others.
* All rights reserved. Please see the file LICENSE
* in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.cli.*;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.ControlledVocabularyRepository;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.EntrySetChecker;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntrySetTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.parser.EntrySetParser;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.persister.EntrySetPersister;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.util.BioSourceFactory;
import uk.ac.ebi.intact.util.Chrono;
import uk.ac.ebi.intact.util.UpdateProteins;
import uk.ac.ebi.intact.util.UpdateProteinsI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * That class allows to convert a PSI XML file into Intact data.
 * That processing is done in 3 steps
 * Features:
 *      - PSI XML parsing using the DOM API.
 *      -
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class PsiDataLoader {

    private static void displayUsage( Options options ) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "PsiDataLoader " +
                             "-file <filename> " +
                             "[-taxId <biosource.taxId>] " +
                             "[-useInteractionTypeWhenMissing] " +
                             "[-upToParsing] " +
                             "[-upToChecking] " +
                             "[-gui] " +
                             "[-debug] ",
                             options );
    }

    /**
     * D E M O
     * <p/>
     * -file  E:\Programs\cygwin\home\Samuel\intactCore\psi\Lehner_2003.v3.xml -useInteractionTypeWhenMissing MI:0191 -useTaxidWhenMissing 9606
     *
     * TODO:
     *      (1) show time to check and persist
     *
     * @param args
     */
    public static void main( String[] args ) {

        // create Option objects
        Option helpOpt = new Option( "help", "print this message" );

        Option filenameOpt = OptionBuilder.withArgName( "filename" )
                .hasArg()
                .withDescription( "PSI file to upload in IntAct" )
                .create( "file" );
        filenameOpt.setRequired( true );

        Option taxidOpt = OptionBuilder.withArgName( "taxid" )
                .hasArg()
                .withDescription( "TaxId to use when missing in an Interactor " +
                                  "(eg. for human:  9606)" )
                .create( "useTaxidWhenMissing" );
        taxidOpt.setRequired( false );

        Option interactionTypeOpt = OptionBuilder.withArgName( "PSI_ID" )
                .hasArg()
                .withDescription( "PSI id of the CvInteractionType to use when missing from an Interaction " +
                                  "(eg. for aggregation: MI:0191)" )
                .create( "useInteractionTypeWhenMissing" );
        interactionTypeOpt.setRequired( false );

        Option reuseProteinOpt = OptionBuilder.withDescription( "Do not force protein update, reuse existing protein" )
                .create( "reuseExistingProtein" );
        reuseProteinOpt.setRequired( false );

        Option upToParsingOpt = OptionBuilder.withDescription( "Stop processing after parsing." )
                .create( "upToParsing" );
        upToParsingOpt.setRequired( false );

        Option upToCheckingOpt = OptionBuilder.withDescription( "Stop processing after checking." )
                .create( "upToChecking" );
        upToCheckingOpt.setRequired( false );

        Option guiOpt = OptionBuilder.withDescription( "Shows Progress during processing." )
                .create( "gui" );
        guiOpt.setRequired( false );

        Option debugOpt = OptionBuilder.withDescription( "Shows debugging messages." )
                .create( "debug" );
        debugOpt.setRequired( false );

        Options options = new Options();

        options.addOption( helpOpt );
        options.addOption( filenameOpt );
        options.addOption( taxidOpt );
        options.addOption( interactionTypeOpt );
        options.addOption( reuseProteinOpt );
        options.addOption( upToParsingOpt );
        options.addOption( upToCheckingOpt );
        options.addOption( debugOpt );
        options.addOption( guiOpt );

        // create the parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args, true );
        } catch ( ParseException exp ) {
            // Oops, something went wrong

            displayUsage( options );

            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.exit( 1 );
        }


        if( line.hasOption( "help" ) ) {
            displayUsage( options );
            System.exit( 0 );
        }

        // These argument are mandatory.
        final String filename = line.getOptionValue( "file" );
        final String defaultTaxid = line.getOptionValue( "useTaxidWhenMissing" );
        final String defaultInteractionType = line.getOptionValue( "useInteractionTypeWhenMissing" );
        final boolean reuseProtein = line.hasOption( "reuseExistingProtein" );
        final boolean processUpToParsing = line.hasOption( "upToParsing" );
        final boolean processUpToChecking = line.hasOption( "upToChecking" );
        final boolean debugEnabled = line.hasOption( "debug" );
        final boolean guiEnabled = line.hasOption( "gui" );

        CommandLineOptions myOptions = CommandLineOptions.getInstance();

        if( defaultTaxid != null ) {
            myOptions.setDefaultInteractorTaxid( defaultTaxid );
        }

        if( defaultInteractionType != null ) {
            myOptions.setDefaultInteractionType( defaultInteractionType );
        }

        myOptions.setReuseExistingProtein( reuseProtein );
        myOptions.setDebugEnabled( debugEnabled ) ;
        myOptions.setGuiEnabled( guiEnabled ) ;

        System.out.println( "File: " + filename );
        System.out.println( "default taxid: " + defaultTaxid );
        System.out.println( "default InteractionType: " + defaultInteractionType );
        System.out.println( "ReuseProtein: " + reuseProtein );
        System.out.println( "Process up to parsing: " + processUpToParsing );
        System.out.println( "Process up to checking: " + processUpToChecking );
        System.out.println( "Debug mode: " + ( debugEnabled ? "enabled" : "disabled" ) );
        System.out.println( "GUI mode: " + ( guiEnabled ? "enabled" : "disabled" ) );

        System.out.println( "Uploading file: " + filename );

        IntactHelper helper = null;


        //////////////////////////////////
        // (1) Parse the given PSI file
        //////////////////////////////////

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final MessageHolder messages = MessageHolder.getInstance();

        try {
            System.out.print( "\nStep 1: Parsing and extracting relevant data from your PSI file..." );
            final Chrono chrono = new Chrono();
            chrono.start();

            // Parse the PSI file
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse( new File( filename ) );
            final Element rootElement = document.getDocumentElement();

            final EntrySetParser entrySetParser = new EntrySetParser();
            EntrySetTag entrySet = null;
            try {
                entrySet = entrySetParser.process( rootElement );
            } catch ( RuntimeException re ) {
                if( messages.parserMessageExists() ) {
                    System.err.println( "Before to exit, here are the messages creating during the parsing:" );
                    messages.printParserReport( System.err );
                    System.err.println( "\nPlease fix your file and try again." );
                    System.err.println( "abort." );
                }
                re.printStackTrace();
            }

            chrono.stop();
            System.out.println( "done (took " + chrono + ")" );

            if( messages.parserMessageExists() ) {
                // display parser message.
                messages.printParserReport( System.err );
                System.err.println( "\nPlease fix your file and try again." );
                System.err.println( "abort." );
                System.exit( 1 );
            } else {

                if( debugEnabled ) {
                    System.out.println( "Data collected from you file:" );
                    System.out.println( entrySet );
                }

                System.out.println( "\n\nStep 2: Checking that all the data found in your PSI file are available " +
                                    "(Proteins, BioSources, CVs...)" );


                /////////////////////////////////////////////
                // (2a) Get needed objects before checking
                /////////////////////////////////////////////
                try {
                    System.out.println( "Creating the IntactHelper..." );
                    System.out.flush();
                    helper = new IntactHelper();

                    try {
                        String db = helper.getDbName();
                        System.out.println( "Database: " + db );
                        System.out.println( "User:     " + helper.getDbUserName() );

                        // THIS IS SPECIFIC CODE THAT AVOIDS TO WRITE ON A PRODUCTION DATABASE BY MISTAKES
                        if( "d002".equals( db.toLowerCase() ) || "zpro".equals( db.toLowerCase() ) ) {
                            System.out.println( "You are about to write data in a produciton environment, " +
                                                "do you really want to proceed:" );
                            int ch;
                            System.out.print( "[yes/no] >" );
                            System.out.flush();
                            StringBuffer sb = new StringBuffer();
                            while ( ( ch = System.in.read() ) != 10 ) {
                                sb.append( (char) ch );
                            }

                            String input = sb.toString();

                            if( !"yes".equals( input.toLowerCase() ) ) {
                                System.out.println( "Abort procedure." );
                                System.exit( 0 );
                            }
                        }
                    } catch ( LookupException e ) {
                        e.printStackTrace();
                    } catch ( SQLException e ) {
                        e.printStackTrace();
                    }

                    System.out.println( "done" );
                } catch ( IntactException e ) {
                    System.out.println( "error" );
                    System.err.println( "Could not create an IntactHelper" );
                    e.printStackTrace();
                    System.exit( 1 );
                }

                // create the protien factory
                UpdateProteinsI proteinFactory = null;
                try {
                    System.out.print( "Creating the protein factory (UpdateProteins)..." );
                    System.out.flush();
                    proteinFactory = new UpdateProteins( helper );
                    System.out.println( "done" );

                } catch ( UpdateProteinsI.UpdateException e ) {
                    System.out.println( "error" );
                    System.out.println( "Could not create proteinFactory (UpdateProteins)." );
                    e.printStackTrace();
                    System.exit( 1 );
                }

                // create BioSourceFactory
                BioSourceFactory bioSourceFactory = null;
                try {
                    System.out.print( "Creating the organism factory (BioSourceFactory)..." );
                    System.out.flush();
                    bioSourceFactory = new BioSourceFactory( helper );
                    System.out.println( "done" );
                } catch ( IntactException e ) {
                    System.out.println( "error" );
                    System.out.println( "Could not create BioSourceFactory." );
                    e.printStackTrace();
                    System.exit( 1 );
                }

                if( processUpToParsing ) {
                    System.out.println( "Abort requested (after parsing) by the user from the command line." );
                    System.exit( 0 );
                }


                ///////////////////////////////////////////
                // (2b) Check the content of the entrySet
                ///////////////////////////////////////////
                // get some necessary objects
                ControlledVocabularyRepository.check( helper );

                // check the parsed model
                EntrySetChecker.check( entrySet, helper, proteinFactory, bioSourceFactory );

                if( messages.checkerMessageExists() ) {

                    // display checker messages.
                    MessageHolder.getInstance().printCheckerReport( System.err );
                    System.err.println( "abort." );
                    System.exit( 1 );
                } else {

                    if( processUpToChecking ) {
                        System.out.println( "Abort requested (after checking) by the user from the command line." );
                        System.exit( 0 );
                    }

                    System.out.println( "\n\nStep 3: the data given are valid, make them persitent in your IntAct node." );


                    ////////////////////////////////////////////////
                    // (3) persist the data in the IntAct Database
                    ////////////////////////////////////////////////
                    try {
                        EntrySetPersister.persist( entrySet, helper );

                        if( messages.checkerMessageExists() ) {

                            // display persister messages.
                            MessageHolder.getInstance().printPersisterReport( System.err );
                            System.err.println( "abort." );
                            System.exit( 1 );
                        } else {
                            System.out.println( "The data have been successfully saved in your Intact node." );
                        }

                    } catch ( IntactException e ) {
                        e.printStackTrace();
                        System.exit( 1 );
                    }

                } // Persister
            } // Checker

        } catch ( SAXException sxe ) {
            // Error generated during parsing.
            Exception x = sxe;
            if( sxe.getException() != null ) {
                x = sxe.getException();
            }
            x.printStackTrace();
            System.exit( 1 );

        } catch ( ParserConfigurationException pce ) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            System.exit( 1 );

        } catch ( IOException ioe ) {
            // I/O error
            ioe.printStackTrace();
            System.exit( 1 );

        } finally {
            if( helper != null ) {
                try {
                    helper.closeStore();
                } catch ( IntactException e ) {

                    System.err.println( "Could not close the IntactHelper dataabse connexion." );
                    e.printStackTrace();
                }
            }
        }

        System.exit( 0 );
    }
}
