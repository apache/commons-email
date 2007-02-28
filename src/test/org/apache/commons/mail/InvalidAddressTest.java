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

import org.apache.commons.mail.mocks.MockEmailConcrete;

/**
 * JUnit test case for invalid Addresses in Email Class
 *
 * @since 1.0
 * @author Niall Pemberton
 * @version $Id$
 */
public class InvalidAddressTest extends BaseEmailTestCase
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
        "local\\name@domain.com",

        //      "local\"name@domain.com",
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

    /** */
    private MockEmailConcrete email;

    /**
     * @param name name
     */
    public InvalidAddressTest(String name)
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

    /**
     *
     * @throws Exception Exception
     */
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
            catch (EmailException ignore)
            {
                // Expected Result
            }
        }
    }

    /**
     *
     * @throws Exception Exception
     */
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
            catch (EmailException ignore)
            {
                // Expected Result
            }
        }
    }

    /**
     *
     * @throws Exception Exception
     */
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
            catch (EmailException ignore)
            {
                // Expected Result
            }
        }
    }

    /**
     *
     * @throws Exception Exception
     */
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
            catch (EmailException ignore)
            {
                // Expected Result
            }
        }
    }
}
