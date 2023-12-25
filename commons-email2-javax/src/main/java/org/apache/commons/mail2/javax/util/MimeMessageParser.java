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
package org.apache.commons.mail2.javax.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.apache.commons.mail2.javax.activation.InputStreamDataSource;

/**
 * Parses a MimeMessage and stores the individual parts such a plain text, HTML text and attachments.
 *
 * @since 1.3
 */
public class MimeMessageParser {

    /** The MimeMessage to convert. */
    private final MimeMessage mimeMessage;

    /** Plain mail content from MimeMessage. */
    private String plainContent;

    /** HTML mail content from MimeMessage. */
    private String htmlContent;

    /** List of attachments of MimeMessage. */
    private final List<DataSource> attachmentList;

    /** Attachments stored by their content-id. */
    private final Map<String, DataSource> cidMap;

    /** Is this a Multipart email. */
    private boolean isMultiPart;

    /**
     * Constructs an instance with the MimeMessage to be extracted.
     *
     * @param mimeMessage the message to parse
     */
    public MimeMessageParser(final MimeMessage mimeMessage) {
        this.attachmentList = new ArrayList<>();
        this.cidMap = new HashMap<>();
        this.mimeMessage = mimeMessage;
        this.isMultiPart = false;
    }

    private List<Address> asList(final Address[] recipients) {
        return recipients != null ? Arrays.asList(recipients) : new ArrayList<>();
    }

    /**
     * Parses the MimePart to create a DataSource.
     *
     * @param parent the parent multi-part
     * @param part   the current part to be processed
     * @return the DataSource
     * @throws MessagingException creating the DataSource failed
     * @throws IOException        error getting InputStream or unsupported encoding
     */
    @SuppressWarnings("resource") // Caller closes InputStream
    protected DataSource createDataSource(final Multipart parent, final MimePart part) throws MessagingException, IOException {
        final DataSource dataSource = part.getDataHandler().getDataSource();
        final String contentType = getBaseMimeType(dataSource.getContentType());
        final String dataSourceName = getDataSourceName(part, dataSource);
        return new InputStreamDataSource(dataSource.getInputStream(), contentType, dataSourceName);
    }

    /**
     * Find an attachment using its content-id.
     * <p>
     * The content-id must be stripped of any angle brackets, i.e. "part1" instead of "&lt;part1&gt;".
     * </p>
     *
     * @param cid the content-id of the attachment
     * @return the corresponding datasource or null if nothing was found
     * @since 1.3.4
     */
    public DataSource findAttachmentByCid(final String cid) {
        return cidMap.get(cid);
    }

    /**
     * Find an attachment using its name.
     *
     * @param name the name of the attachment
     * @return the corresponding datasource or null if nothing was found
     */
    public DataSource findAttachmentByName(final String name) {
        for (final DataSource dataSource : getAttachmentList()) {
            if (name.equalsIgnoreCase(dataSource.getName())) {
                return dataSource;
            }
        }
        return null;
    }

    /**
     * Gets the attachment list.
     *
     * @return Returns the attachment list.
     */
    public List<DataSource> getAttachmentList() {
        return attachmentList;
    }

    /**
     * Gets the MIME type.
     *
     * @param fullMimeType the mime type from the mail API
     * @return the real mime type
     */
    private String getBaseMimeType(final String fullMimeType) {
        final int pos = fullMimeType.indexOf(';');
        return pos < 0 ? fullMimeType : fullMimeType.substring(0, pos);
    }

    /**
     * Gets the BCC Address list.
     *
     * @return the 'BCC' recipients of the message
     * @throws MessagingException determining the recipients failed
     */
    public List<Address> getBcc() throws MessagingException {
        return asList(mimeMessage.getRecipients(Message.RecipientType.BCC));
    }

    /**
     * Gets the CC Address list.
     *
     * @return the 'CC' recipients of the message
     * @throws MessagingException determining the recipients failed
     */
    public List<Address> getCc() throws MessagingException {
        return asList(mimeMessage.getRecipients(Message.RecipientType.CC));
    }

    /**
     * Returns a collection of all content-ids in the parsed message.
     * <p>
     * The content-ids are stripped of any angle brackets, i.e. "part1" instead of "&lt;part1&gt;".
     * </p>
     *
     * @return the collection of content ids.
     * @since 1.3.4
     */
    public Collection<String> getContentIds() {
        return Collections.unmodifiableSet(cidMap.keySet());
    }

