/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.mocks.cvexperimentalroles;

import uk.ac.ebi.intact.mocks.CvObjectMock;
import uk.ac.ebi.intact.mocks.IntactObjectSetter;
import uk.ac.ebi.intact.mocks.XrefMock;
import uk.ac.ebi.intact.mocks.cvDatabases.PsiMiMock;
import uk.ac.ebi.intact.mocks.cvXrefQualifiers.IdentityMock;
import uk.ac.ebi.intact.model.CvComponentRole;
import uk.ac.ebi.intact.model.CvObjectXref;

/**
 * TODO comment this
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 * @since TODO
 */
public class FluorophoreAcceptorMock {
    public static CvComponentRole getMock(){
        CvComponentRole fluorophoreAcceptor = CvObjectMock.getMock(CvComponentRole.class,CvComponentRole.FLUROPHORE_ACCEPTOR, CvComponentRole.FLUROPHORE_ACCEPTOR);

        fluorophoreAcceptor = (CvComponentRole) IntactObjectSetter.setBasicObject(fluorophoreAcceptor);

        CvObjectXref xref = XrefMock.getMock(CvObjectXref.class, PsiMiMock.getMock(), IdentityMock.getMock(),CvComponentRole.FLUROPHORE_ACCEPTOR_MI_REF);
        fluorophoreAcceptor.addXref(xref);

        return fluorophoreAcceptor;
    }
}