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

package org.apache.commons.mail2.javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

/**
 * A JavaBeans Activation Framework {@link DataSource} that encapsulates a {@link Path}. It provides data typing services via a {@link FileTypeMap} object.
 *
 * @see javax.activation.DataSource
 * @see javax.activation.FileTypeMap
 * @see javax.activation.MimetypesFileTypeMap
 *
 * @since 1.6.0
 */
public final class PathDataSource implements DataSource {

    /**
     * The source.
     */
    private final Path path;

    /**
     * Defaults to {@link FileTypeMap#getDefaultFileTypeMap()}.
     */
    private final FileTypeMap typeMap;

    /**
     * NIO options to open the source Path.
     */
    private final OpenOption[] options;

    /**
     * Creates a new instance from a Path.
     * <p>
     * The file will not actually be opened until a method is called that requires the path to be opened.
     * </p>
     * <p>
     * The type map defaults to {@link FileTypeMap#getDefaultFileTypeMap()}.
     *
     * @param path the path
     */
    public PathDataSource(final Path path) {
        this(path, FileTypeMap.getDefaultFileTypeMap());
    }

    /**
     * Creates a new instance from a Path.
     * <p>
     * The file will not actually be opened until a method is called that requires the path to be opened.
     * </p>
     *
     * @param path    the path, non-null.
     * @param typeMap the type map, non-null.
     * @param options options for opening file streams.
     */
    public PathDataSource(final Path path, final FileTypeMap typeMap, final OpenOption... options) {
        this.path = Objects.requireNonNull(path, "path");
        this.typeMap = Objects.requireNonNull(typeMap, "typeMap");
        this.options = options;
    }

    /**
     * Gets the MIME type of the data as a String. This method uses the currently installed FileTypeMap. If there is no FileTypeMap explicitly set, the
     * FileDataSource will call the {@link FileTypeMap#getDefaultFileTypeMap} method to acquire a default FileTypeMap.
     * <p>
     * By default, the {@link FileTypeMap} used will be a {@link MimetypesFileTypeMap}.
     * </p>
     *
     * @return the MIME Type
     * @see FileTypeMap#getDefaultFileTypeMap
     */
    @Override
    public String getContentType() {
        return typeMap.getContentType(getName());
    }

    /**
     * Gets an InputStream representing the the data and will throw an IOException if it can not do so. This method will return a new instance of InputStream
     * with each invocation.
     *
     * @return an InputStream
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path, options);
    }

    /**
     * Gets the <em>name</em> of this object. The FileDataSource will return the file name of the object.
     *
     * @return the name of the object or null.
     * @see javax.activation.DataSource
     */
    @Override
    public String getName() {
        return Objects.toString(path.getFileName(), null);
    }

    /**
     * Gets an OutputStream representing the the data and will throw an IOException if it can not do so. This method will return a new instance of OutputStream
     * with each invocation.
     *
     * @return an OutputStream
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(path, options);
    }

    /**
     * Gets the File object that corresponds to this PathDataSource.
     *
     * @return the File object for the file represented by this object.
     */
    public Path getPath() {
        return path;
    }

}
