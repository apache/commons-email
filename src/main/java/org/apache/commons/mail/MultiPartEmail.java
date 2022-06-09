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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

/**
 * A multipart email.
 *
 * <p>This class is used to send multi-part internet email like
 * messages with attachments.
 *
 * <p>To create a multi-part email, call the default constructor and
 * then you can call setMsg() to set the message and call the
 * different attach() methods.
 *
 * @since 1.0
 */
public class MultiPartEmail extends Email
{
    /** Body portion of the email. */
    private MimeMultipart container;

    /** The message container. */
    private BodyPart primaryBodyPart;

    /** The MIME subtype. */
    private String subType;

    /** Indicates if the message has been initialized. */
    private boolean initialized;

    /** Indicates if attachments have been added to the message. */
    private boolean boolHasAttachments;

    /**
     * Set the MIME subtype of the email.
     *
     * @param aSubType MIME subtype of the email
     * @since 1.0
     */
    public void setSubType(final String aSubType)
    {
        this.subType = aSubType;
    }

    /**
     * Get the MIME subtype of the email.
     *
     * @return MIME subtype of the email
     * @since 1.0
     */
    public String getSubType()
    {
        return subType;
    }

    /**
     * Add a new part to the email.
     *
     * @param partContent The content.
     * @param partContentType The content type.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public Email addPart(final String partContent, final String partContentType)
        throws EmailException
    {
            final BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setContent(partContent, partContentType);
            getContainer().addBodyPart(bodyPart);
        }
        catch (final MessagingException me)
        {
            throw new EmailException(me);
        }

        return this;
    }

    /**
     * Add a new part to the email.
     *
     * @param multipart The MimeMultipart.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     *  @since 1.0
     */
    public Email addPart(final MimeMultipart multipart) throws EmailException
    {
        try
        {
            return addPart(multipart, getContainer().getCount());
        }
        catch (final MessagingException me)
        {
            throw new EmailException(me);
        }
    }

    /**
     * Add a new part to the email.
     *
     * @param multipart The part to add.
     * @param index The index to add at.
     * @return The email.
     * @throws EmailException An error occurred while adding the part.
     * @since 1.0
     */
    public Email addPart(final MimeMultipart multipart, final int index) throws EmailException
    {
        final BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setContent(multipart);
            getContainer().addBodyPart(bodyPart, index);
        }
        catch (final MessagingException me)
        {
            throw new EmailException(me);
        }

