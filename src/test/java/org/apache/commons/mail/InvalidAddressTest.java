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

import static org.junit.Assert.fail;

import org.apache.commons.mail.mocks.MockEmailConcrete;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case for invalid Addresses in Email Class
 *
 * @since 1.0
 */
public class InvalidAddressTest extends AbstractEmailTest
{
    /** */
    private static final String [] ARR_INVALID_EMAILS = {
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
        // "local\\name@domain.com", is considered valid for mail-1.4.1
        "local\"name@domain.com",
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

        //      "local.name@domain[com",
        "local.name@domain]com",
        "local.name@domain\\com",
        "local.name@domain\tcom",
        "local.name@domain\ncom",
        "local.name@domain\rcom",
        "local.name@",
        "@domain.com"
    };

    private MockEmailConcrete email;

    @Before
    public void setUpInvalidAddressTest()
    {
        // reusable objects to be used across multiple tests
        this.email = new MockEmailConcrete();
    }

    @Test
    public void testSetInvalidFrom()
            throws Exception
    {
        // ====================================================================
        // Test setting invalid 'from' addresses
        // ====================================================================
        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {
            try
            {
                // set from
                email.setFrom(ARR_INVALID_EMAILS[i]);

                // Expected an exception to be thrown
                fail("setFrom " + i + " passed: " + ARR_INVALID_EMAILS[i]);
            }
            catch (final EmailException ignore)
            {
                // Expected Result
            }
        }
    }

    @Test
    public void testAddInvalidTo()
            throws Exception
    {
        // ====================================================================
        // Test adding invalid 'to' addresses
        // ====================================================================
        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {
            try
            {
                // Add To
                email.addTo(ARR_INVALID_EMAILS[i], "Joe");

                // Expected an exception to be thrown
                fail("addTo " + i + " passed: " + ARR_INVALID_EMAILS[i]);
            }
            catch (final EmailException ignore)
            {
                // Expected Result
            }
        }
    }

    @Test
    public void testAddInvalidCc()
            throws Exception
    {
        // ====================================================================
        // Test adding invalid 'cc' addresses
        // ====================================================================
        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {
            try
            {
                // add cc
                email.addCc(ARR_INVALID_EMAILS[i], "Joe");

                // Expected an exception to be thrown
                fail("addCc " + i + " passed: " + ARR_INVALID_EMAILS[i]);
            }
            catch (final EmailException ignore)
            {
                // Expected Result
            }
        }
    }

    @Test
    public void testAddInvalidBcc()
            throws Exception
    {
        // ====================================================================
        // Test adding invalid 'Bcc' addresses
        // ====================================================================
        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++)
        {
            try
            {
                // add bcc
                email.addBcc(ARR_INVALID_EMAILS[i], "Joe");

                // Expected an exception to be thrown
                fail("addBcc " + i + " passed: " + ARR_INVALID_EMAILS[i]);
            }
            catch (final EmailException ignore)
            {
                // Expected Result
            }
        }
    }
}
