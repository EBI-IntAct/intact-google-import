/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.cvutils;

import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.DataContext;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfo;
import uk.ac.ebi.intact.dataexchange.cvutils.model.AnnotationInfoDataset;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Service allowing to retreive annotation info dataset from the intact databse.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class AnnotationInfoDatasetService {

    public AnnotationInfoDatasetService() {
    }

    /**
     * Extract from the IntAct database all CvObjects having at least one annotation having one of the given CvTopic.
     *
     * @param topics topic filter.
     * @return a non null dataset.
     * @throws AnnotationInfoDatasetServiceException
     *
     */
    public AnnotationInfoDataset retrieveAnnotationInfoDataset( Collection<CvTopic> topics ) throws AnnotationInfoDatasetServiceException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final boolean inTransaction = dataContext.isTransactionActive();

        if ( !inTransaction ) dataContext.beginTransaction();

        AnnotationInfoDataset dataset = new AnnotationInfoDataset();

        DaoFactory daof = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        final CvObjectDao<CvObject> cvDao = daof.getCvObjectDao();

        final int termCount = cvDao.countAll();
        final int batchSize = 50;
        int currentIdx = 0;

        do {
            final List<CvObject> terms = cvDao.getAll( currentIdx, batchSize );

            for ( CvObject cvObject : terms ) {
                final Collection<Annotation> annotations = AnnotatedObjectUtils.findAnnotationsByCvTopic( cvObject, topics );
                for ( Annotation annotation : annotations ) {

                    AnnotationInfo ai = new AnnotationInfo( cvObject.getShortLabel(),
                                                            cvObject.getFullName(),
                                                            CgLibUtil.getDisplayableClassName( cvObject.getClass() ),
                                                            cvObject.getMiIdentifier(),
                                                            annotation.getCvTopic().getShortLabel(),
                                                            annotation.getAnnotationText(),
                                                            false );
                    dataset.addCvAnnotation( ai );
                } // annotations
            }

            currentIdx += terms.size();

        } while ( currentIdx < termCount - 1 );

        try {
            if ( !inTransaction ) dataContext.commitTransaction();
        } catch ( IntactTransactionException e ) {
            throw new AnnotationInfoDatasetServiceException( e );
        }

        return dataset;
    }
}
