/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 ( the "License" );
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.mocks.MockMultiPartEmailConcrete;

/**
 * JUnit test case for MultiPartEmail Class
 *
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id: MultiPartEmailTest.java,v 1.1 2004/11/25 11:14:53 epugh Exp $
 */

public class MultiPartEmailTest extends BaseEmailTestCase
{
    /** */
    private MockMultiPartEmailConcrete email = null;
    /** File to used to test file attachmetns (Must be valid) */
    private File testFile;

    /**
     * @param name name
     */
    public MultiPartEmailTest(String name)
    {
        super(name);
    }

    /** */
    protected void setUp()
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockMultiPartEmailConcrete();
        try
        {
            testFile = File.createTempFile("testfile", ".txt");
        }
        catch (IOException ioe)
        {
            fail(ioe.getMessage());
        }
    }

    /** */
    public void testSetMsg()
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        // without charset set
        for (int i = 0; i < testCharsValid.length; i++)
        {
            try
            {
                this.email.setMsg(testCharsValid[i]);
                assertEquals(testCharsValid[i], this.email.getMsg());
            }
            catch (MessagingException e)
            {
                e.printStackTrace();
                fail("Unexpected exception thrown");
            }
        }

        // with charset set
        this.email.setCharset(Email.US_ASCII);
        for (int i = 0; i < testCharsValid.length; i++)
        {
            try
            {
                this.email.setMsg(testCharsValid[i]);
                assertEquals(testCharsValid[i], this.email.getMsg());
            }
            catch (MessagingException e)
            {
                e.printStackTrace();
                fail("Unexpected exception thrown");
            }
        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        String badTest = null;
        for (int i = 0; i < testCharsNotValid.length; i++)
        {
            try
            {
                this.email.setMsg(testCharsNotValid[i]);
                fail("Should have thrown an exception");
            }
            catch (MessagingException e)
            {
                assertTrue(true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                fail("Unexpected exception thrown");
            }
        }
    }

    /** */
    public void testSend()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
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
            testEmail.setFrom(this.strTestMailFrom);
            testEmail.addTo(this.strTestMailTo);
            testEmail.attach(attachment);
            testEmail.setSubType("subType");

            if (StringUtils.isNotEmpty(this.strTestUser)
                && StringUtils.isNotEmpty(this.strTestPasswd))
            {
                testEmail.setAuthentication(
                    this.strTestUser,
                    this.strTestPasswd);
            }

            testEmail.setSubject(strSubject);

            testEmail.setMsg("Test Message");

            Hashtable ht = new Hashtable();
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
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Failed to save email to output file");
        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        try
        {
            this.getMailServer();

            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (MessagingException e)
        {
            this.fakeMailServer.stop();
            assertTrue(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    /** */
    public void testAttach()
    {
        EmailAttachment attachment;
        // ====================================================================
        // Test Success - File
        // ====================================================================
        attachment = new EmailAttachment();
        try
        {
            attachment.setName("Test Attachment");
            attachment.setDescription("Test Attachment Desc");
            attachment.setPath(testFile.getAbsolutePath());
            this.email.attach(attachment);
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // ====================================================================
        // Test Success - URL
        // ====================================================================
        attachment = new EmailAttachment();
        try
        {
            attachment.setName("Test Attachment");
            attachment.setDescription("Test Attachment Desc");
            attachment.setURL(new URL(this.strTestURL));
            this.email.attach(attachment);
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // null attachment
        try
        {
            this.email.attach(null);
            fail("Should have thrown an exception");
        }
        catch (MessagingException e)
        {
            assertTrue(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // bad url
        attachment = new EmailAttachment();
        try
        {
            attachment.setURL(new URL("http://bad.url"));
            this.email.attach(attachment);
            fail("Should have thrown an exception");
        }
        catch (MessagingException e)
        {
            assertTrue(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // bad file
        attachment = new EmailAttachment();
        try
        {
            attachment.setPath("");
            this.email.attach(attachment);
            fail("Should have thrown an exception");
        }
        catch (MessagingException e)
        {
            assertTrue(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    /** */
    public void testAttach2()
    {
        // ====================================================================
        // Test Success - URL
        // ====================================================================
        try
        {
            this.email.attach(
                new URL(this.strTestURL),
                "Test Attachment",
                "Test Attachment Desc");
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // bad name
        try
        {
            this.email.attach(
                new URL(this.strTestURL),
                null,
                "Test Attachment Desc");
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    /** */
    public void testAttach3()
    {
        // ====================================================================
        // Test Success - URL
        // ====================================================================
        try
        {
            this.email.attach(
                new URLDataSource(new URL(this.strTestURL)),
                "Test Attachment",
                "Test Attachment Desc");
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

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
        catch (MessagingException e)
        {
            assertTrue(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }

        // invalid datasource
        try
        {
            URLDataSource urlDs = new URLDataSource(new URL("http://bad.url/"));
            this.email.attach(urlDs, "Test Attachment", "Test Attachment Desc");
            fail("Should have thrown an exception");
        }
        catch (MessagingException e)
        {
            assertTrue(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    /** */
    public void testAddPart()
    {
        try
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
                this
                    .email
                    .getContainer()
                    .getBodyPart(0)
                    .getDataHandler()
                    .getContent());
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    /** */
    public void testAddPart2()
    {
        try
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
        catch (MessagingException e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    /** @todo implement test for GetContainer */
    public void testGetContainer()
    {
        assertTrue(true);
    }

    /** */
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
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception thrown");
        }
    }

    /** */
    public void testGetSetSubType()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setSubType(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getSubType());
        }
    }
}