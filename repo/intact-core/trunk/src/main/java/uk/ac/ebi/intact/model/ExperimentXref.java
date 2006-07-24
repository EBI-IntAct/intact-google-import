/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21-Jul-2006</pre>
 */
@Entity
@DiscriminatorValue("uk.ac.ebi.intact.model.Experiment")
public class ExperimentXref extends Xref
{

    private static final Log log = LogFactory.getLog(ExperimentXref.class);


    public ExperimentXref()
    {
    }

    public ExperimentXref(Institution anOwner, CvDatabase aDatabase, String aPrimaryId, String aSecondaryId, String aDatabaseRelease, CvXrefQualifier aCvXrefQualifier)
    {
        super(anOwner, aDatabase, aPrimaryId, aSecondaryId, aDatabaseRelease, aCvXrefQualifier);
    }

    public ExperimentXref(Institution anOwner, CvDatabase aDatabase, String aPrimaryId, CvXrefQualifier aCvXrefQualifier)
    {
        super(anOwner, aDatabase, aPrimaryId, aCvXrefQualifier);
    }
    /*
    @ManyToOne(targetEntity = Experiment.class)
    @JoinColumn(name = "parent_ac", updatable = false, insertable = false)
    public AnnotatedObject getParent()
    {
        return super.getParent();
    }
    */
}
