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
package org.apache.commons.mail.util;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Static helper methods.
 *
 * @since 1.3
 */
public final class MimeMessageUtils
{
    /**
     * Instances should NOT be constructed in standard programming.
     */
    private MimeMessageUtils()
    {
        super();
    }

    /**
     * Create a MimeMessage.
     *
     * @param session the mail session
     * @param source the input data
     * @return the MimeMessage
     * @throws MessagingException creating the MimeMessage failed
     * @throws IOException creating the MimeMessage failed
     */
    public static MimeMessage createMimeMessage(final Session session, final byte[] source)
        throws MessagingException, IOException
    {
        ByteArrayInputStream is = null;

        try
        {
            is = new ByteArrayInputStream(source);
            return new MimeMessage(session, is);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    /**
     * Create a MimeMessage.
     *
     * @param session the mail session
     * @param source the input data
     * @return the MimeMessage
     * @throws MessagingException creating the MimeMessage failed
     * @throws IOException creating the MimeMessage failed
     */
    public static MimeMessage createMimeMessage(final Session session, final File source)
        throws MessagingException, IOException
    {
        FileInputStream is = null;

        try
        {
            is = new FileInputStream(source);
            return createMimeMessage(session, is);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    /**
     * Create a MimeMessage.
     *
     * @param session the mail session
     * @param source the input data
     * @return the MimeMessage
     * @throws MessagingException creating the MimeMessage failed
     */
    public static MimeMessage createMimeMessage(final Session session, final InputStream source)
        throws MessagingException
    {
        return new MimeMessage(session, source);
    }

    /**
     * Create a MimeMessage using the platform's default character encoding.
     *
     * @param session the mail session
     * @param source the input data
     * @return the MimeMessage
     * @throws MessagingException creating the MimeMessage failed
     * @throws IOException creating the MimeMessage failed
     */
    public static MimeMessage createMimeMessage(final Session session, final String source)
        throws MessagingException, IOException
    {
        ByteArrayInputStream is = null;

        try
        {
            final byte[] byteSource = source.getBytes();
            is = new ByteArrayInputStream(byteSource);
            return createMimeMessage(session, is);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    /**
     * Convenience method to write a MimeMessage into a file.
     *
     * @param mimeMessage the MimeMessage to write
     * @param resultFile  the file containing the MimeMessgae
     * @throws MessagingException accessing MimeMessage failed
     * @throws IOException        writing the MimeMessage failed
     */
    public static void writeMimeMessage(final MimeMessage mimeMessage, final File resultFile)
        throws MessagingException, IOException
    {

        FileOutputStream fos = null;

        try
        {
            if (!resultFile.getParentFile().exists() && !resultFile.getParentFile().mkdirs())
            {
                throw new IOException(
                        "Failed to create the following parent directories: "
                                + resultFile.getParentFile());
            }

            fos = new FileOutputStream(resultFile);
            mimeMessage.writeTo(fos);
            fos.flush();
            fos.close();
            fos = null;
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }
}
