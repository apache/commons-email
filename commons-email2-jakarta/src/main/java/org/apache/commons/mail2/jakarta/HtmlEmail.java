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
package org.apache.commons.mail2.jakarta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.core.EmailUtils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

/**
 * An HTML multipart email.
 * <p>
 * This class is used to send HTML formatted email. A text message can also be set for HTML unaware email clients, such as text-based email clients.
 * </p>
 * <p>
 * This class also inherits from {@link MultiPartEmail}, so it is easy to add attachments to the email.
 * </p>
 * <p>
 * To send an email in HTML, one should create a {@code HtmlEmail}, then use the {@link #setFrom(String)}, {@link #addTo(String)} etc. methods. The HTML content
 * can be set with the {@link #setHtmlMsg(String)} method. The alternative text content can be set with {@link #setTextMsg(String)}.
 * </p>
 * <p>
 * Either the text or HTML can be omitted, in which case the "main" part of the multipart becomes whichever is supplied rather than a
 * {@code multipart/alternative}.
 * </p>
 * <h2>Embedding Images and Media</h2>
 * <p>
 * It is also possible to embed URLs, files, or arbitrary {@code DataSource}s directly into the body of the mail:
 * </p>
 *
 * <pre>
 * HtmlEmail he = new HtmlEmail();
 * File img = new File("my/image.gif");
 * PNGDataSource png = new PNGDataSource(decodedPNGOutputStream); // a custom class
 * StringBuffer msg = new StringBuffer();
 * msg.append("&lt;html&gt;&lt;body&gt;");
 * msg.append("&lt;img src=cid:").append(he.embed(img)).append("&gt;");
 * msg.append("&lt;img src=cid:").append(he.embed(png)).append("&gt;");
 * msg.append("&lt;/body&gt;&lt;/html&gt;");
 * he.setHtmlMsg(msg.toString());
 * // code to set the other email fields (not shown)
 * </pre>
 * <p>
 * Embedded entities are tracked by their name, which for {@code File}s is the file name itself and for {@code URL}s is the canonical path. It is an error to
 * bind the same name to more than one entity, and this class will attempt to validate that for {@code File}s and {@code URL}s. When embedding a
 * {@code DataSource}, the code uses the {@code equals()} method defined on the {@code DataSource}s to make the determination.
 * </p>
 *
 * @since 1.0
 */
public class HtmlEmail extends MultiPartEmail {

    /**
     * Private bean class that encapsulates data about URL contents that are embedded in the final email.
     *
     * @since 1.1
     */
    private static final class InlineImage {

        /** Content id. */
        private final String cid;

        /** {@code DataSource} for the content. */
        private final DataSource dataSource;

        /** The {@code MimeBodyPart} that contains the encoded data. */
        private final MimeBodyPart mimeBodyPart;

