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
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:unknown">Regis Koenig</a>
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */
public class MultiPartEmail extends Email
{
    /** Body portion of the email. */
    private MimeMultipart container;

    /** The message container. */
    private BodyPart primaryBodyPart;

    /** The MIME subtype. */
    private String subType;

    /** Indicates if the message has been initialized */
    private boolean initialized;

    /** Indicates if attachments have been added to the message */
    private boolean boolHasAttachments;

    /**
     * Set the MIME subtype of the email.
     *
     * @param aSubType MIME subtype of the email
     * @since 1.0
     */
    public void setSubType(String aSubType)
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
    public Email addPart(String partContent, String partContentType)
        throws EmailException
    {
            BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setContent(partContent, partContentType);
            getContainer().addBodyPart(bodyPart);
        }
        catch (MessagingException me)
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
    public Email addPart(MimeMultipart multipart) throws EmailException
    {
        try
        {
            return addPart(multipart, getContainer().getCount());
        }
        catch (MessagingException me)
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
     * @throws EmailException An error occured while adding the part.
     * @since 1.0
     */
    public Email addPart(MimeMultipart multipart, int index) throws EmailException
    {
            BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setContent(multipart);
            getContainer().addBodyPart(bodyPart, index);
        }
        catch (MessagingException me)
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
    public Email setMsg(String msg) throws EmailException
    {
        // throw exception on null message
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }
        try
        {
            BodyPart primary = getPrimaryBodyPart();

            if ((primary instanceof MimePart) && EmailUtils.isNotEmpty(charset))
            {
                ((MimePart) primary).setText(msg, charset);
            }
            else
            {
                primary.setText(msg);
            }
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
        return this;
    }

    /**
     * Builds the actual MimeMessage
     *
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            if (primaryBodyPart != null)
            {
                // before a multipart message can be sent, we must make sure that
                // the content for the main body part was actually set.  If not,
                // an IOException will be thrown during super.send().

                BodyPart body = this.getPrimaryBodyPart();
                try
                {
                    body.getContent();
                }
                catch (IOException e)
                {
                    // do nothing here.  content will be set to an empty string
                    // as a result.
                    // TODO: Should this reallyt be rethrown as an email exception:
                    // throw new EmailException(e);
                }
            }

            if (subType != null)
            {
                getContainer().setSubType(subType);
            }

            super.buildMimeMessage();
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
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
    public MultiPartEmail attach(EmailAttachment attachment)
        throws EmailException
    {
        MultiPartEmail result = null;

        if (attachment == null)
        {
            throw new EmailException("Invalid attachment supplied");
        }

        URL url = attachment.getURL();

        if (url == null)
        {
            String fileName = null;
            try
            {
                fileName = attachment.getPath();
                File file = new File(fileName);
                if (!file.exists())
                {
                    throw new IOException(
                        "\"" + fileName + "\" does not exist");
                }
                result =
                    attach(
                        new FileDataSource(file),
                        attachment.getName(),
                        attachment.getDescription(),
                        attachment.getDisposition());
            }
            catch (Exception e)
            {
                throw new EmailException(
                    "Cannot attach file \"" + fileName + "\"",
                    e);
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
    public MultiPartEmail attach(URL url, String name, String description)
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
        URL url,
        String name,
        String description,
        String disposition)
        throws EmailException
    {
        // verify that the URL is valid
       try
       {
           InputStream is = url.openStream();
           is.close();
       }
       catch (IOException e)
       {
           throw new EmailException("Invalid URL set");
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
        DataSource ds,
        String name,
        String description)
        throws EmailException
    {
        // verify that the DataSource is valid
        try
        {
            if (ds == null || ds.getInputStream() == null)
            {
                throw new EmailException("Invalid Datasource");
            }
        }
        catch (IOException e)
        {
            throw new EmailException("Invalid Datasource");
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
        DataSource ds,
        String name,
        String description,
        String disposition)
        throws EmailException
    {
        if (EmailUtils.isEmpty(name))
        {
            name = ds.getName();
        }
        BodyPart bodyPart = createBodyPart();
        try
        {
            getContainer().addBodyPart(bodyPart);

            bodyPart.setDisposition(disposition);
            bodyPart.setFileName(name);
            bodyPart.setDescription(description);
            bodyPart.setDataHandler(new DataHandler(ds));
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
        setBoolHasAttachments(true);

        return this;
    }

    /**
     * Gets first body part of the message.
     *
     * @return The primary body part.
     * @throws MessagingException An error occured while getting the primary body part.
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
        BodyPart bodyPart = new MimeBodyPart();
        return bodyPart;
    }
    /**
     * Creates a mime multipart object.
     *
     * @return the created mime part
     */
    protected MimeMultipart createMimeMultipart()
    {
        MimeMultipart mmp = new MimeMultipart();
        return mmp;
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
    public void setBoolHasAttachments(boolean b)
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
    protected void setInitialized(boolean b)
    {
        initialized = b;
    }

}
