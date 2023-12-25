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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.core.EmailUtils;
import org.apache.commons.mail2.jakarta.mocks.MockMultiPartEmailConcrete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.activation.FileDataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.internet.MimeMultipart;

public class MultiPartEmailTest extends AbstractEmailTest {

    /** */
    private MockMultiPartEmailConcrete email;

    /** File to used to test file attachments (Must be valid) */
    private File testFile;

    /** File to used to test file attachments (Must be valid) */
    private Path testPath;

    @BeforeEach
    public void setUpMultiPartEmailTest() throws Exception {
        // reusable objects to be used across multiple tests
        email = new MockMultiPartEmailConcrete();
        testFile = File.createTempFile("testfile", ".txt");
        testPath = testFile.toPath();
    }

    @Test
    public void testAddPart() throws Exception {
        // setup
        email = new MockMultiPartEmailConcrete();
        final String strMessage = "hello";
        final String strContentType = "text/plain";
        // add part
        email.addPart(strMessage, strContentType);
        // validate
        assertEquals(strContentType, email.getContainer().getBodyPart(0).getContentType());
        assertEquals(strMessage, email.getContainer().getBodyPart(0).getDataHandler().getContent());
    }

    @Test
    public void testAddPart2() throws Exception {
        // setup
        email = new MockMultiPartEmailConcrete();
        final String strSubtype = "subtype/abc123";
        // add part
        email.addPart(new MimeMultipart(strSubtype));
        // validate
        assertTrue(email.getContainer().getBodyPart(0).getDataHandler().getContentType().contains(strSubtype));
    }

    /**
     * @throws MalformedURLException when a bad attachment URL is used
     * @throws EmailException        when a bad address or attachment is used
     */
    @Test
    public void testAttach2() throws MalformedURLException, EmailException {
        // Test Success - URL
        email.attach(new URL(strTestURL), "Test Attachment", "Test Attachment Desc");
        // bad name
        email.attach(new URL(strTestURL), null, "Test Attachment Desc");
    }

    @Test
    public void testAttach3() throws Exception {
        // Test Success - URL
        email.attach(new URLDataSource(new URL(strTestURL)), "Test Attachment", "Test Attachment Desc");
        // Test Exceptions
        // null datasource
        assertThrows(EmailException.class, () -> email.attach((URLDataSource) null, "Test Attachment", "Test Attachment Desc"));
        // invalid datasource
        assertThrows(EmailException.class, () -> email.attach(new URLDataSource(createInvalidURL()), "Test Attachment", "Test Attachment Desc"));
    }

    @Test
    public void testAttachFile() throws Exception {
        final EmailAttachment attachment1;
        // Test Success - EmailAttachment
        attachment1 = new EmailAttachment();
        attachment1.setName("Test Attachment");
        attachment1.setDescription("Test Attachment Desc");
        attachment1.setPath(testFile.getAbsolutePath());
        email.attach(attachment1);
        assertTrue(email.isBoolHasAttachments());
        // Test Success - URL
        final EmailAttachment attachment2 = new EmailAttachment();
        attachment2.setName("Test Attachment");
        attachment2.setDescription("Test Attachment Desc");
        attachment2.setURL(new URL(strTestURL));
        email.attach(attachment2);
        // Test Success - File
        email.attach(testFile);
        assertTrue(email.isBoolHasAttachments());
        // Test Exceptions
        // null attachment
        assertThrows(EmailException.class, () -> email.attach((EmailAttachment) null));
        // bad url
        final EmailAttachment attachment3 = new EmailAttachment();
        attachment3.setURL(createInvalidURL());
        assertThrows(EmailException.class, () -> email.attach(attachment3));

        // bad file
        final EmailAttachment attachment4 = new EmailAttachment();
        attachment4.setPath("");
        assertThrows(EmailException.class, () -> email.attach(attachment4));
    }

    @Test
    public void testAttachFileLocking() throws Exception {
        // EMAIL-120: attaching a FileDataSource may result in a locked file
        // resource on windows systems
        final File tmpFile = File.createTempFile("attachment", ".eml");
        email.attach(new FileDataSource(tmpFile), "Test Attachment", "Test Attachment Desc");
        assertTrue(tmpFile.delete());
    }

