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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.util.MimeMessageUtils;

/**
 * Utility methods used by commons-email.
 *
 * <p>
 * These methods are copied from other commons components (commons-lang) to avoid creating
 * a dependency for such a small component.
 * </p>
 *
 * <p>
 * This is a package scoped class, and should not be used directly by users.
 * </p>
 *
 * @since 1.0
 */
final class EmailUtils
{
    /**
     * Random object used by random method. This has to be not local to the random method
     * so as to not return the same value in the same millisecond.
     */
    private static final Random RANDOM = new Random();

    /**
     * The default charset used for URL encoding.
     */
    private static final String US_ASCII = "US-ASCII";

    /**
     * Radix used in encoding.
     */
    private static final int RADIX = 16;

    /**
     * The escape character used for the URL encoding scheme.
     */
    private static final char ESCAPE_CHAR = '%';

    /**
     * BitSet of RFC 2392 safe URL characters.
     */
    private static final BitSet SAFE_URL = new BitSet(256);

    // Static initializer for safe_uri
    static {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++)
        {
            SAFE_URL.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++)
        {
            SAFE_URL.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++)
        {
            SAFE_URL.set(i);
        }

        // safe chars
        SAFE_URL.set('-');
        SAFE_URL.set('_');
        SAFE_URL.set('.');
        SAFE_URL.set('*');
        SAFE_URL.set('+');
        SAFE_URL.set('$');
        SAFE_URL.set('!');
        SAFE_URL.set('\'');
        SAFE_URL.set('(');
        SAFE_URL.set(')');
        SAFE_URL.set(',');
        SAFE_URL.set('@');
    }

    /**
     * Constructs a new {@code EmailException} with no detail message.
     */
    private EmailUtils()
    {
        super();
    }

    /**
     * Checks if a String is empty ("") or null.
     *
     * @param str the String to check, may be null
     *
     * @return {@code true} if the String is empty or null
     *
     * @since Commons Lang v2.1, svn 240418
     */
    static boolean isEmpty(final String str)
    {
        return str == null || str.length() == 0;
    }

    /**
     * Checks if a String is not empty ("") and not null.
     *
     * @param str the String to check, may be null
     *
     * @return {@code true} if the String is not empty and not null
     *
     * @since Commons Lang v2.1, svn 240418
     */
    static boolean isNotEmpty(final String str)
    {
        return str != null && str.length() > 0;
    }

    /**
     * Validate an argument, throwing {@code IllegalArgumentException}
     * if the argument is {@code null}.
     *
     * @param object the object to check is not {@code null}
     * @param message the exception message you would like to see if the object is {@code null}
     *
     * @throws IllegalArgumentException if the object is {@code null}
     *
     * @since Commons Lang v2.1, svn 201930
     */
    static void notNull(final Object object, final String message)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of alphabetic characters.
     * </p>
     *
     * @param count the length of random string to create
     *
     * @return the random string
     *
     * @since Commons Lang v2.1, svn 201930
     */
    static String randomAlphabetic(final int count)
    {
        return random(count, 0, 0, true, false, null, RANDOM);
    }

    /**
     * Creates a random string based on a variety of options, using supplied source of randomness.
     *
     * <p>
     * If start and end are both {@code 0}, start and end are set to {@code ' '} and {@code 'z'},
     * the ASCII printable characters, will be used, unless letters and numbers are both {@code false},
     * in which case, start and end are set to {@code 0} and {@code Integer.MAX_VALUE}.
     * </p>
     *
     * <p>
     * If set is not {@code null}, characters between start and end are chosen.
     * </p>
     *
     * <p>
     * This method accepts a user-supplied {@link Random} instance to use as a source of randomness. By seeding a
     * single {@link Random} instance with a fixed seed and using it for each call, the same random sequence of strings
     * can be generated repeatedly and predictably.
     * </p>
     *
     * @param count the length of random string to create
     * @param start the position in set of chars to start at
     * @param end the position in set of chars to end before
     * @param letters only allow letters?
     * @param numbers only allow numbers?
     * @param chars the set of chars to choose randoms from. If {@code null},
     *              then it will use the set of all chars.
     * @param random a source of randomness.
     *
     * @return the random string
     *
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     *
     * @since Commons Lang v2.1, svn 201930
     */
    private static String random(
        int count,
        int start,
        int end,
        final boolean letters,
        final boolean numbers,
        final char [] chars,
        final Random random)
    {
        if (count == 0)
        {
            return "";
        }
        else if (count < 0)
        {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }

        if (start == 0 && end == 0)
        {
            end = 'z' + 1;
            start = ' ';

            if (!letters && !numbers)
            {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }

        final StringBuffer buffer = new StringBuffer();
        final int gap = end - start;

        while (count-- != 0)
        {
            char ch;

            if (chars == null)
            {
                ch = (char) (random.nextInt(gap) + start);
            }
            else
            {
                ch = chars[random.nextInt(gap) + start];
            }

            if ((letters && numbers && Character.isLetterOrDigit(ch)) || (letters && Character.isLetter(ch))
                            || (numbers && Character.isDigit(ch)) || (!letters && !numbers))
            {
                buffer.append(ch);
            }
            else
            {
                count++;
            }
        }

        return buffer.toString();
    }

    /**
     * Replaces end-of-line characters with spaces.
     *
     * @param input the input string to be scanned.
     * @return a clean string
     */
    static String replaceEndOfLineCharactersWithSpaces(final String input)
    {
        return input == null ? null : input.replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * Encodes an input string according to RFC 2392. Unsafe characters are escaped.
     *
     * @param input the input string to be URL encoded
     * @return a URL encoded string
     * @throws UnsupportedEncodingException if "US-ASCII" charset is not available
     * @see <a href="http://tools.ietf.org/html/rfc2392">RFC 2392</a>
     */
    static String encodeUrl(final String input) throws UnsupportedEncodingException
    {
        if (input == null)
        {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        for (final byte c : input.getBytes(US_ASCII))
        {
            int b = c;
            if (b < 0)
            {
                b = 256 + b;
            }
            if (SAFE_URL.get(b))
            {
                builder.append((char) b);
            }
            else
            {
                builder.append(ESCAPE_CHAR);
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                builder.append(hex1);
                builder.append(hex2);
            }
        }
        return builder.toString();
    }

    /**
     * Convenience method to write a MimeMessage into a file.
     *
     * @param resultFile the file containing the MimeMessgae
     * @param mimeMessage the MimeMessage to write
     * @throws IOException writing the MimeMessage failed
     * @throws MessagingException writing the MimeMessage failed
     */
    static void writeMimeMessage(final File resultFile, final MimeMessage mimeMessage)
            throws IOException, MessagingException
    {
        MimeMessageUtils.writeMimeMessage(mimeMessage, resultFile);
    }
}
