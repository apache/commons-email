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

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collection;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.settings.EmailConfiguration;

/**
 * This are regression test sending REAL email to REAL mail
 * servers. The intention is to field-test certain aspects
 * of email using a variety of mail clients.
 */

public class EmailLiveTest extends BaseEmailTestCase
{
    /** the mail session */
    private Session session;

    /** the recipients of the test emails */
    private Collection toList;

    /**
     * @param name name
     */
    public EmailLiveTest(String name)
    {
        super(name);
    }

    /**
     * @throws Exception  */
    protected void setUp() throws Exception
    {
        super.setUp();

        // prepare a mail session

        Properties properties = new Properties();
        properties.setProperty(Email.MAIL_DEBUG, "" + EmailConfiguration.MAIL_DEBUG);
        properties.setProperty(Email.MAIL_PORT,  "" + EmailConfiguration.MAIL_SERVER_PORT);
        properties.setProperty(Email.MAIL_HOST, EmailConfiguration.MAIL_SERVER);
        properties.setProperty(Email.MAIL_SMTP_AUTH, "true");
        DefaultAuthenticator authenticator = new DefaultAuthenticator( EmailConfiguration.TEST_USER, EmailConfiguration.TEST_PASSWD);
        this.session = Session.getInstance(properties, authenticator);

        // prepare recipient list

        this.toList = new ArrayList();
        this.toList.add( new InternetAddress( EmailConfiguration.TEST_TO) );

    }

    protected Session getSession() {
        return session;
    }

    public Collection getToList() {
        return toList;
    }

    protected Email send(Email email) throws EmailException {

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
            email.send();
        }
        else {
            email.buildMimeMessage();
        }

