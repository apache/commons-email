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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.apache.commons.mail.mocks.MockEmailConcrete;

/**
 * JUnit test case for Email Class
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */

public class EmailTest extends BaseEmailTestCase
{
    /** */
    private MockEmailConcrete email = null;

    /** */
    public static final String[] ARR_VALID_EMAILS =
        {
            "me@home.com",
            "joe.doe@apache.org",
            "someone_here@work-address.com.au" };
    /**
     * @param name name
     */
    public EmailTest(String name)
    {
        super(name);
    }

    /**
     * @throws Exception  */
    protected void setUp() throws Exception
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.email = new MockEmailConcrete();
    }

    /** */
    public void testGetSetDebug()
    {
        // JUnitDoclet begin method setBoolTest isBoolTest
        boolean[] tests = { true, false };

        for (int i = 0; i < tests.length; i++)
        {
            this.email.setDebug(tests[i]);
            assertEquals(tests[i], this.email.isDebug());
        }
    }

    /**
     *
     * @throws Exception Exception
     */
    public void testGetSetSession() throws Exception
    {

        Properties properties = new Properties(System.getProperties());
        properties.setProperty(Email.MAIL_TRANSPORT_PROTOCOL, Email.SMTP);

        properties.setProperty(
            Email.MAIL_PORT,
            String.valueOf(this.getMailServerPort()));
        properties.setProperty(Email.MAIL_HOST, this.strTestMailServer);
        properties.setProperty(Email.MAIL_DEBUG, String.valueOf(false));

        Session mySession = Session.getInstance(properties, null);

        this.email.setMailSession(mySession);
        assertEquals(mySession, this.email.getMailSession());

    }

    /** */
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

    /** */
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

    /** */
    public void testGetSetCharset()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setCharset(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getCharset());
        }
    }

    /** */
    public void testSetContentMimeMultipart()
    {
        MimeMultipart[] tests =
            { new MimeMultipart(), new MimeMultipart("abc123"), null };

        for (int i = 0; i < tests.length; i++)
        {
            this.email.setContent(tests[i]);
            assertEquals(tests[i], this.email.getContentMimeMultipart());
        }
    }

    /** */
    public void testSetContentObject()
    {
        // setup
        String testObject = "test string object";
        String testContentType = "";

        // ====================================================================
        // test (string object and valid content type)
        testObject = "test string object";
        testContentType = " ; charset=" + Email.US_ASCII;

        this.email.setContent(testObject, testContentType);
        assertEquals(testObject, this.email.getContentObject());
        assertEquals(testContentType, this.email.getContentType());

        // ====================================================================
        // test (null string object and valid content type)
        testObject = null;
        testContentType = " ; charset=" + Email.US_ASCII + " some more here";

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

    /** */
    public void testGetSetHostName()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setHostName(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getHostName());
        }
    }

    /** */
    public void testGetSetSmtpPort()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        int[] tests = { 1, Integer.MAX_VALUE };

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
        int[] testExs = { Integer.MIN_VALUE, -1, 0 };

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

    /**
     *
     * @throws Exception Exception
     */
    public void testSetFrom() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        ArrayList arrExpected = new ArrayList();
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

    /**
     *
     * @throws Exception Exception
     */
    public void testSetFromWithEnconding() throws Exception
    {
        // ====================================================================
        // Test Success (with charset set)
        // ====================================================================
        String testValidEmail = "me@home.com";

        InternetAddress inetExpected =
            new InternetAddress("me@home.com", "me@home.com");

        // set from
        this.email.setCharset(Email.ISO_8859_1);
        this.email.setFrom(testValidEmail);

        // retrieve and verify
        assertEquals(inetExpected, this.email.getFromAddress());

    }

    /**
     *
     * @throws Exception Exception
     */
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
        String[] testEmailNames = { "Name1", "", null };
        ArrayList arrExpected = new ArrayList();
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
        // bad encoding
        try
        {
            // reset the mail class
            MockEmailConcrete anotherEmail = new MockEmailConcrete();
            // set a dodgy encoding scheme
            anotherEmail.setCharset("bad.encoding\uc5ec\n");
            // set a valid address but bad personal name
            anotherEmail.setFrom(
                "me@home.com",
                "\t.bad.personal.name.\uc5ec\n");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws EmailException  
     * @throws UnsupportedEncodingException */
    public void testAddTo() throws EmailException, UnsupportedEncodingException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        ArrayList arrExpected = new ArrayList();
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
            this.email.addTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToList().size());
        assertEquals(arrExpected.toString(), this.email.getToList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddToWithEncoding() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.email.charset = Email.US_ASCII;

        ArrayList arrExpected = new ArrayList();
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
            this.email.addTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToList().size());
        assertEquals(arrExpected.toString(), this.email.getToList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddTo2() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = { "Name1", "", null };

        ArrayList arrExpected = new ArrayList();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
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
            this.email.addTo(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getToList().size());
        assertEquals(arrExpected.toString(), this.email.getToList().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // bad encoding
        try
        {
            // reset the mail class
            MockEmailConcrete anotherEmail = new MockEmailConcrete();
            // set a dodgy encoding scheme
            anotherEmail.setCharset("bad.encoding\uc5ec\n");
            // set a valid address but bad personal name
            anotherEmail.addTo("me@home.com", "\t.bad.name.\uc5ec\n");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws UnsupportedEncodingException   
     * @throws EmailException 
     */
    public void testSetTo() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        ArrayList testEmailValid2 = new ArrayList();
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
        assertEquals(testEmailValid2.size(), this.email.getToList().size());
        assertEquals(
            testEmailValid2.toString(),
            this.email.getToList().toString());

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
            this.email.setTo(new ArrayList());
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddCc() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        ArrayList arrExpected = new ArrayList();
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
            this.email.addCc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcList().size());
        assertEquals(arrExpected.toString(), this.email.getCcList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddCcWithEncoding() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.email.charset = Email.US_ASCII;

        ArrayList arrExpected = new ArrayList();
        arrExpected.add(new InternetAddress("me@home.com", "me@home.com"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        // add valid ccs
        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            this.email.addCc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcList().size());
        assertEquals(arrExpected.toString(), this.email.getCcList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddCc2() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = { "Name1", "", null };

        ArrayList arrExpected = new ArrayList();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
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
            this.email.addCc(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getCcList().size());
        assertEquals(arrExpected.toString(), this.email.getCcList().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // bad encoding
        try
        {
            // reset the mail class
            MockEmailConcrete anotherEmail = new MockEmailConcrete();
            // set a dodgy encoding scheme
            anotherEmail.setCharset("bad.encoding\uc5ec\n");
            // set a valid address but bad personal name
            anotherEmail.addCc("me@home.com", "\t.bad.name.\uc5ec\n");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws EmailException  */
    public void testSetCc() throws EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        ArrayList testEmailValid2 = new ArrayList();
        testEmailValid2.add("Name1 <me@home.com>");
        testEmailValid2.add("\"joe.doe@apache.org\" <joe.doe@apache.org>");
        testEmailValid2.add(
            "\"someone_here@work.com.au\" <someone_here@work.com.au>");

        this.email.setCc(testEmailValid2);
        assertEquals(testEmailValid2, this.email.getCcList());

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
            this.email.setCc(new ArrayList());
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddBcc() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        ArrayList arrExpected = new ArrayList();
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
            // add a valid bcc
            this.email.addBcc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccList().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddBccWithEncoding() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.email.charset = Email.US_ASCII;

        ArrayList arrExpected = new ArrayList();
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
            // set bccs
            this.email.addBcc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccList().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddBcc2() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = { "Name1", "", null };

        ArrayList arrExpected = new ArrayList();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
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
            this.email.addBcc(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getBccList().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getBccList().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // bad encoding
        try
        {
            // reset the mail class
            MockEmailConcrete anotherEmail = new MockEmailConcrete();
            // set a dodgy encoding scheme
            anotherEmail.setCharset("bad.encoding\uc5ec\n");
            // set a valid address but bad personal name
            anotherEmail.addBcc("me@home.com", "\t.bad.name.\uc5ec\n");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testSetBcc() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        ArrayList testInetEmailValid = new ArrayList();
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
        assertEquals(testInetEmailValid, this.email.getBccList());

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
            this.email.setBcc(new ArrayList());
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddReplyTo() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        ArrayList arrExpected = new ArrayList();
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
            // set replyTo
            this.email.addReplyTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getReplyList().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getReplyList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddReplyToWithEncoding() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        this.email.charset = Email.US_ASCII;

        ArrayList arrExpected = new ArrayList();
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
            // set replyTo
            this.email.addReplyTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getReplyList().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getReplyList().toString());
    }

    /**
     * @throws UnsupportedEncodingException  
     * @throws EmailException */
    public void testAddReplyTo2() throws UnsupportedEncodingException, EmailException
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = { "Name1", "", null };

        ArrayList arrExpected = new ArrayList();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
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
            // set replyTo
            this.email.addReplyTo(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), this.email.getReplyList().size());
        assertEquals(
            arrExpected.toString(),
            this.email.getReplyList().toString());

        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // bad encoding
        try
        {
            // reset the mail class
            MockEmailConcrete anotherEmail = new MockEmailConcrete();
            // set a dodgy encoding scheme
            anotherEmail.setCharset("bad.encoding\uc5ec\n");
            // set a valid address but bad personal name
            anotherEmail.addReplyTo("me@home.com", "\t.bad.name.\uc5ec\n");
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(true);
        }
    }

    /** */
    public void testAddHeader()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        Hashtable ht = new Hashtable();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        Enumeration enumKey = ht.keys();

        while (enumKey.hasMoreElements())
        {
            String strName = (String) enumKey.nextElement();
            String strValue = (String) ht.get(strName);

            this.email.addHeader(strName, strValue);
        }

        assertEquals(ht.size(), this.email.getHeaders().size());
        assertEquals(ht, this.email.getHeaders());
    }

    /** */
    public void testAddHeaderEx()
    {
        // ====================================================================
        // Test Exceptions
        // ====================================================================
        Hashtable htBad = new Hashtable();
        htBad.put("X-Mailer", "");
        htBad.put("X-Priority", "");
        htBad.put("", "me@home.com");

        Hashtable arrExpected = new Hashtable();
        Enumeration enumKeyBad = htBad.keys();

        while (enumKeyBad.hasMoreElements())
        {
            try
            {
                String strName = (String) enumKeyBad.nextElement();
                String strValue = (String) htBad.get(strName);

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

    /** */
    public void testSetHeaders()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        Hashtable ht = new Hashtable();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        this.email.setHeaders(ht);

        assertEquals(ht.size(), this.email.getHeaders().size());
        assertEquals(ht, this.email.getHeaders());
    }

    /** */
    public void testSetHeadersEx()
    {
        // ====================================================================
        // Test Exceptions
        // ====================================================================
        // first test
        Hashtable htBad = new Hashtable();
        htBad.put("X-Mailer", "");

        Hashtable arrExpected = new Hashtable();

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
        htBad = new Hashtable();
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

    /** */
    public void testSetSubject()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.email.setSubject(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.email.getSubject());
        }
    }

    /** */
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
                " ; charset=" + Email.US_ASCII);

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
    }

    /** */
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
     * @throws EmailException  
     * @throws UnsupportedEncodingException */
    public void testToInternetAddressArray() throws EmailException, UnsupportedEncodingException
    {
        ArrayList testInetEmailValid = new ArrayList();

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
            this.email.toInternetAddressArray(
                this.email.getBccList()).length);
    }

    /** */
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

}
