/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.plugin.experimentlistgenerator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Deletes the files generated by the plugin
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:$
 * @since <pre>04/08/2006</pre>
 *
 * @goal clean
 */
public class ExperimentListGeneratorCleanMojo extends ExperimentListGeneratorAbstractMojo
{
    
    public void execute() throws MojoExecutionException
    {
        

         if (targetPath.exists())
         {
             try
             {
                 FileUtils.deleteDirectory( targetPath );
             }
             catch (IOException e)
             {
                 throw new MojoExecutionException("Problem deleting folder: "+targetPath);
             }
         }
    }
}