        return email;
    }

    protected String getFromUrl(URL url) throws Exception {

        URLDataSource dataSource = new URLDataSource(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(dataSource.getInputStream(), baos);
        return new String(baos.toByteArray(), "UTF-8");
    }


    // ======================================================================
    // Start of Tests
    // ======================================================================
        
    /**
     * This test checks the various options of building a HTML email.
     *
     * https://issues.apache.org/jira/browse/EMAIL-65
     *
     * @throws Exception the test failed
     */
    public void testHtmlMailMimeLayout() throws Exception
    {
        String textMsg;
        String htmlMsg;

        // prepare attachments

        String cid;

        URL url = new URL(EmailConfiguration.TEST_URL);
        File imageFile = new File("./src/test/images/asf_logo_wide.gif");

        EmailAttachment attachment = new EmailAttachment();
        File attachmentFile = new File("./src/test/attachments/logo.pdf");
        attachment.setName("logo.pdf");
        attachment.setDescription("The official Apache logo");
        attachment.setPath(attachmentFile.getAbsolutePath());

        // 1) text + html content

        HtmlEmail htmlEmail1 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        htmlMsg = "<html><b>This is a HTML message without any image</b><html>";

        htmlEmail1.setSubject( "[email] 1.Test: text + html content");
        htmlEmail1.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail1.setTo(getToList());
        htmlEmail1.setTextMsg(textMsg);
        htmlEmail1.setHtmlMsg(htmlMsg);
        htmlEmail1.setMailSession(getSession());

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemai12.eml"), send(htmlEmail1).getMimeMessage());

        // 2) text + html content + image as attachment

        HtmlEmail htmlEmail2 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        htmlMsg = "<html><b>This is a HTML message with an image attachment</b><html>";

        htmlEmail2.setSubject( "[email] 2.Test: text + html content + image as attachment");
        htmlEmail2.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail2.setTo(getToList());
        htmlEmail2.setTextMsg(textMsg);
        htmlEmail2.setHtmlMsg(htmlMsg);
        htmlEmail2.attach(url, "Apache Logo", "The official Apache logo" );
        htmlEmail2.setMailSession( getSession() );

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail2.eml"), send(htmlEmail2).getMimeMessage());

        // 3) text + html content + inline image

        HtmlEmail htmlEmail3 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        cid = htmlEmail3.embed(imageFile, "Apache Logo");

        htmlMsg = "<html><b>This is a HTML message with an inline image - <img src=\"cid:"
            + cid + "\"> and NO attachment</b><html>";

        htmlEmail3.setSubject( "[email] 3.Test: text + html content + inline image");
        htmlEmail3.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail3.setTo(getToList());
        htmlEmail3.setTextMsg(textMsg);
        htmlEmail3.setHtmlMsg(htmlMsg);
        htmlEmail3.setMailSession(getSession());

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail3.eml"), send(htmlEmail3).getMimeMessage());

        // 4) text + html content + inline image + attachment

        HtmlEmail htmlEmail4 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        cid = htmlEmail4.embed(imageFile, "Apache Logo");
        htmlMsg = "<html><b>This is a HTML message with an inline image - <img src=\"cid:" + cid + "\"> and attachment</b><html>";

        htmlEmail4.setSubject( "[email] 4.Test: text + html content + inline image + attachment");
        htmlEmail4.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail4.setTo(getToList());
        htmlEmail4.setTextMsg(textMsg);
        htmlEmail4.setHtmlMsg(htmlMsg);
        htmlEmail4.attach(attachment);
        htmlEmail4.setMailSession(getSession());

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail4.eml"), send(htmlEmail4).getMimeMessage());        
    }

    /**
     * This test checks the correct character encoding when sending
     * non-ASCII content using SimpleEmail.
     *
     * https://issues.apache.org/jira/browse/EMAIL-79
     *
     * @throws Exception the test failed
     */
    public void testCorrectCharacterEncoding() throws Exception
    {
        // U+03B1 : GREEK SMALL LETTER ALPHA
        // U+03B2 : GREEK SMALL LETTER BETA
        // U+03B3 : GREEK SMALL LETTER GAMMA

        final String subject = "[email] 5.Test: Subject with three greek UTF-8 characters : \u03B1\u03B2\u03B3";
        final String textMsg = "My test body with with three greek UTF-8 characters : \u03B1\u03B2\u03B3";
        final String attachmentName = "\u03B1\u03B2\u03B3.txt";

        MultiPartEmail email = new MultiPartEmail();
        email.setDebug(true);
        email.setAuthenticator(new DefaultAuthenticator(EmailConfiguration.TEST_USER, EmailConfiguration.TEST_PASSWD));
        email.setHostName(EmailConfiguration.MAIL_SERVER);
        email.setSmtpPort(EmailConfiguration.MAIL_SERVER_PORT);
        email.addTo(EmailConfiguration.TEST_TO);
        email.setFrom(EmailConfiguration.TEST_FROM);
        email.setSubject(subject);
        email.setMsg(textMsg);
        email.setCharset("utf-8");

        // attachment in Greek
        DataSource attachment = new ByteArrayDataSource(textMsg, "text/plain");
        email.attach(attachment, attachmentName, "Attachment in Greek");
        
        EmailUtils.writeMimeMessage( new File("./target/test-emails/correct-encoding.eml"), send(email).getMimeMessage());

        System.out.println("Encoding: " + email.getMimeMessage().getEncoding());
        System.out.println("Type: " + email.getMimeMessage().getContentType());

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
          // the encoding is only set when sending the email!!
          assertEquals("quoted-printable", email.getMimeMessage().getEncoding());
          assertEquals("text/plain; charset=UTF-8", email.getMimeMessage().getContentType());
        }
    }

    /**
     * Test sending a image HTML mail bases on a local HTML page.
     *
     * @throws Exception the test failed                               
     */
    public void testImageHtmlEmailLocal() throws Exception {

        // use a simple HTML page with one image - please note that the Apache logo
        // is defined in CSS and not in HTML.

        String htmlMsg1 = FileUtils.readFileToString(new File("./src/test/html/www.apache.org.html"));

        ImageHtmlEmail email = new ImageHtmlEmail();
        email.setSubject( "[testImageHtmlEmail] 1.Test: simple html content");
        email.setFrom(EmailConfiguration.TEST_FROM);
        email.setTo(getToList());
        email.setHtmlMsg(htmlMsg1);
        email.setMailSession(getSession());

        EmailUtils.writeMimeMessage( new File("./target/test-emails/testImageHtmlEmailLocal.eml"), send(email).getMimeMessage());
    }

    /**
     * Test sending a image HTML mail based on a complex real world website.
     *
     * @throws Exception the test failed
     */
    public void testImageHtmlEmailRemote() throws Exception {

        URL url = new URL("http://www.theserverside.com");
        String htmlMsg = getFromUrl(url);

        ImageHtmlEmail email = new ImageHtmlEmail();
        email.setSubject( "[testImageHtmlEmail] 2.Test: complex html content");
        email.setFrom(EmailConfiguration.TEST_FROM);
        email.setTo(getToList());
        email.setHtmlMsg(htmlMsg, url);
        email.setMailSession(getSession());

        EmailUtils.writeMimeMessage( new File("./target/test-emails/testImageHtmlEmailRemote.eml"), send(email).getMimeMessage());
    }
}