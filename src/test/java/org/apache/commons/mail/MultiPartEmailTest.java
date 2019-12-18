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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.mocks.MockMultiPartEmailConcrete;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * JUnit test case for MultiPartEmail Class.
 *
 * @since 1.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { MockMultiPartEmailConcrete.class, URLDataSource.class })
public class MultiPartEmailTest extends AbstractEmailTest
{
    /** */
    private MockMultiPartEmailConcrete email;
    /** File to used to test file attachments (Must be valid) */
    private File testFile;

    @Before
    public void setUpMultiPartEmailTest() throws Exception
    {
        // reusable objects to be used across multiple tests
        this.email = new MockMultiPartEmailConcrete();
        testFile = File.createTempFile("testfile", ".txt");
    }

    @Test
    public void testSetMsg() throws EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        // without charset set
        for (final String validChar : testCharsValid)
        {
            this.email.setMsg(validChar);
            assertEquals(validChar, this.email.getMsg());
        }

        // with charset set
        this.email.setCharset(EmailConstants.US_ASCII);
        for (final String validChar : testCharsValid)
        {
            this.email.setMsg(validChar);
            assertEquals(validChar, this.email.getMsg());
        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        for (final String invalidChar : testCharsNotValid)
        {
            try
            {
                this.email.setMsg(invalidChar);
                fail("Should have thrown an exception");
            }
            catch (final EmailException e)
            {
                assertTrue(true);
            }
        }
    }

    /**
     * @throws EmailException when a bad address or attachment is used
     * @throws IOException when sending fails
     */
    @Test
    public void testSend() throws EmailException, IOException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.getMailServer();

        final String strSubject = "Test Multipart Send Subject";

        final EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(testFile.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName("Test_Attachment");
        attachment.setDescription("Test Attachment Desc");

        final MockMultiPartEmailConcrete testEmail =
            new MockMultiPartEmailConcrete();
        testEmail.setHostName(this.strTestMailServer);
        testEmail.setSmtpPort(this.getMailServerPort());
        testEmail.setFrom(this.strTestMailFrom);
        testEmail.addTo(this.strTestMailTo);
        testEmail.attach(attachment);
        testEmail.setSubType("subType");

        if (EmailUtils.isNotEmpty(this.strTestUser)
            && EmailUtils.isNotEmpty(this.strTestPasswd))
        {
            testEmail.setAuthentication(
                this.strTestUser,
                this.strTestPasswd);
        }

        testEmail.setSubject(strSubject);

        testEmail.setMsg("Test Message");

        final Map<String, String> ht = new HashMap<String, String>();
        ht.put("X-Priority", "2");
        ht.put("Disposition-Notification-To", this.strTestMailFrom);
        ht.put("X-Mailer", "Sendmail");

        testEmail.setHeaders(ht);

        testEmail.send();

        this.fakeMailServer.stop();
        // validate message
        validateSend(
            this.fakeMailServer,
            strSubject,
            testEmail.getMsg(),
            testEmail.getFromAddress(),
            testEmail.getToAddresses(),
            testEmail.getCcAddresses(),
            testEmail.getBccAddresses(),
            true);

        // validate attachment
        validateSend(
            this.fakeMailServer,
            strSubject,
            attachment.getName(),
            testEmail.getFromAddress(),
            testEmail.getToAddresses(),
            testEmail.getCcAddresses(),
            testEmail.getBccAddresses(),
            false);

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        try
        {
            this.getMailServer();

            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            this.fakeMailServer.stop();
        }
    }

