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

import jakarta.mail.internet.InternetAddress;
import java.net.IDN;

/**
 * Converts email addresses containing International Domain Names into an ASCII
 * representation suitable for sending an email.
 *
 * @see <a href="https://docs.oracle.com/javase/tutorial/i18n/network/idn.html">https://docs.oracle.com/javase/tutorial/i18n/network/idn.html</a>
 * @see <a href="https://en.wikipedia.org/wiki/Punycode">https://en.wikipedia.org/wiki/Punycode</a>
 * @see <a href="https://tools.ietf.org/html/rfc5891">https://tools.ietf.org/html/rfc5891</a>
 * @see <a href="https://en.wikipedia.org/wiki/Punycode">https://en.wikipedia.org/wiki/Punycode</a>
 *
 * @since 1.5
 */
public class IDNEmailAddressConverter
{
    /**
     * Convert an email address to its ASCII representation using "Punycode".
     *
     * @param email email address.
     * @return The ASCII representation
     */
    public String toASCII(final String email)
    {
        final int idx = findAtSymbolIndex(email);

        if (idx < 0)
        {
            return email;
        }

        return getLocalPart(email, idx) + '@' + IDN.toASCII(getDomainPart(email, idx));
    }

    /**
     * Convert the address part of an InternetAddress to its Unicode representation.
     *
     * @param address email address.
     * @return The Unicode representation
     */
    String toUnicode(final InternetAddress address)
    {
        return address != null ? toUnicode(address.getAddress()) : null;
    }

    /**
     * Convert an "Punycode" email address to its Unicode representation.
     *
     * @param email email address.
     * @return The Unicode representation
     */
    String toUnicode(final String email)
    {
        final int idx = findAtSymbolIndex(email);

        if (idx < 0)
        {
            return email;
        }

        return getLocalPart(email, idx) + '@' + IDN.toUnicode(getDomainPart(email, idx));
    }

    /**
     * Extracts the local part of the email address.
     *
     * @param email email address.
     * @param idx index of '@' character.
     * @return local part of email
     */
    private String getLocalPart(final String email, final int idx)
    {
        return email.substring(0, idx);
    }

    /**
     * Extracts the domain part of the email address.
     *
     * @param email email address.
     * @param idx index of '@' character.
     * @return domain part of email
     */
    private String getDomainPart(final String email, final int idx)
    {
        return email.substring(idx + 1);
    }

    /**
     * Null-safe wrapper for {@link String#indexOf} to find the '@' character.
     *
     * @param value String value.
     * @return index of first '@' character or {@code -1}
     */
    private int findAtSymbolIndex(final String value)
    {
        if (value == null)
        {
            return -1;
        }

        return value.indexOf('@');
    }
}
