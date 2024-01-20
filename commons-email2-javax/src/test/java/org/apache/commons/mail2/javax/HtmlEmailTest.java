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
package org.apache.commons.mail2.javax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.core.EmailUtils;
import org.apache.commons.mail2.javax.mocks.MockHtmlEmailConcrete;
import org.apache.commons.mail2.javax.settings.EmailConfiguration;
import org.apache.commons.mail2.javax.util.MimeMessageParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for HtmlEmail Class.
 */
public class HtmlEmailTest extends AbstractEmailTest {

    private MockHtmlEmailConcrete email;

    private void assertCorrectContentType(final String picture, final String contentType) throws Exception {
        final HtmlEmail htmlEmail = createDefaultHtmlEmail();
        final String cid = htmlEmail.embed(new File("./src/test/resources/images/" + picture), "Apache Logo");
        final String htmlMsg = "<html><img src=\"cid:" + cid + "\"><html>";
        htmlEmail.setHtmlMsg(htmlMsg);
        htmlEmail.buildMimeMessage();

        final MimeMessage mm = htmlEmail.getMimeMessage();
        mm.saveChanges();
        final MimeMessageParser mmp = new MimeMessageParser(mm);
        mmp.parse();

        final List<?> attachments = mmp.getAttachmentList();
        assertEquals(1, attachments.size(), "Attachment size");

        final DataSource ds = (DataSource) attachments.get(0);
        assertEquals(contentType, ds.getContentType(), "Content type");
    }

    private HtmlEmail createDefaultHtmlEmail() throws EmailException {
        final HtmlEmail htmlEmail = new HtmlEmail();
        htmlEmail.setHostName(strTestMailServer);
        htmlEmail.setSmtpPort(getMailServerPort());
        htmlEmail.setFrom("a@b.com");
        htmlEmail.addTo("c@d.com");
        return htmlEmail;
    }

    @BeforeEach
    public void setUpHtmlEmailTest() {
        // reusable objects to be used across multiple tests
        email = new MockHtmlEmailConcrete();
    }

