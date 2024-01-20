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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.IntStream;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.mocks.MockEmailConcrete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for invalid Addresses in Email Class
 */
public class InvalidAddressTest extends AbstractEmailTest {

    // @formatter:off
    private static final String[] ARR_INVALID_EMAILS = {
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
            // "local.name@domain[com",
            "local.name@domain]com",
            "local.name@domain\\com",
            "local.name@domain\tcom",
            "local.name@domain\ncom",
            "local.name@domain\rcom",
            "local.name@",
            "@domain.com" };
    // @formatter:on

    private MockEmailConcrete email;

    @BeforeEach
    public void setUpInvalidAddressTest() {
        // reusable objects to be used across multiple tests
        email = new MockEmailConcrete();
    }

    @Test
    public void testAddInvalidBcc() throws Exception {
        // Test adding invalid 'BCC' addresses
        // @formatter:off
        IntStream.range(0, ARR_INVALID_EMAILS.length).forEach(i -> assertThrows(EmailException.class,
                () -> email.addBcc(ARR_INVALID_EMAILS[i], "Joe"),
                () -> "addBcc " + i + " passed: " + ARR_INVALID_EMAILS[i]));
        // @formatter:on
    }

    @Test
    public void testAddInvalidCc() throws Exception {
        // Test adding invalid 'CC' addresses
        // @formatter:off
        IntStream.range(0, ARR_INVALID_EMAILS.length).forEach(i -> assertThrows(EmailException.class,
                () -> email.addCc(ARR_INVALID_EMAILS[i], "Joe"),
                () -> "addCc " + i + " passed: " + ARR_INVALID_EMAILS[i]));
        // @formatter:on
    }

    @Test
    public void testAddInvalidTo() throws Exception {
        // Test adding invalid 'to' addresses
        // @formatter:off
        IntStream.range(0, ARR_INVALID_EMAILS.length).forEach(i -> assertThrows(EmailException.class,
                () -> email.addTo(ARR_INVALID_EMAILS[i], "Joe"),
                () -> "addTo " + i + " passed: " + ARR_INVALID_EMAILS[i]));
        // @formatter:on
    }

    @Test
    public void testSetInvalidFrom() throws Exception {
        // Test setting invalid 'from' addresses
        // @formatter:off
        IntStream.range(0, ARR_INVALID_EMAILS.length).forEach(i -> assertThrows(EmailException.class,
                () -> email.setFrom(ARR_INVALID_EMAILS[i], "Joe"),
                () -> "setFrom " + i + " passed: " + ARR_INVALID_EMAILS[i]));
        // @formatter:on
    }
}
