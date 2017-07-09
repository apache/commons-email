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
package org.apache.commons.mail.util;

import static org.junit.Assert.assertEquals;

import javax.mail.internet.InternetAddress;

import org.junit.Test;

public class IDNEmailAddressConverterTest {

    private static final String AUSTRIAN_IDN_EMAIL_ADDRESS = "noreply@d\u00F6m\u00E4in.example";
    private static final String CZECH_IDN_EMAIL_ADDRESS = "noreply@\u010Desk\u00E1republika.icom.museum";
    private static final String RUSSIAN_IDN_EMAIL_ADDRESS = "noreply@\u0440\u043E\u0441\u0441\u0438\u044F.\u0438\u043A\u043E\u043C.museum";

    private static final String GERMAN_IDN_EMAIL_NAME = "noreply@d\u00F6m\u00E4in.example";

    private static final String[] IDN_EMAIL_ADDRESSES = { AUSTRIAN_IDN_EMAIL_ADDRESS, CZECH_IDN_EMAIL_ADDRESS, RUSSIAN_IDN_EMAIL_ADDRESS };

    private final IDNEmailAddressConverter idnEmailConverter = new IDNEmailAddressConverter();

    @Test
    public void testConvertInvalidEmailAddressToAscii()
    {
        assertEquals(null, idnEmailConverter.toASCII(null));
        assertEquals("", idnEmailConverter.toASCII(""));
        assertEquals("@", idnEmailConverter.toASCII("@"));
        assertEquals("@@", idnEmailConverter.toASCII("@@"));
        assertEquals("foo", idnEmailConverter.toASCII("foo"));
        assertEquals("foo@", idnEmailConverter.toASCII("foo@"));
        assertEquals("@badhost.com", idnEmailConverter.toASCII("@badhost.com"));
    }

    @Test
    public void testIDNEmailAddressToAsciiConversion()
    {
        assertEquals("noreply@xn--dmin-moa0i.example", idnEmailConverter.toASCII(AUSTRIAN_IDN_EMAIL_ADDRESS));
        assertEquals("noreply@xn--h1alffa9f.xn--h1aegh.museum", idnEmailConverter.toASCII(RUSSIAN_IDN_EMAIL_ADDRESS));
    }

    @Test
    public void testMultipleIDNEmailAddressToAsciiConversion()
    {
        assertEquals("noreply@xn--dmin-moa0i.example", idnEmailConverter.toASCII(idnEmailConverter.toASCII(AUSTRIAN_IDN_EMAIL_ADDRESS)));
    }

    @Test
    public void testNonIDNEmailAddressToAsciiConversion()
    {
        assertEquals("me@home.com", idnEmailConverter.toASCII("me@home.com"));
    }


    @Test
    public void testInternetAddressToAsciiConversion() throws Exception
    {
        final InternetAddress address = new InternetAddress(idnEmailConverter.toASCII(AUSTRIAN_IDN_EMAIL_ADDRESS));
        assertEquals(AUSTRIAN_IDN_EMAIL_ADDRESS, idnEmailConverter.toUnicode(address));

        final InternetAddress addressWithPersonalName = new InternetAddress(idnEmailConverter.toASCII(AUSTRIAN_IDN_EMAIL_ADDRESS), GERMAN_IDN_EMAIL_NAME);
        assertEquals(AUSTRIAN_IDN_EMAIL_ADDRESS, idnEmailConverter.toUnicode(addressWithPersonalName));
    }

    @Test
    public void testRoundTripConversionOfIDNEmailAddress()
    {
        for(final String email : IDN_EMAIL_ADDRESSES)
        {
            assertEquals(email, idnEmailConverter.toUnicode(idnEmailConverter.toASCII(email)));
        }
    }

}
