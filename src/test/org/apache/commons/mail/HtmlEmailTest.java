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

import javax.mail.MessagingException;

import org.apache.commons.mail.mocks.MockHtmlEmailConcrete;
import org.apache.commons.mail.settings.EmailConfiguration;

/**
 * JUnit test case for HtmlEmail Class
 *
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id: HtmlEmailTest.java,v 1.1 2004/11/25 11:14:53 epugh Exp $
 */

public class HtmlEmailTest extends BaseEmailTestCase
{
    /** */
    private MockHtmlEmailConcrete email = null;

    /**
     * @param name name
     */
    public HtmlEmailTest(String name)
    {
        super(name);
    }

    /** */
    protected void setUp()
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockHtmlEmailConcrete();
    }

    /** */
    public void testGetSetTextMsg()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            for (int i = 0; i < testCharsValid.length; i++)
            {
                this.email.setTextMsg(testCharsValid[i]);
                assertEquals(testCharsValid[i], this.email.getTextMsg());
            }
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }

        // ====================================================================
        // Test Exception
        // ====================================================================
        for (int i = 0; i < this.testCharsNotValid.length; i++)
        {
            try
            {
                this.email.setTextMsg(this.testCharsNotValid[i]);
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
    public void testGetSetHtmlMsg()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            for (int i = 0; i < testCharsValid.length; i++)
            {
                this.email.setHtmlMsg(testCharsValid[i]);
                assertEquals(testCharsValid[i], this.email.getHtmlMsg());
            }
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }

        // ====================================================================
        // Test Exception
        // ====================================================================
        for (int i = 0; i < this.testCharsNotValid.length; i++)
        {
            try
            {
                this.email.setHtmlMsg(this.testCharsNotValid[i]);
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
    public void testGetSetMsg()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            for (int i = 0; i < testCharsValid.length; i++)
            {
                this.email.setMsg(testCharsValid[i]);
                assertEquals(testCharsValid[i], this.email.getTextMsg());

                assertTrue(
                    this.email.getHtmlMsg().indexOf(testCharsValid[i]) != -1);
            }
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }

        // ====================================================================
        // Test Exception
        // ====================================================================
        for (int i = 0; i < this.testCharsNotValid.length; i++)
        {
            try
            {
                this.email.setMsg(this.testCharsNotValid[i]);
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
    public void testEmbed()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            String strEmbed =
                this.email.embed(new URL(this.strTestURL), "Test name");
            assertNotNull(strEmbed);
            assertEquals(HtmlEmail.CID_LENGTH, strEmbed.length());
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
        // bad URL
        try
        {
            this.email.embed(new URL("http://bad.url"), "Test name");
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
    public void testSend()
    {
        EmailAttachment attachment = new EmailAttachment();
        File testFile = null;

        try
        {
            /** File to used to test file attachmetns (Must be valid) */
            testFile = File.createTempFile("commons-email-testfile", ".txt");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Test file cannot be found");
        }

        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            this.getMailServer();

            String strSubject = "Test HTML Send #1 Subject (w charset)";

            this.email = new MockHtmlEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);

            /** File to used to test file attachmetns (Must be valid) */
            attachment.setName("Test Attachment");
            attachment.setDescription("Test Attachment Desc");
            attachment.setPath(testFile.getAbsolutePath());
            this.email.attach(attachment);

            this.email.setAuthentication(this.strTestUser, this.strTestPasswd);

            this.email.setCharset(Email.ISO_8859_1);
            this.email.setSubject(strSubject);

            URL url = new URL(EmailConfiguration.TEST_URL);
            String cid = this.email.embed(url, "Apache Logo");

            String strHtmlMsg =
                "<html>The Apache logo - <img src=\"cid:" + cid + "\"><html>";

            this.email.setHtmlMsg(strHtmlMsg);
            this.email.setTextMsg(
                "Your email client does not support HTML emails");

            this.email.send();
            this.fakeMailServer.stop();
            // validate txt message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getTextMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                true);

            // validate html message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getHtmlMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                false);

            // validate attachment
            validateSend(
                this.fakeMailServer,
                strSubject,
                attachment.getName(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                false);
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
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Failed to save email to output file");
        }

        try
        {
            this.getMailServer();

            this.email = new MockHtmlEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);

            if (this.strTestUser != null && this.strTestPasswd != null)
            {
                this.email.setAuthentication(
                    this.strTestUser,
                    this.strTestPasswd);
            }

            String strSubject = "Test HTML Send #1 Subject (wo charset)";
            this.email.setSubject(strSubject);
            this.email.setTextMsg("Test message");

            this.email.send();
            this.fakeMailServer.stop();
            // validate txt message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getTextMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                true);
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
    }

    /** */
    public void testSend2()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        try
        {
            this.getMailServer();

            this.email = new MockHtmlEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);

            if (this.strTestUser != null && this.strTestPasswd != null)
            {
                this.email.setAuthentication(
                    this.strTestUser,
                    this.strTestPasswd);
            }

            String strSubject = "Test HTML Send #2 Subject (wo charset)";
            this.email.setSubject(strSubject);
            this.email.setMsg("Test txt msg");

            this.email.send();
            this.fakeMailServer.stop();
            // validate txt message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getTextMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                true);

            // validate html message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getHtmlMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                false);
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

        try
        {
            this.getMailServer();

            this.email = new MockHtmlEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);

            if (this.strTestUser != null && this.strTestPasswd != null)
            {
                this.email.setAuthentication(
                    this.strTestUser,
                    this.strTestPasswd);
            }

            String strSubject = "Test HTML Send #2 Subject (w charset)";
            this.email.setCharset(Email.ISO_8859_1);
            this.email.setSubject(strSubject);
            this.email.setMsg("Test txt msg");

            this.email.send();
            this.fakeMailServer.stop();
            // validate txt message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getTextMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                true);

            // validate html message
            validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getHtmlMsg(),
                this.email.getFromAddress(),
                this.email.getToList(),
                this.email.getCcList(),
                this.email.getBccList(),
                false);
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

}