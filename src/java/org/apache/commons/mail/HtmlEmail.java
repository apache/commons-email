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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

/**
 * An HTML multipart email.
 *
 * <p>This class is used to send HTML formatted email.  A text message
 * can also be set for HTML unaware email clients, such as text-based
 * email clients.
 *
 * <p>This class also inherits from MultiPartEmail, so it is easy to
 * add attachents to the email.
 *
 * <p>To send an email in HTML, one should create a HtmlEmail, then
 * use the setFrom, addTo, etc. methods.  The HTML content can be set
 * with the setHtmlMsg method.  The alternate text content can be set
 * with setTextMsg.
 *
 * <p>Either the text or HTML can be omitted, in which case the "main"
 * part of the multipart becomes whichever is supplied rather than a
 * multipart/alternative.
 *
 * @author <a href="mailto:unknown">Regis Koenig</a>
 * @author <a href="mailto:sean@informage.net">Sean Legassick</a>
 * @version $Id$
 */
public class HtmlEmail extends MultiPartEmail
{
    /**
     * Text part of the message.  This will be used as alternative text if
     * the email client does not support HTML messages.
     */
    protected String text;

    /** Html part of the message */
    protected String html;

    /** Embeded images */
    protected List inlineImages = new ArrayList();

    /** Defintion of the length of generated CID's */
    public static final int CID_LENGTH = 10;

    /**
     * Set the text content.
     *
     * @param aText A String.
     * @return An HtmlEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for defintions
     */
    public HtmlEmail setTextMsg(String aText) throws EmailException
    {
        if (StringUtils.isEmpty(aText))
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
     *  for defintions
     */
    public HtmlEmail setHtmlMsg(String aHtml) throws EmailException
    {
        if (StringUtils.isEmpty(aHtml))
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
     *  for defintions
     */
    public Email setMsg(String msg) throws EmailException
    {
        if (StringUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }

        setTextMsg(msg);

        setHtmlMsg(
            new StringBuffer()
                .append("<html><body><pre>")
                .append(msg)
                .append("</pre></body></html>")
                .toString());

        return this;
    }

    /**
     * Embeds an URL in the HTML.
     *
     * <p>This method allows to embed a file located by an URL into
     * the mail body.  It allows, for instance, to add inline images
     * to the email.  Inline files may be referenced with a
     * <code>cid:xxxxxx</code> URL, where xxxxxx is the Content-ID
     * returned by the embed function.
     *
     * <p>Example of use:<br><code><pre>
     * HtmlEmail he = new HtmlEmail();
     * he.setHtmlMsg("&lt;html&gt;&lt;img src=cid:" +
     *  embed("file:/my/image.gif","image.gif") +
     *  "&gt;&lt;/html&gt;");
     * // code to set the others email fields (not shown)
     * </pre></code>
     *
     * @param url The URL of the file.
     * @param name The name that will be set in the filename header
     * field.
     * @return A String with the Content-ID of the file.
     * @throws EmailException when URL suplpied is invalid
     *  also see javax.mail.internet.MimeBodyPart for defintions
     */
    public String embed(URL url, String name) throws EmailException
    {
        // verify that the URL is valid
        try
        {
            InputStream is = url.openStream();
            is.close();
        }
        catch (IOException e)
        {
            throw new EmailException("Invalid URL");
        }

        MimeBodyPart mbp = new MimeBodyPart();

        try
        {
            mbp.setDataHandler(new DataHandler(new URLDataSource(url)));
            mbp.setFileName(name);
            mbp.setDisposition("inline");
            String cid = RandomStringUtils.randomAlphabetic(HtmlEmail.CID_LENGTH).toLowerCase();
            mbp.addHeader("Content-ID", "<" + cid + ">");
            this.inlineImages.add(mbp);
            return cid;            
        } 
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
        
    }

    /**
     * Does the work of actually sending the email.
     *
     * @exception EmailException if there was an error.
     */
    public void send() throws EmailException
    {
        try
        {
            // if the email has attachments then the base type is mixed, 
            // otherwise it should be related
            if (this.isBoolHasAttachments())
            {
                this.buildAttachments();
            }
            else
            {
                this.buildNoAttachments();
            }

        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
        super.send();
    }

    /**
     * @throws EmailException EmailException
     * @throws MessagingException MessagingException
     */
    private void buildAttachments() throws MessagingException, EmailException
    {
        MimeMultipart container = this.getContainer();
        MimeMultipart subContainer = null;
		MimeMultipart subContainerHTML = new MimeMultipart("related");
        BodyPart msgHtml = null;
        BodyPart msgText = null;

        container.setSubType("mixed");
        subContainer = new MimeMultipart("alternative");

        if (StringUtils.isNotEmpty(this.text))
        {
            msgText = new MimeBodyPart();
            subContainer.addBodyPart(msgText);

            if (StringUtils.isNotEmpty(this.charset))
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

        if (StringUtils.isNotEmpty(this.html))
        {
			if (this.inlineImages.size() > 0)
			{
				msgHtml = new MimeBodyPart();
				subContainerHTML.addBodyPart(msgHtml);
			}
			else
			{
				msgHtml = new MimeBodyPart();
				subContainer.addBodyPart(msgHtml);
			}

            if (StringUtils.isNotEmpty(this.charset))
            {
                msgHtml.setContent(
                    this.html,
                    Email.TEXT_HTML + "; charset=" + this.charset);
            }
            else
            {
                msgHtml.setContent(this.html, Email.TEXT_HTML);
            }

            Iterator iter = this.inlineImages.iterator();
			while (iter.hasNext())
			{
				subContainerHTML.addBodyPart((BodyPart) iter.next());
			}
        }

        // add sub containers to message
        this.addPart(subContainer);

		if (this.inlineImages.size() > 0)
		{
			// add sub container to message
			this.addPart(subContainerHTML);
		}
    }

    /**
     * @throws EmailException EmailException
     * @throws MessagingException MessagingException
     */
    private void buildNoAttachments() throws MessagingException, EmailException
    {
        MimeMultipart container = this.getContainer();
        MimeMultipart subContainerHTML = new MimeMultipart("related");

        container.setSubType("alternative");

        BodyPart msgText = null;
        BodyPart msgHtml = null;

        if (StringUtils.isNotEmpty(this.text))
        {
            msgText = this.getPrimaryBodyPart();
            if (StringUtils.isNotEmpty(this.charset))
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

        if (StringUtils.isNotEmpty(this.html))
        {
            // if the txt part of the message was null, then the html part
            // will become the primary body part
            if (msgText == null)
            {
                msgHtml = getPrimaryBodyPart();
            }
            else
            {
                if (this.inlineImages.size() > 0)
                {
                    msgHtml = new MimeBodyPart();
                    subContainerHTML.addBodyPart(msgHtml);
                }
                else
                {
                    msgHtml = new MimeBodyPart();
                    container.addBodyPart(msgHtml);
                }
            }

            if (StringUtils.isNotEmpty(this.charset))
            {
                msgHtml.setContent(
                    this.html,
                    Email.TEXT_HTML + "; charset=" + this.charset);
            }
            else
            {
                msgHtml.setContent(this.html, Email.TEXT_HTML);
            }

            Iterator iter = this.inlineImages.iterator();
            while (iter.hasNext())
            {
                subContainerHTML.addBodyPart((BodyPart) iter.next());
            }

            if (this.inlineImages.size() > 0)
            {
                // add sub container to message
                this.addPart(subContainerHTML);
            }
        }
    }
}
