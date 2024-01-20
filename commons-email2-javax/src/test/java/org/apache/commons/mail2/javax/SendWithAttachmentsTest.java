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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.mail.internet.MimeUtility;

import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.javax.mocks.MockHtmlEmailConcrete;
import org.apache.commons.mail2.javax.settings.EmailConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case verifying bugzilla issue 30973 is fixed.
 */
public class SendWithAttachmentsTest extends AbstractEmailTest {
    private MockHtmlEmailConcrete email;

    @BeforeEach
    public void setUpSendWithAttachmentsTest() {
        // reusable objects to be used across multiple tests
        email = new MockHtmlEmailConcrete();
    }

    /**
     * @throws EmailException on an email error
     * @throws IOException    when sending fails, or a bad URL is used
     */
    @Test
    public void testSendNoAttachments() throws EmailException, IOException {
        getMailServer();

        final String strSubject = "Test HTML Send #1 Subject (w charset)";

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        email.setAuthentication(strTestUser, strTestPasswd);

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
    }

    /**
     * @throws EmailException on an email error
     * @throws IOException    when sending fails, or a bad URL is used
     */
    @Test
    public void testSendWAttachments() throws EmailException, IOException {
        final EmailAttachment attachment = new EmailAttachment();

        /** File to used to test file attachments (Must be valid) */
        final File testFile = File.createTempFile("commons-email-testfile", ".txt");
        // Test Success
        getMailServer();

        final String strSubject = "Test HTML Send #1 Subject (w charset)";

        email = new MockHtmlEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        /** File to be used to test file attachments (Must be valid) */
        /** Use umlaut characters to test if the file name is properly encoded */
        // use short name to avoid folding. Otherwise need to unfold when checking result.
        attachment.setName("a>ä, o>ö, u>ü, au>äu");
        attachment.setDescription("Test Attachment Desc");
        attachment.setPath(testFile.getAbsolutePath());
        email.attach(attachment);

        email.setAuthentication(strTestUser, strTestPasswd);

        email.setCharset(EmailConstants.ISO_8859_1);
        email.setSubject(strSubject);

        final String strHtmlMsg = "<html>Test Message<html>";

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
        validateSend(fakeMailServer, strSubject, MimeUtility.encodeText(attachment.getName()), email.getFromAddress(), email.getToAddresses(),
                email.getCcAddresses(), email.getBccAddresses(), false);
    }

}
