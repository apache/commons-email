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
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case demonstrating InternetAddress validation.
 *
 * @since 1.0
 */
public class InvalidInternetAddressTest extends AbstractEmailTest
{
    /** */
    private static final String VALID_QUOTED_EMAIL = "\"John O'Groats\"@domain.com";

    /** JavaMail 1.2. does not know about this */
    private static Method validateMethod;

    /** */
    private static final String[] ARR_INVALID_EMAILS =
        {
            "local name@domain.com",
            "local(name@domain.com",
            "local)name@domain.com",
            "local<name@domain.com",
            "local>name@domain.com",
            "local,name@domain.com",
            "local;name@domain.com",
            "local:name@domain.com",
            "local[name@domain.com",
            "local]name@domain.com",
            // "local\\name@domain.com", -- works for javamail-1.4.4
            // "local\"name@domain.com", -- works for javamail-1.4.4
            "local\tname@domain.com",
            "local\nname@domain.com",
            "local\rname@domain.com",
            "local.name@domain com",
            "local.name@domain(com",
            "local.name@domain)com",
            "local.name@domain<com",
            "local.name@domain>com",
            "local.name@domain,com",
            "local.name@domain;com",
            "local.name@domain:com",
            // "local.name@domain[com", -- works for javamail-1.5.5
            "local.name@domain]com",
            "local.name@domain\\com",
            "local.name@domain\tcom",
            "local.name@domain\ncom",
            "local.name@domain\rcom",
            "local.name@",
            "@domain.com" };

    /**
     * Setup for a test
     */
    @Before
    public void setUpInvalidInternetAddressTest()
    {
        try
        {
            validateMethod = InternetAddress.class.getMethod("validate");
        }
        catch (final Exception e)
        {
            assertEquals("Got wrong Exception when looking for validate()", NoSuchMethodException.class, e.getClass());
        }
    }

    @Test
    public void testStrictConstructor() throws Exception
    {
        // ====================================================================
        // Prove InternetAddress constructor is throwing exception.
        // ====================================================================


        // test Invalid Email addresses
        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {

            try
            {
                // Create Internet Address using "strict" constructor
                new InternetAddress(ARR_INVALID_EMAILS[i]);

                // Expected an exception to be thrown
                fail("Strict " + i + " passed: " + ARR_INVALID_EMAILS[i]);
            }
            catch (final Exception ex)
            {
                // Expected Result
            }

        }

        // test valid 'quoted' Email addresses
        try
        {

            // Create Internet Address using "strict" constructor
            new InternetAddress(VALID_QUOTED_EMAIL);

        }
        catch (final Exception ex)
        {
            fail("Valid Quoted Email failed: " + VALID_QUOTED_EMAIL
                + " - " + ex.getMessage());
        }
    }

    @Test
    public void testValidateMethod() throws Exception
    {
        if (validateMethod == null)
        {
            return;
        }

        // ====================================================================
        // Prove InternetAddress constructor isn't throwing exception and
        // the validate() method is
        // ====================================================================

        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {

            final InternetAddress address = new InternetAddress(ARR_INVALID_EMAILS[i], "Joe");

            // N.B. validate() doesn't check addresses containing quotes or '['
            final boolean quoted = ARR_INVALID_EMAILS[i].contains("\"");
            final int atIndex    = ARR_INVALID_EMAILS[i].indexOf("@");
            final boolean domainBracket  = atIndex >= 0
                && ARR_INVALID_EMAILS[i].indexOf("[", atIndex)  >= 0;
            try
            {
                validateMethod.invoke(address, (Object[]) null);

                if (!(quoted || domainBracket))
                {
                    fail("Validate " + i + " passed: " + ARR_INVALID_EMAILS[i]);
                }
            }
            catch (final Exception ex)
            {
                if (quoted || domainBracket)
                {
                    fail("Validate " + i + " failed: " + ARR_INVALID_EMAILS[i]
                        + " - " + ex.getMessage());
                }
            }
        }

        // test valid 'quoted' Email addresses
        try
        {
            validateMethod.invoke(new InternetAddress(VALID_QUOTED_EMAIL, "Joe"), (Object[]) null);
        }
        catch (final Exception ex)
        {
            fail("Valid Quoted Email failed: " + VALID_QUOTED_EMAIL
                + " - " + ex.getMessage());
        }
    }

    @Test
    public void testValidateMethodCharset() throws Exception
    {
        if (validateMethod == null)
        {
            return;
        }

        // ====================================================================
        // Prove InternetAddress constructor isn't throwing exception and
        // the validate() method is
        // ====================================================================

        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {

            final InternetAddress address = new InternetAddress(ARR_INVALID_EMAILS[i], "Joe", "UTF-8");

            // N.B. validate() doesn't check addresses containing quotes or '['
            final boolean quoted = ARR_INVALID_EMAILS[i].contains("\"");
            final int atIndex    = ARR_INVALID_EMAILS[i].indexOf("@");
            final boolean domainBracket  = atIndex >= 0
                && ARR_INVALID_EMAILS[i].indexOf("[", atIndex)  >= 0;

            try
            {
                validateMethod.invoke(address, (Object[]) null);
                if (!(quoted || domainBracket))
                {
                    fail("Validate " + i + " passed: " + ARR_INVALID_EMAILS[i]);
                }

            }
            catch (final Exception ex)
            {
                if (quoted || domainBracket)
                {
                    fail("Validate " + i + " failed: " + ARR_INVALID_EMAILS[i]
                        + " - " + ex.getMessage());
                }
            }

        }

        // test valid 'quoted' Email addresses
        try
        {
            validateMethod.invoke(new InternetAddress(VALID_QUOTED_EMAIL, "Joe", "UTF-8"), (Object[]) null);
        }
        catch (final Exception ex)
        {
            fail("Valid Quoted Email failed: " + VALID_QUOTED_EMAIL
                + " - " + ex.getMessage());
        }
    }

}
