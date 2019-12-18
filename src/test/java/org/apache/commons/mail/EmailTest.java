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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.apache.commons.mail.mocks.MockEmailConcrete;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case for Email Class
 *
 * @since 1.0
 */
public class EmailTest extends AbstractEmailTest
{
    private static final String[] VALID_EMAILS =
        {
            "me@home.com",
            "joe.doe@apache.org",
            "someone_here@work-address.com.au"
        };

    /** mock for testing */
    private MockEmailConcrete email;

    @Before
    public void setUpEmailTest()
    {
        // reusable objects to be used across multiple tests
        email = new MockEmailConcrete();
    }

    @Test
    public void testGetSetDebug()
    {
        email.setDebug(true);
        assertTrue(email.isDebug());
        email.setDebug(false);
        assertFalse(email.isDebug());
    }

    @Test
    public void testGetSetSession() throws Exception
    {

        final Properties properties = new Properties(System.getProperties());
        properties.setProperty(EmailConstants.MAIL_TRANSPORT_PROTOCOL, EmailConstants.SMTP);

        properties.setProperty(
            EmailConstants.MAIL_PORT,
            String.valueOf(getMailServerPort()));
        properties.setProperty(EmailConstants.MAIL_HOST, strTestMailServer);
        properties.setProperty(EmailConstants.MAIL_DEBUG, String.valueOf(false));

        final Session mySession = Session.getInstance(properties, null);

        email.setMailSession(mySession);
        assertEquals(mySession, email.getMailSession());

    }

    @Test
    public void testGetSetAuthentication()
    {
        // setup
        final String strUsername = "user.name";
        final String strPassword = "user.pwd";
        email.setAuthentication(strUsername, strPassword);

        // this is cast into DefaultAuthenticator for convenience
        // and give us access to the getPasswordAuthentication fn
        final DefaultAuthenticator retrievedAuth =
            (DefaultAuthenticator) email.getAuthenticator();

        // tests
        assertEquals(
            strUsername,
            retrievedAuth.getPasswordAuthentication().getUserName());
        assertEquals(
            strPassword,
            retrievedAuth.getPasswordAuthentication().getPassword());
    }

    @Test
    public void testGetSetAuthenticator()
    {
        // setup
        final String strUsername = "user.name";
        final String strPassword = "user.pwd";
        final DefaultAuthenticator authenticator =
            new DefaultAuthenticator(strUsername, strPassword);
        email.setAuthenticator(authenticator);

        // this is cast into DefaultAuthenticator for convenience
        // and give us access to the getPasswordAuthentication fn
        final DefaultAuthenticator retrievedAuth =
            (DefaultAuthenticator) email.getAuthenticator();

        // tests
        assertEquals(
                strUsername,
                retrievedAuth.getPasswordAuthentication().getUserName());
        assertEquals(
            strPassword,
            retrievedAuth.getPasswordAuthentication().getPassword());
    }

    @Test
    public void testGetSetCharset()
    {
        // test ASCII and UTF-8 charsets; since every JVM is required
        // to support these, testing them should always succeed.
        Charset set = Charset.forName("US-ASCII");
        email.setCharset(set.name());
        assertEquals(set.name(), email.getCharset());

        set = Charset.forName("UTF-8");
        email.setCharset(set.name());
        assertEquals(set.name(), email.getCharset());
    }

    @Test
    public void testSetContentEmptyMimeMultipart()
    {
        final MimeMultipart part = new MimeMultipart();
        email.setContent(part);

        assertEquals(part, email.getContentMimeMultipart());
    }

    @Test
    public void testSetContentMimeMultipart()
    {
        final MimeMultipart part = new MimeMultipart("abc123");
        email.setContent(part);

        assertEquals(part, email.getContentMimeMultipart());
    }

    @Test
    public void testSetContentNull() throws Exception
    {
        email.setContent(null);
        assertNull(email.getContentMimeMultipart());
    }

    @Test
    public void testSetContentObject()
    {
        // ====================================================================
        // test (string object and valid content type)
        String testObject = "test string object";
        String testContentType = " ; charset=" + EmailConstants.US_ASCII;

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());