        /**
         * Creates an InlineImage object to represent the specified content ID and {@code MimeBodyPart}.
         *
         * @param cid          the generated content ID, not null.
         * @param dataSource   the {@code DataSource} that represents the content, not null.
         * @param mimeBodyPart the {@code MimeBodyPart} that contains the encoded data, not null.
         */
        private InlineImage(final String cid, final DataSource dataSource, final MimeBodyPart mimeBodyPart) {
            this.cid = Objects.requireNonNull(cid, "cid");
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
            this.mimeBodyPart = Objects.requireNonNull(mimeBodyPart, "mimeBodyPart");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof InlineImage)) {
                return false;
            }
            final InlineImage other = (InlineImage) obj;
            return Objects.equals(cid, other.cid);
        }

        /**
         * Returns the unique content ID of this InlineImage.
         *
         * @return the unique content ID of this InlineImage
         */
        private String getCid() {
            return cid;
        }

        /**
         * Returns the {@code DataSource} that represents the encoded content.
         *
         * @return the {@code DataSource} representing the encoded content
         */
        private DataSource getDataSource() {
            return dataSource;
        }

        /**
         * Returns the {@code MimeBodyPart} that contains the encoded InlineImage data.
         *
         * @return the {@code MimeBodyPart} containing the encoded InlineImage data
         */
        private MimeBodyPart getMimeBodyPart() {
            return mimeBodyPart;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cid);
        }
    }

    /** Definition of the length of generated CID's. */
    public static final int CID_LENGTH = 10;

    /** Prefix for default HTML mail. */
    private static final String HTML_MESSAGE_START = "<html><body><pre>";

    /** Suffix for default HTML mail. */
    private static final String HTML_MESSAGE_END = "</pre></body></html>";

    /**
     * Text part of the message. This will be used as alternative text if the email client does not support HTML messages.
     */
    private String text;

    /**
     * HTML part of the message.
     */
    private String html;

    /**
     * Embedded images Map&lt;String, InlineImage&gt; where the key is the user-defined image name.
     */
    private final Map<String, InlineImage> inlineEmbeds = new HashMap<>();

    /**
     * Constructs a new instance.
     */
    public HtmlEmail() {
        // empty
    }

    /**
     * @throws EmailException     EmailException
     * @throws MessagingException MessagingException
     */
    private void build() throws MessagingException, EmailException {
        final MimeMultipart rootContainer = getContainer();
        MimeMultipart bodyEmbedsContainer = rootContainer;
        MimeMultipart bodyContainer = rootContainer;
        MimeBodyPart msgHtml = null;
        MimeBodyPart msgText = null;

        rootContainer.setSubType("mixed");

        // determine how to form multiparts of email

        if (EmailUtils.isNotEmpty(html) && !EmailUtils.isEmpty(inlineEmbeds)) {
            // If HTML body and embeds are used, create a related container and add it to the root container
            bodyEmbedsContainer = new MimeMultipart("related");
            bodyContainer = bodyEmbedsContainer;
            addPart(bodyEmbedsContainer, 0);

            // If TEXT body was specified, create a alternative container and add it to the embeds container
            if (EmailUtils.isNotEmpty(text)) {
                bodyContainer = new MimeMultipart("alternative");
                final BodyPart bodyPart = createBodyPart();
                try {
                    bodyPart.setContent(bodyContainer);
                    bodyEmbedsContainer.addBodyPart(bodyPart, 0);
                } catch (final MessagingException e) {
                    throw new EmailException(e);
                }
            }
        } else if (EmailUtils.isNotEmpty(text) && EmailUtils.isNotEmpty(html)) {
            // EMAIL-142: if we have both an HTML and TEXT body, but no attachments or
            // inline images, the root container should have mimetype
            // "multipart/alternative".
            // reference: https://tools.ietf.org/html/rfc2046#section-5.1.4
            if (!EmailUtils.isEmpty(inlineEmbeds) || isBoolHasAttachments()) {
                // If both HTML and TEXT bodies are provided, create an alternative
                // container and add it to the root container
                bodyContainer = new MimeMultipart("alternative");
                this.addPart(bodyContainer, 0);
            } else {
                // no attachments or embedded images present, change the mimetype
                // of the root container (= body container)
                rootContainer.setSubType("alternative");
            }
        }

        if (EmailUtils.isNotEmpty(html)) {
            msgHtml = new MimeBodyPart();
            bodyContainer.addBodyPart(msgHtml, 0);

            // EMAIL-104: call explicitly setText to use default mime charset
            // (property "mail.mime.charset") in case none has been set
            msgHtml.setText(html, getCharsetName(), EmailConstants.TEXT_SUBTYPE_HTML);

            // EMAIL-147: work-around for buggy JavaMail implementations;
            // in case setText(...) does not set the correct content type,
            // use the setContent() method instead.
            final String contentType = msgHtml.getContentType();
            if (contentType == null || !contentType.equals(EmailConstants.TEXT_HTML)) {
                // apply default charset if one has been set
                if (EmailUtils.isNotEmpty(getCharsetName())) {
                    msgHtml.setContent(html, EmailConstants.TEXT_HTML + "; charset=" + getCharsetName());
                } else {
                    // unfortunately, MimeUtility.getDefaultMIMECharset() is package private
                    // and thus can not be used to set the default system charset in case
                    // no charset has been provided by the user
                    msgHtml.setContent(html, EmailConstants.TEXT_HTML);
                }
            }

            for (final InlineImage image : inlineEmbeds.values()) {
                bodyEmbedsContainer.addBodyPart(image.getMimeBodyPart());
            }
        }

        if (EmailUtils.isNotEmpty(text)) {
            msgText = new MimeBodyPart();
            bodyContainer.addBodyPart(msgText, 0);

            // EMAIL-104: call explicitly setText to use default mime charset
            // (property "mail.mime.charset") in case none has been set
            msgText.setText(text, getCharsetName());
        }
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
            build();
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }
        super.buildMimeMessage();
    }

    /**
     * Embeds the specified {@code DataSource} in the HTML using a randomly generated Content-ID. Returns the generated Content-ID string.
     *
     * @param dataSource the {@code DataSource} to embed
     * @param name       the name that will be set in the file name header field
     * @return the generated Content-ID for this {@code DataSource}
     * @throws EmailException if the embedding fails or if {@code name} is null or empty
     * @see #embed(DataSource, String, String)
     * @since 1.1
     */
    public String embed(final DataSource dataSource, final String name) throws EmailException {
        // check if the DataSource has already been attached;
        // if so, return the cached CID value.
        final InlineImage inlineImage = inlineEmbeds.get(name);
        if (inlineImage != null) {
            // make sure the supplied URL points to the same thing
            // as the one already associated with this name.
            if (dataSource.equals(inlineImage.getDataSource())) {
                return inlineImage.getCid();
            }
            throw new EmailException("embedded DataSource '" + name + "' is already bound to name " + inlineImage.getDataSource().toString()
                    + "; existing names cannot be rebound");
        }

        final String cid = EmailUtils.toLower(EmailUtils.randomAlphabetic(HtmlEmail.CID_LENGTH));
        return embed(dataSource, name, cid);
    }

    /**
     * Embeds the specified {@code DataSource} in the HTML using the specified Content-ID. Returns the specified Content-ID string.
     *
     * @param dataSource the {@code DataSource} to embed
     * @param name       the name that will be set in the file name header field
     * @param cid        the Content-ID to use for this {@code DataSource}
     * @return the URL encoded Content-ID for this {@code DataSource}
     * @throws EmailException if the embedding fails or if {@code name} is null or empty
     * @since 1.1
     */
    public String embed(final DataSource dataSource, final String name, final String cid) throws EmailException {
        EmailException.checkNonEmpty(name, () -> "Name cannot be null or empty");
        final MimeBodyPart mbp = new MimeBodyPart();
        try {
            // URL encode the cid according to RFC 2392
            final String encodedCid = EmailUtils.encodeUrl(cid);
            mbp.setDataHandler(new DataHandler(dataSource));
            mbp.setFileName(name);
            mbp.setDisposition(EmailAttachment.INLINE);
            mbp.setContentID("<" + encodedCid + ">");
            this.inlineEmbeds.put(name, new InlineImage(encodedCid, dataSource, mbp));
            return encodedCid;
        } catch (final MessagingException e) {
            throw new EmailException(e);
        }
    }

    /**
     * Embeds a file in the HTML. This implementation delegates to {@link #embed(File, String)}.
     *
     * @param file The {@code File} object to embed
     * @return A String with the Content-ID of the file.
     * @throws EmailException when the supplied {@code File} cannot be used; also see {@link jakarta.mail.internet.MimeBodyPart} for definitions
     *
     * @see #embed(File, String)
     * @since 1.1
     */
    public String embed(final File file) throws EmailException {
        return embed(file, EmailUtils.toLower(EmailUtils.randomAlphabetic(HtmlEmail.CID_LENGTH)));
    }

    /**
     * Embeds a file in the HTML.
     *
     * <p>
     * This method embeds a file located by an URL into the mail body. It allows, for instance, to add inline images to the email. Inline files may be
     * referenced with a {@code cid:xxxxxx} URL, where xxxxxx is the Content-ID returned by the embed function. Files are bound to their names, which is the
     * value returned by {@link java.io.File#getName()}. If the same file is embedded multiple times, the same CID is guaranteed to be returned.
     *
     * <p>
     * While functionally the same as passing {@code FileDataSource} to {@link #embed(DataSource, String, String)}, this method attempts to validate the file
     * before embedding it in the message and will throw {@code EmailException} if the validation fails. In this case, the {@code HtmlEmail} object will not be
     * changed.
     *
     * @param file The {@code File} to embed
     * @param cid  the Content-ID to use for the embedded {@code File}
     * @return A String with the Content-ID of the file.
     * @throws EmailException when the supplied {@code File} cannot be used or if the file has already been embedded; also see
     *                        {@link jakarta.mail.internet.MimeBodyPart} for definitions
     * @since 1.1
     */
    public String embed(final File file, final String cid) throws EmailException {
        EmailException.checkNonEmpty(file.getName(), () -> "File name cannot be null or empty");

        // verify that the File can provide a canonical path
        String filePath = null;
        try {
            filePath = file.getCanonicalPath();
        } catch (final IOException e) {
            throw new EmailException("couldn't get canonical path for " + file.getName(), e);
        }

        // check if a FileDataSource for this name has already been attached;
        // if so, return the cached CID value.
        final InlineImage inlineImage = inlineEmbeds.get(file.getName());
        if (inlineImage != null) {
            final FileDataSource fileDataSource = (FileDataSource) inlineImage.getDataSource();
            // make sure the supplied file has the same canonical path
            // as the one already associated with this name.
            String existingFilePath = null;
            try {
                existingFilePath = fileDataSource.getFile().getCanonicalPath();
            } catch (final IOException e) {
                throw new EmailException("couldn't get canonical path for file " + fileDataSource.getFile().getName() + "which has already been embedded", e);
            }
            if (filePath.equals(existingFilePath)) {
                return inlineImage.getCid();
            }
            throw new EmailException(
                    "embedded name '" + file.getName() + "' is already bound to file " + existingFilePath + "; existing names cannot be rebound");
        }

        // verify that the file is valid
        if (!file.exists()) {
            throw new EmailException("file " + filePath + " doesn't exist");
        }
        if (!file.isFile()) {
            throw new EmailException("file " + filePath + " isn't a normal file");
        }
        if (!file.canRead()) {
            throw new EmailException("file " + filePath + " isn't readable");
        }

        return embed(new FileDataSource(file), file.getName(), cid);
    }

    /**
     * Parses the specified {@code String} as a URL that will then be embedded in the message.
     *
     * @param urlString String representation of the URL.
     * @param name      The name that will be set in the file name header field.
     * @return A String with the Content-ID of the URL.
     * @throws EmailException when URL supplied is invalid or if {@code name} is null or empty; also see {@link jakarta.mail.internet.MimeBodyPart} for
     *                        definitions
     *
     * @see #embed(URL, String)
     * @since 1.1
     */
    public String embed(final String urlString, final String name) throws EmailException {
        try {
            return embed(new URL(urlString), name);
        } catch (final MalformedURLException e) {
            throw new EmailException("Invalid URL", e);
        }
    }

    /**
     * Embeds an URL in the HTML.
     *
     * <p>
     * This method embeds a file located by an URL into the mail body. It allows, for instance, to add inline images to the email. Inline files may be
     * referenced with a {@code cid:xxxxxx} URL, where xxxxxx is the Content-ID returned by the embed function. It is an error to bind the same name to more
     * than one URL; if the same URL is embedded multiple times, the same Content-ID is guaranteed to be returned.
     * </p>
     * <p>
     * While functionally the same as passing {@code URLDataSource} to {@link #embed(DataSource, String, String)}, this method attempts to validate the URL
     * before embedding it in the message and will throw {@code EmailException} if the validation fails. In this case, the {@code HtmlEmail} object will not be
     * changed.
     * </p>
     * <p>
     * NOTE: Clients should take care to ensure that different URLs are bound to different names. This implementation tries to detect this and throw
     * {@code EmailException}. However, it is not guaranteed to catch all cases, especially when the URL refers to a remote HTTP host that may be part of a
     * virtual host cluster.
     * </p>
     *
     * @param url  The URL of the file.
     * @param name The name that will be set in the file name header field.
     * @return A String with the Content-ID of the file.
     * @throws EmailException when URL supplied is invalid or if {@code name} is null or empty; also see {@link jakarta.mail.internet.MimeBodyPart} for
     *                        definitions
     * @since 1.0
     */
    public String embed(final URL url, final String name) throws EmailException {
        EmailException.checkNonEmpty(name, () -> "Name cannot be null or empty");
        // check if a URLDataSource for this name has already been attached;
        // if so, return the cached CID value.
        final InlineImage inlineImage = inlineEmbeds.get(name);
        if (inlineImage != null) {
            final URLDataSource urlDataSource = (URLDataSource) inlineImage.getDataSource();
            // make sure the supplied URL points to the same thing
            // as the one already associated with this name.
            // NOTE: Comparing URLs with URL.equals() is a blocking operation
            // in the case of a network failure therefore we use
            // url.toExternalForm().equals() here.
            if (url.toExternalForm().equals(urlDataSource.getURL().toExternalForm())) {
                return inlineImage.getCid();
            }
            throw new EmailException("embedded name '" + name + "' is already bound to URL " + urlDataSource.getURL() + "; existing names cannot be rebound");
        }
        // verify that the URL is valid
        try (InputStream inputStream = url.openStream()) {
            // Make sure we can read.
            inputStream.read();
        } catch (final IOException e) {
            throw new EmailException("Invalid URL", e);
        }
        return embed(new URLDataSource(url), name);
    }

    /**
     * Gets the HTML content.
     *
     * @return the HTML content.
     * @since 1.6.0
     */
    public String getHtml() {
        return html;
    }

    /**
     * Gets the message text.
     *
     * @return the message text.
     * @since 1.6.0
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the HTML content.
     *
     * @param html A String.
     * @return An HtmlEmail.
     * @throws EmailException see jakarta.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public HtmlEmail setHtmlMsg(final String html) throws EmailException {
        this.html = EmailException.checkNonEmpty(html, () -> "Invalid message.");
        return this;
    }

    /**
     * Sets the message.
     *
     * <p>
     * This method overrides {@link MultiPartEmail#setMsg(String)} in order to send an HTML message instead of a plain text message in the mail body. The
     * message is formatted in HTML for the HTML part of the message; it is left as is in the alternate text part.
     * </p>
     *
     * @param msg the message text to use
     * @return this {@code HtmlEmail}
     * @throws EmailException if msg is null or empty; see jakarta.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    @Override
    public Email setMsg(final String msg) throws EmailException {
        setTextMsg(msg);
        final StringBuilder htmlMsgBuf = new StringBuilder(msg.length() + HTML_MESSAGE_START.length() + HTML_MESSAGE_END.length());
        htmlMsgBuf.append(HTML_MESSAGE_START).append(msg).append(HTML_MESSAGE_END);
        setHtmlMsg(htmlMsgBuf.toString());
        return this;
    }

    /**
     * Sets the text content.
     *
     * @param text A String.
     * @return An HtmlEmail.
     * @throws EmailException see jakarta.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public HtmlEmail setTextMsg(final String text) throws EmailException {
        this.text = EmailException.checkNonEmpty(text, () -> "Invalid message.");
        return this;
    }
}
