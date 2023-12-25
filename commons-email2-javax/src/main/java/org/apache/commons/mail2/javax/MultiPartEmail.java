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
package org.apache.commons.mail2.javax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.core.EmailUtils;
import org.apache.commons.mail2.javax.activation.PathDataSource;

/**
 * A multipart email.
 * <p>
 * This class is used to send multi-part internet email like messages with attachments.
 * </p>
 * <p>
 * To create a multi-part email, call the default constructor and then you can call setMsg() to set the message and call the different attach() methods.
 * </p>
 *
 * @since 1.0
 */
public class MultiPartEmail extends Email {

    /** Body portion of the email. */
    private MimeMultipart container;

    /** The message container. */
    private BodyPart primaryBodyPart;

    /** The MIME subtype. */
    private String subType;

    /** Indicates if the message has been initialized. */
    private boolean initialized;

    /** Indicates if attachments have been added to the message. */
    private boolean hasAttachments;

    /**
     * Constructs a new instance.
     */
    public MultiPartEmail() {
        // empty
    }

    /**
     * Adds a new part to the email.
     *
     * @param multipart The MimeMultipart.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public Email addPart(final MimeMultipart multipart) throws EmailException {
        try {
            return addPart(multipart, getContainer().getCount());
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }
    }

    /**
     * Adds a new part to the email.
     *
     * @param multipart The part to add.
     * @param index     The index to add at.
     * @return The email.
     * @throws EmailException An error occurred while adding the part.
     * @since 1.0
     */
    public Email addPart(final MimeMultipart multipart, final int index) throws EmailException {
        final BodyPart bodyPart = createBodyPart();
        try {
            bodyPart.setContent(multipart);
            getContainer().addBodyPart(bodyPart, index);
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }

        return this;
    }

    /**
     * Adds a new part to the email.
     *
     * @param partContent     The content.
     * @param partContentType The content type.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public Email addPart(final String partContent, final String partContentType) throws EmailException {
        final BodyPart bodyPart = createBodyPart();
        try {
            bodyPart.setContent(partContent, partContentType);
            getContainer().addBodyPart(bodyPart);
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }

        return this;
    }

    /**
     * Attaches a file specified as a DataSource interface.
     *
     * @param dataSource  A DataSource interface for the file.
     * @param name        The name field for the attachment.
     * @param description A description for the attachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final DataSource dataSource, final String name, final String description) throws EmailException {
        EmailException.checkNonNull(dataSource, () -> "Invalid Datasource.");
        // verify that the DataSource is valid
        try (InputStream inputStream = dataSource.getInputStream()) {
            EmailException.checkNonNull(inputStream, () -> "Invalid Datasource.");
        } catch (final IOException e) {
            throw new EmailException("Invalid Datasource.", e);
        }
        return attach(dataSource, name, description, EmailAttachment.ATTACHMENT);
    }

    /**
     * Attaches a file specified as a DataSource interface.
     *
     * @param dataSource  A DataSource interface for the file.
     * @param name        The name field for the attachment.
     * @param description A description for the attachment.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final DataSource dataSource, String name, final String description, final String disposition) throws EmailException {
        if (EmailUtils.isEmpty(name)) {
            name = dataSource.getName();
        }
        try {
            final BodyPart bodyPart = createBodyPart();
            bodyPart.setDisposition(disposition);
            bodyPart.setFileName(MimeUtility.encodeText(name));
            bodyPart.setDescription(description);
            bodyPart.setDataHandler(new DataHandler(dataSource));
            getContainer().addBodyPart(bodyPart);
        } catch (final UnsupportedEncodingException | MessagingException e) {
            // in case the file name could not be encoded
            throw new EmailException(e);
        }
        setBoolHasAttachments(true);
        return this;
    }

    /**
     * Attaches an EmailAttachment.
     *
     * @param attachment An EmailAttachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final EmailAttachment attachment) throws EmailException {
        EmailException.checkNonNull(attachment, () -> "Invalid attachment.");
        MultiPartEmail result = null;
        final URL url = attachment.getURL();
        if (url == null) {
            String fileName = null;
            try {
                fileName = attachment.getPath();
                final File file = new File(fileName);
                if (!file.exists()) {
                    throw new IOException("\"" + fileName + "\" does not exist");
                }
                result = attach(new FileDataSource(file), attachment.getName(), attachment.getDescription(), attachment.getDisposition());
            } catch (final IOException e) {
                throw new EmailException("Cannot attach file \"" + fileName + "\"", e);
            }
        } else {
            result = attach(url, attachment.getName(), attachment.getDescription(), attachment.getDisposition());
        }
        return result;
    }

    /**
     * Attaches a file.
     *
     * @param file A file attachment
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.3
     */
    public MultiPartEmail attach(final File file) throws EmailException {
        final String fileName = file.getAbsolutePath();
        try {
            if (!file.exists()) {
                throw new IOException("\"" + fileName + "\" does not exist");
            }
            return attach(new FileDataSource(file), file.getName(), null, EmailAttachment.ATTACHMENT);
        } catch (final IOException e) {
            throw new EmailException("Cannot attach file \"" + fileName + "\"", e);
        }
    }

