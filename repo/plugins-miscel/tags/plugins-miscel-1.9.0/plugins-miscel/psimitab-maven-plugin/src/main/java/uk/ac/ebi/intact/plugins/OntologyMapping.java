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
package uk.ac.ebi.intact.plugins;

import java.net.URL;

/**
 * TODO comment that class header
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 */
public class OntologyMapping extends uk.ac.ebi.intact.bridges.ontologies.OntologyMapping {

    private String name;
    private URL url;

    public OntologyMapping() {
        super(null, null);
    }

    public OntologyMapping( String name, URL url ) {
        super(null, null);
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl( URL url ) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "OntologyMapping{" +
                "name='" + name + '\'' +
                ", url=" + url +
                '}';
    }
}
