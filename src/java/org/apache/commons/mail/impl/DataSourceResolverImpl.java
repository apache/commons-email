/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail.impl;

import org.apache.commons.mail.DataSourceResolver;
import org.apache.commons.mail.util.URLFactory;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import java.io.IOException;
import java.net.URL;

/**
 * Creates a <code>DataSource</code> based on an URL.
 *
 * @since 1.3
 */
public class DataSourceResolverImpl implements DataSourceResolver
{
    /**  the base url of the resource when resolving relative paths */
    private final URL baseUrl;

    /** shall we ignore resources not found or complain with an exception */
    private final boolean isLenient;

    /**
     * Constructor.
     *
     * @param baseUrl the base URL used for resolving relative resource locations
     */
    public DataSourceResolverImpl(URL baseUrl)
    {
        this(baseUrl, false);
    }

    /**
     * Constructor.
     *
     * @param baseUrl the base URL used for resolving relative resource locations
     * @param lenient shall we ignore resources not found or complain with an exception
     */
    public DataSourceResolverImpl(URL baseUrl, boolean lenient)
    {
        this.baseUrl = baseUrl;
        this.isLenient = lenient;
    }

    /**
     * Get the base URL used for resolving relative resource locations.
     *
     * @return the baseUrl
     */
    public URL getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * Shall we ignore resources not found or throw an exception?
     *
     * @return the lenient flag
     */
    public boolean isLenient()
    {
        return isLenient;
    }

    /**
     * Resolve a resource location to be embedded into the email. When using
     * the lenient mode a resource which can't be resolved returns "null".
     * When using the non-lenient mode an exception would be thrown.
     *
     * @param resourceLocation the location of the resource
     * @return the data source containing the resource
     * @throws IOException resolving the resource failed
     */
    public DataSource resolve(String resourceLocation) throws IOException
    {
        DataSource result = null;

        try
        {
            if (!resourceLocation.startsWith("cid:"))
            {
                URL url = URLFactory.createUrl(getBaseUrl(), resourceLocation);
                result = new URLDataSource(url);
                result.getInputStream();
            }

            return result;
        }
        catch (IOException e)
        {
            if (isLenient())
            {
                return null;
            }
            else
            {
                throw e;
            }
        }
    }
}
