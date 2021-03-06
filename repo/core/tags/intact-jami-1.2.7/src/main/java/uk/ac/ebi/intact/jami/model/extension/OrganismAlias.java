package uk.ac.ebi.intact.jami.model.extension;

import psidev.psi.mi.jami.model.CvTerm;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Implementation of alias for organism
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/01/14</pre>
 */
@Entity
@Table( name = "ia_biosource_alias" )
public class OrganismAlias extends AbstractIntactAlias{

    protected OrganismAlias() {
    }

    public OrganismAlias(CvTerm type, String name) {
        super(type, name);
    }

    public OrganismAlias(String name) {
        super(name);
    }
}