    @Test
    public void testAttachPath() throws Exception {
        final EmailAttachment attachment1;
        // Test Success - EmailAttachment
        attachment1 = new EmailAttachment();
        attachment1.setName("Test Attachment");
        attachment1.setDescription("Test Attachment Desc");
        attachment1.setPath(testPath.toAbsolutePath().toString());
        email.attach(attachment1);
        assertTrue(email.isBoolHasAttachments());
        // Test Success - URL
        final EmailAttachment attachment2 = new EmailAttachment();
        attachment2.setName("Test Attachment");
        attachment2.setDescription("Test Attachment Desc");
        attachment2.setURL(new URL(strTestURL));
        email.attach(attachment2);
        // Test Success - File
        email.attach(testPath);
        assertTrue(email.isBoolHasAttachments());
        // Test Exceptions
        // null attachment
        assertThrows(EmailException.class, () -> email.attach((EmailAttachment) null));

        // bad url
        final EmailAttachment attachment3 = new EmailAttachment();
        attachment3.setURL(createInvalidURL());
        assertThrows(EmailException.class, () -> email.attach(attachment3));

        // bad file
        final EmailAttachment attachment4 = new EmailAttachment();
        attachment4.setPath("");
        assertThrows(EmailException.class, () -> email.attach(attachment4));
    }

    /** TODO implement test for GetContainer */
    @Test
    public void testGetContainer() {
        assertTrue(true);
    }

    /** Test get/set sub type */
    @Test
    public void testGetSetSubType() {
        for (final String validChar : testCharsValid) {
            email.setSubType(validChar);
            assertEquals(validChar, email.getSubType());
        }
    }

    /** Init called twice should fail */
    @Test
    public void testInit() {
        email.init();
        // call the init function twice to trigger the IllegalStateException
        assertThrows(IllegalStateException.class, email::init);
    }

    /**
     * @throws EmailException when a bad address or attachment is used
     * @throws IOException    when sending fails
     */
    @Test
    public void testSend() throws EmailException, IOException {
        // Test Success
        getMailServer();

        final String strSubject = "Test Multipart Send Subject";

        final EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(testFile.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName("Test_Attachment");
        attachment.setDescription("Test Attachment Desc");

        final MockMultiPartEmailConcrete testEmail = new MockMultiPartEmailConcrete();
        testEmail.setHostName(strTestMailServer);
        testEmail.setSmtpPort(getMailServerPort());
        testEmail.setFrom(strTestMailFrom);
        testEmail.addTo(strTestMailTo);
        testEmail.attach(attachment);
        testEmail.setSubType("subType");

        if (EmailUtils.isNotEmpty(strTestUser) && EmailUtils.isNotEmpty(strTestPasswd)) {
            testEmail.setAuthentication(strTestUser, strTestPasswd);
        }

        testEmail.setSubject(strSubject);

        testEmail.setMsg("Test Message");

        final Map<String, String> ht = new HashMap<>();
        ht.put("X-Priority", "2");
        ht.put("Disposition-Notification-To", strTestMailFrom);
        ht.put("X-Mailer", "Sendmail");

        testEmail.setHeaders(ht);

        testEmail.send();

        stopServer();
        // validate message
        validateSend(fakeMailServer, strSubject, testEmail.getMsg(), testEmail.getFromAddress(), testEmail.getToAddresses(), testEmail.getCcAddresses(),
                testEmail.getBccAddresses(), true);

        // validate attachment
        validateSend(fakeMailServer, strSubject, attachment.getName(), testEmail.getFromAddress(), testEmail.getToAddresses(), testEmail.getCcAddresses(),
                testEmail.getBccAddresses(), false);
        // Test Exceptions
        getMailServer();
        assertThrows(EmailException.class, email::send);
    }

    @Test
    public void testSetMsg() throws EmailException {
        // Test Success

        // without charset set
        for (final String validChar : testCharsValid) {
            email.setMsg(validChar);
            assertEquals(validChar, email.getMsg());
        }

        // with charset set
        email.setCharset(EmailConstants.US_ASCII);
        for (final String validChar : testCharsValid) {
            email.setMsg(validChar);
            assertEquals(validChar, email.getMsg());
        }
        // Test Exceptions
        for (final String invalidChar : testCharsNotValid) {
            assertThrows(EmailException.class, () -> email.setMsg(invalidChar));

        }
    }
}
