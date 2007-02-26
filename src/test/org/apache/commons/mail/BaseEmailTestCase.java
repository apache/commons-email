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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import org.apache.commons.mail.settings.EmailConfiguration;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

/**
 * Base test case for Email test classes
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh</a>
 * @version $Id$
 */

public class BaseEmailTestCase extends TestCase
{
    /** Padding at end of body added by dumbser/send */
    public static final int BODY_END_PAD = 3;
    /** Padding at start of body added by dumbser/send */
    public static final int BODY_START_PAD = 2;

    /** default port */
    private static int mailServerPort = EmailConfiguration.MAIL_SERVER_PORT;

    /** The fake Dumbster email server */
    protected SimpleSmtpServer fakeMailServer;

    /** Mail server used for testing */
    protected String strTestMailServer = EmailConfiguration.MAIL_SERVER;
    /** From address for the test email */
    protected String strTestMailFrom = EmailConfiguration.TEST_FROM;
    /** Destination address for the test email */
    protected String strTestMailTo = EmailConfiguration.TEST_TO;
    /** Mailserver username (set if needed) */
    protected String strTestUser = EmailConfiguration.TEST_USER;
    /** Mailserver strTestPasswd (set if needed) */
    protected String strTestPasswd = EmailConfiguration.TEST_PASSWD;
    /** URL to used to test URL attachmetns (Must be valid) */
    protected String strTestURL = EmailConfiguration.TEST_URL;

    /** Test characters acceptable to email */
    protected String[] testCharsValid =
    {
            " ",
            "a",
            "A",
            "\uc5ec",
            "0123456789",
            "012345678901234567890",
            "\n"
    };

    /** Array of test strings */
    protected String[] testCharsNotValid = {"", null};

    /** Where to save email output **/
    private File emailOutputDir;

    /**
     * @param name name
     */
    public BaseEmailTestCase(String name)
    {
        super(name);
        emailOutputDir = new File("target/test-emails");
        if (!emailOutputDir.exists())
        {
            emailOutputDir.mkdirs();
        }
    }

    /** */
    protected void tearDown()
    {
        //stop the fake email server (if started)
        if (this.fakeMailServer != null && !this.fakeMailServer.isStopped())
        {
            this.fakeMailServer.stop();
            assertTrue("Mail server didn't stop", this.fakeMailServer.isStopped());
        }

        this.fakeMailServer = null;
    }

    /**
     * Gets the mail server port.
     * @return the port the server is running on.
     */
    protected int getMailServerPort()
    {
        return mailServerPort;
    }

    /**
     *
     * @param email email
     * @throws IOException Exception
     */
    protected void saveEmailToFile(SmtpMessage email) throws IOException
    {
        File emailFile =
            new File(emailOutputDir, "email" + new Date().getTime() + ".txt");
        FileWriter fw = new FileWriter(emailFile);
        fw.write(email.toString());
        fw.close();
    }

    /**
     * @param intMsgNo the message to retrieve
     * @return message as string
     */
    public String getMessageAsString(int intMsgNo)
    {
        assertTrue(this.fakeMailServer.getReceivedEmailSize() >= intMsgNo);
        Iterator emailIter = fakeMailServer.getReceivedEmail();
        SmtpMessage emailMessage = null;
        for (int intCurMsg = 0; intCurMsg < intMsgNo; intCurMsg++)
        {
            emailMessage = (SmtpMessage) emailIter.next();
        }

        if (emailMessage != null)
        {
            return emailMessage.toString();
        }
        fail("Message note found");
        return "";
    }

    /** */
    public void getMailServer()
    {
        if (this.fakeMailServer == null || this.fakeMailServer.isStopped())
        {
            mailServerPort++;

            this.fakeMailServer =
                    SimpleSmtpServer.start(getMailServerPort());

            assertFalse("fake mail server didn't start", this.fakeMailServer.isStopped());

            Date dtStartWait = new Date();
            while (this.fakeMailServer.isStopped())
            {
                // test for connected
                if (this.fakeMailServer != null
                    && !this.fakeMailServer.isStopped())
                {
                    break;
                }

                // test for timeout
                if ((dtStartWait.getTime() + EmailConfiguration.TIME_OUT)
                    <= new Date().getTime())
                {
                    fail("Mail server failed to start");
                }
            }
        }
    }

