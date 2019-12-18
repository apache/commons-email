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

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.settings.EmailConfiguration;
import org.junit.After;
import org.junit.Before;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Base test case for Email test classes.
 *
 * @since 1.0
 */
public abstract class AbstractEmailTest
{
    /** Padding at end of body added by wiser/send */
    public static final int BODY_END_PAD = 3;

    /** Padding at start of body added by wiser/send */
    public static final int BODY_START_PAD = 2;

    /** Line separator in email messages */
    private static final String LINE_SEPARATOR = "\r\n";

    /** default port */
    private static int mailServerPort = 2500;

    /** The fake Wiser email server */
    protected Wiser fakeMailServer;

    /** Mail server used for testing */
    protected String strTestMailServer = "localhost";

    /** From address for the test email */
    protected String strTestMailFrom = "test_from@apache.org";

    /** Destination address for the test email */
    protected String strTestMailTo = "test_to@apache.org";

    /** Mailserver username (set if needed) */
    protected String strTestUser = "user";

    /** Mailserver strTestPasswd (set if needed) */
    protected String strTestPasswd = "password";

    /** URL to used to test URL attachments (Must be valid) */
    protected String strTestURL = EmailConfiguration.TEST_URL;

    /** Test characters acceptable to email */
    protected String[] testCharsValid =
    {
            " ",
            "a",
            "A",
            "\uc5ec",
            "0123456789",
            "012345678901234567890"
    };

    /** Test characters not acceptable to email */
    protected String[] endOfLineCombinations =
    {
            "\n",
            "\r",
            "\r\n",
            "\n\r",
    };

    /** Array of test strings */
    protected String[] testCharsNotValid = {"", null};

    /** Where to save email output **/
    private File emailOutputDir;

    /** counter for creating a file name */
    private static int fileNameCounter;

    @Before
    public void setUpAbstractEmailTest()
    {
        emailOutputDir = new File("target/test-emails");
        if (!emailOutputDir.exists())
        {
            emailOutputDir.mkdirs();
        }
    }

