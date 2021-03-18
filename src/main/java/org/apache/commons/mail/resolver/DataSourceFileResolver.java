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
import jakarta.activation.FileDataSource;
import java.io.File;
import java.io.IOException;

/**
 * Creates a {@code DataSource} based on an URL. The implementation
 * also resolves file resources.
 *
 * @since 1.3
 */
public class DataSourceFileResolver extends DataSourceBaseResolver
{
    /** the base directory of the resource when resolving relative paths */
    private final File baseDir;

    /**
     * Constructor.
     */
    public DataSourceFileResolver()
    {
        baseDir = new File(".");
    }

    /**
     * Constructor.
     *
     * @param baseDir the base directory of the resource when resolving relative paths
     */
    public DataSourceFileResolver(final File baseDir)
    {
        this.baseDir = baseDir;
    }

    /**
     * Constructor.
     *
     * @param baseDir the base directory of the resource when resolving relative paths
     * @param lenient shall we ignore resources not found or complain with an exception
     */
    public DataSourceFileResolver(final File baseDir, final boolean lenient)
    {
        super(lenient);
        this.baseDir = baseDir;
    }

    /**
     * Get the base directory used for resolving relative resource locations.
     *
     * @return the baseUrl
     */
    public File getBaseDir()
    {
        return baseDir;
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
        File file;
        DataSource result = null;

        if (!isCid(resourceLocation))
        {
            file = new File(resourceLocation);

            if (!file.isAbsolute())
            {
                file = getBaseDir() != null ? new File(getBaseDir(), resourceLocation) : new File(resourceLocation);
            }

            if (file.exists())
            {
                result = new FileDataSource(file);
            }
            else
            {
                if (!isLenient)
                {
                    throw new IOException("Cant resolve the following file resource :" + file.getAbsolutePath());
                }
            }
        }

        return result;
    }
}
