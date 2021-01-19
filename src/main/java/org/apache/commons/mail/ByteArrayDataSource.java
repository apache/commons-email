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
package org.apache.commons.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import jakarta.activation.DataSource;

/**
 * This class implements a typed DataSource from:<br>
 *
 * - an InputStream<br>
 * - a byte array<br>
 * - a String<br>
 *
 * <p>
 * From version 1.3.1, it is possible to set a name for this DataSource,
 * and it is recommended to do so.
 *
 * @since 1.0
 * @deprecated since 1.4, use {@link jakarta.mail.util.ByteArrayDataSource} instead
 */
@Deprecated
public class ByteArrayDataSource implements DataSource
{
    /** Define the buffer size. */
    public static final int BUFFER_SIZE = 512;

    /** Stream containing the Data. */
    private ByteArrayOutputStream baos;

    /** The Content-type. */
    private final String type; // = "application/octet-stream";

    /**
     * The name associated with this data source.
     * By default, the name is an empty string, similar to jakarta.mail.util.ByteArrayDataSource.
     * @since 1.3.1
     */
    private String name = "";

    /**
     * Create a datasource from a byte array.
     *
     * @param data A byte[].
     * @param aType A String.
     * @throws IOException IOException
     * @since 1.0
     */
    public ByteArrayDataSource(final byte[] data, final String aType) throws IOException
    {
        this.type = aType;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data))
        {
            this.byteArrayDataSource(bis);
        }
    }

    /**
     * Create a datasource from an input stream.
     *
     * @param aIs An InputStream.
     * @param aType A String.
     * @throws IOException IOException
     * @since 1.0
     */
    public ByteArrayDataSource(final InputStream aIs, final String aType) throws IOException
    {
        this.type = aType;
        this.byteArrayDataSource(aIs);
    }

    /**
     * Create a datasource from a String.
     * N.B. assumes the data string can be converted using the charset iso-8859-1.
     *
     * @param data A String.
     * @param aType A String.
     * @throws IOException IOException
     * @since 1.0
     */
    public ByteArrayDataSource(final String data, final String aType) throws IOException
    {
        this.type = aType;

        try
        {
            baos = new ByteArrayOutputStream();

            // Assumption that the string contains only ASCII characters!
            // Else just pass in a charset into this constructor and use it in getBytes().
            baos.write(data.getBytes("iso-8859-1"));
            baos.flush();
            baos.close();
        }
        catch (final UnsupportedEncodingException uex)
        {
            throw new IOException("The Character Encoding is not supported.");
        }
        finally
        {
            if (baos != null)
            {
                baos.close();
            }
        }
    }

    /**
      * Create a datasource from an input stream.
      *
      * @param aIs An InputStream.
      * @throws IOException IOException
      */
    private void byteArrayDataSource(final InputStream aIs)
        throws IOException
    {
        BufferedInputStream bis = null;
        BufferedOutputStream osWriter = null;

        try
        {
            int length = 0;
            final byte[] buffer = new byte[ByteArrayDataSource.BUFFER_SIZE];

            bis = new BufferedInputStream(aIs);
            baos = new ByteArrayOutputStream();
            osWriter = new BufferedOutputStream(baos);

            // Write the InputData to OutputStream
            while ((length = bis.read(buffer)) != -1)
            {
                osWriter.write(buffer, 0, length);
            }
            osWriter.flush();
            osWriter.close();

        }
        finally
        {
            if (bis != null)
            {
                bis.close();
            }
            if (baos != null)
            {
                baos.close();
            }
            if (osWriter != null)
            {
                osWriter.close();
            }
        }
    }

    /**
     * Get the content type.
     *
     * @return A String.
     * @since 1.0
     */
    @Override
    public String getContentType()
    {
        return type == null ? "application/octet-stream" : type;
    }

    /**
     * Get the input stream.
     *
     * @return An InputStream.
     * @throws IOException IOException
     * @since 1.0
     */
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (baos == null)
        {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Sets the name for this DataSource.
     *
     * @param name The name.
     * @since 1.3.1
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Get the name.
     *
     * @return A String.
     * @since 1.0
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Get the OutputStream to write to.
     *
     * @return  An OutputStream
     * @since 1.0
     */
    @Override
    public OutputStream getOutputStream()
    {
        baos = new ByteArrayOutputStream();
        return baos;
    }
}
