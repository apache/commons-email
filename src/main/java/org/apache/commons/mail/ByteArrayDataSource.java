/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.activation.DataSource;

/**
 * A typed DataSource for InputStream, byte array, and String.
 * <p>
 * From version 1.3.1, it is possible to set a name for this DataSource, and it is recommended to do so.
 * </p>
 *
 * @since 1.0
 * @deprecated since 1.4, use {@link javax.mail.util.ByteArrayDataSource} or {@link javax.mail.util.SharedByteArrayInputStream}.
 */
@Deprecated
public class ByteArrayDataSource implements DataSource {

    /** Default content type. */
    private static final String CONTENT_TYPE = "application/octet-stream";

    /** Define the buffer size. */
    public static final int BUFFER_SIZE = 512;

    /** Stream containing the Data. */
    private ByteArrayOutputStream outputStream;

    /** The Content-contentType. */
    private final String contentType;

    /**
     * The name associated with this data source. By default, the name is an empty string, similar to javax.mail.util.ByteArrayDataSource.
     *
     * @since 1.3.1
     */
    private String name = "";

    /**
     * Constructs a new instance from a byte array.
     *
     * @param data        A byte[].
     * @param contentType A String.
     * @throws IOException IOException
     * @since 1.0
     */
    public ByteArrayDataSource(final byte[] data, final String contentType) throws IOException {
        this.contentType = contentType;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            read(bis);
        }
    }

    /**
     * Constructs a new instance from an input stream.
     *
     * @param inputStream An InputStream.
     * @param contentType A String.
     * @throws IOException IOException
     * @since 1.0
     */
    public ByteArrayDataSource(final InputStream inputStream, final String contentType) throws IOException {
        this.contentType = contentType;
        this.read(inputStream);
    }

    /**
     * Constructs a new instance from a String. N.B. assumes the data string can be converted using the charset iso-8859-1.
     *
     * @param data        A String.
     * @param contentType A String.
     * @throws IOException IOException
     * @since 1.0
     */
    public ByteArrayDataSource(final String data, final String contentType) throws IOException {
        this.contentType = contentType;
        this.outputStream = new ByteArrayOutputStream();
        try {
            // Assumption that the string contains only ASCII characters!
            // Else just pass in a charset into this constructor and use it in getBytes().
            outputStream.write(data.getBytes(StandardCharsets.ISO_8859_1));
            outputStream.flush();
            outputStream.close();
        } catch (final UnsupportedEncodingException e) {
            throw new IOException("The Character Encoding is not supported.");
        } finally {
            outputStream.close();
        }
    }

    /**
     * Gets the content contentType.
     *
     * @return A String.
     * @since 1.0
     */
    @Override
    public String getContentType() {
        return contentType == null ? CONTENT_TYPE : contentType;
    }

    /**
     * Gets the input stream.
     *
     * @return An InputStream.
     * @throws IOException IOException
     * @since 1.0
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (outputStream == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Gets the name.
     *
     * @return A String.
     * @since 1.0
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the OutputStream to write to.
     *
     * @return An OutputStream
     * @since 1.0
     */
    @Override
    public OutputStream getOutputStream() {
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    /**
     * Reads the given input stream into this data source.
     *
     * @param inputStream An InputStream.
     * @throws IOException IOException
     */
    private void read(final InputStream inputStream) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream osWriter = null;
        try {
            int length = 0;
            final byte[] buffer = new byte[ByteArrayDataSource.BUFFER_SIZE];

            bis = new BufferedInputStream(inputStream);
            outputStream = new ByteArrayOutputStream();
            osWriter = new BufferedOutputStream(outputStream);

            // Write the InputData to OutputStream
            while ((length = bis.read(buffer)) != -1) {
                osWriter.write(buffer, 0, length);
            }
            osWriter.flush();
            osWriter.close();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (osWriter != null) {
                osWriter.close();
            }
        }
    }

    /**
     * Sets the name for this DataSource.
     *
     * @param name The name.
     * @since 1.3.1
     */
    public void setName(final String name) {
        this.name = name;
    }
}
