/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.persistence.dao.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteractionType;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test for <code>CvObjectDaoImplTest</code>
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 10/10/2006
 */
public class CvObjectDaoImplTest extends TestCase
{
    public CvObjectDaoImplTest(String name)
    {
        super(name);
    }

    private CvObjectDao<CvObject> dao;

    public void setUp() throws Exception
    {
        super.setUp();
        dao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
        dao = null;
    }

    public void testGetByXref_andDagObjectChildren() throws Exception
    {
        CvInteractionType cvInteractionType =
                (CvInteractionType) dao.getByXref(CvInteractionType.DIRECT_INTERACTION_MI_REF);

        assertNotNull(cvInteractionType);
        assertFalse(cvInteractionType.getChildren().isEmpty());
    }

    public void testGetByPsiMiRefCollection() throws Exception
    {
        List<CvObject> results = dao.getByPsiMiRefCollection(Arrays.asList(new String[] {CvTopic.COMMENT_MI_REF}));

        assertFalse(results.isEmpty());
    }

    public void testGetByObjClass() throws Exception
    {
        List<CvObject> results = dao.getByObjClass(new Class[] { CvDatabase.class, CvTopic.class });

        Set<String> set = new HashSet<String>(2);

        for (CvObject cvObject : results)
        {
            set.add(cvObject.getObjClass());
        }

        assertEquals(2, set.size());
    }

    public static Test suite()
    {
        return new TestSuite(CvObjectDaoImplTest.class);
    }
}
