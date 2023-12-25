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
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;

import org.apache.commons.mail2.javax.activation.PathDataSource;

/**
 * Creates a {@link DataSource} based on a {@link Path}. The implementation also resolves file resources.
 *
 * @since 1.6.0
 */
public final class DataSourcePathResolver extends DataSourceBaseResolver {

    /**
     * The base directory of the resource when resolving relative paths.
     */
    private final Path baseDir;

    /**
     * NIO options to open the data source.
     */
    private final OpenOption[] options;

    /**
     * Constructs a new instance.
     */
    public DataSourcePathResolver() {
        this(Paths.get("."));
    }

    /**
     * Constructs a new instance.
     *
     * @param baseDir the base directory of the resource when resolving relative paths
     */
    public DataSourcePathResolver(final Path baseDir) {
        this(baseDir, false);
    }

    /**
     * Constructs a new instance.
     *
     * @param baseDir the base directory of the resource when resolving relative paths
     * @param lenient shall we ignore resources not found or complain with an exception
     * @param options options for opening streams.
     */
    public DataSourcePathResolver(final Path baseDir, final boolean lenient, final OpenOption... options) {
        super(lenient);
        this.baseDir = baseDir;
        this.options = options;
    }

    /**
     * Gets the base directory used for resolving relative resource locations.
     *
     * @return the baseUrl
     */
    public Path getBaseDir() {
        return baseDir;
    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation) throws IOException {
        return resolve(resourceLocation, isLenient());
    }

    /** {@inheritDoc} */
    @Override
    public DataSource resolve(final String resourceLocation, final boolean isLenient) throws IOException {
        Path file;
        DataSource result = null;

        if (!isCid(resourceLocation)) {
            file = Paths.get(resourceLocation);

            if (!file.isAbsolute()) {
                file = getBaseDir() != null ? getBaseDir().resolve(resourceLocation) : Paths.get(resourceLocation);
            }

            if (Files.exists(file)) {
                result = new PathDataSource(file, FileTypeMap.getDefaultFileTypeMap(), options);
            } else if (!isLenient) {
                throw new IOException("Cant resolve the following file resource :" + file.toAbsolutePath());
            }
        }

        return result;
    }
}
