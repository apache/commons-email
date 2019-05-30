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
import java.net.URL;

import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.mocks.MockHtmlEmailConcrete;
import org.apache.commons.mail.settings.EmailConfiguration;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case verifying bugzilla issue 30973 is fixed.
 *
 * @since 1.0
 */
public class SendWithAttachmentsTest extends AbstractEmailTest
{
    private MockHtmlEmailConcrete email;

    @Before
    public void setUpSendWithAttachmentsTest()
    {
        // reusable objects to be used across multiple tests
        this.email = new MockHtmlEmailConcrete();
    }

    /**
     * @throws EmailException on an email error
     * @throws IOException when sending fails, or a bad URL is used
     */
    @Test
    public void testSendNoAttachments() throws EmailException, IOException
    {
        this.getMailServer();

        final String strSubject = "Test HTML Send #1 Subject (w charset)";

        this.email = new MockHtmlEmailConcrete();
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom(this.strTestMailFrom);
        this.email.addTo(this.strTestMailTo);

        this.email.setAuthentication(this.strTestUser, this.strTestPasswd);

        this.email.setCharset(EmailConstants.ISO_8859_1);
        this.email.setSubject(strSubject);

        final URL url = new URL(EmailConfiguration.TEST_URL);
        final String cid = this.email.embed(url, "Apache Logo");

        final String strHtmlMsg =
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
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            true);

        // validate html message
        validateSend(
            this.fakeMailServer,
            strSubject,
            this.email.getHtmlMsg(),
            this.email.getFromAddress(),
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            false);
    }

    /**
     * @throws EmailException on an email error
     * @throws IOException when sending fails, or a bad URL is used
     */
    @Test
    public void testSendWAttachments() throws EmailException, IOException
    {
        final EmailAttachment attachment = new EmailAttachment();
        File testFile = null;

        /** File to used to test file attachments (Must be valid) */
        testFile = File.createTempFile("commons-email-testfile", ".txt");

        // ====================================================================
        // Test Success
        // ====================================================================
        this.getMailServer();

        final String strSubject = "Test HTML Send #1 Subject (w charset)";

        this.email = new MockHtmlEmailConcrete();
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom(this.strTestMailFrom);
        this.email.addTo(this.strTestMailTo);

        /** File to be used to test file attachments (Must be valid) */
        /** Use umlaut characters to test if the file name is properly encoded */
        // use short name to avoid folding. Otherwise need to unfold when checking result.
        attachment.setName("a>ä, o>ö, u>ü, au>äu");
        attachment.setDescription("Test Attachment Desc");
        attachment.setPath(testFile.getAbsolutePath());
        this.email.attach(attachment);

        this.email.setAuthentication(this.strTestUser, this.strTestPasswd);

        this.email.setCharset(EmailConstants.ISO_8859_1);
        this.email.setSubject(strSubject);

        final String strHtmlMsg = "<html>Test Message<html>";

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
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            true);

        // validate html message
        validateSend(
            this.fakeMailServer,
            strSubject,
            this.email.getHtmlMsg(),
            this.email.getFromAddress(),
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            false);

        // validate attachment
        validateSend(
            this.fakeMailServer,
            strSubject,
            MimeUtility.encodeText(attachment.getName()),
            this.email.getFromAddress(),
            this.email.getToAddresses(),
            this.email.getCcAddresses(),
            this.email.getBccAddresses(),
            false);
    }

}
