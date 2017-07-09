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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.mail.mocks.MockSimpleEmail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit test case for SimpleEmailTest
 * @since 1.0
 */
public class SimpleEmailTest extends AbstractEmailTest
{
    private MockSimpleEmail email;

    @Before
    public void setUpSimpleEmailTest()
    {
        // reusable objects to be used across multiple tests
        this.email = new MockSimpleEmail();
    }

    @Test
    public void testGetSetMsg() throws EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        for (final String validChar : testCharsValid)
        {
            this.email.setMsg(validChar);
            assertEquals(validChar, this.email.getMsg());
        }

        // ====================================================================
        // Test Exception
        // ====================================================================
        for (final String invalidChar : this.testCharsNotValid)
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
     * @throws EmailException when a bad address is set.
     * @throws IOException when sending fails
     * TODO Add code to test the popBeforeSmtp() settings
     */
    @Test
    public void testSend() throws EmailException, IOException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.getMailServer();

        this.email = new MockSimpleEmail();
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom(this.strTestMailFrom);
        this.email.addTo(this.strTestMailTo);

        if (this.strTestUser != null && this.strTestPasswd != null)
        {
            this.email.setAuthentication(
                this.strTestUser,
                this.strTestPasswd);
        }

        final String strSubject = "Test Msg Subject";
        final String strMessage = "Test Msg Body";

        this.email.setCharset(EmailConstants.ISO_8859_1);
        this.email.setSubject(strSubject);

        this.email.setMsg(strMessage);

        this.email.send();

        this.fakeMailServer.stop();
        validateSend(
            this.fakeMailServer,
            strSubject,
            this.email.getMsg(),
            this.email.getFromAddress(),
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            true);
    }

    @Test
    @Ignore
    public void testDefaultMimeCharset() throws EmailException, IOException
    {
        /*
         * disabling this test as it is dependent on execution order.
         * MimeUtility.getDefaultMIMECharset does some internal caching and if
         * mail.mime.charset is not defined, reverts to the default java charset
         * which is basically the system default file encoding.
         */
        System.setProperty(EmailConstants.MAIL_MIME_CHARSET, "utf-8");

        // ====================================================================
        // Test Success
        // ====================================================================
        this.getMailServer();

        this.email = new MockSimpleEmail();
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom(this.strTestMailFrom);
        this.email.addTo(this.strTestMailTo);

        if (this.strTestUser != null && this.strTestPasswd != null)
        {
            this.email.setAuthentication(
                this.strTestUser,
                this.strTestPasswd);
        }

        final String strSubject = "Test Msg Subject";
        final String strMessage = "Test Msg Body ä"; // add non-ascii character, otherwise us-ascii will be used

        this.email.setSubject(strSubject);
        this.email.setMsg(strMessage);

        this.email.send();

        this.fakeMailServer.stop();

        validateSend(
                this.fakeMailServer,
                strSubject,
                this.email.getMsg().substring(0, 13), // only check the start, the ä will be encoded
                this.email.getFromAddress(),
                this.email.getToAddresses(),
                this.email.getCcAddresses(),
                this.email.getBccAddresses(),
                true);

        final String message = getMessageAsString(0);
        // check that the charset has been correctly set
        assertTrue(message.toLowerCase().contains("content-type: text/plain; charset=utf-8"));

        System.clearProperty(EmailConstants.MAIL_MIME_CHARSET);
    }
}