    /**
     * Attaches a path.
     *
     * @param file    A file attachment.
     * @param options options for opening file streams.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.6.0
     */
    public MultiPartEmail attach(final Path file, final OpenOption... options) throws EmailException {
        final Path fileName = file.toAbsolutePath();
        try {
            if (!Files.exists(file)) {
                throw new IOException("\"" + fileName + "\" does not exist");
            }
            return attach(new PathDataSource(file, FileTypeMap.getDefaultFileTypeMap(), options), Objects.toString(file.getFileName(), null), null,
                    EmailAttachment.ATTACHMENT);
        } catch (final IOException e) {
            throw new EmailException("Cannot attach file \"" + fileName + "\"", e);
        }
    }

    /**
     * Attaches a file located by its URL. The disposition of the file is set to mixed.
     *
     * @param url         The URL of the file (may be any valid URL).
     * @param name        The name field for the attachment.
     * @param description A description for the attachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final URL url, final String name, final String description) throws EmailException {
        return attach(url, name, description, EmailAttachment.ATTACHMENT);
    }

    /**
     * Attaches a file located by its URL.
     *
     * @param url         The URL of the file (may be any valid URL).
     * @param name        The name field for the attachment.
     * @param description A description for the attachment.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final URL url, final String name, final String description, final String disposition) throws EmailException {
        // verify that the URL is valid
        try {
            url.openStream().close();
        } catch (final IOException e) {
            throw new EmailException("Invalid URL set:" + url, e);
        }
        return attach(new URLDataSource(url), name, description, disposition);
    }

    /**
     * Builds the MimeMessage. Please note that a user rarely calls this method directly and only if he/she is interested in the sending the underlying
     * MimeMessage without commons-email.
     *
     * @throws EmailException if there was an error.
     * @since 1.0
     */
    @Override
    public void buildMimeMessage() throws EmailException {
        try {
            if (primaryBodyPart != null) {
                // before a multipart message can be sent, we must make sure that
                // the content for the main body part was actually set. If not,
                // an IOException will be thrown during super.send().

                final BodyPart body = getPrimaryBodyPart();
                try {
                    body.getContent();
                } catch (final IOException e) { // NOPMD
                    // do nothing here.
                    // content will be set to an empty string as a result.
                    // (Should this really be rethrown as an email exception?)
                    // throw new EmailException(e);
                }
            }

            if (subType != null) {
                getContainer().setSubType(subType);
            }

            super.buildMimeMessage();
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }
    }

    /**
     * Creates a body part object. Can be overridden if you don't want to create a BodyPart.
     *
     * @return the created body part
     */
    protected BodyPart createBodyPart() {
        return new MimeBodyPart();
    }

    /**
     * Creates a mime multipart object.
     *
     * @return the created mime part
     */
    protected MimeMultipart createMimeMultipart() {
        return new MimeMultipart();
    }

    /**
     * Gets the message container.
     *
     * @return The message container.
     * @since 1.0
     */
    protected MimeMultipart getContainer() {
        if (!initialized) {
            init();
        }
        return container;
    }

    /**
     * Gets first body part of the message.
     *
     * @return The primary body part.
     * @throws MessagingException An error occurred while getting the primary body part.
     * @since 1.0
     */
    protected BodyPart getPrimaryBodyPart() throws MessagingException {
        if (!initialized) {
            init();
        }
        // Add the first body part to the message. The fist body part must be
        if (primaryBodyPart == null) {
            primaryBodyPart = createBodyPart();
            getContainer().addBodyPart(primaryBodyPart, 0);
        }
        return primaryBodyPart;
    }

    /**
     * Gets the MIME subtype of the email.
     *
     * @return MIME subtype of the email
     * @since 1.0
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Initialize the multipart email.
     *
     * @since 1.0
     */
    protected void init() {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }
        container = createMimeMultipart();
        super.setContent(container);
        initialized = true;
    }

    /**
     * Tests whether there are attachments.
     *
     * @return true if there are attachments
     * @since 1.0
     */
    public boolean isBoolHasAttachments() {
        return hasAttachments;
    }

    /**
     * Tests if this object is initialized.
     *
     * @return true if initialized
     */
    protected boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets whether there are attachments.
     *
     * @param hasAttachments the attachments flag
     * @since 1.0
     */
    public void setBoolHasAttachments(final boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    /**
     * Sets the initialized status of this object.
     *
     * @param initialized the initialized status flag
     */
    protected void setInitialized(final boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Sets the message of the email.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    @Override
    public Email setMsg(final String msg) throws EmailException {
        EmailException.checkNonEmpty(msg, () -> "Invalid message.");
        try {
            final BodyPart primary = getPrimaryBodyPart();
            if (primary instanceof MimePart && EmailUtils.isNotEmpty(getCharsetName())) {
                ((MimePart) primary).setText(msg, getCharsetName());
            } else {
                primary.setText(msg);
            }
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }
        return this;
    }

    /**
     * Sets the MIME subtype of the email.
     *
     * @param subType MIME subtype of the email
     * @since 1.0
     */
    public void setSubType(final String subType) {
        this.subType = subType;
    }

}
