/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.sanity.rules.experiment;

import uk.ac.ebi.intact.util.sanity.rules.Rule;
import uk.ac.ebi.intact.util.sanity.rules.util.MethodArgumentValidator;
import uk.ac.ebi.intact.util.sanity.rules.util.CommonMethods;
import uk.ac.ebi.intact.util.sanity.rules.messages.GeneralMessage;
import uk.ac.ebi.intact.util.sanity.exception.SanityCheckerException;
import uk.ac.ebi.intact.util.sanity.annotation.SanityRule;
import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.model.Experiment;

import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO comment this
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 * @since TODO
 */

@SanityRule(target = Experiment.class)

public class OnHoldExperiment implements Rule {
    private static final String DESCRIPTION = "This/those experiments are on hold";
    private static final String SUGGESTION = "";

    public Collection<GeneralMessage> check(IntactObject intactObject) throws SanityCheckerException {
        MethodArgumentValidator.isValidArgument(intactObject, Experiment.class);
        Collection<GeneralMessage> messages = new ArrayList<GeneralMessage>();
        Experiment experiment = (Experiment) intactObject;
        if(CommonMethods.isOnHold(experiment)){
            messages.add(new GeneralMessage(DESCRIPTION, GeneralMessage.AVERAGE_LEVEL,SUGGESTION,experiment));
        }
        return messages;
    }


    public static String getDescription() {
        return DESCRIPTION;
    }

    public static String getSuggestion() {
        return SUGGESTION;
    }
}