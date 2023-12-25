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

import javax.activation.DataSource;

/**
 * A JavaBeans Activation Framework {@link DataSource} specialized for {@link InputStream}.
 * <p>
 * Copied from <a href="https://cxf.apache.org/">Apache CXF</a> and modified.
 * </p>
 *
 * @since 1.6.0
 */
public final class InputStreamDataSource implements DataSource {

    /**
     * Default content type documented in {@link DataSource#getContentType()}.
     */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * The MIME content type.
     */
    private final String contentType;

    /**
     * The source.
     */
    private final InputStream inputStream;

    /**
     * The optional name.
     */
    private final String name;

    /**
     * Constructs a new instance.
     *
     * @param inputStream An input stream.
     * @param contentType A content type.
     */
    public InputStreamDataSource(final InputStream inputStream, final String contentType) {
        this(inputStream, contentType, null);
    }

    /**
     * Constructs a new instance.
     *
     * @param inputStream An input stream.
     * @param contentType A content type.
     * @param name        A name.
     */
    public InputStreamDataSource(final InputStream inputStream, final String contentType, final String name) {
        this.inputStream = inputStream;
        this.contentType = contentType != null ? contentType : DEFAULT_CONTENT_TYPE;
        this.name = name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Always throws {@link UnsupportedOperationException}.
     *
     * @return Always throws {@link UnsupportedOperationException}.
     * @throws UnsupportedOperationException Always throws {@link UnsupportedOperationException}.
     */
    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

}
