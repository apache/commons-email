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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.mail2.core.EmailConstants;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.core.EmailUtils;
import org.apache.commons.mail2.jakarta.mocks.MockSimpleEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for SimpleEmailTest
 */
public class SimpleEmailTest extends AbstractEmailTest {

    private MockSimpleEmail email;

    @BeforeEach
    public void setUpSimpleEmailTest() {
        // reusable objects to be used across multiple tests
        email = new MockSimpleEmail();
    }

    @Test
    @Disabled
    public void testDefaultMimeCharset() throws EmailException, IOException {
        /*
         * disabling this test as it is dependent on execution order. MimeUtility.getDefaultMIMECharset does some internal caching and if mail.mime.charset is
         * not defined, reverts to the default Java charset which is basically the system default file encoding.
         */
        System.setProperty(EmailConstants.MAIL_MIME_CHARSET, StandardCharsets.UTF_8.name());
        // Test Success
        getMailServer();

        email = new MockSimpleEmail();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        final String strSubject = "Test Msg Subject";
        final String strMessage = "Test Msg Body ä"; // add non-ascii character, otherwise us-ascii will be used

        email.setSubject(strSubject);
        email.setMsg(strMessage);

        email.send();

        fakeMailServer.stop();

        validateSend(fakeMailServer, strSubject, email.getContentAsString().substring(0, 13), // only check the start, the ä will be encoded
                email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(), email.getBccAddresses(), true);

        final String message = getMessageAsString(0);
        // check that the charset has been correctly set
        assertTrue(EmailUtils.toLower(message).contains("content-type: text/plain; charset=utf-8"));

        System.clearProperty(EmailConstants.MAIL_MIME_CHARSET);
    }

    @Test
    public void testGetSetMsg() throws EmailException {
        // Test Success
        for (final String validChar : testCharsValid) {
            email.setMsg(validChar);
            assertEquals(validChar, email.getContentAsString());
        }
        // Test Exception
        for (final String invalidChar : testCharsNotValid) {
            assertThrows(EmailException.class, () -> email.setMsg(invalidChar));
        }

    }

    /**
     * @throws EmailException when a bad address is set.
     * @throws IOException    when sending fails TODO Add code to test the popBeforeSmtp() settings
     */
    @Test
    public void testSend() throws EmailException, IOException {
        // Test Success
        getMailServer();

        email = new MockSimpleEmail();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);

        if (strTestUser != null && strTestPasswd != null) {
            email.setAuthentication(strTestUser, strTestPasswd);
        }

        final String strSubject = "Test Msg Subject";
        final String strMessage = "Test Msg Body";

        email.setCharset(EmailConstants.ISO_8859_1);
        email.setSubject(strSubject);

        email.setMsg(strMessage);

        email.send();

        fakeMailServer.stop();
        validateSend(fakeMailServer, strSubject, email.getContentAsString(), email.getFromAddress(), email.getToAddresses(), email.getCcAddresses(),
                email.getBccAddresses(), true);
    }
}