        return this;
    }

    /**
     * Initialize the multipart email.
     * @since 1.0
     */
    protected void init()
    {
        if (initialized)
        {
            throw new IllegalStateException("Already initialized");
        }

        container = createMimeMultipart();
        super.setContent(container);

        initialized = true;
    }

    /**
     * Set the message of the email.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    @Override
    public Email setMsg(final String msg) throws EmailException
    {
        // throw exception on null message
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }
        try
        {
            final BodyPart primary = getPrimaryBodyPart();

            if (primary instanceof MimePart && EmailUtils.isNotEmpty(charset))
            {
                ((MimePart) primary).setText(msg, charset);
            }
            else
            {
                primary.setText(msg);
            }
        }
        catch (final MessagingException me)
        {
            throw new EmailException(me);
        }
        return this;
    }

    /**
     * Does the work of actually building the MimeMessage. Please note that
     * a user rarely calls this method directly and only if he/she is
     * interested in the sending the underlying MimeMessage without
     * commons-email.
     *
     * @throws EmailException if there was an error.
     * @since 1.0
     */
    @Override
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            if (primaryBodyPart != null)
            {
                // before a multipart message can be sent, we must make sure that
                // the content for the main body part was actually set.  If not,
                // an IOException will be thrown during super.send().

                final BodyPart body = this.getPrimaryBodyPart();
                try
                {
                    body.getContent();
                }
                catch (final IOException e) // NOPMD
                {
                    // do nothing here.
                    // content will be set to an empty string as a result.
                    // (Should this really be rethrown as an email exception?)
                    // throw new EmailException(e);
                }
            }

            if (subType != null)
            {
                getContainer().setSubType(subType);
            }

            super.buildMimeMessage();
        }
        catch (final MessagingException me)
        {
            throw new EmailException(me);
        }
    }

    /**
     * Attach a file.
     *
     * @param file A file attachment
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.3
     */
    public MultiPartEmail attach(final File file)
        throws EmailException
    {
        final String fileName = file.getAbsolutePath();

        try
        {
            if (!file.exists())
            {
                throw new IOException("\"" + fileName + "\" does not exist");
            }

            final FileDataSource fds = new FileDataSource(file);

            return attach(fds, file.getName(), null, EmailAttachment.ATTACHMENT);
        }
        catch (final IOException e)
        {
            throw new EmailException("Cannot attach file \"" + fileName + "\"", e);
        }
    }

    /**
     * Attach an EmailAttachment.
     *
     * @param attachment An EmailAttachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final EmailAttachment attachment)
        throws EmailException
    {
        MultiPartEmail result = null;

        if (attachment == null)
        {
            throw new EmailException("Invalid attachment supplied");
        }

        final URL url = attachment.getURL();

        if (url == null)
        {
            String fileName = null;
            try
            {
                fileName = attachment.getPath();
                final File file = new File(fileName);
                if (!file.exists())
                {
                    throw new IOException("\"" + fileName + "\" does not exist");
                }
                result =
                    attach(
                        new FileDataSource(file),
                        attachment.getName(),
                        attachment.getDescription(),
                        attachment.getDisposition());
            }
            catch (final IOException e)
            {
                throw new EmailException("Cannot attach file \"" + fileName + "\"", e);
            }
        }
        else
        {
            result =
                attach(
                    url,
                    attachment.getName(),
                    attachment.getDescription(),
                    attachment.getDisposition());
        }

        return result;
    }

    /**
     * Attach a file located by its URL.  The disposition of the file
     * is set to mixed.
     *
     * @param url The URL of the file (may be any valid URL).
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(final URL url, final String name, final String description)
        throws EmailException
    {
        return attach(url, name, description, EmailAttachment.ATTACHMENT);
    }

    /**
     * Attach a file located by its URL.
     *
     * @param url The URL of the file (may be any valid URL).
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(
        final URL url,
        final String name,
        final String description,
        final String disposition)
        throws EmailException
    {
        // verify that the URL is valid
       try
       {
           url.openStream().close();
       }
       catch (final IOException e)
       {
           throw new EmailException("Invalid URL set:" + url, e);
       }

       return attach(new URLDataSource(url), name, description, disposition);
    }

    /**
     * Attach a file specified as a DataSource interface.
     *
     * @param ds A DataSource interface for the file.
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(
        final DataSource ds,
        final String name,
        final String description)
        throws EmailException
    {
        if (ds == null)
        {
            throw new EmailException("Invalid Datasource");
        }
        // verify that the DataSource is valid
        try (InputStream is = ds.getInputStream())
        {
            if (is == null)
            {
                throw new EmailException("Invalid Datasource");
            }
        }
        catch (final IOException e)
        {
            throw new EmailException("Invalid Datasource", e);
        }
        return attach(ds, name, description, EmailAttachment.ATTACHMENT);
    }

    /**
     * Attach a file specified as a DataSource interface.
     *
     * @param ds A DataSource interface for the file.
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public MultiPartEmail attach(
        final DataSource ds,
        String name,
        final String description,
        final String disposition)
        throws EmailException
    {
        if (EmailUtils.isEmpty(name))
        {
            name = ds.getName();
        }
        final BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setDisposition(disposition);
            bodyPart.setFileName(MimeUtility.encodeText(name));
            bodyPart.setDescription(description);
            bodyPart.setDataHandler(new DataHandler(ds));

            getContainer().addBodyPart(bodyPart);
        }
        catch (final UnsupportedEncodingException | MessagingException me)
        {
            // in case the file name could not be encoded
            throw new EmailException(me);
        }
        setBoolHasAttachments(true);

        return this;
    }

    /**
     * Gets first body part of the message.
     *
     * @return The primary body part.
     * @throws MessagingException An error occurred while getting the primary body part.
     * @since 1.0
     */
    protected BodyPart getPrimaryBodyPart() throws MessagingException
    {
        if (!initialized)
        {
            init();
        }

        // Add the first body part to the message.  The fist body part must be
        if (this.primaryBodyPart == null)
        {
            primaryBodyPart = createBodyPart();
            getContainer().addBodyPart(primaryBodyPart, 0);
        }

        return primaryBodyPart;
    }

    /**
     * Gets the message container.
     *
     * @return The message container.
     * @since 1.0
     */
    protected MimeMultipart getContainer()
    {
        if (!initialized)
        {
            init();
        }
        return container;
    }

    /**
     * Creates a body part object.
     * Can be overridden if you don't want to create a BodyPart.
     *
     * @return the created body part
     */
    protected BodyPart createBodyPart()
    {
        return new MimeBodyPart();
    }

    /**
     * Creates a mime multipart object.
     *
     * @return the created mime part
     */
    protected MimeMultipart createMimeMultipart()
    {
        return new MimeMultipart();
    }

    /**
     * Checks whether there are attachments.
     *
     * @return true if there are attachments
     * @since 1.0
     */
    public boolean isBoolHasAttachments()
    {
        return boolHasAttachments;
    }

    /**
     * Sets whether there are attachments.
     *
     * @param b  the attachments flag
     * @since 1.0
     */
    public void setBoolHasAttachments(final boolean b)
    {
        boolHasAttachments = b;
    }

    /**
     * Checks if this object is initialized.
     *
     * @return true if initialized
     */
    protected boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Sets the initialized status of this object.
     *
     * @param b  the initialized status flag
     */
    protected void setInitialized(final boolean b)
    {
        initialized = b;
    }

}
