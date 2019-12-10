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

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.util.ByteArrayDataSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a {@code DataSource} based on an class path.
 *
 * @since 1.3
 */
public class DataSourceClassPathResolver extends DataSourceBaseResolver
{
    /** the base string of the resource relative to the classpath when resolving relative paths */
    private final String classPathBase;

    /**
     * Constructor
     */
    public DataSourceClassPathResolver()
    {
        this.classPathBase = "/";
    }

    /**
     * Constructor.
     *
     * @param classPathBase a base class path
     */
    public DataSourceClassPathResolver(final String classPathBase)
    {
        this.classPathBase = classPathBase.endsWith("/") ? classPathBase : classPathBase + "/";
    }

    /**
     * Constructor.
     *
     * @param classPathBase a base class path
     * @param lenient shall we ignore resources not found or throw an exception?
     */
    public DataSourceClassPathResolver(final String classPathBase, final boolean lenient)
    {
        super(lenient);
        this.classPathBase = classPathBase.endsWith("/") ? classPathBase : classPathBase + "/";
    }

    /**
     * @return the classPathBase
     */
    public String getClassPathBase()
    {
        return classPathBase;
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
            if (!isCid(resourceLocation) && !isHttpUrl(resourceLocation))
            {
                final String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(resourceLocation);
                final String resourceName = getResourceName(resourceLocation);
                final InputStream is = DataSourceClassPathResolver.class.getResourceAsStream(resourceName);

                if (is != null)
                {
                    try
                    {
                        final ByteArrayDataSource ds = new ByteArrayDataSource(is, mimeType);
                        // EMAIL-125: set the name of the DataSource to the normalized resource URL
                        // similar to other DataSource implementations, e.g. FileDataSource, URLDataSource
                        ds.setName(DataSourceClassPathResolver.class.getResource(resourceName).toString());
                        result = ds;
                    }
                    finally
                    {
                        is.close();
                    }
                }
                else
                {
                    if (isLenient)
                    {
                        return null;
                    }
                    throw new IOException("The following class path resource was not found : " + resourceLocation);
                }
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
     * Returns the resource name for a given resource location.
     *
     * @param resourceLocation the resource location
     * @return {@link #getClassPathBase()} + {@code resourceLocation}
     * @see #getClassPathBase()
     */
    private String getResourceName(final String resourceLocation)
    {
        return (getClassPathBase() + resourceLocation).replaceAll("//", "/");
    }
}
