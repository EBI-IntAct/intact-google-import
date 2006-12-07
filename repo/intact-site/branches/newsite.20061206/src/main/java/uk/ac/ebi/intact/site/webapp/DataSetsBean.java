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
package uk.ac.ebi.intact.site.webapp;

import uk.ac.ebi.intact.site.items.Datasets;
import static uk.ac.ebi.intact.site.items.Datasets.Dataset;

import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DataSetsBean implements Serializable {

    private static final String DATASET_OF_THE_MONTH_URL = "uk.ac.ebi.intact.DATASET_OF_THE_MONTH_URL";

    private List<Dataset> dataSets;
    private Dataset dataSetOfTheMonth;

    public DataSetsBean()
    {
       // read datasets from XML
        String datasetsXml = FacesContext.getCurrentInstance().getExternalContext().getInitParameter(DATASET_OF_THE_MONTH_URL);

        Datasets datasets = null;
        try {
            URL datasetsUrl = new URL(datasetsXml);
            datasets = readDatasetsXml(datasetsUrl.openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (datasets != null)
        {
            dataSets = datasets.getDataset();
            dataSetOfTheMonth = dataSets.iterator().next();
        }
        else
        {
            dataSets = new ArrayList<Dataset>();
        }
    }

    private static Datasets readDatasetsXml(InputStream is) throws JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance(Datasets.class.getPackage().getName());
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (Datasets)
                unmarshaller.unmarshal(is);
    }

    public List<Dataset> getDataSets()
    {
        return dataSets;
    }

    public void setDataSets(List<Dataset> dataSets)
    {
        this.dataSets = dataSets;
    }

    public Dataset getDataSetOfTheMonth()
    {
        return dataSetOfTheMonth;
    }

    public void setDataSetOfTheMonth(Dataset dataSetOfTheMonth)
    {
        this.dataSetOfTheMonth = dataSetOfTheMonth;
    }
}
