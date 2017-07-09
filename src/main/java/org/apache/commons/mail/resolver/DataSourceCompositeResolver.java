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

import org.apache.commons.mail.DataSourceResolver;

import javax.activation.DataSource;
import java.io.IOException;

/**
 * A composite data source resolver. It allows to resolve data sources coming from
 * multiple locations such as the classpath, the file system or an URL.
 *
 * @since 1.3
 */
public class DataSourceCompositeResolver extends DataSourceBaseResolver
{
    /** the list of resolvers */
    private final DataSourceResolver[] dataSourceResolvers;

    /**
     * Constructor.
     *
     * @param dataSourceResolvers a list of of resolvers being used
     */
    public DataSourceCompositeResolver(final DataSourceResolver[] dataSourceResolvers)
    {
        this.dataSourceResolvers = new DataSourceResolver[dataSourceResolvers.length];
        System.arraycopy(dataSourceResolvers, 0, this.dataSourceResolvers, 0, dataSourceResolvers.length);
    }

    /**
     * Constructor.
     *
     * @param dataSourceResolvers a list of of resolvers being used
     * @param isLenient shall we ignore resources not found or throw an exception?
     */
    public DataSourceCompositeResolver(final DataSourceResolver[] dataSourceResolvers, final boolean isLenient)
    {
        super(isLenient);
        this.dataSourceResolvers = new DataSourceResolver[dataSourceResolvers.length];
        System.arraycopy(dataSourceResolvers, 0, this.dataSourceResolvers, 0, dataSourceResolvers.length);
    }

    /**
     * Get the underlying data source resolvers.
     *
     * @return underlying data source resolvers
     */
    public DataSourceResolver[] getDataSourceResolvers()
    {
        // clone the internal array to prevent external modification (see EMAIL-116)
        final DataSourceResolver[] resolvers = new DataSourceResolver[dataSourceResolvers.length];
        System.arraycopy(dataSourceResolvers, 0, resolvers, 0, dataSourceResolvers.length);
        return resolvers;
    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation) throws IOException
    {
        final DataSource result = resolve(resourceLocation, true);

        if (isLenient() || result != null)
        {
            return result;
        }
        throw new IOException("The following resource was not found : " + resourceLocation);

    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation, final boolean isLenient) throws IOException
    {
        for (int i = 0; i < getDataSourceResolvers().length; i++)
        {
            final DataSourceResolver dataSourceResolver = getDataSourceResolvers()[i];
            final DataSource dataSource = dataSourceResolver.resolve(resourceLocation, isLenient);

            if (dataSource != null)
            {
                return dataSource;
            }
        }

        if (isLenient)
        {
            return null;
        }
        throw new IOException("The following resource was not found : " + resourceLocation);
    }
}
