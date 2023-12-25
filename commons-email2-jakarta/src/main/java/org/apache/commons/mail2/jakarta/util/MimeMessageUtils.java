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
package org.apache.commons.mail2.jakarta.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.SharedByteArrayInputStream;

/**
 * Creates {@link MimeMessage} instances and other helper methods.
 *
 * @since 1.3
 */
public final class MimeMessageUtils {

    /**
     * Creates a MimeMessage.
     *
     * @param session the mail session.
     * @param source  the input data.
     * @return the MimeMessage.
     * @throws MessagingException creating the MimeMessage failed.
     * @throws IOException        creating the MimeMessage failed.
     */
    public static MimeMessage createMimeMessage(final Session session, final byte[] source) throws MessagingException, IOException {
        try (InputStream inputStream = new SharedByteArrayInputStream(source)) {
            return new MimeMessage(session, inputStream);
        }
    }

    /**
     * Creates a MimeMessage.
     *
     * @param session the mail session.
     * @param source  the input data.
     * @return the MimeMessage.
     * @throws MessagingException creating the MimeMessage failed.
     * @throws IOException        creating the MimeMessage failed.
     */
    public static MimeMessage createMimeMessage(final Session session, final File source) throws MessagingException, IOException {
        try (InputStream inputStream = new FileInputStream(source)) {
            return createMimeMessage(session, inputStream);
        }
    }

    /**
     * Creates a MimeMessage.
     *
     * @param session the mail session.
     * @param source  the input data.
     * @return the MimeMessage.
     * @throws MessagingException creating the MimeMessage failed.
     */
    public static MimeMessage createMimeMessage(final Session session, final InputStream source) throws MessagingException {
        return new MimeMessage(session, source);
    }

    /**
     * Creates a MimeMessage.
     *
     * @param session the mail session.
     * @param source  the input data.
     * @param options options specifying how the file is opened.
     * @return the MimeMessage.
     * @throws MessagingException creating the MimeMessage failed.
     * @throws IOException        creating the MimeMessage failed.
     */
    public static MimeMessage createMimeMessage(final Session session, final Path source, final OpenOption... options) throws MessagingException, IOException {
        try (InputStream inputStream = Files.newInputStream(source, options)) {
            return createMimeMessage(session, inputStream);
        }
    }

    /**
     * Creates a MimeMessage using the platform's default character encoding.
     *
     * @param session the mail session.
     * @param source  the input data.
     * @return the MimeMessage.
     * @throws MessagingException creating the MimeMessage failed.
     * @throws IOException        creating the MimeMessage failed.
     */
    public static MimeMessage createMimeMessage(final Session session, final String source) throws MessagingException, IOException {
        // RFC1341: https://www.w3.org/Protocols/rfc1341/7_1_Text.html
        return createMimeMessage(session, source.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Writes a MimeMessage into a file.
     *
     * @param mimeMessage the MimeMessage to write.
     * @param resultFile  the file containing the MimeMessage.
     * @throws MessagingException accessing MimeMessage failed.
     * @throws IOException        writing the MimeMessage failed.
     */
    public static void writeMimeMessage(final MimeMessage mimeMessage, final File resultFile) throws MessagingException, IOException {
        if (!resultFile.getParentFile().exists() && !resultFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create the following parent directories: " + resultFile.getParentFile());
        }
        try (OutputStream outputStream = new FileOutputStream(resultFile)) {
            mimeMessage.writeTo(outputStream);
            outputStream.flush();
        }
    }

    /**
     * Instances should NOT be constructed in standard programming.
     */
    private MimeMessageUtils() {
    }
}
