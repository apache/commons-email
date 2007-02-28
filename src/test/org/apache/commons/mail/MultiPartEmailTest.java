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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.activation.URLDataSource;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.mocks.MockMultiPartEmailConcrete;

/**
 * JUnit test case for MultiPartEmail Class
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */

public class MultiPartEmailTest extends BaseEmailTestCase
{
    /** */
    private MockMultiPartEmailConcrete email;
    /** File to used to test file attachmetns (Must be valid) */
    private File testFile;

    /**
     * @param name name
     */
    public MultiPartEmailTest(String name)
    {
        super(name);
    }

    /**
     * @throws Exception  */
    protected void setUp() throws Exception
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockMultiPartEmailConcrete();
        testFile = File.createTempFile("testfile", ".txt");
    }

    /**
     * @throws EmailException  */
    public void testSetMsg() throws EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        // without charset set
        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setMsg(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getMsg());
        }

        // with charset set
        this.email.setCharset(Email.US_ASCII);
        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setMsg(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getMsg());
        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        for (int i = 0; i < testCharsNotValid.length; i++)
        {
            try
            {
                this.email.setMsg(testCharsNotValid[i]);
                fail("Should have thrown an exception");
            }
            catch (EmailException e)
            {
                assertTrue(true);
            }
        }
    }

    /**
     * @throws EmailException when a bad address or attachment is used
     * @throws IOException when sending fails
     */
    public void testSend() throws EmailException, IOException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.getMailServer();

        String strSubject = "Test Multipart Send Subject";

        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(testFile.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName("Test_Attachment");
        attachment.setDescription("Test Attachment Desc");

        MockMultiPartEmailConcrete testEmail =
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

        Map ht = new HashMap();
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
            testEmail.getToList(),
            testEmail.getCcList(),
            testEmail.getBccList(),
            true);

        // validate attachment
        validateSend(
            this.fakeMailServer,
            strSubject,
            attachment.getName(),
            testEmail.getFromAddress(),
            testEmail.getToList(),
            testEmail.getCcList(),
            testEmail.getBccList(),
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
        catch (EmailException e)
        {
            this.fakeMailServer.stop();
        }
    }

    /**
     * @throws MalformedURLException when a bad attachment URL is used
     * @throws EmailException when a bad address or attachment is used
     */
    public void testAttach() throws MalformedURLException, EmailException
    {
        EmailAttachment attachment;
        // ====================================================================
        // Test Success - File
        // ====================================================================
        attachment = new EmailAttachment();
        attachment.setName("Test Attachment");
        attachment.setDescription("Test Attachment Desc");
        attachment.setPath(testFile.getAbsolutePath());
        this.email.attach(attachment);

        // ====================================================================
        // Test Success - URL
        // ====================================================================
        attachment = new EmailAttachment();
        attachment.setName("Test Attachment");
        attachment.setDescription("Test Attachment Desc");
        attachment.setURL(new URL(this.strTestURL));
        this.email.attach(attachment);

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // null attachment
        try
        {
            this.email.attach(null);
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }

        // bad url
        attachment = new EmailAttachment();
        try
        {
            attachment.setURL(new URL("http://bad.url"));
            this.email.attach(attachment);
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
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
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws MalformedURLException when a bad attachment URL is used
     * @throws EmailException when a bad address or attachment is used
     */
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

    /**
     * @throws MalformedURLException when a bad attachment URL is used
     * @throws EmailException when a bad address or attachment is used
     */
    public void testAttach3() throws MalformedURLException, EmailException
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
            URLDataSource urlDs = null;
            this.email.attach(urlDs, "Test Attachment", "Test Attachment Desc");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }

        // invalid datasource
        try
        {
            URLDataSource urlDs = new URLDataSource(new URL("http://bad.url/"));
            this.email.attach(urlDs, "Test Attachment", "Test Attachment Desc");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     *
     * @throws Exception Exception
     */
    public void testAddPart() throws Exception
    {

        // setup
        this.email = new MockMultiPartEmailConcrete();
        String strMessage = "hello";
        String strContentType = "text/plain";

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

    /**
     *
     * @throws Exception Exception
     */
    public void testAddPart2() throws Exception
    {

        // setup
        this.email = new MockMultiPartEmailConcrete();
        String strSubtype = "subtype/abc123";

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
                .indexOf(strSubtype)
                != -1);

    }

    /** @todo implement test for GetContainer */
    public void testGetContainer()
    {
        assertTrue(true);
    }

    /** init called twice should fail */
    public void testInit()
    {
        // call the init function twice to trigger the IllegalStateException
        try
        {
            this.email.init();
            this.email.init();
            fail("Should have thrown an exception");
        }
        catch (IllegalStateException e)
        {
            assertTrue(true);
        }
    }

    /** test get/set sub type */
    public void testGetSetSubType()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setSubType(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getSubType());
        }
    }
}