        // ====================================================================
        // test (null string object and valid content type)
        testObject = null;
        testContentType = " ; charset=" + EmailConstants.US_ASCII + " some more here";

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());

        // ====================================================================
        // test (string object and null content type)
        testObject = "test string object";
        testContentType = null;

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());

        // ====================================================================
        // test (string object and invalid content type)
        testObject = "test string object";
        testContentType = " something incorrect ";

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());
    }

    @Test
    public void testGetSetHostName()
    {
        for (final String validChar : testCharsValid) {
            email.setHostName(validChar);
            assertEquals(validChar, email.getHostName());
        }
    }

    @Test
    public void testGetSetSmtpPort()
    {
        email.setSmtpPort(1);
        assertEquals(
            1,
            Integer.valueOf(email.getSmtpPort()).intValue());

        email.setSmtpPort(Integer.MAX_VALUE);
        assertEquals(
                Integer.MAX_VALUE,
                Integer.valueOf(email.getSmtpPort()).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSmtpPortZero()
    {
        email.setSmtpPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSmptPortNegative()
    {
        email.setSmtpPort(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSmtpPortMinValue()
    {
        email.setSmtpPort(Integer.MIN_VALUE);
    }

    @Test
    public void testSetFrom() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "me@home.com"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {

            // set from
            email.setFrom(VALID_EMAILS[i]);

            // retrieve and verify
            assertEquals(arrExpected.get(i), email.getFromAddress());
        }
    }

    @Test
    public void testSetFromWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success (with charset set)
        // ====================================================================
        final String testValidEmail = "me@home.com";

        final InternetAddress inetExpected =
            new InternetAddress("me@home.com", "me@home.com", EmailConstants.ISO_8859_1);

        // set from
        email.setFrom(testValidEmail, testValidEmail, EmailConstants.ISO_8859_1);

        // retrieve and verify
        assertEquals(inetExpected, email.getFromAddress());

    }

    @Test
    public void testSetFrom2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final String[] testEmailNames = {"Name1", "", null};
        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set from
            email.setFrom(VALID_EMAILS[i], testEmailNames[i]);

            // retrieve and verify
            assertEquals(arrExpected.get(i), email.getFromAddress());

        }
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testSetFromBadEncoding() throws Exception {
        email.setFrom("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testAddTo() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (final String address : VALID_EMAILS) {
            email.addTo(address);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test
    public void testAddToArray() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        //set To
        email.addTo(VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test
    public void testAddToWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final String testCharset = EmailConstants.ISO_8859_1;
        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(
            new InternetAddress(
                "me@home.com",
                "Name1",
                testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set from
            email.addTo(VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test
    public void testAddTo2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set from
            email.addTo(VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddToBadEncoding() throws Exception
    {
        email.addTo("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testSetTo() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final List<InternetAddress> testEmailValid2 = new ArrayList<InternetAddress>();
        testEmailValid2.add(new InternetAddress("me@home.com", "Name1"));
        testEmailValid2.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        testEmailValid2.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        email.setTo(testEmailValid2);

        // retrieve and verify
        assertEquals(testEmailValid2.size(), email.getToAddresses().size());
        assertEquals(
                testEmailValid2.toString(),
                email.getToAddresses().toString());
    }

    @Test(expected = EmailException.class)
    public void testSetToNull() throws Exception
    {
        email.setTo(null);
    }

    @Test(expected = EmailException.class)
    public void testSetToEmpty() throws Exception
    {
        email.setTo(Collections.<InternetAddress>emptyList());
    }

    @Test
    public void testAddCc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (final String address : VALID_EMAILS) {
            email.addCc(address);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test
    public void testAddCcArray() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        //set Cc array
        email.addCc(VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test
    public void testAddCcWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final String testCharset = EmailConstants.ISO_8859_1;
        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(
            new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        // add valid ccs
        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            email.addCc(VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test
    public void testAddCc2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set from
            email.addCc(VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddCcBadEncoding() throws Exception
    {
        email.addCc("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testSetCc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final List<InternetAddress> testEmailValid2 = new ArrayList<InternetAddress>();
        testEmailValid2.add(new InternetAddress("Name1 <me@home.com>"));
        testEmailValid2.add(new InternetAddress("\"joe.doe@apache.org\" <joe.doe@apache.org>"));
        testEmailValid2.add(
                new InternetAddress("\"someone_here@work.com.au\" <someone_here@work.com.au>"));

        email.setCc(testEmailValid2);
        assertEquals(testEmailValid2, email.getCcAddresses());
    }

    @Test(expected = EmailException.class)
    public void testSetCcNull() throws Exception
    {
        email.setCc(null);
    }

    @Test(expected = EmailException.class)
    public void testSetCcEmpty() throws Exception
    {
        email.setCc(Collections.<InternetAddress>emptyList());
    }

    @Test
    public void testAddBcc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (final String address : VALID_EMAILS) {
            email.addBcc(address);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test
    public void testAddBccArray() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        // add a valid bcc
        email.addBcc(VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test
    public void testAddBccWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final String testCharset = EmailConstants.ISO_8859_1;
        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set bccs
            email.addBcc(VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test
    public void testAddBcc2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final String[] testEmailNames = {"Name1", "", null};


        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set from
            email.addBcc(VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddBccBadEncoding() throws Exception
    {
        email.addBcc("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testSetBcc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final List<InternetAddress> testInetEmailValid = new ArrayList<InternetAddress>();
        testInetEmailValid.add(new InternetAddress("me@home.com", "Name1"));
        testInetEmailValid.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        testInetEmailValid.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        email.setBcc(testInetEmailValid);
        assertEquals(testInetEmailValid, email.getBccAddresses());
    }

    @Test(expected = EmailException.class)
    public void testSetBccNull() throws Exception
    {
        email.setBcc(null);
    }

    @Test(expected = EmailException.class)
    public void testSetBccEmpty() throws Exception
    {
        email.setBcc(Collections.<InternetAddress>emptyList());
    }

    @Test
    public void testAddReplyTo() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (final String address : VALID_EMAILS) {
            email.addReplyTo(address);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getReplyToAddresses().toString());
    }

    @Test
    public void testAddReplyToWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final String testCharset = EmailConstants.ISO_8859_1;
        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set replyTo
            email.addReplyTo(VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getReplyToAddresses().toString());
    }

    @Test
    public void testAddReplyTo2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        final String[] testEmailNames = {"Name1", "", null};

        final List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < VALID_EMAILS.length; i++)
        {
            // set replyTo
            email.addReplyTo(VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getReplyToAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddReplyToBadEncoding() throws Exception
    {
        email.addReplyTo("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testAddHeader()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Priority", "1");
        headers.put("Disposition-Notification-To", "me@home.com");
        headers.put("X-Mailer", "Sendmail");

        for (final Map.Entry<String, String> header : headers.entrySet()) {
            final String name = header.getKey();
            final String value = header.getValue();
            email.addHeader(name, value);
        }

        assertEquals(headers.size(), email.getHeaders().size());
        assertEquals(headers, email.getHeaders());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderEmptyName() throws Exception
    {
        email.addHeader("", "me@home.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderNullName() throws Exception
    {
        email.addHeader(null, "me@home.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderEmptyValue() throws Exception
    {
        email.addHeader("X-Mailer", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderNullValue() throws Exception
    {
        email.addHeader("X-Mailer", null);
    }

    @Test
    public void testSetHeaders()
    {
        final Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        email.setHeaders(ht);

        assertEquals(ht.size(), email.getHeaders().size());
        assertEquals(ht, email.getHeaders());
    }

    @Test
    public void testGetHeader()
    {
        final Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Foo", "Bar");
        ht.put("X-Int", "1");

        email.setHeaders(ht);

        assertEquals("Bar", email.getHeader("X-Foo"));
        assertEquals("1", email.getHeader("X-Int"));
    }

    @Test
    public void testGetHeaders()
    {
        final Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Foo", "Bar");
        ht.put("X-Int", "1");

        email.setHeaders(ht);

        assertEquals(ht.size(), email.getHeaders().size());
    }

    @Test
    public void testFoldingHeaders() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        final String headerValue = "1234567890 1234567890 123456789 01234567890 123456789 0123456789 01234567890 01234567890";
        email.addHeader("X-LongHeader", headerValue);

        assertTrue(email.getHeaders().size() == 1);
        // the header should not yet be folded -> will be done by buildMimeMessage()
        assertFalse(email.getHeaders().get("X-LongHeader").contains("\r\n"));

        email.buildMimeMessage();

        final MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();

        final String[] values = msg.getHeader("X-LongHeader");
        assertEquals(1, values.length);

        // the header should be split in two lines
        final String[] lines = values[0].split("\\r\\n");
        assertEquals(2, lines.length);

        // there should only be one line-break
        assertTrue(values[0].indexOf("\n") == values[0].lastIndexOf("\n"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderEmptyValue() throws Exception
    {
        email.setHeaders(Collections.singletonMap("X-Mailer", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderNullValue() throws Exception
    {
        email.setHeaders(Collections.singletonMap("X-Mailer", (String) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderEmptyName() throws Exception
    {
        email.setHeaders(Collections.singletonMap("", "me@home.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderNullName() throws Exception
    {
        email.setHeaders(Collections.singletonMap((String) null, "me@home.com"));
    }

    @Test
    public void testSetSubjectValid() {
        for (final String validChar : testCharsValid) {
            email.setSubject(validChar);
            assertEquals(validChar, email.getSubject());
        }
        assertEquals(null, email.setSubject(null).getSubject());
        assertEquals("", email.setSubject("").getSubject());
        assertEquals("   ", email.setSubject("   ").getSubject());
        assertEquals("\t", email.setSubject("\t").getSubject());
    }

    @Test
    public void testEndOflineCharactersInSubjectAreReplacedWithSpaces() {
        for (final String invalidChar : endOfLineCombinations) {
            email.setSubject(invalidChar);
            assertNotEquals(invalidChar, email.getSubject());
        }
        assertEquals("abcdefg", email.setSubject("abcdefg").getSubject());
        assertEquals("abc defg", email.setSubject("abc\rdefg").getSubject());
        assertEquals("abc defg", email.setSubject("abc\ndefg").getSubject());
        assertEquals("abc  defg", email.setSubject("abc\r\ndefg").getSubject());
        assertEquals("abc  defg", email.setSubject("abc\n\rdefg").getSubject());
    }

    @Test(expected = EmailException.class)
    public void testSendNoHostName() throws Exception
    {
        getMailServer();

        email = new MockEmailConcrete();
        email.send();
    }

    @Test
    public void testSendBadHostName()
    {
        try
        {
            getMailServer();

            email = new MockEmailConcrete();
            email.setSubject("Test Email #1 Subject");
            email.setHostName("bad.host.com");
            email.setFrom("me@home.com");
            email.addTo("me@home.com");
            email.addCc("me@home.com");
            email.addBcc("me@home.com");
            email.addReplyTo("me@home.com");

            email.setContent(
                    "test string object",
                    " ; charset=" + EmailConstants.US_ASCII);

            email.send();
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(e.getCause() instanceof ParseException);
            fakeMailServer.stop();
        }
    }

    @Test(expected = EmailException.class)
    public void testSendFromNotSet() throws Exception
    {
         getMailServer();

         email = new MockEmailConcrete();
         email.setHostName(strTestMailServer);
         email.setSmtpPort(getMailServerPort());
         email.addTo("me@home.com");

         email.send();
    }

    @Test
    public void testSendFromSetInSession() throws Exception
    {
         getMailServer();

         email = new MockEmailConcrete();
         email.setHostName(strTestMailServer);
         email.setSmtpPort(getMailServerPort());
         email.addTo("me@home.com");
         email.getSession().getProperties().setProperty(EmailConstants.MAIL_FROM, "me@home.com");

         email.send();
    }

    @Test(expected = EmailException.class)
    public void testSendDestinationNotSet() throws Exception
    {
        getMailServer();

        email = new MockEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("me@home.com");

        email.send();
    }

    @Test(expected = EmailException.class)
    public void testSendBadAuthSet() throws Exception
    {
        getMailServer();

        email = new MockEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setAuthentication(null, null);

        email.send();
    }

    @Test
    public void testSendCorrectSmtpPortContainedInException()
    {
        try
        {
            getMailServer();

            email = new MockEmailConcrete();
            email.setHostName("bad.host.com");
            email.setSSLOnConnect(true);
            email.setFrom(strTestMailFrom);
            email.addTo(strTestMailTo);
            email.setAuthentication(null, null);
            email.send();
            fail("Should have thrown an exception");
        }
        catch (final EmailException e)
        {
            assertTrue(e.getMessage().contains("bad.host.com:465"));
            fakeMailServer.stop();
        }
    }

    @Test
    public void testGetSetSentDate()
    {
        // with input date

        final Date dtTest = Calendar.getInstance().getTime();
        email.setSentDate(dtTest);
        assertEquals(dtTest, email.getSentDate());

        // with null input (this is a fudge :D)
        email.setSentDate(null);

        final Date sentDate = email.getSentDate();

        // Date objects are millisecond specific. If you have a slow processor,
        // time passes between the generation of dtTest and the new Date() in
        // getSentDate() and this test fails. Make sure that the difference
        // is less than a second...
        assertTrue(Math.abs(sentDate.getTime() - dtTest.getTime()) < 1000);
    }

    @Test
    public void testToInternetAddressArray() throws Exception
    {
        final List<InternetAddress> testInetEmailValid = new ArrayList<InternetAddress>();

        testInetEmailValid.add(new InternetAddress("me@home.com", "Name1"));
        testInetEmailValid.add(
                new InternetAddress(
                        "joe.doe@apache.org",
                        "joe.doe@apache.org"));
        testInetEmailValid.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        email.setBcc(testInetEmailValid);
        assertEquals(
                testInetEmailValid.size(),
                email.getBccAddresses().size());
    }

    @Test
    public void testSetPopBeforeSmtp()
    {
        // simple test (can be improved)
        final boolean boolPopBeforeSmtp = true;
        final String strHost = "mail.home.com";
        final String strUsername = "user.name";
        final String strPassword = "user.passwd";

        email.setPopBeforeSmtp(
            boolPopBeforeSmtp,
            strHost,
            strUsername,
            strPassword);

        // retrieve and verify
        assertEquals(boolPopBeforeSmtp, email.isPopBeforeSmtp());
        assertEquals(strHost, email.getPopHost());
        assertEquals(strUsername, email.getPopUsername());
        assertEquals(strPassword, email.getPopPassword());
    }

    /**
     * Test: When Email.setCharset() is called, a subsequent setContent()
     * should use that charset for text content types unless overridden
     * by the contentType parameter.
     * See https://issues.apache.org/jira/browse/EMAIL-1.
     *
     *
     * Case 1:
     * Setting a default charset results in adding that charset info to
     * to the content type of a text/based content object.
     * @throws Exception on any error
     */
    @Test
    public void testDefaultCharsetAppliesToTextContent() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        email.setContent("test content", "text/plain");
        email.buildMimeMessage();
        final MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("text/plain; charset=ISO-8859-1", msg.getContentType());
    }

    /**
     * Case 2:
     * A default charset is overridden by an explicitly specified
     * charset in setContent().
     * @throws Exception on any error
     */
    @Test
    public void testDefaultCharsetCanBeOverriddenByContentType()
        throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        email.setContent("test content", "text/plain; charset=US-ASCII");
        email.buildMimeMessage();
        final MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("text/plain; charset=US-ASCII", msg.getContentType());
    }

    /**
     * Case 3:
     * A non-text content object ignores a default charset entirely.
     * @throws Exception on any error
     */
    @Test
    public void testDefaultCharsetIgnoredByNonTextContent()
        throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        email.setContent("test content", "application/octet-stream");
        email.buildMimeMessage();
        final MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("application/octet-stream", msg.getContentType());
    }

    @Test
    public void testCorrectContentTypeForPNG() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        final File png = new File("./target/test-classes/images/logos/maven-feather.png");
        email.setContent(png, "image/png");
        email.buildMimeMessage();
        final MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("image/png", msg.getContentType());
    }

    @Test
    public void testGetSetBounceAddress()
    {
        assertNull(email.getBounceAddress());

        final String bounceAddress = "test_bounce@apache.org";
        email.setBounceAddress(bounceAddress);

        // tests
        assertEquals(bounceAddress, email.getBounceAddress());
    }

    @Test
    public void testSetNullAndEmptyBounceAddress()
    {
        assertNull(email.setBounceAddress(null).getBounceAddress());
        assertEquals("", email.setBounceAddress("").getBounceAddress());
    }

    @Test(expected = RuntimeException.class)
    public void testGetSetInvalidBounceAddress()
    {
        email.setBounceAddress("invalid-bounce-address");
    }

    @Test
    public void testSupportForInternationalDomainNames() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("from@d\u00F6m\u00E4in.example");
        email.addTo("to@d\u00F6m\u00E4in.example");
        email.addCc("cc@d\u00F6m\u00E4in.example");
        email.addBcc("bcc@d\u00F6m\u00E4in.example");
        email.setSubject("test mail");
        email.setSubject("testSupportForInternationalDomainNames");
        email.setMsg("This is a test mail ... :-)");

        email.buildMimeMessage();
        final MimeMessage msg = email.getMimeMessage();

        assertEquals("from@xn--dmin-moa0i.example", msg.getFrom()[0].toString());
        assertEquals("to@xn--dmin-moa0i.example", msg.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("cc@xn--dmin-moa0i.example", msg.getRecipients(Message.RecipientType.CC)[0].toString());
        assertEquals("bcc@xn--dmin-moa0i.example", msg.getRecipients(Message.RecipientType.BCC)[0].toString());
    }
}