    /**
     * Create a HTML email containing an URL pointing to a ZIP file to be downloaded. According to EMAIL-93 the resulting URL
     * "https://paradisedelivery.homeip.net/delivery/?file=TZC268X93337..zip" contains TWO dots instead of one dot which breaks the link.
     */
    @Test
    public void testAddZipUrl() throws Exception {
        final String htmlMsg = "Please click on the following link: <br><br>"
                + "<a href=\"http://paradisedelivery.homeip.net/delivery/?file=3DTZC268X93337.zip\">"
                + "http://paradisedelivery.homeip.net/delivery/?file=3DTZC268X93337.zip" + "</a><br><br>Customer satisfaction is very important for us.";

        getMailServer();

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setCharset(EmailConstants.ISO_8859_1);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        final String strSubject = "A dot (\".\") is appended to some ULRs of a HTML mail.";
        email.setSubject(strSubject);
        email.setHtmlMsg(htmlMsg);

        email.send();
        fakeMailServer.stop();

        // validate html message
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), false);

        // make sure that no double dots show up
        assertTrue(email.getHtml().contains("3DTZC268X93337.zip"));
        assertFalse(email.getHtml().contains("3DTZC268X93337..zip"));
    }

    /**
     * According to EMAIL-95 calling buildMimeMessage() before calling send() causes duplicate mime parts - now we throw an exception to catch the problem
     */
    @Test
    public void testCallingBuildMimeMessageBeforeSent() throws Exception {

        final String htmlMsg = "<b>Hello World</b>";

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setCharset(EmailConstants.ISO_8859_1);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        final String strSubject = "testCallingBuildMimeMessageBeforeSent";
        email.setSubject(strSubject);
        email.setHtmlMsg(htmlMsg);

        // this should NOT be called when sending a message
        email.buildMimeMessage();

        assertThrows(IllegalStateException.class, email::send);
    }

    @Test
    public void testEmbedDataSource() throws Exception {
        final File tmpFile = File.createTempFile("testEmbedDataSource", "txt");
        tmpFile.deleteOnExit();
        final FileDataSource dataSource = new FileDataSource(tmpFile);

        // does embedding a datasource without a name fail?
        assertThrows(EmailException.class, () -> email.embed(dataSource, ""));

        // properly embed the datasource
        final String cid = email.embed(dataSource, "testname");

        // does embedding the same datasource under the same name return
        // the original cid?
        final String sameCid = email.embed(dataSource, "testname");

        assertEquals(cid, sameCid, "didn't get same CID for embedding same datasource twice");

        // does embedding another datasource under the same name fail?
        final File anotherFile = File.createTempFile("testEmbedDataSource2", "txt");
        anotherFile.deleteOnExit();
        final FileDataSource anotherDS = new FileDataSource(anotherFile);
        assertThrows(EmailException.class, () -> email.embed(anotherDS, "testname"));
    }

    @Test
    public void testEmbedFile() throws Exception {
        // Test Success

        final File file = File.createTempFile("testEmbedFile", "txt");
        file.deleteOnExit();
        final String strEmbed = email.embed(file);
        assertNotNull(strEmbed);

        assertEquals(HtmlEmail.CID_LENGTH, strEmbed.length(), "generated CID has wrong length");

        // if we embed the same file again, do we get the same content ID
        // back?
        final String testCid = email.embed(file);
        assertEquals(strEmbed, testCid, "didn't get same CID after embedding same file twice");

        // if we embed a new file, is the content ID unique?
        final File otherFile = File.createTempFile("testEmbedFile2", "txt");
        otherFile.deleteOnExit();
        final String newCid = email.embed(otherFile);

        assertNotEquals(strEmbed, newCid, "didn't get unique CID from embedding new file");
    }

    /**
     * Test that the specified Content-ID is used when embedding a File object in an HtmlEmail.
     *
     * Rolled back the changes since they broke real emails therefore the test is currently disabled.
     *
     * see https://issues.apache.org/jira/browse/EMAIL-101
     */
    @Test
    public void testEmbedFileWithCID() throws Exception {
        // Test Success

        final File file = File.createTempFile("testEmbedFile", "txt");
        file.deleteOnExit();

        final String testCid = "Test CID";
        final String encodedCid = EmailUtils.encodeUrl(testCid);

        // if we embed a new file, do we get the content ID we specified back?
        final String strEmbed = email.embed(file, testCid);
        assertNotNull(strEmbed);
        assertEquals(encodedCid, strEmbed, "didn't get same CID when embedding with a specified CID");

        // if we embed the same file again, do we get the same content ID
        // back?
        final String returnedCid = email.embed(file);
        assertEquals(encodedCid, returnedCid, "didn't get same CID after embedding same file twice");
    }

    @Test
    public void testEmbedUrl() throws Exception {
        // Test Success

        final String strEmbed = email.embed(new URL(strTestURL), "Test name");
        assertNotNull(strEmbed);
        assertEquals(HtmlEmail.CID_LENGTH, strEmbed.length());

        // if we embed the same name again, do we get the same content ID
        // back?
        final String testCid = email.embed(new URL(strTestURL), "Test name");
        assertEquals(strEmbed, testCid);

        // if we embed the same URL under a different name, is the content ID
        // unique?
        final String newCid = email.embed(new URL(strTestURL), "Test name 2");
        assertNotEquals(strEmbed, newCid);
        // Test Exceptions

        // Does an invalid URL throw an exception?
        assertThrows(EmailException.class, () -> email.embed(createInvalidURL(), "Bad URL"));

        // if we try to embed a different URL under a previously used name,
        // does it complain?
        assertThrows(EmailException.class, () -> email.embed(new URL("http://www.google.com"), "Test name"));
    }

    @Test
    public void testEmbedUrlAndFile() throws Exception {
        final File tmpFile = File.createTempFile("testfile", "txt");
        tmpFile.deleteOnExit();
        final String fileCid = email.embed(tmpFile);

        final URL fileUrl = tmpFile.toURI().toURL();
        final String urlCid = email.embed(fileUrl, "urlName");

        assertNotEquals(fileCid, urlCid, "file and URL cids should be different even for same resource");
    }

    @Test
    public void testGetSetHtmlMsg() throws EmailException {
        // Test Success
        for (final String validChar : testCharsValid) {
            email.setHtmlMsg(validChar);
            assertEquals(validChar, email.getHtml());
        }
        // Test Exception
        for (final String invalidChar : testCharsNotValid) {
            assertThrows(EmailException.class, () -> email.setHtmlMsg(invalidChar));
        }

    }

    @Test
    public void testGetSetMsg() throws EmailException {
        // Test Success
        for (final String validChar : testCharsValid) {
            email.setMsg(validChar);
            assertEquals(validChar, email.getText());

            assertTrue(email.getHtml().contains(validChar));
        }
        // Test Exception
        for (final String invalidChar : testCharsNotValid) {
            assertThrows(EmailException.class, () -> email.setMsg(invalidChar));
        }

    }

    @Test
    public void testGetSetTextMsg() throws EmailException {
        // Test Success
        for (final String validChar : testCharsValid) {
            email.setTextMsg(validChar);
            assertEquals(validChar, email.getText());
        }
        // Test Exception
        for (final String invalidChar : testCharsNotValid) {
            assertThrows(EmailException.class, () -> email.setTextMsg(invalidChar));
        }

    }

    @Test
    public void testHtmlMailMimeLayout() throws Exception {
        assertCorrectContentType("contentTypeTest.gif", "image/gif");
        assertCorrectContentType("contentTypeTest.jpg", "image/jpeg");
        assertCorrectContentType("contentTypeTest.png", "image/png");
    }

    /**
     * @throws EmailException when bad addresses and attachments are used
     * @throws IOException    if creating a temp file, URL or sending fails
     */
    @Test
    public void testSend() throws EmailException, IOException {
        final EmailAttachment attachment = new EmailAttachment();

        /* File to used to test file attachments (Must be valid) */
        final File testFile = File.createTempFile("commons-email-testfile", ".txt");
        testFile.deleteOnExit();
        // Test Success
        getMailServer();

        String strSubject = "Test HTML Send #1 Subject (w charset)";

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        /* File to used to test file attachments (Must be valid) */
        attachment.setName("Test Attachment");
        attachment.setDescription("Test Attachment Desc");
        attachment.setPath(testFile.getAbsolutePath());
        email.attach(attachment);

        // email.setAuthentication(strTestUser, strTestPasswd);

        email.setCharset(EmailConstants.ISO_8859_1);
        email.setSubject(strSubject);

        final URL url = new URL(EmailConfiguration.TEST_URL);
        final String cid = email.embed(url, "Apache Logo");

        final String strHtmlMsg = "<html>The Apache logo - <img src=\"cid:" + cid + "\"><html>";

        email.setHtmlMsg(strHtmlMsg);
        email.setTextMsg("Your email client does not support HTML emails");

        email.send();
        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);

        // validate html message
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), false);

        // validate attachment
        validateSend(fakeMailServer, strSubject, attachment.getName(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), false);

        getMailServer();

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        strSubject = "Test HTML Send #1 Subject (wo charset)";
        email.setSubject(strSubject);
        email.setTextMsg("Test message");

        email.send();
        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);
    }

    @Test
    public void testSend2() throws Exception {
        // Test Success

        getMailServer();

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        String strSubject = "Test HTML Send #2 Subject (wo charset)";
        email.setSubject(strSubject);
        email.setMsg("Test txt msg");

        email.send();
        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);

        // validate html message
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), false);

        getMailServer();

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setFrom(strTestMailFrom);
        email.setSmtpPort(getMailServerPort());
        email.addTo(strTestMailTo);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        strSubject = "Test HTML Send #2 Subject (w charset)";
        email.setCharset(EmailConstants.ISO_8859_1);
        email.setSubject(strSubject);
        email.setMsg("Test txt msg");

        email.send();
        fakeMailServer.stop();
        // validate txt message
        validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);

        // validate html message
        validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), false);

    }

    @Test
    @Disabled
    public void testSendWithDefaultCharset() throws Exception {
        // Test is disabled as its result is dependent on the execution order:
        // the mail.mime.charset property is normally cached by the MimeUtility
        // class, thus setting it to another value while running the tests
        // might not have the expected result.
        // Test Success

        System.setProperty(EmailConstants.MAIL_MIME_CHARSET, "iso-8859-15");

        getMailServer();

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        final String strSubject = "Test HTML Send Subject (w default charset)";
        email.setSubject(strSubject);
        email.setMsg("Test txt msg ä"); // add non-ascii character, otherwise us-ascii will be used

        email.send();
        fakeMailServer.stop();
        // validate charset
        validateSend(fakeMailServer, strSubject, "charset=iso-8859-15", email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);

        System.clearProperty(EmailConstants.MAIL_MIME_CHARSET);

    }

    /**
     * EMAIL-73 - check that providing a plain text content using setMsg() creates a plain content and HTML content.
     */
    @Test
    public void testSendWithPlainTextButNoHtmlContent() throws EmailException, IOException {
        getMailServer();

        final String strSubject = "testSendWithPlainTextButNoHtmlContent";

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setAuthentication(strTestUser, strTestPasswd);
        email.setCharset(EmailConstants.ISO_8859_1);
        email.setSubject(strSubject);
        email.setMsg("This is a plain text content : <b><&npsb;></html></b>");

        email.send();

        fakeMailServer.stop();

        // validate text message
        validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);
    }
}
