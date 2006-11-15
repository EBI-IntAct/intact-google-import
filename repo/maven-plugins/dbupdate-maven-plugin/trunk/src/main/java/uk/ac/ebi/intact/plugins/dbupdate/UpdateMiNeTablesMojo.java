/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.plugins.dbupdate;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import uk.ac.ebi.intact.plugin.IntactHibernateMojo;
import uk.ac.ebi.intact.plugin.MojoUtils;
import uk.ac.ebi.intact.dbutil.mine.MineDatabaseFill;
import uk.ac.ebi.intact.dbutil.mine.MineDatabaseFillReport;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;

/**
 * Example mojo. This mojo is executed when the goal "mine" is called.
 *
 * @goal mine
 * @phase process-resources
 */
public class UpdateMiNeTablesMojo extends IntactHibernateMojo {

    /**
     * @parameter default-value="${project.build.directory}/MiNe-ecluded-interactors.txt"
     * @required
     */
    private File nullTaxIdFile;

    /**
     * Main execution method, which is called after hibernate has been initialized
     */
    public void executeIntactMojo() throws MojoExecutionException, MojoFailureException, IOException {
        PrintStream ps = new PrintStream(getOutputFile());

        MineDatabaseFillReport report = null;

        try {
            report = MineDatabaseFill.buildDatabase(ps);
        } catch ( SQLException e ) {
            e.printStackTrace();
            throw new MojoExecutionException( "SQL error while building the MiNe table. cf. nested Exception !", e );
        }

        // excluded
        MojoUtils.prepareFile(nullTaxIdFile, true);
        MojoUtils.writeStandardHeaderToFile("Null taxID interactors", "Interactors with null taxID",
                getProject(), nullTaxIdFile);

        FileWriter writer = new FileWriter(nullTaxIdFile);
        for (String interactorAc : report.getNullTaxidInteractors())
        {
            writer.write(interactorAc+NEW_LINE);
        }
        writer.close();
    }
}