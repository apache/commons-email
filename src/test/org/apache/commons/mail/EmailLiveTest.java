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
import java.net.URL;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collection;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.settings.EmailConfiguration;

/**
 * This are regression test sending REAL email to REAL mail
 * servers. The intention is to field-test certain aspects
 * of email using a variety of mail clients.
 */

public class EmailLiveTest extends BaseEmailTestCase
{
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
    }    

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
        Collection toList;

        // prepare recipient list

        toList = new ArrayList();
        toList.add( new InternetAddress( EmailConfiguration.TEST_TO) );

        // prepare attachments

        String cid;

        URL url = new URL(EmailConfiguration.TEST_URL);
        URL imageUrl = new URL("http://www.apache.org/images/asf_logo_wide.gif");

        EmailAttachment attachment = new EmailAttachment();
        File attachmentFile = new File("./src/test/attachments/logo.pdf");
        attachment.setName("logo.pdf");
        attachment.setDescription("The official Apache logo");
        attachment.setPath(attachmentFile.getAbsolutePath());

        // prepare a mail session

        Properties properties = new Properties();
        properties.setProperty(Email.MAIL_DEBUG, "" + EmailConfiguration.MAIL_DEBUG);
        properties.setProperty(Email.MAIL_PORT,  "" + EmailConfiguration.MAIL_SERVER_PORT);
        properties.setProperty(Email.MAIL_HOST, EmailConfiguration.MAIL_SERVER);
        properties.setProperty(Email.MAIL_SMTP_AUTH, "true");
        DefaultAuthenticator authenticator = new DefaultAuthenticator( EmailConfiguration.TEST_USER, EmailConfiguration.TEST_PASSWD);
        Session session = Session.getInstance(properties, authenticator);

        // 1) text + html content

        HtmlEmail htmlEmail1 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        htmlMsg = "<html><b>This is a HTML message without any image</b><html>";

        htmlEmail1.setSubject( "[email] 1.Test: text + html content");
        htmlEmail1.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail1.setTo(toList);
        htmlEmail1.setTextMsg(textMsg);
        htmlEmail1.setHtmlMsg(htmlMsg);
        htmlEmail1.setMailSession( session );

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
            htmlEmail1.send();
        }
        else {
            htmlEmail1.buildMimeMessage();
        }

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail1.eml"), htmlEmail1.getMimeMessage());

        // 2) text + html content + image as attachment

        HtmlEmail htmlEmail2 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        htmlMsg = "<html><b>This is a HTML message with an image attachment</b><html>";

        htmlEmail2.setSubject( "[email] 2.Test: text + html content + image as attachment");
        htmlEmail2.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail2.setTo( toList );
        htmlEmail2.setTextMsg(textMsg);
        htmlEmail2.setHtmlMsg(htmlMsg);
        htmlEmail2.attach(url, "Apache Logo", "The official Apache logo" );
        htmlEmail2.setMailSession( session );

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
            htmlEmail2.send();
        }
        else {
            htmlEmail2.buildMimeMessage();
        }

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail2.eml"), htmlEmail2.getMimeMessage());

        // 3) text + html content + inline image

        HtmlEmail htmlEmail3 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        cid = htmlEmail3.embed(imageUrl, "Apache Logo");
        htmlMsg = "<html><b>This is a HTML message with an inline image - <img src=\"cid:"
            + cid + "\"> and NO attachment</b><html>";

        htmlEmail3.setSubject( "[email] 3.Test: text + html content + inline image");
        htmlEmail3.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail3.setTo(toList);
        htmlEmail3.setTextMsg(textMsg);
        htmlEmail3.setHtmlMsg(htmlMsg);
        htmlEmail3.setMailSession( session );

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
            htmlEmail3.send();
        }
        else {
            htmlEmail3.buildMimeMessage();
        }

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail3.eml"), htmlEmail3.getMimeMessage());

        // 4) text + html content + inline image + attachment

        HtmlEmail htmlEmail4 = new HtmlEmail();
        textMsg = "Your email client does not support HTML messages";
        cid = htmlEmail4.embed(imageUrl, "Apache Logo");
        htmlMsg = "<html><b>This is a HTML message with an inline image - <img src=\"cid:" + cid + "\"> and attachment</b><html>";

        htmlEmail4.setSubject( "[email] 4.Test: text + html content + inline image + attachment");
        htmlEmail4.setFrom(EmailConfiguration.TEST_FROM);
        htmlEmail4.setTo(toList);
        htmlEmail4.setTextMsg(textMsg);
        htmlEmail4.setHtmlMsg(htmlMsg);
        htmlEmail4.attach(attachment);
        htmlEmail4.setMailSession( session );

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
            htmlEmail4.send();
        }
        else {
            htmlEmail4.buildMimeMessage();
        }

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail4.eml"), htmlEmail4.getMimeMessage());
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

        SimpleEmail email = new SimpleEmail();
        email.setDebug(true);
        email.setAuthenticator(new DefaultAuthenticator(EmailConfiguration.TEST_USER, EmailConfiguration.TEST_PASSWD));
        email.setHostName(EmailConfiguration.MAIL_SERVER);
        email.setSmtpPort(EmailConfiguration.MAIL_SERVER_PORT);
        email.addTo(EmailConfiguration.TEST_TO);
        email.setFrom(EmailConfiguration.TEST_FROM);
        email.setSubject(subject);
        email.setMsg(textMsg);
        email.setCharset("utf-8");

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
            email.send();
        }
        else {
            email.buildMimeMessage();
        }

        EmailUtils.writeMimeMessage( new File("./target/test-emails/correct-encoding.eml"), email.getMimeMessage());

        System.out.println("Encoding: " + email.getMimeMessage().getEncoding());
        System.out.println("Type: " + email.getMimeMessage().getContentType());

        if( EmailConfiguration.MAIL_FORCE_SEND ) {
          // the encoding is only set when sending the email
          assertEquals(email.getMimeMessage().getEncoding(), "quoted-printable");
          assertEquals(email.getMimeMessage().getContentType(), "text/plain; charset=UTF-8");
        }
    }
}