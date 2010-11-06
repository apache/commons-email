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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.impl.DataSourceResolverImpl;
import org.apache.commons.mail.settings.EmailConfiguration;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This are regression test sending REAL email to REAL mail
 * servers using REAL recipients.
 *
 * The intention is to field-test certain aspects
 * of email using a variety of mail clients since I'm not
 * a mockist (see http://martinfowler.com/articles/mocksArentStubs.html#ClassicalAndMockistTesting).
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

        // enforce a default charset UTF-8 otherwise non-ASCII attachment names will not work 
        System.setProperty("mail.mime.charset", "utf-8");

        // enforce encoding of non-ASCII characters (violating the MIME specification - see
        // http://java.sun.com/products/javamail/javadocs/javax/mail/internet/package-summary.html
        System.setProperty("mail.mime.encodefilename", "true");
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

    /**
     * Factory method to create a pre-configured email instance.
     *
     * @param clazz the requested implementation class
     * @return the new instance
     * @throws Exception creating the Email instance failed
     */
    private Email create(Class clazz) throws Exception {

        Email email = (Email) clazz.newInstance();

        email.setTLS(EmailConfiguration.MAIL_USE_TLS);
        email.setSSL(EmailConfiguration.MAIL_USE_SSL);
        email.setHostName(EmailConfiguration.MAIL_SERVER);
        email.setSmtpPort(EmailConfiguration.MAIL_SERVER_PORT);
        email.setBounceAddress(EmailConfiguration.TEST_FROM);
        email.setDebug(EmailConfiguration.MAIL_DEBUG);
        email.setCharset(EmailConfiguration.MAIL_CHARSET);        
        email.setFrom(EmailConfiguration.TEST_FROM);
        email.addTo(EmailConfiguration.TEST_TO);

        if(EmailConfiguration.TEST_USER != null) {
            email.setAuthenticator(new DefaultAuthenticator(EmailConfiguration.TEST_USER, EmailConfiguration.TEST_PASSWD));
        }

        return email;
    }

    // ======================================================================
    // Start of Tests
    // ======================================================================

    /**
     * A sanity check that a simple email also works in reality.
     *
     * @throws Exception the test failed
     */
    public void testSimpleEmail() throws Exception
    {
        SimpleEmail email = (SimpleEmail) create(SimpleEmail.class);
        email.setSubject("TestMail");
        email.setMsg("This is a test mail ... :-)");

        EmailUtils.writeMimeMessage( new File("./target/test-emails/simplemail.eml"), send(email).getMimeMessage());
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

        HtmlEmail htmlEmail1 = (HtmlEmail) create(HtmlEmail.class);
        textMsg = "Your email client does not support HTML messages";
        htmlMsg = "<html><b>This is a HTML message without any image</b><html>";

        htmlEmail1.setSubject( "[email] 1.Test: text + html content");
        htmlEmail1.setTextMsg(textMsg);
        htmlEmail1.setHtmlMsg(htmlMsg);

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemai12.eml"), send(htmlEmail1).getMimeMessage());

        // 2) text + html content + image as attachment

        HtmlEmail htmlEmail2 = (HtmlEmail) create(HtmlEmail.class);
        textMsg = "Your email client does not support HTML messages";
        htmlMsg = "<html><b>This is a HTML message with an image attachment</b><html>";

        htmlEmail2.setSubject( "[email] 2.Test: text + html content + image as attachment");
        htmlEmail2.setTextMsg(textMsg);
        htmlEmail2.setHtmlMsg(htmlMsg);
        htmlEmail2.attach(url, "Apache Logo", "The official Apache logo" );

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail2.eml"), send(htmlEmail2).getMimeMessage());

        // 3) text + html content + inline image

        HtmlEmail htmlEmail3 = (HtmlEmail) create(HtmlEmail.class);
        textMsg = "Your email client does not support HTML messages";
        cid = htmlEmail3.embed(imageFile, "Apache Logo");

        htmlMsg = "<html><b>This is a HTML message with an inline image - <img src=\"cid:"
            + cid + "\"> and NO attachment</b><html>";

        htmlEmail3.setSubject( "[email] 3.Test: text + html content + inline image");
        htmlEmail3.setTextMsg(textMsg);
        htmlEmail3.setHtmlMsg(htmlMsg);

        EmailUtils.writeMimeMessage( new File("./target/test-emails/htmlemail3.eml"), send(htmlEmail3).getMimeMessage());

        // 4) text + html content + inline image + attachment

        HtmlEmail htmlEmail4 = (HtmlEmail) create(HtmlEmail.class);
        textMsg = "Your email client does not support HTML messages";
        cid = htmlEmail4.embed(imageFile, "Apache Logo");
        htmlMsg = "<html><b>This is a HTML message with an inline image - <img src=\"cid:" + cid + "\"> and attachment</b><html>";

        htmlEmail4.setSubject( "[email] 4.Test: text + html content + inline image + attachment");
        htmlEmail4.setTextMsg(textMsg);
        htmlEmail4.setHtmlMsg(htmlMsg);
        htmlEmail4.attach(attachment);

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
        final String textMsg = "My test body with with three greek UTF-8 characters : \u03B1\u03B2\u03B3\n";
        final String attachmentName = "\u03B1\u03B2\u03B3.txt";

        // make sure to set the charset before adding the message content
        MultiPartEmail email = (MultiPartEmail) create(MultiPartEmail.class);
        email.setSubject(subject);
        email.setMsg(textMsg);

        // create a proper UTF-8 sequence for the text attachment (matching our default charset)
        DataSource attachment = new ByteArrayDataSource(textMsg.getBytes("utf-8"), "text/plain");
        email.attach(attachment, attachmentName, "Attachment in Greek");
        
        EmailUtils.writeMimeMessage( new File("./target/test-emails/correct-encoding.eml"), send(email).getMimeMessage());
    }

    /**
     * Test sending a image HTML mail bases on a local HTML page and local image.
     *
     * @throws Exception the test failed                               
     */
    public void testImageHtmlEmailLocal() throws Exception
    {
        // use a simple HTML page with one image 

        File htmlFile = new File("./src/test/html/www.apache.org.html");
        String htmlMsg1 = FileUtils.readFileToString(htmlFile);

        ImageHtmlEmail email = (ImageHtmlEmail) create(ImageHtmlEmail.class);
        email.setDataSourceResolver(new DataSourceResolverImpl(htmlFile.getParentFile().toURI().toURL(), false));
        email.setSubject("[testImageHtmlEmail] 1.Test: simple html content");
        email.setHtmlMsg(htmlMsg1);

        EmailUtils.writeMimeMessage( new File("./target/test-emails/testImageHtmlEmailLocal.eml"), send(email).getMimeMessage());
    }

    /**
     * Test sending a image HTML mail based on a real world website. We
     * would expect to see the ApacheCon logo at the bottom of the email.
     * Please note that not all major email clients can display the email
     * properly.
     *
     * @throws Exception the test failed
     */
    public void testImageHtmlEmailRemote() throws Exception
    {
        if(EmailConfiguration.MAIL_FORCE_SEND)
        {
            URL url = new URL("http://commons.apache.org/email/");
            // URL url = new URL("http://nemo.sonarsource.org/project/index/221295?page_id=2");
            String htmlMsg = getFromUrl(url);

            ImageHtmlEmail email = (ImageHtmlEmail) create(ImageHtmlEmail.class);
            email.setDataSourceResolver(new DataSourceResolverImpl(url, false));
            email.setSubject("[testImageHtmlEmail] 2.Test: complex html content");
            email.setHtmlMsg(htmlMsg);

            EmailUtils.writeMimeMessage( new File("./target/test-emails/testImageHtmlEmailRemote.eml"), send(email).getMimeMessage());
        }
    }

    /**
     * Testing if we are able to send a few emails in a batch, i.e.
     * using a single authenticated <code>Transport</code> instance.
     * Use a single instance speeds up processing since the
     * authorization is only done once.
     *
     * https://issues.apache.org/jira/browse/EMAIL-72
     *
     * @throws Exception the test failed.
     */
    public void testSendingEmailsInBatch() throws Exception
    {
        List emails = new ArrayList();

        // we need to instantiate an email to provide the mail session - a bit ugly
        Session session = create(SimpleEmail.class).getMailSession();
        Transport transport = session.getTransport();

        // simulate creating a bunch of emails using an existing mail session
        for(int i=0; i<3; i++)
        {
            SimpleEmail personalizedEmail = (SimpleEmail) create(SimpleEmail.class);
            personalizedEmail.setMailSession(session);
            personalizedEmail.setSubject("Personalized Test Mail Nr. " + i);
            personalizedEmail.setMsg("This is a personalized test mail ... :-)");
            personalizedEmail.buildMimeMessage();
            emails.add(personalizedEmail);
        }

        // send the list of emails using a single 'Transport' instance.
        if( EmailConfiguration.MAIL_FORCE_SEND )
        {
            transport.connect();

            for(int i=0; i<emails.size(); i++)
            {
                Email personalizedEmail = (Email) emails.get(i);
                MimeMessage mimeMessage =  personalizedEmail.getMimeMessage();
                Transport.send(mimeMessage);
                System.out.println("Successfully sent the following email : " + mimeMessage.getMessageID());
            }

            transport.close();
         }
    }
}