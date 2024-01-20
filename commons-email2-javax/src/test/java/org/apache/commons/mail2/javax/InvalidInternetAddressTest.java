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
package org.apache.commons.mail2.javax;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;

/**
 * JUnit test case demonstrating InternetAddress validation.
 */
public class InvalidInternetAddressTest extends AbstractEmailTest {
    /** */
    private static final String VALID_QUOTED_EMAIL = "\"John O'Groats\"@domain.com";

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
    // @formatter:on

    @Test
    public void testStrictConstructor() throws Exception {
        // Prove InternetAddress constructor is throwing exception.

        // test Invalid Email addresses
        // @formatter:off
        IntStream.range(0, ARR_INVALID_EMAILS.length).forEach(i -> assertThrows(Exception.class,
                () -> new InternetAddress(ARR_INVALID_EMAILS[i]),
                () -> "Strict " + i + " passed: " + ARR_INVALID_EMAILS[i]));
        // @formatter:on

        // test valid 'quoted' Email addresses
        // Create Internet Address using "strict" constructor
        assertDoesNotThrow(() -> new InternetAddress(VALID_QUOTED_EMAIL), () -> "Valid Quoted Email failed: " + VALID_QUOTED_EMAIL);
    }

    @Test
    public void testValidateMethod() throws Exception {
        // Prove InternetAddress constructor isn't throwing exception and
        // the validate() method is

        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++) {

            final InternetAddress address = new InternetAddress(ARR_INVALID_EMAILS[i], "Joe");

            // N.B. validate() doesn't check addresses containing quotes or '['
            final boolean quoted = ARR_INVALID_EMAILS[i].contains("\"");
            final int atIndex = ARR_INVALID_EMAILS[i].indexOf("@");
            final boolean domainBracket = atIndex >= 0 && ARR_INVALID_EMAILS[i].indexOf("[", atIndex) >= 0;
            try {
                address.validate();

                if (!(quoted || domainBracket)) {
                    fail("Validate " + i + " passed: " + ARR_INVALID_EMAILS[i]);
                }
            } catch (final Exception ex) {
                if (quoted || domainBracket) {
                    fail("Validate " + i + " failed: " + ARR_INVALID_EMAILS[i] + " - " + ex.getMessage());
                }
            }
        }

        // test valid 'quoted' Email addresses
        assertDoesNotThrow(() -> new InternetAddress(VALID_QUOTED_EMAIL, "Joe").validate(), () -> "Valid Quoted Email failed: " + VALID_QUOTED_EMAIL);
    }

    @Test
    public void testValidateMethodCharset() throws Exception {
        // Prove InternetAddress constructor isn't throwing exception and
        // the validate() method is

        for (int i = 0; i < ARR_INVALID_EMAILS.length; i++) {

            final InternetAddress address = new InternetAddress(ARR_INVALID_EMAILS[i], "Joe", StandardCharsets.UTF_8.name());

            // N.B. validate() doesn't check addresses containing quotes or '['
            final boolean quoted = ARR_INVALID_EMAILS[i].contains("\"");
            final int atIndex = ARR_INVALID_EMAILS[i].indexOf("@");
            final boolean domainBracket = atIndex >= 0 && ARR_INVALID_EMAILS[i].indexOf("[", atIndex) >= 0;

            try {
                address.validate();
                if (!(quoted || domainBracket)) {
                    fail("Validate " + i + " passed: " + ARR_INVALID_EMAILS[i]);
                }

            } catch (final Exception ex) {
                if (quoted || domainBracket) {
                    fail("Validate " + i + " failed: " + ARR_INVALID_EMAILS[i] + " - " + ex.getMessage());
                }
            }

        }

        // test valid 'quoted' Email addresses
        assertDoesNotThrow(() -> new InternetAddress(VALID_QUOTED_EMAIL, "Joe", StandardCharsets.UTF_8.name()).validate(),
                () -> "Valid Quoted Email failed: " + VALID_QUOTED_EMAIL);

    }

}
