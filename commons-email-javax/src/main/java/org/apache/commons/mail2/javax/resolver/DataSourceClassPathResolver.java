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
package org.apache.commons.mail2.javax.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.util.ByteArrayDataSource;

/**
 * Creates a {@code DataSource} based on an class path.
 *
 * @since 1.3
 */
public class DataSourceClassPathResolver extends DataSourceBaseResolver {
    /** The base string of the resource relative to the classpath when resolving relative paths */
    private final String classPathBase;

    /**
     * Constructs a new instance.
     */
    public DataSourceClassPathResolver() {
        this("/");
    }

    /**
     * Constructs a new instance.
     *
     * @param classPathBase a base class path
     */
    public DataSourceClassPathResolver(final String classPathBase) {
        this(classPathBase, false);
    }

    /**
     * Constructs a new instance.
     *
     * @param classPathBase a base class path
     * @param lenient       shall we ignore resources not found or throw an exception?
     */
    public DataSourceClassPathResolver(final String classPathBase, final boolean lenient) {
        super(lenient);
        this.classPathBase = classPathBase.endsWith("/") ? classPathBase : classPathBase + "/";
    }

    /**
     * Gets the class path base.
     *
     * @return the classPathBase
     */
    public String getClassPathBase() {
        return classPathBase;
    }

    /**
     * Returns the resource name for a given resource location.
     *
     * @param resourceLocation the resource location
     * @return {@link #getClassPathBase()} + {@code resourceLocation}
     * @see #getClassPathBase()
     */
    private String getResourceName(final String resourceLocation) {
        return (getClassPathBase() + resourceLocation).replace("//", "/");
    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation) throws IOException {
        return resolve(resourceLocation, isLenient());
    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation, final boolean isLenient) throws IOException {
        try {
            if (!isCid(resourceLocation) && !isHttpUrl(resourceLocation)) {
                final String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(resourceLocation);
                final String resourceName = getResourceName(resourceLocation);
                try (InputStream inputStream = DataSourceClassPathResolver.class.getResourceAsStream(resourceName)) {
                    if (inputStream == null) {
                        if (isLenient) {
                            return null;
                        }
                        throw new IOException("The following class path resource was not found : " + resourceLocation);
                    }
                    final ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, mimeType);
                    // EMAIL-125: set the name of the DataSource to the normalized resource URL
                    // similar to other DataSource implementations, e.g. FileDataSource, URLDataSource
                    final URL resource = DataSourceClassPathResolver.class.getResource(resourceName);
                    if (resource != null) {
                        ds.setName(resource.toString());
                    } else if (isLenient) {
                        return null;
                    } else {
                        throw new IOException("The following class path resource was not found : " + resourceName);
                    }
                    return ds;
                }
            }
            return null;
        } catch (final IOException e) {
            if (isLenient) {
                return null;
            }
            throw e;
        }
    }
}
