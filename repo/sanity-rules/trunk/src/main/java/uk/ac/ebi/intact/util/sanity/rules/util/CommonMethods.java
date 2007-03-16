/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.sanity.rules.util;

import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.AnnotatedObject;

import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 * @since TODO
 */
public class CommonMethods {

    public static boolean isOnHold(AnnotatedObject annotatedObject){
        boolean isOnHold = false;
        Collection<Annotation> annotations = annotatedObject.getAnnotations();
        for(Annotation annotation : annotations){
            if(CvTopic.ON_HOLD.equals(annotation.getCvTopic().getShortLabel())){
                isOnHold = true;
                break;
            }
        }
        return isOnHold;
    }

     public static boolean isToBeReviewed(AnnotatedObject annotatedObject){
        Collection<Annotation> annotations = annotatedObject.getAnnotations();
        for(Annotation annotation : annotations){
            if(CvTopic.TO_BE_REVIEWED.equals(annotation.getCvTopic().getShortLabel())){
                return true;
            }
        }
        return false;
    }

    public static boolean isAccepted(AnnotatedObject annotatedObject){
        Collection<Annotation> annotations = annotatedObject.getAnnotations();
        for(Annotation annotation : annotations){
            if(CvTopic.ACCEPTED.equals(annotation.getCvTopic().getShortLabel())){
                return true;
            }
        }
        return false;
    }
}