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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
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
 * <p>This class also inherits from MultiPartEmail, so it is easy to
 * add attachments to the email.
 *
 * <p>To send an email in HTML, one should create a HtmlEmail, then
 * use the setFrom, addTo, etc. methods.  The HTML content can be set
 * with the setHtmlMsg method.  The alternative text content can be set
 * with setTextMsg.
 *
 * <p>Either the text or HTML can be omitted, in which case the "main"
 * part of the multipart becomes whichever is supplied rather than a
 * multipart/alternative.
 *
 * @since 1.0
 * @author <a href="mailto:unknown">Regis Koenig</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @version $Id$
 */
public class HtmlEmail extends MultiPartEmail
{
    /** Definition of the length of generated CID's */
    public static final int CID_LENGTH = 10;

    /**
     * Text part of the message.  This will be used as alternative text if
     * the email client does not support HTML messages.
     */
    protected String text;

    /** Html part of the message */
    protected String html;

    /**
     * Embedded images Map<String,InlineImages> where the key is the
     * user-defined image name
     */
    protected Map inlineImages = new HashMap();

    /** HTML prefix and suffix for default HTML mail */
    private static final String HTML_MESSAGE_START = "<html><body><pre>";
    private static final String HTML_MESSAGE_END = "</pre></body></html>";

    /**
     * Set the text content.
     *
     * @param aText A String.
     * @return An HtmlEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public HtmlEmail setTextMsg(String aText) throws EmailException
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
    public HtmlEmail setHtmlMsg(String aHtml) throws EmailException
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
     * <p>This method overrides the MultiPartEmail setMsg() method in
     * order to send an HTML message instead of a full text message in
     * the mail body. The message is formatted in HTML for the HTML
     * part of the message, it is let as is in the alternate text
     * part.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * @since 1.0
     */
    public Email setMsg(String msg) throws EmailException
    {
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }

        setTextMsg(msg);

