/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.gui.Monitor;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ExperimentDescriptionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinInteractorTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.business.IntactHelper;
import uk.ac.ebi.intact.util.BioSourceFactory;
import uk.ac.ebi.intact.util.UpdateProteinsI;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * That class .
 * 
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class EntryChecker {

    public static void check( final EntryTag entry,
                              final IntactHelper helper,
                              final UpdateProteinsI proteinFactory,
                              final BioSourceFactory bioSourceFactory ) {

        Collection keys;
        final Map experimentDescriptions = entry.getExperimentDescriptions();
        final Map proteinInteractors = entry.getProteinInteractors();
        final Collection interactions = entry.getInteractions();

        keys = experimentDescriptions.keySet();
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            final String key = (String) iterator.next();
            final ExperimentDescriptionTag experimentDescription =
                    (ExperimentDescriptionTag) experimentDescriptions.get( key );
            ExperimentDescriptionChecker.check( experimentDescription, helper, bioSourceFactory );
        }

        // According to the object model this should not be needed, that test will be done at the interaction level.
        keys = proteinInteractors.keySet();
        boolean guiEnabled = CommandLineOptions.getInstance().isGuiEnabled();
        Monitor monitor = null;
        if( guiEnabled ) {
            monitor = new Monitor( keys.size(), "Protein update" );
            monitor.show();
        }
        int current = 0;
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            final String key = (String) iterator.next();
            final ProteinInteractorTag proteinInteractor = (ProteinInteractorTag) proteinInteractors.get( key );
            ProteinInteractorChecker.check( proteinInteractor, helper, proteinFactory, bioSourceFactory );
            if( guiEnabled ) {
                monitor.updateProteinProcessedCound( ++current );
            }
        }
        if( guiEnabled ) {
            monitor.hide();
        }


        for ( Iterator iterator = interactions.iterator(); iterator.hasNext(); ) {
            final InteractionTag interaction = (InteractionTag) iterator.next();
            InteractionChecker.check( interaction, helper, proteinFactory, bioSourceFactory );
        }
    }
}
