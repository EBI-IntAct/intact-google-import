/**
 * Copyright 2006 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.unitdataset.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.dbunit.database.*;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.commons.util.TestDataset;
import uk.ac.ebi.intact.commons.util.TestDatasetProvider;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.plugin.IntactHibernateMojo;
import uk.ac.ebi.intact.plugin.MojoUtils;
import uk.ac.ebi.intact.plugin.cv.obo.OboImportMojo;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Generates a DBUnit-dataset from a set of PSI 2.5 xml files
 *
 * @goal dataset
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class UnitDatasetGeneratorMojo
        extends IntactHibernateMojo {

    private static final String PROVIDER_TEMPLATE = "PsiUnitDatasetProvider.vm";

    /**
     * Project instance
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * project-helper instance, used to make addition of resources
     * simpler.
     *
     * @component
     */
    private MavenProjectHelper helper;

    /**
     * Dataset information
     *
     * @parameter
     * @required
     */
    private List<Dataset> datasets;

    /**
     * Cv configuration
     *
     * @parameter
     */
    private CvConfiguration cvConfiguration;

    /**
     * @parameter default-value="uk.ac.ebi.intact.unitdataset"
     * @required
     */
    private String generatedPackage = "uk.ac.ebi.intact.unitdataset";

    /**
     * @parameter
     * @required
     */
    private String providerName;

    /**
     * Main execution method, which is called after hibernate has been initialized
     */
    protected void executeIntactMojo() throws MojoExecutionException, MojoFailureException, IOException {

        getLog().info("Executing DBUnit dataset generator");

        if (datasets.isEmpty()) {
            throw new MojoFailureException("No datasets to import");
        }

        getLog().debug("Datasets to import ("+datasets.size()+"):");   
        for (Dataset dataset : datasets) {
            getLog().debug("\tProcessing dataset: "+dataset.getId());

            processDataset(dataset);
        }

        getLog().debug("Creating enum class");
        try
        {
            generateDatasetEnum();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Problem generating enum class", e);
        }

        // add the resources into the classpath
        List includes = Collections.singletonList("**/*.xml");
        List excludes = null;
        helper.addResource(project, getGeneratedResourcesDir().toString(), includes, excludes);
        project.addCompileSourceRoot(getGeneratedResourcesDir().toString());
    }

    public boolean idIsInvalid(String id) {
        return (id.contains(" ")
                || id.contains(",")
                || id.contains("."));
    }

    public void processDataset(Dataset dataset) throws MojoExecutionException, MojoFailureException {
        IntactContext context = IntactContext.getCurrentInstance();

        if (idIsInvalid(dataset.getId())) {
            throw new MojoExecutionException("Dataset with invalid id (it must not contain spaces or punctuation - except underscore): "+dataset.getId());
        }

        if (dataset.isContainsAllCVs()) {
            beginTransaction();

             if (cvConfiguration != null) {
                getLog().debug("\tImporting CVs from OBO: "+cvConfiguration.getOboUrl());

                URL oboUrl = cvConfiguration.getOboUrl();
                URL additionalUrl = cvConfiguration.getAdditionalUrl();
                URL additionalAnnotationsUrl = cvConfiguration.getAdditionalAnnotationsUrl();

                 File oboFile = urlToFile(oboUrl);
                 checkFile(oboFile);

                OboImportMojo oboImportMojo = new OboImportMojo(project);
                oboImportMojo.setImportedOboFile(oboFile);

                if (additionalUrl != null) {
                    File additionalFile = urlToFile(additionalUrl);
                    oboImportMojo.setAdditionalCsvFile(additionalFile);
                }

                if (additionalAnnotationsUrl != null) {
                    File additionalAnnotationsFile = urlToFile(additionalAnnotationsUrl);
                    oboImportMojo.setAdditionalAnnotationsCsvFile(additionalAnnotationsFile);
                }

                 try {
                     oboImportMojo.executeIntactMojo();
                 } catch (IOException e) {
                     throw new MojoExecutionException("Problems importing CVs", e);
                 }

                 commitTransaction();


            } else {
                getLog().info("No CV configuration found. CVs won't be imported");
            }

            beginTransaction();
            getLog().debug("\t\tImported "+context.getDataContext().getDaoFactory().getCvObjectDao().countAll()+" CVs");
            commitTransaction();
        } else {
            getLog().debug("\tNot importing all CVs");
        }

        if (dataset.getFiles() != null) {
            getLog().debug("\tStarting to import dataset files...");

            try {
                beginTransaction();
                importDataset(dataset);
                commitTransaction();
            } catch (Exception e) {
                getLog().error(e);
                throw new MojoExecutionException("Exception importing dataset: "+dataset.getId(),e);
            }

            beginTransaction();
            getLog().debug("\t\tImported "+context.getDataContext().getDaoFactory().getInteractionDao().countAll()+" Interactions in "+
                    context.getDataContext().getDaoFactory().getExperimentDao().countAll() + " Experiments");

            commitTransaction();

        } else {
            getLog().debug("\tNo dataset files to import");
        }


        // create the dbunit dataset.xml
        getLog().debug("\tCreating DBUnit dataset...");

        try {
            beginTransaction();
            IDataSet dbUnitDataSet = createDbUnitForDataset(dataset);
            exportDbUnitDataSetToFile(dbUnitDataSet, getDbUnitFileForDataset(dataset));
            commitTransaction();

            // truncate tables after export, so next datasets have a clean db
            resetSchema();

        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Exception creating dbUnit dataset", e);
        }
    }

    private static File urlToFile(URL url) throws  MojoExecutionException {
        File tempFile = null;
        try {
            InputStream is = url.openStream();
            tempFile = File.createTempFile("intact-", ".obo");
            tempFile.deleteOnExit();

            // Obtain a channel
            WritableByteChannel channel = new FileOutputStream(tempFile).getChannel();

            // Create a direct ByteBuffer;
            // see also e158 Creating a ByteBuffer
            ByteBuffer buf = ByteBuffer.allocateDirect(10);

            byte[] bytes = new byte[1024];
            int count = 0;
            int index = 0;

            // Continue writing bytes until there are no more
            while (count >= 0) {
                if (index == count) {
                    count = is.read(bytes);
                    index = 0;
                }
                // Fill ByteBuffer
                while (index < count && buf.hasRemaining()) {
                    buf.put(bytes[index++]);
                }

                // Set the limit to the current position and the position to 0
                // making the new bytes visible for write()
                buf.flip();

                // Write the bytes to the channel
                int numWritten = channel.write(buf);

                // Check if all bytes were written
                if (buf.hasRemaining()) {
                    // If not all bytes were written, move the unwritten bytes
                    // to the beginning and set position just after the last
                    // unwritten byte; also set limit to the capacity
                    buf.compact();
                } else {
                    // Set the position to 0 and the limit to capacity
                    buf.clear();
                }
            }

            // Close the file
            channel.close();
        }
        catch (IOException e) {
            throw new MojoExecutionException("Exception copying from the URL to a temporary dir", e);
        }

        return tempFile;
    }

    public IDataSet createDbUnitForDataset(Dataset dataset) throws SQLException, DataSetException {
        Connection con = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();

        IDatabaseConnection conn = getDatabaseConnection();
        ITableFilter filter = new DatabaseSequenceFilter(conn);

        ResultSet tables = con.getMetaData().getTables(null, null, "IA_%", new String[]{"TABLE"});
        QueryDataSet allTablesDataSet = new QueryDataSet(conn);
        while (tables.next())
        {
            String tableName = tables.getString(3);
            allTablesDataSet.addTable(tableName);
        }

        return new FilteredDataSet(filter, allTablesDataSet);
    }

    public IDatabaseConnection getDatabaseConnection() {
        IntactContext context = IntactContext.getCurrentInstance();

        Connection con = context.getDataContext().getDaoFactory().connection();
        IDatabaseConnection connection = new DatabaseConnection(con);

        DatabaseConfig config = connection.getConfig();
        config.setProperty( DatabaseConfig.PROPERTY_DATATYPE_FACTORY,new HsqldbDataTypeFactory() );

        return connection;
    }

    public void exportDbUnitDataSetToFile(IDataSet dataset, File file) throws IOException, DataSetException {
        FileOutputStream fos = new FileOutputStream(file);
        FlatXmlDataSet.write( dataset, fos);
        fos.close();
    }

    public void resetSchema() throws MojoExecutionException {
        try
        {
            IntactUnit iu = new IntactUnit();
            iu.resetSchema();
        }
        catch (IntactTransactionException e)
        {
            throw new MojoExecutionException("Problem resetting schema: "+e.getMessage(), e);
        }
    }

    public void generateDatasetEnum() throws Exception {
        // create velocity context
        VelocityContext context = new VelocityContext();
        context.put("mojo", this);
        context.put("artifactId", project.getArtifactId());
        context.put("version", project.getVersion());
        context.put("classSimpleName", providerName);
        context.put("datasetInterfaceName", TestDataset.class.getName());
        context.put("providerInterfaceName", TestDatasetProvider.class.getName());
        context.put("datasets", datasets);

        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class." + VelocityEngine.RESOURCE_LOADER + ".class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        Velocity.init(props);

        Template template = Velocity.getTemplate(PROVIDER_TEMPLATE);

        // write the resulting file with velocity
        Writer writer = new FileWriter(getGeneratedEnumFile());
        template.merge(context, writer);
        writer.close();
    }

    private void commitTransactionAndBegin() throws MojoExecutionException {
        commitTransaction();
        beginTransaction();
    }

    private void beginTransaction() throws MojoExecutionException {
        IntactContext context = IntactContext.getCurrentInstance();
        context.getDataContext().beginTransaction();
    }

    private void commitTransaction() throws MojoExecutionException {
        IntactContext context = IntactContext.getCurrentInstance();
         try {
                context.getDataContext().commitTransaction();
            } catch (IntactTransactionException e) {
                throw new MojoExecutionException("Problem committing the transaction", e);
            }
    }

    private void importDataset(Dataset dataset) throws FileNotFoundException, PersisterException, MojoExecutionException {
        for (File psiFile : dataset.getFiles()) {
            checkFile(psiFile);
            beginTransaction();
            
            PsiExchange.importIntoIntact(new FileInputStream(psiFile), false);

            commitTransactionAndBegin();
        }
    }

    private void checkFile(File file) throws MojoExecutionException {
        if (!file.exists()) {
            throw new MojoExecutionException("File does not exist: "+file);
        }

        if (file.isDirectory()) {
            throw new MojoExecutionException("File is a directory: "+file);
        }
    }

    private File getDbUnitFileForDataset(Dataset dataset) throws IOException {
        File file = new File(getGeneratePackageFile(), dataset.getId()+".xml");
        MojoUtils.prepareFile(file);
        return file;
    }

    private File getGeneratedResourcesDir() {
        return new File(project.getBuild().getDirectory(), "datasets/");
    }

    private File getGeneratePackageFile() throws IOException {
        String strFile = getGeneratedPackage().replaceAll("\\.", "/");
        File file = new File(getGeneratedResourcesDir(), strFile+"/");
        MojoUtils.prepareFile(file);
        return file;
    }

    private File getGeneratedEnumFile() throws IOException {
        File file = new File(getGeneratePackageFile(), providerName+".java");
        MojoUtils.prepareFile(file);
        return file;
    }

    /**
     * Implementation of abstract method from superclass
     */
    public MavenProject getProject() {
        return project;
    }

    public String getGeneratedPackage()
    {
        return generatedPackage;
    }

    public List<Dataset> getDatasets()
    {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets)
    {
        this.datasets = datasets;
    }

    public CvConfiguration getCvConfiguration()
    {
        return cvConfiguration;
    }

    public void setCvConfiguration(CvConfiguration cvConfiguration)
    {
        this.cvConfiguration = cvConfiguration;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }
}