    /**
     * Determines the name of the data source if it is not already set.
     *
     * @param part       the mail part
     * @param dataSource the data source
     * @return the name of the data source or {@code null} if no name can be determined
     * @throws MessagingException           accessing the part failed
     * @throws UnsupportedEncodingException decoding the text failed
     */
    protected String getDataSourceName(final Part part, final DataSource dataSource) throws MessagingException, UnsupportedEncodingException {
        String result = dataSource.getName();
        if (isEmpty(result)) {
            result = part.getFileName();
        }
        if (!isEmpty(result)) {
            result = MimeUtility.decodeText(result);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Gets the FROM field.
     *
     * @return the FROM field of the message
     * @throws MessagingException parsing the mime message failed
     */
    public String getFrom() throws MessagingException {
        final Address[] addresses = mimeMessage.getFrom();
        if (isEmpty(addresses)) {
            return null;
        }
        return ((InternetAddress) addresses[0]).getAddress();
    }

    /**
     * Gets the htmlContent if any.
     *
     * @return Returns the htmlContent if any
     */
    public String getHtmlContent() {
        return htmlContent;
    }

    /**
     * Gets the MimeMessage.
     *
     * @return Returns the mimeMessage.
     */
    public MimeMessage getMimeMessage() {
        return mimeMessage;
    }

    /**
     * Gets the plain content if any.
     *
     * @return Returns the plainContent if any
     */
    public String getPlainContent() {
        return plainContent;
    }

    /**
     * Gets the 'replyTo' address of the email.
     *
     * @return the 'replyTo' address of the email
     * @throws MessagingException parsing the mime message failed
     */
    public String getReplyTo() throws MessagingException {
        final Address[] addresses = mimeMessage.getReplyTo();
        if (isEmpty(addresses)) {
            return null;
        }
        return ((InternetAddress) addresses[0]).getAddress();
    }

    /**
     * Gets the MIME message subject.
     *
     * @return the MIME message subject.
     * @throws MessagingException parsing the mime message failed.
     */
    public String getSubject() throws MessagingException {
        return mimeMessage.getSubject();
    }

    /**
     * Gets the MIME message 'to' list.
     *
     * @return the 'to' recipients of the message.
     * @throws MessagingException determining the recipients failed
     */
    public List<Address> getTo() throws MessagingException {
        return asList(mimeMessage.getRecipients(Message.RecipientType.TO));
    }

    /**
     * Tests if attachments are present.
     *
     * @return true if attachments are present.
     */
    public boolean hasAttachments() {
        return !attachmentList.isEmpty();
    }

    /**
     * Tests is HTML content is present.
     *
     * @return true if HTML content is present.
     */
    public boolean hasHtmlContent() {
        return htmlContent != null;
    }

    /**
     * Tests is plain content is present.
     *
     * @return true if a plain content is present.
     */
    public boolean hasPlainContent() {
        return plainContent != null;
    }

    private boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    private boolean isEmpty(final String result) {
        return result == null || result.isEmpty();
    }

    /**
     * Tests whether the MimePart contains an object of the given mime type.
     *
     * @param part     the current MimePart
     * @param mimeType the mime type to check
     * @return {@code true} if the MimePart matches the given mime type, {@code false} otherwise
     * @throws MessagingException parsing the MimeMessage failed
     */
    private boolean isMimeType(final MimePart part, final String mimeType) throws MessagingException {
        // Do not use part.isMimeType(String) as it is broken for MimeBodyPart
        // and does not really check the actual content type.
        try {
            return new ContentType(part.getDataHandler().getContentType()).match(mimeType);
        } catch (final ParseException ex) {
            return part.getContentType().equalsIgnoreCase(mimeType);
        }
    }

    /**
     * Tests whether this is multipart.
     *
     * @return Returns the isMultiPart.
     */
    public boolean isMultipart() {
        return isMultiPart;
    }

    /**
     * Does the actual extraction.
     *
     * @return this instance
     * @throws MessagingException parsing the mime message failed
     * @throws IOException        parsing the mime message failed
     */
    public MimeMessageParser parse() throws MessagingException, IOException {
        parse(null, mimeMessage);
        return this;
    }

    /**
     * Extracts the content of a MimeMessage recursively.
     *
     * @param parent the parent multi-part
     * @param part   the current MimePart
     * @throws MessagingException parsing the MimeMessage failed
     * @throws IOException        parsing the MimeMessage failed
     */
    protected void parse(final Multipart parent, final MimePart part) throws MessagingException, IOException {
        if (isMimeType(part, "text/plain") && plainContent == null && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            plainContent = (String) part.getContent();
        } else if (isMimeType(part, "text/html") && htmlContent == null && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            htmlContent = (String) part.getContent();
        } else if (isMimeType(part, "multipart/*")) {
            isMultiPart = true;
            final Multipart multipart = (Multipart) part.getContent();
            final int count = multipart.getCount();
            // iterate over all MimeBodyPart
            for (int i = 0; i < count; i++) {
                parse(multipart, (MimeBodyPart) multipart.getBodyPart(i));
            }
        } else {
            final String cid = stripContentId(part.getContentID());
            final DataSource dataSource = createDataSource(parent, part);
            if (cid != null) {
                cidMap.put(cid, dataSource);
            }
            attachmentList.add(dataSource);
        }
    }

    /**
     * Strips the content id of any whitespace and angle brackets.
     *
     * @param contentId the string to strip
     * @return a stripped version of the content id
     */
    private String stripContentId(final String contentId) {
        return contentId == null ? null : contentId.trim().replaceAll("[\\<\\>]", "");
    }
}
