/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.imex.imp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Provider;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.meta.ImexObject;
import uk.ac.ebi.intact.model.meta.ImexObjectStatus;
import uk.ac.ebi.intact.persistence.dao.ImexObjectDao;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports from the IntAct-IMEx repository into the database
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:ImexImporter.java 9285 2007-08-02 10:33:06Z baranda $
 */
public class ImexImporter {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ImexImporter.class);

    private Repository repository;
    private Map<String,Institution> institutions;

    public ImexImporter(Repository repository) {
        this.repository = repository;

        institutions = new HashMap<String,Institution>();
    }

    public ImportReport importNewAndFailed() {
        ImportReport report = importFailed();

        ImexObjectDao imexObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getImexObjectDao();

        beginTransaction();
        List<String> pmidsOk = imexObjectDao.getAllOkPmids();
        commitTransaction();

        List<RepoEntry> newRepoEntries = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService().findImportableExcluding(pmidsOk);

        for (RepoEntry repoEntry : newRepoEntries) {
            ImexObject imexObject = new ImexObject(getInstitution(repoEntry.getRepoEntrySet().getProvider()), repoEntry.getPmid(), ImexObjectStatus.OK);
            importRepoEntry(repoEntry, report, imexObject);

            if (imexObject.getStatus().equals(ImexObjectStatus.OK)) {
                report.getNewPmisImported().add(repoEntry.getPmid());
            }

            beginTransaction();
            imexObjectDao.persist(imexObject);
            commitTransaction();
        }

        return report;
    }

    /**
     * Tries to re-import from the Repository those PMIDs that have failed in previous imports
     *
     * @return a report of the import
     */
    public ImportReport importFailed() {
        ImexObjectDao imexObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getImexObjectDao();

        beginTransaction();
        List<ImexObject> failedImexObjects = imexObjectDao.getFailed();
        commitTransaction();

        if (log.isDebugEnabled()) {
            log.debug("Found " + failedImexObjects.size() + " pubmed IDs that have failed previously. Will try to import them.");
        }

        ImportReport report = new ImportReport();

        for (ImexObject imexObject : failedImexObjects) {
            String pmid = imexObject.getPmid();

            if (log.isInfoEnabled()) log.info("Importing (previously failed) PMID: " + pmid);

            RepoEntry repoEntry;
            try {
                repoEntry = repository.findRepoEntryByPmid(pmid);
            } catch (NoResultException e) {
                if (log.isErrorEnabled()) log.error("Entry with pmid '" + pmid + "' not found in the Repository");
                report.getPmidsNotFoundInRepo().add(pmid);
                continue;
            }

            importRepoEntry(repoEntry, report, imexObject);

            beginTransaction();
            imexObjectDao.merge(imexObject);
            commitTransaction();
        }

        return report;
    }

    protected void importRepoEntry(RepoEntry repoEntry, ImportReport report, ImexObject imexObject) {
        if (report == null) {
            throw new NullPointerException("An ImportReport instance is needed");
        }

        if (imexObject == null) {
            throw new NullPointerException("An ImexObject instance is needed");
        }

        RepositoryHelper helper = new RepositoryHelper(repository);
        File entryFile = helper.getEntryFile(repoEntry);

        imexObject.setOriginalFilename(entryFile.getName());

        final String pmid = repoEntry.getPmid();

        try {
            PsiExchange.importIntoIntact(new FileInputStream(entryFile), false);
            report.getSucessfullPmids().add(pmid);

            imexObject.setStatus(ImexObjectStatus.OK);

        } catch (Exception e) {
            if (log.isErrorEnabled()) log.error("Entry with pmid '" + pmid + "' failed to be imported");
            report.getFailedPmids().put(pmid, e);
            imexObject.setStatus(ImexObjectStatus.ERROR);
        }
    }

    protected Institution getInstitution(Provider provider) {
        final String providerName = provider.getName();

        if (institutions.containsKey(providerName)) {
            return institutions.get(providerName);
        }
        beginTransaction();

        Institution institution = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInstitutionDao().getByShortLabel(providerName);

        commitTransaction();

        if (institution != null) {
            institutions.put(providerName, institution);
        } else {
            throw new RuntimeException("Institution not found for provider: "+ providerName);
        }

        return institution;
    }

    private void beginTransaction() {
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    private void commitTransaction() {
        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            throw new ImportException(e);
        }
    }
}