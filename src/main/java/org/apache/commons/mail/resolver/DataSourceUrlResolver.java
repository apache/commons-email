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
package org.apache.commons.mail.resolver;

import jakarta.activation.DataSource;
import jakarta.activation.URLDataSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Creates a {@code DataSource} based on an URL.
 *
 * @since 1.3
 */
public class DataSourceUrlResolver extends DataSourceBaseResolver
{
    /** the base url of the resource when resolving relative paths */
    private final URL baseUrl;

    /**
     * Constructor.
     *
     * @param baseUrl the base URL used for resolving relative resource locations
     */
    public DataSourceUrlResolver(final URL baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * Constructor.
     *
     * @param baseUrl the base URL used for resolving relative resource locations
     * @param lenient shall we ignore resources not found or complain with an exception
     */
    public DataSourceUrlResolver(final URL baseUrl, final boolean lenient)
    {
        super(lenient);
        this.baseUrl = baseUrl;
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

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation) throws IOException
    {
        return resolve(resourceLocation, isLenient());
    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation, final boolean isLenient) throws IOException
    {
        DataSource result = null;

        try
        {
            if (!isCid(resourceLocation))
            {
                final URL url = createUrl(resourceLocation);
                result = new URLDataSource(url);
                result.getInputStream();
            }

            return result;
        }
        catch (final IOException e)
        {
            if (isLenient)
            {
                return null;
            }
            throw e;
        }
    }

    /**
     * Create an URL based on a base URL and a resource location suitable for loading
     * the resource.
     *
     * @param resourceLocation a resource location
     * @return the corresponding URL
     * @throws java.net.MalformedURLException creating the URL failed
     */
    protected URL createUrl(final String resourceLocation) throws MalformedURLException
    {
        // if we get an non-existing base url than the resource can
        // be directly used to create an URL
        if (baseUrl == null)
        {
            return new URL(resourceLocation);
        }

        // if we get an non-existing location what we shall do?
        if (resourceLocation == null || resourceLocation.isEmpty())
        {
            throw new IllegalArgumentException("No resource defined");
        }

        // if we get a stand-alone resource than ignore the base url
        if (isFileUrl(resourceLocation) || isHttpUrl(resourceLocation))
        {
            return new URL(resourceLocation);
        }

        return new URL(getBaseUrl(), resourceLocation.replace("&amp;", "&"));
    }
}
