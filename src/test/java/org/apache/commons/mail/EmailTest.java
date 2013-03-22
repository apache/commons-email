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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.AddressException;
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
 * @version $Id$
 */
public class EmailTest extends AbstractEmailTest
{
    /** valid email addresses */
    public static final String[] ARR_VALID_EMAILS =
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
        this.email = new MockEmailConcrete();
    }

    @Test
    public void testGetSetDebug()
    {
        // JUnitDoclet begin method setBoolTest isBoolTest
        boolean[] tests = {true, false};

        for (int i = 0; i < tests.length; i++)
        {
            this.email.setDebug(tests[i]);
            assertEquals(tests[i], this.email.isDebug());
        }
    }

    @Test
    public void testGetSetSession() throws Exception
    {

        Properties properties = new Properties(System.getProperties());
        properties.setProperty(EmailConstants.MAIL_TRANSPORT_PROTOCOL, EmailConstants.SMTP);

        properties.setProperty(
            EmailConstants.MAIL_PORT,
            String.valueOf(this.getMailServerPort()));
        properties.setProperty(EmailConstants.MAIL_HOST, this.strTestMailServer);
        properties.setProperty(EmailConstants.MAIL_DEBUG, String.valueOf(false));

        Session mySession = Session.getInstance(properties, null);

        this.email.setMailSession(mySession);
        assertEquals(mySession, this.email.getMailSession());

    }

    @Test
    public void testGetSetAuthentication()
    {
        // setup
        String strUsername = "user.name";
        String strPassword = "user.pwd";
        this.email.setAuthentication(strUsername, strPassword);

        // this is cast into DefaultAuthenticator for convenience
        // and give us access to the getPasswordAuthentication fn
        DefaultAuthenticator retrievedAuth =
            (DefaultAuthenticator) this.email.getAuthenticator();

        // tests
        assertTrue(
            Authenticator.class.isInstance(this.email.getAuthenticator()));
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
        String strUsername = "user.name";
        String strPassword = "user.pwd";
        DefaultAuthenticator authenicator =
            new DefaultAuthenticator(strUsername, strPassword);
        this.email.setAuthenticator(authenicator);

        // this is cast into DefaultAuthenticator for convenience
        // and give us access to the getPasswordAuthentication fn
        DefaultAuthenticator retrievedAuth =
            (DefaultAuthenticator) this.email.getAuthenticator();

        // tests
        assertTrue(
            Authenticator.class.isInstance(this.email.getAuthenticator()));
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
        this.email.setCharset(set.name());
        assertEquals(set.name(), this.email.getCharset());

        set = Charset.forName("UTF-8");
        this.email.setCharset(set.name());
        assertEquals(set.name(), this.email.getCharset());
    }

    @Test
    public void testSetContentMimeMultipart()
    {
        MimeMultipart[] tests =
            {new MimeMultipart(), new MimeMultipart("abc123"), null};

        for (int i = 0; i < tests.length; i++)
        {
            this.email.setContent(tests[i]);
            assertEquals(tests[i], this.email.getContentMimeMultipart());
        }
    }

    @Test
    public void testSetContentObject()
    {
        // setup
        String testObject = "test string object";
        String testContentType = "";

        // ====================================================================
        // test (string object and valid content type)
        testObject = "test string object";
        testContentType = " ; charset=" + EmailConstants.US_ASCII;

        this.email.setContent(testObject, testContentType);
        assertEquals(testObject, this.email.getContentObject());
        assertEquals(testContentType, this.email.getContentType());

        // ====================================================================
        // test (null string object and valid content type)
        testObject = null;
        testContentType = " ; charset=" + EmailConstants.US_ASCII + " some more here";

        this.email.setContent(testObject, testContentType);
        assertEquals(testObject, this.email.getContentObject());
        assertEquals(testContentType, this.email.getContentType());

        // ====================================================================
        // test (string object and null content type)
        testObject = "test string object";
        testContentType = null;

        this.email.setContent(testObject, testContentType);
        assertEquals(testObject, this.email.getContentObject());
        assertEquals(testContentType, this.email.getContentType());

        // ====================================================================
        // test (string object and invalid content type)
        testObject = "test string object";
        testContentType = " something incorrect ";

        this.email.setContent(testObject, testContentType);
        assertEquals(testObject, this.email.getContentObject());
        assertEquals(testContentType, this.email.getContentType());
    }

    @Test
    public void testGetSetHostName()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setHostName(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getHostName());
        }
    }

    @Test
    public void testGetSetSmtpPort()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        int[] tests = {1, Integer.MAX_VALUE};

        for (int i = 0; i < tests.length; i++)
        {
            this.email.setSmtpPort(tests[i]);
            assertEquals(
                tests[i],
                Integer.valueOf(this.email.getSmtpPort()).intValue());
        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        int[] testExs = {Integer.MIN_VALUE, -1, 0};

        for (int i = 0; i < testExs.length; i++)
        {
            try
            {
                this.email.setSmtpPort(testExs[i]);
                fail("Should have thrown an exception");
            }
            catch (IllegalArgumentException e)
            {
                assertTrue(true);
            }
        }

    }

    @Test
    public void testSetFrom() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "me@home.com"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {

            // set from
            this.email.setFrom(ARR_VALID_EMAILS[i]);

            // retrieve and verify
            assertEquals(arrExpected.get(i), this.email.getFromAddress());
        }
    }

    @Test
    public void testSetFromWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success (with charset set)
        // ====================================================================
        String testValidEmail = "me@home.com";

        InternetAddress inetExpected =
            new InternetAddress("me@home.com", "me@home.com", EmailConstants.ISO_8859_1);

        // set from
        this.email.setFrom(testValidEmail, testValidEmail, EmailConstants.ISO_8859_1);

        // retrieve and verify
        assertEquals(inetExpected, this.email.getFromAddress());

    }

    @Test
    public void testSetFrom2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String[] testEmails =
        {
            "me@home.com",
            "joe.doe@apache.org",
            "someone_here@work-address.com.au"
        };
        String[] testEmailNames = {"Name1", "", null};
        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        for (int i = 0; i < testEmails.length; i++)
        {
            // set from
            this.email.setFrom(testEmails[i], testEmailNames[i]);

            // retrieve and verify
            assertEquals(arrExpected.get(i), this.email.getFromAddress());

        }

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // reset the mail class
        MockEmailConcrete anotherEmail = new MockEmailConcrete();

        // bad encoding
        try
        {
            // set a dodgy encoding scheme
            anotherEmail.setFrom("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
            fail("setting invalid charset should have failed!");
        }
        catch (IllegalCharsetNameException e)
        {
            // expected runtime exception.
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test    
    public void testAddTo() throws EmailException, AddressException, UnsupportedEncodingException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            this.email.addTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getToAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddToArray() throws EmailException, AddressException, UnsupportedEncodingException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        //set To
        this.email.addTo(ARR_VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getToAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddToWithEncoding() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(
            new InternetAddress(
                "me@home.com",
                "Name1",
                testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            this.email.addTo(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getToAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddTo2() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            this.email.addTo(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getToAddresses().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // reset the mail class
        MockEmailConcrete anotherEmail = new MockEmailConcrete();

        // bad encoding
        try
        {
            // set a dodgy encoding scheme
            anotherEmail.addTo("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
            fail("setting invalid charset should have failed!");
        }
        catch (IllegalCharsetNameException  e)
        {
            // expected runtime exception.
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testSetTo() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        List<InternetAddress> testEmailValid2 = new ArrayList<InternetAddress>();
        testEmailValid2.add(new InternetAddress("me@home.com", "Name1"));
        testEmailValid2.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        testEmailValid2.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        this.email.setTo(testEmailValid2);

        // retrieve and verify
        assertEquals(testEmailValid2.size(), this.email.getToAddresses().size());
        assertEquals(
            testEmailValid2.toString(),
            this.email.getToAddresses().toString());

        // ====================================================================
        // Exception (Null Input)
        // ====================================================================
        try
        {
            this.email.setTo(null);
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }

        // ====================================================================
        // Exception (Empty Collection)
        // ====================================================================
        try
        {
            this.email.setTo(new ArrayList<InternetAddress>());
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddCc() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            this.email.addCc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getCcAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddCcArray() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        //set Cc array
        this.email.addCc(ARR_VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getCcAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddCcWithEncoding() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(
            new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        // add valid ccs
        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            this.email.addCc(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getCcAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddCc2() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            this.email.addCc(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), this.email.getCcAddresses().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // reset the mail class
        MockEmailConcrete anotherEmail = new MockEmailConcrete();

        // bad encoding
        try
        {
            // set a dodgy encoding scheme
            anotherEmail.addCc("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
            fail("setting invalid charset should have failed!");
        }
        catch (IllegalCharsetNameException e)
        {
            // expected runtime exception.
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     */
    @Test
    public void testSetCc() throws EmailException, AddressException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        List<InternetAddress> testEmailValid2 = new ArrayList<InternetAddress>();
        testEmailValid2.add(new InternetAddress("Name1 <me@home.com>"));
        testEmailValid2.add(new InternetAddress("\"joe.doe@apache.org\" <joe.doe@apache.org>"));
        testEmailValid2.add(
                new InternetAddress("\"someone_here@work.com.au\" <someone_here@work.com.au>"));

        this.email.setCc(testEmailValid2);
        assertEquals(testEmailValid2, this.email.getCcAddresses());

        // ====================================================================
        // Exception (Null Input)
        // ====================================================================
        try
        {
            this.email.setCc(null);
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }

        // ====================================================================
        // Exception (Empty Collection)
        // ====================================================================
        try
        {
            this.email.setCc(new ArrayList<InternetAddress>());
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddBcc() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // add a valid bcc
            this.email.addBcc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddBccArray() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        // add a valid bcc
        this.email.addBcc(ARR_VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddBccWithEncoding() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set bccs
            this.email.addBcc(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddBcc2() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};


        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            this.email.addBcc(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccAddresses().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // reset the mail class
        MockEmailConcrete anotherEmail = new MockEmailConcrete();

        // bad encoding
        try
        {
            // set a dodgy encoding scheme
            anotherEmail.addBcc("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
            fail("setting invalid charset should have failed!");
        }
        catch (IllegalCharsetNameException e)
        {
            // expected runtime exception.
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testSetBcc() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        List<InternetAddress> testInetEmailValid = new ArrayList<InternetAddress>();
        testInetEmailValid.add(new InternetAddress("me@home.com", "Name1"));
        testInetEmailValid.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        testInetEmailValid.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        this.email.setBcc(testInetEmailValid);
        assertEquals(testInetEmailValid, this.email.getBccAddresses());

        // ====================================================================
        // Exception (Null Input)
        // ====================================================================
        try
        {
            this.email.setBcc(null);
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }

        // ====================================================================
        // Exception (Empty Collection)
        // ====================================================================
        try
        {
            this.email.setBcc(new ArrayList<InternetAddress>());
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddReplyTo() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set replyTo
            this.email.addReplyTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getReplyToAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddReplyToWithEncoding() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set replyTo
            this.email.addReplyTo(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getReplyToAddresses().toString());
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testAddReplyTo2() throws UnsupportedEncodingException, AddressException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set replyTo
            this.email.addReplyTo(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getReplyToAddresses().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // reset the mail class
        MockEmailConcrete anotherEmail = new MockEmailConcrete();

        // bad encoding
        try
        {
            // set a dodgy encoding scheme
            anotherEmail.addReplyTo("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
            fail("setting invalid charset should have failed!");
        }
        catch (IllegalCharsetNameException e)
        {
            // expected runtime exception.
        }
    }

    @Test
    public void testAddHeader()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        for (Iterator<Map.Entry<String, String>> items = ht.entrySet().iterator(); items.hasNext();)
        {
            Map.Entry<String, String> entry = items.next();
            String strName = entry.getKey();
            String strValue = entry.getValue();
            this.email.addHeader(strName, strValue);
        }

        assertEquals(ht.size(), this.email.getHeaders().size());
        assertEquals(ht, this.email.getHeaders());
    }

    @Test
    public void testAddHeaderEx()
    {
        // ====================================================================
        // Test Exceptions
        // ====================================================================
        Map<String, String> htBad = new Hashtable<String, String>();
        htBad.put("X-Mailer", "");
        htBad.put("X-Priority", "");
        htBad.put("", "me@home.com");

        Map<?, ?> arrExpected = new Hashtable<Object, Object>();
        for (Iterator<Map.Entry<String, String>> items = htBad.entrySet().iterator(); items.hasNext();)
        {
            Map.Entry<String, String> element = items.next();
            try
            {
                String strName = element.getKey();
                String strValue = element.getValue();

                this.email.addHeader(strName, strValue);
                fail("Should have thrown an exception");
            }
            catch (IllegalArgumentException e)
            {
                assertTrue(true);
            }
        }

        assertEquals(arrExpected.size(), this.email.getHeaders().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getHeaders().toString());
    }

    @Test
    public void testSetHeaders()
    {
        Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        this.email.setHeaders(ht);

        assertEquals(ht.size(), this.email.getHeaders().size());
        assertEquals(ht, this.email.getHeaders());
    }

    @Test
    public void testFoldingHeaders() throws Exception
    {
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom("a@b.com");
        this.email.addTo("c@d.com");
        this.email.setSubject("test mail");

        final String headerValue = "1234567890 1234567890 123456789 01234567890 123456789 0123456789 01234567890 01234567890";
        this.email.addHeader("X-LongHeader", headerValue);
        
        assertTrue(this.email.getHeaders().size() == 1);
        // the header should not yet be folded -> will be done by buildMimeMessage()
        assertTrue(this.email.getHeaders().get("X-LongHeader").toString().indexOf("\r\n") == -1);
        
        this.email.buildMimeMessage();

        MimeMessage msg = this.email.getMimeMessage();
        msg.saveChanges();
        
        String[] values = msg.getHeader("X-LongHeader");
        assertEquals(1, values.length);
        
        // the header should be split in two lines
        String[] lines = values[0].split("\\r\\n");
        assertEquals(2, lines.length);
        
        // there should only be one line-break
        assertTrue(values[0].indexOf("\n") == values[0].lastIndexOf("\n"));
    }

    @Test
    public void testSetHeadersEx()
    {
        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // first test
        Map<String, String> htBad = new Hashtable<String, String>();
        htBad.put("X-Mailer", "");

        Map<?, ?> arrExpected = new Hashtable<Object, Object>();

        try
        {
            this.email.setHeaders(htBad);
            fail("Should have thrown an exception");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(true);
        }

        assertEquals(arrExpected.size(), this.email.getHeaders().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getHeaders().toString());

        // ====================================================================
        // second test
        htBad = new Hashtable<String, String>();
        htBad.put("", "me@home.com");

        try
        {
            this.email.setHeaders(htBad);
            fail("Should have thrown an exception");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(true);
        }

        assertEquals(arrExpected.size(), this.email.getHeaders().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getHeaders().toString());
    }

    @Test
    public void testSetSubject()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setSubject(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getSubject());
        }
    }

    @Test
    public void testSendEx()
    {
        // ====================================================================
        // Test Exceptions (in getMailSession)
        // ====================================================================
        // hostname not set
        try
        {
            this.getMailServer();

            this.email = new MockEmailConcrete();
            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            this.fakeMailServer.stop();
            assertTrue(true);
        }

        // bad hostname
        try
        {
            this.getMailServer();

            this.email = new MockEmailConcrete();
            this.email.setSubject("Test Email #1 Subject");
            this.email.setHostName("bad.host.com");
            this.email.setFrom("me@home.com");
            this.email.addTo("me@home.com");
            this.email.addCc("me@home.com");
            this.email.addBcc("me@home.com");
            this.email.addReplyTo("me@home.com");

            this.email.setContent(
                "test string object",
                " ; charset=" + EmailConstants.US_ASCII);

            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(e.getCause() instanceof ParseException);
            this.fakeMailServer.stop();
            assertTrue(true);
        }

        // ====================================================================
        // Test Exceptions (in send)
        // ====================================================================
        // from add not set
        try
        {
            this.getMailServer();

            this.email = new MockEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setSmtpPort(this.getMailServerPort());

            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            this.fakeMailServer.stop();
            assertTrue(true);
        }

        // destination (to/cc/bcc) dd not set
        try
        {
            this.getMailServer();

            this.email = new MockEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setSmtpPort(this.getMailServerPort());
            this.email.setFrom("me@home.com");
            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            this.fakeMailServer.stop();
            assertTrue(true);
        }

        // bad auth set
        try
        {
            this.getMailServer();

            this.email = new MockEmailConcrete();
            this.email.setHostName(this.strTestMailServer);
            this.email.setSmtpPort(this.getMailServerPort());
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);
            this.email.setAuthentication(null, null);
            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            this.fakeMailServer.stop();
            assertTrue(true);
        }
        
        // validate that the correct smtp port is visible in the exception message
        // in case ssl connection is used
        try
        {
            this.getMailServer();

            this.email = new MockEmailConcrete();
            this.email.setHostName("bad.host.com");
            this.email.setSSLOnConnect(true);
            this.email.setFrom(this.strTestMailFrom);
            this.email.addTo(this.strTestMailTo);
            this.email.setAuthentication(null, null);
            this.email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(e.getMessage().contains("bad.host.com:465"));
            this.fakeMailServer.stop();
            assertTrue(true);
        }
        
    }

    @Test
    public void testGetSetSentDate()
    {
        // with input date

        Date dtTest = Calendar.getInstance().getTime();
        this.email.setSentDate(dtTest);
        assertEquals(dtTest, this.email.getSentDate());

        // with null input (this is a fudge :D)
        this.email.setSentDate(null);

        Date sentDate = this.email.getSentDate();

        // Date objects are millisecond specific. If you have a slow processor,
        // time passes between the generation of dtTest and the new Date() in
        // getSentDate() and this test fails. Make sure that the difference
        // is less than a second...
        assertTrue(Math.abs(sentDate.getTime() - dtTest.getTime()) < 1000);
    }

    /**
     * @throws EmailException when there are problems adding an address
     * @throws UnsupportedEncodingException on bad email addresses
     */
    @Test
    public void testToInternetAddressArray() throws EmailException, UnsupportedEncodingException
    {
        List<InternetAddress> testInetEmailValid = new ArrayList<InternetAddress>();

        testInetEmailValid.add(new InternetAddress("me@home.com", "Name1"));
        testInetEmailValid.add(
                new InternetAddress(
                        "joe.doe@apache.org",
                        "joe.doe@apache.org"));
        testInetEmailValid.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        this.email.setBcc(testInetEmailValid);
        assertEquals(
                testInetEmailValid.size(),
                this.email.getBccAddresses().size());
    }

    @Test
    public void testSetPopBeforeSmtp()
    {
        // simple test (can be improved)
        boolean boolPopBeforeSmtp = true;
        String strHost = "mail.home.com";
        String strUsername = "user.name";
        String strPassword = "user.passwd";

        this.email.setPopBeforeSmtp(
            boolPopBeforeSmtp,
            strHost,
            strUsername,
            strPassword);

        // retrieve and verify
        assertEquals(boolPopBeforeSmtp, this.email.isPopBeforeSmtp());
        assertEquals(strHost, this.email.getPopHost());
        assertEquals(strUsername, this.email.getPopUsername());
        assertEquals(strPassword, this.email.getPopPassword());
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
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom("a@b.com");
        this.email.addTo("c@d.com");
        this.email.setSubject("test mail");

        this.email.setCharset("ISO-8859-1");
        this.email.setContent("test content", "text/plain");
        this.email.buildMimeMessage();
        MimeMessage msg = this.email.getMimeMessage();
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
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom("a@b.com");
        this.email.addTo("c@d.com");
        this.email.setSubject("test mail");

        this.email.setCharset("ISO-8859-1");
        this.email.setContent("test content", "text/plain; charset=US-ASCII");
        this.email.buildMimeMessage();
        MimeMessage msg = this.email.getMimeMessage();
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
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom("a@b.com");
        this.email.addTo("c@d.com");
        this.email.setSubject("test mail");

        this.email.setCharset("ISO-8859-1");
        this.email.setContent("test content", "application/octet-stream");
        this.email.buildMimeMessage();
        MimeMessage msg = this.email.getMimeMessage();
        msg.saveChanges();
        assertEquals("application/octet-stream", msg.getContentType());
    }

    @Test
    public void testCorrectContentTypeForPNG() throws Exception
    {
        this.email.setHostName(this.strTestMailServer);
        this.email.setSmtpPort(this.getMailServerPort());
        this.email.setFrom("a@b.com");
        this.email.addTo("c@d.com");
        this.email.setSubject("test mail");

        this.email.setCharset("ISO-8859-1");
        File png = new File("./target/test-classes/images/logos/maven-feather.png");
        this.email.setContent(png, "image/png");
        this.email.buildMimeMessage();
        MimeMessage msg = this.email.getMimeMessage();
        msg.saveChanges();
        assertEquals("image/png", msg.getContentType());
    }    
}