    @After
    public void tearDownEmailTest()
    {
        //stop the fake email server (if started)
        if (this.fakeMailServer != null && !isMailServerStopped(fakeMailServer))
        {
            this.fakeMailServer.stop();
            assertTrue("Mail server didn't stop", isMailServerStopped(fakeMailServer));
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
     * Safe a mail to a file using a more or less unique file name.
     *
     * @param email email
     * @throws IOException writing the email failed
     * @throws MessagingException writing the email failed
     */
    protected void saveEmailToFile(final WiserMessage email) throws IOException, MessagingException
    {
        final int currCounter = fileNameCounter++ % 10;
        final String emailFileName = "email" + new Date().getTime() + "-" + currCounter + ".eml";
        final File emailFile = new File(emailOutputDir, emailFileName);
        EmailUtils.writeMimeMessage(emailFile, email.getMimeMessage() );
    }

    /**
     * @param intMsgNo the message to retrieve
     * @return message as string
     */
    public String getMessageAsString(final int intMsgNo)
    {
        final List<?> receivedMessages = fakeMailServer.getMessages();
        assertTrue("mail server didn't get enough messages", receivedMessages.size() >= intMsgNo);

        final WiserMessage emailMessage = (WiserMessage) receivedMessages.get(intMsgNo);

        if (emailMessage != null)
        {
            try
            {
                return serializeEmailMessage(emailMessage);
            }
            catch (final Exception e)
            {
                // ignore, since the test will fail on an empty string return
            }
        }
        fail("Message not found");
        return "";
    }

    /**
     * Initializes the stub mail server. Fails if the server cannot be proven
     * to have started. If the server is already started, this method returns
     * without changing the state of the server.
     */
    public void getMailServer()
    {
        if (this.fakeMailServer == null || isMailServerStopped(fakeMailServer))
        {
            mailServerPort++;

            this.fakeMailServer = new Wiser();
            this.fakeMailServer.setPort(getMailServerPort());
            this.fakeMailServer.start();

            assertFalse("fake mail server didn't start", isMailServerStopped(fakeMailServer));

            final Date dtStartWait = new Date();
            while (isMailServerStopped(fakeMailServer))
            {
                // test for connected
                if (this.fakeMailServer != null
                    && !isMailServerStopped(fakeMailServer))
                {
                    break;
                }

                // test for timeout
                if (dtStartWait.getTime() + EmailConfiguration.TIME_OUT
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
     * @return WiserMessage email to check
     * @throws IOException Exception
     */
    protected WiserMessage validateSend(
        final Wiser mailServer,
        final String strSubject,
        final InternetAddress fromAdd,
        final List<InternetAddress> toAdd,
        final List<InternetAddress> ccAdd,
        final List<InternetAddress> bccAdd,
        final boolean boolSaveToFile)
        throws IOException
    {
        assertTrue("mail server doesn't contain expected message",
                mailServer.getMessages().size() == 1);
        final WiserMessage emailMessage = mailServer.getMessages().get(0);

        if (boolSaveToFile)
        {
            try
            {
                this.saveEmailToFile(emailMessage);
            }
            catch(final MessagingException me)
            {
                final IllegalStateException ise =
                    new IllegalStateException("caught MessagingException during saving the email");
                ise.initCause(me);
                throw ise;
            }
        }

        try
        {
            // get the MimeMessage
            final MimeMessage mimeMessage = emailMessage.getMimeMessage();

            // test subject
            assertEquals("got wrong subject from mail",
                    strSubject, mimeMessage.getHeader("Subject", null));

            //test from address
            assertEquals("got wrong From: address from mail",
                    fromAdd.toString(), mimeMessage.getHeader("From", null));

            //test to address
            assertTrue("got wrong To: address from mail",
                    toAdd.toString().contains(mimeMessage.getHeader("To", null)));

            //test cc address
            if (ccAdd.size() > 0)
            {
                assertTrue("got wrong Cc: address from mail",
                    ccAdd.toString().contains(mimeMessage.getHeader("Cc", null)));
            }

            //test bcc address
            if (bccAdd.size() > 0)
            {
                assertTrue("got wrong Bcc: address from mail",
                    bccAdd.toString().contains(mimeMessage.getHeader("Bcc", null)));
            }
        }
        catch (final MessagingException me)
        {
            final IllegalStateException ise =
                new IllegalStateException("caught MessagingException in validateSend()");
            ise.initCause(me);
            throw ise;
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
        final Wiser mailServer,
        final String strSubject,
        final Multipart content,
        final InternetAddress fromAdd,
        final List<InternetAddress> toAdd,
        final List<InternetAddress> ccAdd,
        final List<InternetAddress> bccAdd,
        final boolean boolSaveToFile)
        throws IOException
    {
        // test other properties
        final WiserMessage emailMessage = this.validateSend(
            mailServer,
            strSubject,
            fromAdd,
            toAdd,
            ccAdd,
            bccAdd,
            boolSaveToFile);

        // test message content

        // get sent email content
        final String strSentContent =
            content.getContentType();
        // get received email content (chop off the auto-added \n
        // and -- (front and end)
        final String emailMessageBody = getMessageBody(emailMessage);
        final String strMessageBody =
            emailMessageBody.substring(AbstractEmailTest.BODY_START_PAD,
                                       emailMessageBody.length()
                                       - AbstractEmailTest.BODY_END_PAD);
        assertTrue("didn't find expected content type in message body",
                strMessageBody.contains(strSentContent));
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
        final Wiser mailServer,
        final String strSubject,
        final String strMessage,
        final InternetAddress fromAdd,
        final List<InternetAddress> toAdd,
        final List<InternetAddress> ccAdd,
        final List<InternetAddress> bccAdd,
        final boolean boolSaveToFile)
        throws IOException
    {
        // test other properties
        final WiserMessage emailMessage = this.validateSend(
            mailServer,
            strSubject,
            fromAdd,
            toAdd,
            ccAdd,
            bccAdd,
            true);

        // test message content
        assertThat("didn't find expected message content in message body",
                getMessageBody(emailMessage), containsString(strMessage));
    }

    /**
     * Serializes the {@link MimeMessage} from the {@code WiserMessage}
     * passed in. The headers are serialized first followed by the message
     * body.
     *
     * @param wiserMessage The {@code WiserMessage} to serialize.
     * @return The string format of the message.
     * @throws MessagingException
     * @throws IOException
     *             Thrown while serializing the body from
     *             {@link DataHandler#writeTo(java.io.OutputStream)}.
     * @throws MessagingException
     *             Thrown while getting the body content from
     *             {@link MimeMessage#getDataHandler()}
     * @since 1.1
     */
    private String serializeEmailMessage(final WiserMessage wiserMessage)
            throws MessagingException, IOException
    {
        if (wiserMessage == null)
        {
            return "";
        }

        final StringBuffer serializedEmail = new StringBuffer();
        final MimeMessage message = wiserMessage.getMimeMessage();

        // Serialize the headers
        for (final Enumeration<?> headers = message.getAllHeaders(); headers
                .hasMoreElements();)
        {
            final Header header = (Header) headers.nextElement();
            serializedEmail.append(header.getName());
            serializedEmail.append(": ");
            serializedEmail.append(header.getValue());
            serializedEmail.append(LINE_SEPARATOR);
        }

        // Serialize the body
        final byte[] messageBody = getMessageBodyBytes(message);

        serializedEmail.append(LINE_SEPARATOR);
        serializedEmail.append(messageBody);
        serializedEmail.append(LINE_SEPARATOR);

        return serializedEmail.toString();
    }

    /**
     * Returns a string representation of the message body. If the message body
     * cannot be read, an empty string is returned.
     *
     * @param wiserMessage The wiser message from which to extract the message body
     * @return The string representation of the message body
     * @throws IOException
     *             Thrown while serializing the body from
     *             {@link DataHandler#writeTo(java.io.OutputStream)}.
     * @since 1.1
     */
    private String getMessageBody(final WiserMessage wiserMessage)
            throws IOException
    {
        if (wiserMessage == null)
        {
            return "";
        }

        byte[] messageBody = null;

        try
        {
            final MimeMessage message = wiserMessage.getMimeMessage();
            messageBody = getMessageBodyBytes(message);
        }
        catch (final MessagingException me)
        {
            // Thrown while getting the body content from
            // {@link MimeMessage#getDataHandler()}
            final IllegalStateException ise =
                new IllegalStateException("couldn't process MimeMessage from WiserMessage in getMessageBody()");
            ise.initCause(me);
            throw ise;
        }

        return messageBody != null ? new String(messageBody).intern() : "";
    }

    /**
     * Gets the byte making up the body of the message.
     *
     * @param mimeMessage
     *            The mime message from which to extract the body.
     * @return A byte array representing the message body
     * @throws IOException
     *             Thrown while serializing the body from
     *             {@link DataHandler#writeTo(java.io.OutputStream)}.
     * @throws MessagingException
     *             Thrown while getting the body content from
     *             {@link MimeMessage#getDataHandler()}
     * @since 1.1
     */
    private byte[] getMessageBodyBytes(final MimeMessage mimeMessage)
            throws IOException, MessagingException
    {
        final DataHandler dataHandler = mimeMessage.getDataHandler();
        final ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
        final BufferedOutputStream buffOs = new BufferedOutputStream(
                byteArrayOutStream);
        dataHandler.writeTo(buffOs);
        buffOs.flush();

        return byteArrayOutStream.toByteArray();
    }

    /**
     * Checks if an email server is running at the address stored in the
     * {@code fakeMailServer}.
     *
     * @param fakeMailServer
     *            The server from which the address is picked up.
     * @return {@code true} if the server claims to be running
     * @since 1.1
     */
    protected boolean isMailServerStopped(final Wiser fakeMailServer) {
        return !fakeMailServer.getServer().isRunning();
    }

    /**
     * Create a mocked URL object which always throws an IOException
     * when the openStream() method is called.
     * <p>
     * Several ISPs do resolve invalid URLs like {@code http://example.invalid}
     * to some error page causing tests to fail otherwise.
     *
     * @return an invalid URL
     */
    protected URL createInvalidURL() throws Exception {
        final URL url = createMock(URL.class);
        expect(url.openStream()).andThrow(new IOException());
        replay(url);

        return url;
    }
}
