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

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Parses a MimeMessage and stores the individual parts such a plain text,
 * HTML text and attachments.
 *
 * @since 1.3
 */
public class MimeMessageParser
{
    /** The MimeMessage to convert */
    private MimeMessage mimeMessage;

    /** Plain mail content from MimeMessage */
    private String plainContent;

    /** Html mail content from MimeMessage */
    private String htmlContent;

    /** List of attachments of MimeMessage */
    private List attachmentList;

    /** Is this a Multipart email */
    private boolean isMultiPart;

    /**
     * Constructs an instance with the MimeMessage to be extracted.
     *
     * @param message the message to parse
     */
    public MimeMessageParser(MimeMessage message)
    {
        attachmentList = new ArrayList();
        this.mimeMessage = message;
        this.isMultiPart = false;
    }

    /**
     * Does the actual extraction.
     *
     * @return this instance
     * @throws Exception parsing the mime message failed
     */
    public MimeMessageParser parse() throws Exception
    {
        this.parse(null, mimeMessage);
        return this;
    }

    /**
     * @return the 'to' recipents of the message
     * @throws Exception determining the recipients failed
     */
    public Collection getTo() throws Exception
    {
        javax.mail.Address[] recipients = this.mimeMessage.getRecipients(Message.RecipientType.TO);
        return recipients != null ? Arrays.asList(recipients) : new ArrayList();
    }

    /**
     * @return the 'cc' recipents of the message
     * @throws Exception determining the recipients failed
     */
    public Collection getCc() throws Exception
    {
        javax.mail.Address[] recipients = this.mimeMessage.getRecipients(Message.RecipientType.CC);
        return recipients != null ? Arrays.asList(recipients) : new ArrayList();
    }

    /**
     * @return the 'bcc' recipents of the message
     * @throws Exception determining the recipients failed
     */
    public Collection getBcc() throws Exception
    {
        javax.mail.Address[] recipients = this.mimeMessage.getRecipients(Message.RecipientType.BCC);
        return recipients != null ? Arrays.asList(recipients) : new ArrayList();
    }

    /**
     * @return the 'from' field of the message
     * @throws Exception parsing the mime message failed
     */
    public String getFrom() throws Exception
    {
        javax.mail.Address[] addresses = this.mimeMessage.getFrom();
        if ((addresses == null) || (addresses.length == 0))
        {
            return null;
        }
        else
        {
            return ((InternetAddress) addresses[0]).getAddress();
        }
    }

    /**
     * @return the 'replyTo' address of the email
     * @throws Exception parsing the mime message failed
     */
    public String getReplyTo() throws Exception
    {
        javax.mail.Address[] addresses = this.mimeMessage.getReplyTo();
        if ((addresses == null) || (addresses.length == 0))
        {
            return null;
        }
        else
        {
            return ((InternetAddress) addresses[0]).getAddress();
        }
    }

    /**
     * @return the mail subject
     * @throws Exception parsing the mime message failed
     */
    public String getSubject() throws Exception
    {
        return this.mimeMessage.getSubject();
    }

    /**
     * Extracts the content of a MimeMessage recursively.
     *
     * @param parent the parent Mulitpart
     * @param part   the current MimePart
     * @throws MessagingException parsing the MimeMessage failed
     * @throws IOException        parsing the MimeMessage failed
     */
    protected void parse(Multipart parent, MimePart part)
        throws MessagingException, IOException
    {
        if (part.isMimeType("text/plain") && (plainContent == null))
        {
            plainContent = (String) part.getContent();
        }
        else
        {
            if (part.isMimeType("text/html") && (htmlContent == null))
            {
                htmlContent = (String) part.getContent();
            }
            else
            {
                if (part.isMimeType("multipart/*"))
                {
                    this.isMultiPart = true;
                    Multipart mp = (Multipart) part.getContent();
                    int count = mp.getCount();

                    // iterate over all MimeBodyPart

                    for (int i = 0; i < count; i++)
                    {
                        parse(mp, (MimeBodyPart) mp.getBodyPart(i));
                    }
                }
                else
                {
                    this.attachmentList.add(createDataSource(parent, part));
                }
            }
        }
    }

    /**
     * Parses the MimePart to create a DataSource.
     *
     * @param parent the parent MultiPart
     * @param part   the part to be processed
     * @return the DataSource
     * @throws MessagingException creating the DataSource failed
     * @throws IOException        creating the DataSource failed
     */
    protected DataSource createDataSource(Multipart parent, MimePart part)
        throws MessagingException, IOException
    {
        DataHandler dataHandler = part.getDataHandler();
        DataSource dataSource = dataHandler.getDataSource();
        String contentType = getBaseMimeType(dataSource.getContentType());
        byte[] content = this.getContent(dataSource.getInputStream());
        ByteArrayDataSource result = new ByteArrayDataSource(content, contentType);
        String dataSourceName = MimeUtility.decodeText(dataSource.getName());

        result.setName(dataSourceName);
        return result;
    }

    /** @return Returns the mimeMessage. */
    public MimeMessage getMimeMessage()
    {
        return mimeMessage;
    }

    /** @return Returns the isMultiPart. */
    public boolean isMultipart()
    {
        return isMultiPart;
    }

    /** @return Returns the plainContent if any */
    public String getPlainContent()
    {
        return plainContent;
    }

    /** @return Returns the attachmentList. */
    public List getAttachmentList()
    {
        return attachmentList;
    }

    /** @return Returns the htmlContent if any */
    public String getHtmlContent()
    {
        return htmlContent;
    }

    /** @return true if a plain content is available */
    public boolean hasPlainContent()
    {
        return this.plainContent != null;
    }

    /** @return true if HTML content is available */
    public boolean hasHtmlContent()
    {
        return this.htmlContent != null;
    }

    /** @return true if attachments are available */
    public boolean hasAttachments()
    {
        return this.attachmentList.size() > 0;
    }

    /**
     * Find an attachment using its name.
     *
     * @param name the name of the attachment
     * @return the corresponding datasource or null if nothing was found
     */
    public DataSource findAttachmentByName(String name)
    {
        DataSource dataSource;

        for (int i = 0; i < getAttachmentList().size(); i++)
        {
            dataSource = (DataSource) getAttachmentList().get(i);
            if (name.equalsIgnoreCase(dataSource.getName()))
            {
                return dataSource;
            }
        }

        return null;
    }

    /**
     * Read the content of the input stream.
     *
     * @param is the input stream to process
     * @return the content of the input stream
     * @throws IOException reading the input stream failed
     */
    private byte[] getContent(InputStream is)
        throws IOException
    {
        int ch;
        byte[] result;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedInputStream isReader = new BufferedInputStream(is);
        BufferedOutputStream osWriter = new BufferedOutputStream(os);

        while ((ch = isReader.read()) != -1)
        {
            osWriter.write(ch);
        }

        osWriter.flush();
        result = os.toByteArray();
        osWriter.close();

        return result;
    }

    /**
     * Parses the mimeType.
     *
     * @param fullMimeType the mime type from the mail api
     * @return the real mime type
     */
    private String getBaseMimeType(String fullMimeType)
    {
        int pos = fullMimeType.indexOf(';');
        if (pos >= 0)
        {
            return fullMimeType.substring(0, pos);
        }
        else
        {
            return fullMimeType;
        }
    }
}
