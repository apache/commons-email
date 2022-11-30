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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * An HTML multipart email.
 *
 * <p>This class is used to send HTML formatted email.  A text message
 * can also be set for HTML unaware email clients, such as text-based
 * email clients.
 *
 * <p>This class also inherits from {@link MultiPartEmail}, so it is easy to
 * add attachments to the email.
 *
 * <p>To send an email in HTML, one should create a {@code HtmlEmail}, then
 * use the {@link #setFrom(String)}, {@link #addTo(String)} etc. methods.
 * The HTML content can be set with the {@link #setHtmlMsg(String)} method. The
 * alternative text content can be set with {@link #setTextMsg(String)}.
 *
 * <p>Either the text or HTML can be omitted, in which case the "main"
 * part of the multipart becomes whichever is supplied rather than a
 * {@code multipart/alternative}.
 *
 * <h2>Embedding Images and Media</h2>
 *
 * <p>It is also possible to embed URLs, files, or arbitrary
 * {@code DataSource}s directly into the body of the mail:
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
 *
 * <p>Embedded entities are tracked by their name, which for {@code File}s is
 * the file name itself and for {@code URL}s is the canonical path. It is
 * an error to bind the same name to more than one entity, and this class will
 * attempt to validate that for {@code File}s and {@code URL}s. When
 * embedding a {@code DataSource}, the code uses the {@code equals()}
 * method defined on the {@code DataSource}s to make the determination.
 *
 * @since 1.0
 */
public class HtmlEmail extends MultiPartEmail
{
    /** Definition of the length of generated CID's. */
    public static final int CID_LENGTH = 10;

    /** prefix for default HTML mail. */
    private static final String HTML_MESSAGE_START = "<html><body><pre>";
    /** suffix for default HTML mail. */
    private static final String HTML_MESSAGE_END = "</pre></body></html>";


    /**
     * Text part of the message. This will be used as alternative text if
     * the email client does not support HTML messages.
     */
    protected String text;

    /** Html part of the message. */
    protected String html;

    /**
     * @deprecated As of commons-email 1.1, no longer used. Inline embedded
     * objects are now stored in {@link #inlineEmbeds}.
     */
    @Deprecated
    protected List<InlineImage> inlineImages;

    /**
     * Embedded images Map&lt;String, InlineImage&gt; where the key is the
     * user-defined image name.
     */
    protected Map<String, InlineImage> inlineEmbeds = new HashMap<>();

    /**
     * Set the text content.
     *
     * @param aText A String.
     * @return An HtmlEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public HtmlEmail setTextMsg(final String aText) throws EmailException
    {
        if (EmailUtils.isEmpty(aText))
        {
            throw new EmailException("Invalid message supplied");
        }

        this.text = aText;
        return this;
    }

    /**
     * Set the HTML content.
     *
     * @param aHtml A String.
     * @return An HtmlEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public HtmlEmail setHtmlMsg(final String aHtml) throws EmailException
    {
        if (EmailUtils.isEmpty(aHtml))
        {
            throw new EmailException("Invalid message supplied");
        }

        this.html = aHtml;
        return this;
    }

    /**
     * Set the message.
     *
     * <p>This method overrides {@link MultiPartEmail#setMsg(String)} in
     * order to send an HTML message instead of a plain text message in
     * the mail body. The message is formatted in HTML for the HTML
     * part of the message; it is left as is in the alternate text
     * part.
     *
     * @param msg the message text to use
     * @return this {@code HtmlEmail}
     * @throws EmailException if msg is null or empty;
     * see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    @Override
    public Email setMsg(final String msg) throws EmailException
    {
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }

        setTextMsg(msg);

        final StringBuilder htmlMsgBuf = new StringBuilder(
            msg.length()
            + HTML_MESSAGE_START.length()
            + HTML_MESSAGE_END.length()
        );

        htmlMsgBuf.append(HTML_MESSAGE_START)
            .append(msg)
            .append(HTML_MESSAGE_END);

        setHtmlMsg(htmlMsgBuf.toString());

        return this;
    }

    /**
     * Attempts to parse the specified {@code String} as a URL that will
     * then be embedded in the message.
     *
     * @param urlString String representation of the URL.
     * @param name The name that will be set in the file name header field.
     * @return A String with the Content-ID of the URL.
     * @throws EmailException when URL supplied is invalid or if {@code name} is null
     * or empty; also see {@link javax.mail.internet.MimeBodyPart} for definitions
     *
     * @see #embed(URL, String)
     * @since 1.1
     */
    public String embed(final String urlString, final String name) throws EmailException
    {
        try
        {
            return embed(new URL(urlString), name);
        }
        catch (final MalformedURLException e)
        {
            throw new EmailException("Invalid URL", e);
        }
    }

    /**
     * Embeds an URL in the HTML.
     *
     * <p>This method embeds a file located by an URL into
     * the mail body. It allows, for instance, to add inline images
     * to the email.  Inline files may be referenced with a
     * {@code cid:xxxxxx} URL, where xxxxxx is the Content-ID
     * returned by the embed function. It is an error to bind the same name
     * to more than one URL; if the same URL is embedded multiple times, the
     * same Content-ID is guaranteed to be returned.
     *
     * <p>While functionally the same as passing {@code URLDataSource} to
     * {@link #embed(DataSource, String, String)}, this method attempts
     * to validate the URL before embedding it in the message and will throw
     * {@code EmailException} if the validation fails. In this case, the
     * {@code HtmlEmail} object will not be changed.
     *
     * <p>
     * NOTE: Clients should take care to ensure that different URLs are bound to
     * different names. This implementation tries to detect this and throw
     * {@code EmailException}. However, it is not guaranteed to catch
     * all cases, especially when the URL refers to a remote HTTP host that
     * may be part of a virtual host cluster.
     *
     * @param url The URL of the file.
     * @param name The name that will be set in the file name header
     * field.
     * @return A String with the Content-ID of the file.
     * @throws EmailException when URL supplied is invalid or if {@code name} is null
     * or empty; also see {@link javax.mail.internet.MimeBodyPart} for definitions
     * @since 1.0
     */
    public String embed(final URL url, final String name) throws EmailException
    {
        if (EmailUtils.isEmpty(name))
        {
            throw new EmailException("name cannot be null or empty");
        }

        // check if a URLDataSource for this name has already been attached;
        // if so, return the cached CID value.
        final InlineImage ii = inlineEmbeds.get(name);
        if (ii != null)
        {
            final URLDataSource urlDataSource = (URLDataSource) ii.getDataSource();
            // make sure the supplied URL points to the same thing
            // as the one already associated with this name.
            // NOTE: Comparing URLs with URL.equals() is a blocking operation
            // in the case of a network failure therefore we use
            // url.toExternalForm().equals() here.
            if (url.toExternalForm().equals(urlDataSource.getURL().toExternalForm()))
            {
                return ii.getCid();
            }
            throw new EmailException("embedded name '" + name
                + "' is already bound to URL " + urlDataSource.getURL()
                + "; existing names cannot be rebound");
        }

        // verify that the URL is valid
        InputStream is = null;
        try
        {
            is = url.openStream();
        }
        catch (final IOException e)
        {
            throw new EmailException("Invalid URL", e);
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            catch (final IOException ioe) // NOPMD
            { /* sigh */ }
        }

        return embed(new URLDataSource(url), name);
    }

    /**
     * Embeds a file in the HTML. This implementation delegates to
     * {@link #embed(File, String)}.
     *
     * @param file The {@code File} object to embed
     * @return A String with the Content-ID of the file.
     * @throws EmailException when the supplied {@code File} cannot be
     * used; also see {@link javax.mail.internet.MimeBodyPart} for definitions
     *
     * @see #embed(File, String)
     * @since 1.1
     */
    public String embed(final File file) throws EmailException
    {
        final String cid = EmailUtils.randomAlphabetic(HtmlEmail.CID_LENGTH).toLowerCase(Locale.ENGLISH);
        return embed(file, cid);
    }

    /**
     * Embeds a file in the HTML.
     *
     * <p>This method embeds a file located by an URL into
     * the mail body. It allows, for instance, to add inline images
     * to the email.  Inline files may be referenced with a
     * {@code cid:xxxxxx} URL, where xxxxxx is the Content-ID
     * returned by the embed function. Files are bound to their names, which is
     * the value returned by {@link java.io.File#getName()}. If the same file
     * is embedded multiple times, the same CID is guaranteed to be returned.
     *
     * <p>While functionally the same as passing {@code FileDataSource} to
     * {@link #embed(DataSource, String, String)}, this method attempts
     * to validate the file before embedding it in the message and will throw
     * {@code EmailException} if the validation fails. In this case, the
     * {@code HtmlEmail} object will not be changed.
     *
     * @param file The {@code File} to embed
     * @param cid the Content-ID to use for the embedded {@code File}
     * @return A String with the Content-ID of the file.
     * @throws EmailException when the supplied {@code File} cannot be used
     *  or if the file has already been embedded;
     *  also see {@link javax.mail.internet.MimeBodyPart} for definitions
     * @since 1.1
     */
    public String embed(final File file, final String cid) throws EmailException
    {
        if (EmailUtils.isEmpty(file.getName()))
        {
            throw new EmailException("file name cannot be null or empty");
        }

        // verify that the File can provide a canonical path
        String filePath = null;
        try
        {
            filePath = file.getCanonicalPath();
        }
        catch (final IOException ioe)
        {
            throw new EmailException("couldn't get canonical path for "
                    + file.getName(), ioe);
        }

        // check if a FileDataSource for this name has already been attached;
        // if so, return the cached CID value.
        final InlineImage ii = inlineEmbeds.get(file.getName());
        if (ii != null)
        {
            final FileDataSource fileDataSource = (FileDataSource) ii.getDataSource();
            // make sure the supplied file has the same canonical path
            // as the one already associated with this name.
            String existingFilePath = null;
            try
            {
                existingFilePath = fileDataSource.getFile().getCanonicalPath();
            }
            catch (final IOException ioe)
            {
                throw new EmailException("couldn't get canonical path for file "
                        + fileDataSource.getFile().getName()
                        + "which has already been embedded", ioe);
            }
            if (filePath.equals(existingFilePath))
            {
                return ii.getCid();
            }
            throw new EmailException("embedded name '" + file.getName()
                + "' is already bound to file " + existingFilePath
                + "; existing names cannot be rebound");
        }

        // verify that the file is valid
        if (!file.exists())
        {
            throw new EmailException("file " + filePath + " doesn't exist");
        }
        if (!file.isFile())
        {
            throw new EmailException("file " + filePath + " isn't a normal file");
        }
        if (!file.canRead())
        {
            throw new EmailException("file " + filePath + " isn't readable");
        }

        return embed(new FileDataSource(file), file.getName(), cid);
    }

    /**
     * Embeds the specified {@code DataSource} in the HTML using a
     * randomly generated Content-ID. Returns the generated Content-ID string.
     *
     * @param dataSource the {@code DataSource} to embed
     * @param name the name that will be set in the file name header field
     * @return the generated Content-ID for this {@code DataSource}
     * @throws EmailException if the embedding fails or if {@code name} is
     * null or empty
     * @see #embed(DataSource, String, String)
     * @since 1.1
     */
    public String embed(final DataSource dataSource, final String name) throws EmailException
    {
        // check if the DataSource has already been attached;
        // if so, return the cached CID value.
        final InlineImage ii = inlineEmbeds.get(name);
        if (ii != null)
        {
            // make sure the supplied URL points to the same thing
            // as the one already associated with this name.
            if (dataSource.equals(ii.getDataSource()))
            {
                return ii.getCid();
            }
            throw new EmailException("embedded DataSource '" + name
                + "' is already bound to name " + ii.getDataSource().toString()
                + "; existing names cannot be rebound");
        }

        final String cid = EmailUtils.randomAlphabetic(HtmlEmail.CID_LENGTH).toLowerCase();
        return embed(dataSource, name, cid);
    }

    /**
     * Embeds the specified {@code DataSource} in the HTML using the
     * specified Content-ID. Returns the specified Content-ID string.
     *
     * @param dataSource the {@code DataSource} to embed
     * @param name the name that will be set in the file name header field
     * @param cid the Content-ID to use for this {@code DataSource}
     * @return the URL encoded Content-ID for this {@code DataSource}
     * @throws EmailException if the embedding fails or if {@code name} is
     * null or empty
     * @since 1.1
     */
    public String embed(final DataSource dataSource, final String name, final String cid)
        throws EmailException
    {
        if (EmailUtils.isEmpty(name))
        {
            throw new EmailException("name cannot be null or empty");
        }

        final MimeBodyPart mbp = new MimeBodyPart();

        try
        {
            // URL encode the cid according to RFC 2392
            final String encodedCid = EmailUtils.encodeUrl(cid);

            mbp.setDataHandler(new DataHandler(dataSource));
            mbp.setFileName(name);
            mbp.setDisposition(EmailAttachment.INLINE);
            mbp.setContentID("<" + encodedCid + ">");

            final InlineImage ii = new InlineImage(encodedCid, dataSource, mbp);
            this.inlineEmbeds.put(name, ii);

            return encodedCid;
        }
        catch (final MessagingException | UnsupportedEncodingException uee)
        {
            throw new EmailException(uee);
        }
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
            build();
        }
        catch (final MessagingException me)
        {
            throw new EmailException(me);
        }
        super.buildMimeMessage();
    }

    /**
     * @throws EmailException EmailException
     * @throws MessagingException MessagingException
     */
    private void build() throws MessagingException, EmailException
    {
        final MimeMultipart rootContainer = this.getContainer();
        MimeMultipart bodyEmbedsContainer = rootContainer;
        MimeMultipart bodyContainer = rootContainer;
        MimeBodyPart msgHtml = null;
        MimeBodyPart msgText = null;

        rootContainer.setSubType("mixed");

        // determine how to form multiparts of email

        if (EmailUtils.isNotEmpty(this.html) && !this.inlineEmbeds.isEmpty())
        {
            //If HTML body and embeds are used, create a related container and add it to the root container
            bodyEmbedsContainer = new MimeMultipart("related");
            bodyContainer = bodyEmbedsContainer;
            this.addPart(bodyEmbedsContainer, 0);

            // If TEXT body was specified, create a alternative container and add it to the embeds container
            if (EmailUtils.isNotEmpty(this.text))
            {
                bodyContainer = new MimeMultipart("alternative");
                final BodyPart bodyPart = createBodyPart();
                try
                {
                    bodyPart.setContent(bodyContainer);
                    bodyEmbedsContainer.addBodyPart(bodyPart, 0);
                }
                catch (final MessagingException me)
                {
                    throw new EmailException(me);
                }
            }
        }
        else if (EmailUtils.isNotEmpty(this.text) && EmailUtils.isNotEmpty(this.html))
        {
            // EMAIL-142: if we have both an HTML and TEXT body, but no attachments or
            //            inline images, the root container should have mimetype
            //            "multipart/alternative".
            // reference: http://tools.ietf.org/html/rfc2046#section-5.1.4
            if (!this.inlineEmbeds.isEmpty() || isBoolHasAttachments())
            {
                // If both HTML and TEXT bodies are provided, create an alternative
                // container and add it to the root container
                bodyContainer = new MimeMultipart("alternative");
                this.addPart(bodyContainer, 0);
            }
            else
            {
                // no attachments or embedded images present, change the mimetype
                // of the root container (= body container)
                rootContainer.setSubType("alternative");
            }
        }

        if (EmailUtils.isNotEmpty(this.html))
        {
            msgHtml = new MimeBodyPart();
            bodyContainer.addBodyPart(msgHtml, 0);

            // EMAIL-104: call explicitly setText to use default mime charset
            //            (property "mail.mime.charset") in case none has been set
            msgHtml.setText(this.html, this.charset, EmailConstants.TEXT_SUBTYPE_HTML);

            // EMAIL-147: work-around for buggy JavaMail implementations;
            //            in case setText(...) does not set the correct content type,
            //            use the setContent() method instead.
            final String contentType = msgHtml.getContentType();
            if (contentType == null || !contentType.equals(EmailConstants.TEXT_HTML))
            {
                // apply default charset if one has been set
                if (EmailUtils.isNotEmpty(this.charset))
                {
                    msgHtml.setContent(this.html, EmailConstants.TEXT_HTML + "; charset=" + this.charset);
                }
                else
                {
                    // unfortunately, MimeUtility.getDefaultMIMECharset() is package private
                    // and thus can not be used to set the default system charset in case
                    // no charset has been provided by the user
                    msgHtml.setContent(this.html, EmailConstants.TEXT_HTML);
                }
            }

            for (final InlineImage image : this.inlineEmbeds.values())
            {
                bodyEmbedsContainer.addBodyPart(image.getMbp());
            }
        }

        if (EmailUtils.isNotEmpty(this.text))
        {
            msgText = new MimeBodyPart();
            bodyContainer.addBodyPart(msgText, 0);

            // EMAIL-104: call explicitly setText to use default mime charset
            //            (property "mail.mime.charset") in case none has been set
            msgText.setText(this.text, this.charset);
        }
    }

    /**
     * Private bean class that encapsulates data about URL contents
     * that are embedded in the final email.
     * @since 1.1
     */
    private static class InlineImage
    {
        /** content id. */
        private final String cid;
        /** {@code DataSource} for the content. */
        private final DataSource dataSource;
        /** the {@code MimeBodyPart} that contains the encoded data. */
        private final MimeBodyPart mbp;

        /**
         * Creates an InlineImage object to represent the
         * specified content ID and {@code MimeBodyPart}.
         * @param cid the generated content ID
         * @param dataSource the {@code DataSource} that represents the content
         * @param mbp the {@code MimeBodyPart} that contains the encoded
         * data
         */
        public InlineImage(final String cid, final DataSource dataSource, final MimeBodyPart mbp)
        {
            this.cid = cid;
            this.dataSource = dataSource;
            this.mbp = mbp;
        }

        /**
         * Returns the unique content ID of this InlineImage.
         * @return the unique content ID of this InlineImage
         */
        public String getCid()
        {
            return cid;
        }

        /**
         * Returns the {@code DataSource} that represents the encoded content.
         * @return the {@code DataSource} representing the encoded content
         */
        public DataSource getDataSource()
        {
            return dataSource;
        }

        /**
         * Returns the {@code MimeBodyPart} that contains the
         * encoded InlineImage data.
         * @return the {@code MimeBodyPart} containing the encoded
         * InlineImage data
         */
        public MimeBodyPart getMbp()
        {
            return mbp;
        }

        // equals()/hashCode() implementations, since this class
        // is stored as a entry in a Map.
        /**
         * {@inheritDoc}
         * @return true if the other object is also an InlineImage with the same cid.
         */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!(obj instanceof InlineImage))
            {
                return false;
            }

            final InlineImage that = (InlineImage) obj;

            return this.cid.equals(that.cid);
        }

        /**
         * {@inheritDoc}
         * @return the cid hashCode.
         */
        @Override
        public int hashCode()
        {
            return cid.hashCode();
        }
    }
}