        StringBuffer htmlMsgBuf = new StringBuffer(
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
     * Embeds an URL in the HTML.
     * @see #embed(URL, String)
     * @since 1.1
     */
    public String embed(String url, String name) throws EmailException
    {
        try
        {
            return embed(new URL(url), name);
        }
        catch (MalformedURLException e)
        {
            throw new EmailException("Invalid URL", e);
        }
    }

    /**
     * Embeds an URL in the HTML.
     *
     * <p>This method allows to embed a file located by an URL into
     * the mail body.  It allows, for instance, to add inline images
     * to the email.  Inline files may be referenced with a
     * <code>cid:xxxxxx</code> URL, where xxxxxx is the Content-ID
     * returned by the embed function. It is an error to bind the same name
     * to more than one URL.
     *
     * <p>Example of use:<br><code><pre>
     * HtmlEmail he = new HtmlEmail();
     * he.setHtmlMsg("&lt;html&gt;&lt;img src=cid:" +
     *  embed(new URL("file:/my/image.gif"),"image.gif") +
     *  "&gt;&lt;/html&gt;");
     * // code to set the others email fields (not shown)
     * </pre></code>
     *
     * <p>
     * NOTE: Clients should take care to ensure that different URLs are bound to
     * different names. This implementation tries to detect this and throw
     * <code>EmailException</code>. However, it is not guaranteed to catch
     * all cases, especially when the URL refers to a remote HTTP host that
     * may be part of a virtual host cluster.
     * 
     * @param url The URL of the file.
     * @param name The name that will be set in the filename header
     * field.
     * @return A String with the Content-ID of the file.
     * @throws EmailException when URL supplied is invalid
     *  also see javax.mail.internet.MimeBodyPart for definitions
     * @since 1.0
     */
    public String embed(URL url, String name) throws EmailException
    {
        InlineImage ii = null;        
        
        // check if the URL contents have already been attached;
        // if so, return the cached CID value.
        if (inlineImages.containsKey(name))
        {
            ii = (InlineImage) inlineImages.get(name);
            // make sure the supplied URL points to the same thing
            // as the one already associated with this name.
            if (url.equals(ii.getURL()))
            {
                return ii.getCid();                
            }
            else
            {
                throw new EmailException("embedded file name '" + name
                    + " is already bound to URL " + ii.getURL().toString()
                    + "; existing names cannot be rebound");
            }
            // NOTE: Comparing URLs with URL.equals() is known to be
            // inconsistent when dealing with virtual hosting over HTTP,
            // but since these are almost always files on the local machine,
            // using equals() should be sufficient.            
        }

        // verify that the URL is valid
        try
        {
            InputStream is = url.openStream();
            is.close();
        }
        catch (IOException e)
        {
            throw new EmailException("Invalid URL", e);
        }

        MimeBodyPart mbp = new MimeBodyPart();

        try
        {
            mbp.setDataHandler(new DataHandler(new URLDataSource(url)));
            mbp.setFileName(name);
            mbp.setDisposition("inline");
            String cid = EmailUtils.randomAlphabetic(HtmlEmail.CID_LENGTH).toLowerCase();
            mbp.setContentID("<" + cid + ">");

            ii = new InlineImage(cid, url, mbp);
            this.inlineImages.put(name, ii);

            return cid;
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
    }

    /**
     * Does the work of actually building the email.
     *
     * @exception EmailException if there was an error.
     * @since 1.0
     */
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            build();
        }
        catch (MessagingException me)
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
        MimeMultipart container = this.getContainer();
        MimeMultipart subContainer = null;
        BodyPart msgHtml = null;
        BodyPart msgText = null;

        container.setSubType("related");
        subContainer = new MimeMultipart("alternative");

        if (EmailUtils.isNotEmpty(this.text))
        {
            msgText = new MimeBodyPart();
            if (this.inlineImages.size() > 0)
            {
                subContainer.addBodyPart(msgText);
            }
            else
            {
                container.addBodyPart(msgText);
            }

            // apply default charset if one has been set
            if (EmailUtils.isNotEmpty(this.charset))
            {
                msgText.setContent(
                    this.text,
                    Email.TEXT_PLAIN + "; charset=" + this.charset);
            }
            else
            {
                msgText.setContent(this.text, Email.TEXT_PLAIN);
            }
        }

        if (EmailUtils.isNotEmpty(this.html))
        {
            msgHtml = new MimeBodyPart();
            if (this.inlineImages.size() > 0)
            {
                subContainer.addBodyPart(msgHtml);
            }
            else
            {
                container.addBodyPart(msgHtml);
            }

            // apply default charset if one has been set
            if (EmailUtils.isNotEmpty(this.charset))
            {
                msgHtml.setContent(
                    this.html,
                    Email.TEXT_HTML + "; charset=" + this.charset);
            }
            else
            {
                msgHtml.setContent(this.html, Email.TEXT_HTML);
            }

            Iterator iter = this.inlineImages.values().iterator();
            while (iter.hasNext())
            {
                InlineImage ii = (InlineImage) iter.next();
                container.addBodyPart(ii.getMbp());
            }
        }

        if (this.inlineImages.size() > 0)
        {
            // add sub container to message
            this.addPart(subContainer, 0);
        }
    }

    /**
     * Private bean class that encapsulates data about URL contents
     * that are embedded in the final email.
     * @since 1.1
     */
    private static class InlineImage
    {
        private String cid;
        private URL url;
        private MimeBodyPart mbp;

        /**
         * Creates an InlineImage object to represent the
         * specified content ID and <code>MimeBodyPart</code>.
         * @param cid the generated content ID
         * @param url the URL that points to the content
         * @param mbp the <code>MimeBodyPart</code> that contains the encoded
         * data
         */
        public InlineImage(String cid, URL url, MimeBodyPart mbp)
        {
            this.cid = cid;
            this.url = url;
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
         * Returns the URL that points to the encoded content.
         * @return the URL pointing to the encoded content
         */
        public URL getURL() {
            return url;
        }
        
        /**
         * Returns the <code>MimeBodyPart</code> that contains the
         * encoded InlineImage data.
         * @return the <code>MimeBodyPart</code> containing the encoded
         * InlineImage data
         */
        public MimeBodyPart getMbp()
        {
            return mbp;
        }
        
        // equals()/hashCode() implementations, since this class
        // is stored as a entry in a Map.
        
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (!(obj instanceof InlineImage)) return false;
            
            InlineImage that = (InlineImage) obj;
            
            return this.cid.equals(that.cid);
        }
        
        public int hashCode()
        {
            return cid.hashCode();
        }
    }
}
