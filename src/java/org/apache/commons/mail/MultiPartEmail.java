/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang.StringUtils;

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
    private MimeMultipart container = null;

    /** The message container. */
    private MimeBodyPart primaryBodyPart = null;

    /** The MIME subtype. */
    private String subType = null;

    /** Indicates if the message has been initialized */
    private boolean initialized = false;

    /** Indicates if attachments have been added to the message */
    private boolean boolHasAttachments = false;
    
    /**
     * Set the MIME subtype of the email.
     * @param aSubType MIME subtype of the email
     */
    public void setSubType(String aSubType)
    {
        this.subType = aSubType;
    }

    /**
     * Get the MIME subtype of the email.
     * @return MIME subtype of the email
     */
    public String getSubType()
    {
        return subType;
    }

    /**
     * Add a new part to the email.
     * @param content The content.
     * @param contentType The content type.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    public Email addPart(String content, String contentType)
        throws EmailException
    {
        MimeBodyPart bodyPart = new MimeBodyPart();
        try
        {
            bodyPart.setContent(content, contentType);
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
     * @param multipart The MimeMultipart.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    public Email addPart(MimeMultipart multipart) throws EmailException
    {
        MimeBodyPart bodyPart = new MimeBodyPart();
        try {
            bodyPart.setContent(multipart);
            getContainer().addBodyPart(bodyPart);
        }
        catch (MessagingException me){
            throw new EmailException(me);
        }            

        return this;
    }

    /**
     * Initialize the multipart email.
     *
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    protected void init()
    {
        if (initialized)
        {
            throw new IllegalStateException("Already initialized");
        }

        container = new MimeMultipart();
        super.setContent(container);       

        initialized = true;
    }

    /**
     * Set the message of the email.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    public Email setMsg(String msg) throws EmailException
    {
        // throw exception on null message
        if (StringUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }
        try {
            if (StringUtils.isNotEmpty(charset))
            {
                getPrimaryBodyPart().setText(msg, charset);
            }
            else
            {
                getPrimaryBodyPart().setText(msg);
            }
        }
        catch (MessagingException me){
            throw new EmailException(me);
        }  
        return this;
    }

    /**
     * Sends the mail message
     *
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    public void send() throws EmailException
    {
        try {
            if (primaryBodyPart != null)
            {
                // before a multipart message can be sent, we must make sure that
                // the content for the main body part was actually set.  If not,
                // an IOException will be thrown during super.send().
    
                MimeBodyPart body = this.getPrimaryBodyPart();
                Object content = null;
                try
                {
                    content = body.getContent();
                }
                catch (IOException e)
                {
                    // do nothing here.  content will be set to an empty string
                    // as a result.
                    content = null;
                }
            }
    
            if (subType != null)
            {
                getContainer().setSubType(subType);
            }
    
            super.send();
        }
        catch (MessagingException me){
            throw new EmailException(me);
        }
    }

    /**
     * Attach an EmailAttachement.
     *
     * @param attachment An EmailAttachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
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
     *  for defintions
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
     *  for defintions
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
     *  for defintions
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
     * @param description A description for the attachement.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    public MultiPartEmail attach(
        DataSource ds,
        String name,
        String description,
        String disposition)
        throws EmailException
    {
        if (StringUtils.isEmpty(name))
        {
            name = ds.getName();
        }
        MimeBodyPart mbp = new MimeBodyPart();
        try {
            getContainer().addBodyPart(mbp);

            mbp.setDisposition(disposition);
            mbp.setFileName(name);
            mbp.setDescription(description);
            mbp.setDataHandler(new DataHandler(ds));
        }
        catch (MessagingException me){
            throw new EmailException(me);
        }            
        this.boolHasAttachments = true;
        
        return this;
    }

    /**
     * Gets first body part of the message.
     *
     * @return The primary body part.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    protected MimeBodyPart getPrimaryBodyPart() throws MessagingException
    {
        if (!initialized)
        {
            init();
        }
        
        // Add the first body part to the message.  The fist body part must be
        if (this.primaryBodyPart == null)
        {
            primaryBodyPart = new MimeBodyPart();
            getContainer().addBodyPart(primaryBodyPart);
        }

        return primaryBodyPart;
    }

    /**
     * Gets the message container.
     *
     * @return The message container.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
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
     * @return boolHasAttachments
     */
    public boolean isBoolHasAttachments()
    {
        return boolHasAttachments;
    }

    /**
     * @param b boolHasAttachments
     */
    public void setBoolHasAttachments(boolean b)
    {
        boolHasAttachments = b;
    }

}