    @Test
    public void testAttach() throws Exception
    {
        EmailAttachment attachment;

        // ====================================================================
        // Test Success - EmailAttachment
        // ====================================================================
        attachment = new EmailAttachment();
        attachment.setName("Test Attachment");
        attachment.setDescription("Test Attachment Desc");
        attachment.setPath(testFile.getAbsolutePath());
        this.email.attach(attachment);
        assertTrue(this.email.isBoolHasAttachments());

        // ====================================================================
        // Test Success - URL
        // ====================================================================
        attachment = new EmailAttachment();
        attachment.setName("Test Attachment");
        attachment.setDescription("Test Attachment Desc");
        attachment.setURL(new URL(this.strTestURL));
        this.email.attach(attachment);

        // ====================================================================
        // Test Success - File
        // ====================================================================
        this.email.attach(testFile);
        assertTrue(this.email.isBoolHasAttachments());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // null attachment
        try
        {
            this.email.attach((EmailAttachment) null);
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(true);
        }

        // bad url
        attachment = new EmailAttachment();
        try
        {
            attachment.setURL(createInvalidURL());
            this.email.attach(attachment);
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(true);
        }

        // bad file
        attachment = new EmailAttachment();
        try
        {
            attachment.setPath("");
            this.email.attach(attachment);
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws MalformedURLException when a bad attachment URL is used
     * @throws EmailException when a bad address or attachment is used
     */
    @Test
    public void testAttach2() throws MalformedURLException, EmailException
    {
        // ====================================================================
        // Test Success - URL
        // ====================================================================
        this.email.attach(
            new URL(this.strTestURL),
            "Test Attachment",
            "Test Attachment Desc");

        // bad name
        this.email.attach(
            new URL(this.strTestURL),
            null,
            "Test Attachment Desc");
    }

    @Test
    public void testAttach3() throws Exception
    {
        // ====================================================================
        // Test Success - URL
        // ====================================================================
        this.email.attach(
            new URLDataSource(new URL(this.strTestURL)),
            "Test Attachment",
            "Test Attachment Desc");

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // null datasource
        try
        {
            final URLDataSource urlDs = null;
            this.email.attach(urlDs, "Test Attachment", "Test Attachment Desc");
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(true);
        }

        // invalid datasource
        try
        {
            final URLDataSource urlDs = new URLDataSource(createInvalidURL());
            this.email.attach(urlDs, "Test Attachment", "Test Attachment Desc");
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(true);
        }
    }

    @Test
    public void testAttachFileLocking() throws Exception {

        // ====================================================================
        // EMAIL-120: attaching a FileDataSource may result in a locked file
        //            resource on windows systems
        // ====================================================================

        final File tmpFile = File.createTempFile("attachment", ".eml");

        this.email.attach(
                new FileDataSource(tmpFile),
                "Test Attachment",
                "Test Attachment Desc");

        assertTrue(tmpFile.delete());
    }

    @Test
    public void testAddPart() throws Exception
    {

        // setup
        this.email = new MockMultiPartEmailConcrete();
        final String strMessage = "hello";
        final String strContentType = "text/plain";

        // add part
        this.email.addPart(strMessage, strContentType);

        // validate
        assertEquals(
            strContentType,
            this.email.getContainer().getBodyPart(0).getContentType());
        assertEquals(
            strMessage,
            this.email.getContainer().getBodyPart(0).getDataHandler()
                .getContent());

    }

    @Test
    public void testAddPart2() throws Exception
    {

        // setup
        this.email = new MockMultiPartEmailConcrete();
        final String strSubtype = "subtype/abc123";

        // add part
        this.email.addPart(new MimeMultipart(strSubtype));

        // validate
        assertTrue(
            this
                .email
                .getContainer()
                .getBodyPart(0)
                .getDataHandler()
                .getContentType()
                .contains(strSubtype));

    }

    /** TODO implement test for GetContainer */
    @Test
    public void testGetContainer()
    {
        assertTrue(true);
    }

    /** init called twice should fail */
    @Test
    public void testInit()
    {
        // call the init function twice to trigger the IllegalStateException
        try
        {
            this.email.init();
            this.email.init();
            fail("Should have thrown an exception");
        }
        catch (final IllegalStateException e)
        {
            assertTrue(true);
        }
    }

    /** test get/set sub type */
    @Test
    public void testGetSetSubType()
    {
        for (final String validChar : testCharsValid)
        {
            this.email.setSubType(validChar);
            assertEquals(validChar, this.email.getSubType());
        }
    }
}