    /**
     * Validate the message was sent properly
     * @param mailServer reference to the fake mail server
     * @param strSubject expected subject
     * @param fromAdd expected from address
     * @param toAdd list of expected to addresses
     * @param ccAdd list of expected cc addresses
     * @param bccAdd list of expected bcc addresses
     * @param boolSaveToFile true will output to file, false doesnt
     * @return SmtpMessage email to check
     * @throws IOException Exception
     */
    protected SmtpMessage validateSend(
        SimpleSmtpServer mailServer,
        String strSubject,
        InternetAddress fromAdd,
        List toAdd,
        List ccAdd,
        List bccAdd,
        boolean boolSaveToFile)
        throws IOException
    {
        assertTrue(mailServer.getReceivedEmailSize() == 1);
        Iterator emailIter = fakeMailServer.getReceivedEmail();
        SmtpMessage emailMessage = (SmtpMessage) emailIter.next();

        if (boolSaveToFile)
        {
            this.saveEmailToFile(emailMessage);
        }

        // test subject
        assertEquals(strSubject, emailMessage.getHeaderValue("Subject"));

        //test from address
        assertEquals(fromAdd.toString(), emailMessage.getHeaderValue("From"));

        //test to address
        assertTrue(
            toAdd.toString().indexOf(emailMessage.getHeaderValue("To")) != -1);

        //test cc address
        if (ccAdd.size() > 0)
        {
            assertTrue(
                ccAdd.toString().indexOf(emailMessage.getHeaderValue("Cc"))
                    != -1);
        }

        //test bcc address
        if (bccAdd.size() > 0)
        {
            assertTrue(
                bccAdd.toString().indexOf(emailMessage.getHeaderValue("Bcc"))
                    != -1);
        }

        return emailMessage;
    }

    /**
     * Validate the message was sent properly
     * @param mailServer reference to the fake mail server
     * @param strSubject expected subject
     * @param content the expected message content
     * @param fromAdd expected from address
     * @param toAdd list of expected to addresses
     * @param ccAdd list of expected cc addresses
     * @param bccAdd list of expected bcc addresses
     * @param boolSaveToFile true will output to file, false doesnt
     * @throws IOException Exception
     */
    protected void validateSend(
        SimpleSmtpServer mailServer,
        String strSubject,
        Multipart content,
        InternetAddress fromAdd,
        List toAdd,
        List ccAdd,
        List bccAdd,
        boolean boolSaveToFile)
        throws IOException
    {
        // test other properties
        SmtpMessage emailMessage = this.validateSend(
            mailServer,
            strSubject,
            fromAdd,
            toAdd,
            ccAdd,
            bccAdd,
            boolSaveToFile);

        // test message content

        // get sent email content
        String strSentContent =
            content.getContentType();
        // get received email content (chop off the auto-added \n
        // and -- (front and end)
        String strMessageBody =
            emailMessage.getBody().substring(
                BaseEmailTestCase.BODY_START_PAD,
                emailMessage.getBody().length()
                    - BaseEmailTestCase.BODY_END_PAD);
        assertTrue(strMessageBody.indexOf(strSentContent) != -1);
    }

    /**
     * Validate the message was sent properly
     * @param mailServer reference to the fake mail server
     * @param strSubject expected subject
     * @param strMessage the expected message as a string
     * @param fromAdd expected from address
     * @param toAdd list of expected to addresses
     * @param ccAdd list of expected cc addresses
     * @param bccAdd list of expected bcc addresses
     * @param boolSaveToFile true will output to file, false doesnt
     * @throws IOException Exception
     */
    protected void validateSend(
        SimpleSmtpServer mailServer,
        String strSubject,
        String strMessage,
        InternetAddress fromAdd,
        List toAdd,
        List ccAdd,
        List bccAdd,
        boolean boolSaveToFile)
        throws IOException
    {
        // test other properties
        SmtpMessage emailMessage = this.validateSend(
            mailServer,
            strSubject,
            fromAdd,
            toAdd,
            ccAdd,
            bccAdd,
            true);

        // test message content
        assertTrue(emailMessage.getBody().indexOf(strMessage) != -1);
    }
}
