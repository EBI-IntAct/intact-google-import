/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.io.File;

/**
 * Deletes the files generated by the plugin
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:IntactCleanMojo.java 5772 2006-08-11 16:08:37 +0100 (Fri, 11 Aug 2006) baranda $
 * @since 0.1
 *
 * @goal clean
 *
 */
public class IntactCleanMojo extends IntactAbstractMojo
{

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File builddir = getDirectory();

         if (builddir.exists())
         {
             try
             {
                 FileUtils.deleteDirectory( builddir );
             }
             catch (IOException e)
             {
                 throw new MojoExecutionException("Problem deleting folder: "+builddir);
             }
         }
    }
}
