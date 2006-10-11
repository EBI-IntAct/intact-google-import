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
package uk.ac.ebi.intact.persistence.dao;

/**
 * General DAO utilities
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>09-Oct-2006</pre>
 */
public class DaoUtils
{

    private DaoUtils() {}

    /**
     * Removes the wildcards (*) from the beginning and end of a value,
     * so it can be used in a like query
     * @param value the value to use
     * @return the value with replaced wildcards
     */
    public static String replaceWildcardsByPercent(String value)
    {
        if (value.startsWith("*"))
        {
            value = value.replaceFirst("\\*", "%");
        }

        if (value.endsWith("*"))
        {
            value = value.substring(0, value.length()-1)+"%";
        }

        return value;
    }

    /**
     * Adds a percent symbol (%) to the start and end of the value
     * so it can be used in like queries
     * @param value the value to use. If it already contains percents, no new
     * percents will be added
     * @return the value with percents
     */
    public static String addPercents(String value)
    {
        value = addStartPercent(value);
        value = addEndPercent(value);

        return value;
    }

    public static String addEndPercent(String value)
    {
        if (value == null)
        {
            throw new NullPointerException("value");
        }

        value = replaceWildcardsByPercent(value);

        if (!value.endsWith("%"))
        {
            value = value+"%";
        }

        return value;
    }

    public static String addStartPercent(String value)
    {
        if (value == null)
        {
            throw new NullPointerException("value");
        }

        value = replaceWildcardsByPercent(value);

        if (!value.startsWith("%"))
        {
            value = "%"+value;
        }

        return value;
    }
}
