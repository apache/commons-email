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

import org.apache.commons.mail.util.MimeMessageUtils;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import java.util.Random;
import java.io.File;
import java.io.IOException;

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
 * @author jakarta-commons
 * @version $Id$
 *
 * @since 1.0
 */
final class EmailUtils
{
    /**
     * <p>
     * Random object used by random method. This has to be not local to the random method
     * so as to not return the same value in the same millisecond.
     * </p>
     */
    private static final Random RANDOM = new Random();

    /**
     * Constructs a new <code>EmailException</code> with no detail message.
     */
    private EmailUtils()
    {
        super();
    }

    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * @param str the String to check, may be null
     *
     * @return <code>true</code> if the String is empty or null
     *
     * @since Commons Lang v2.1, svn 240418
     */
    static boolean isEmpty(String str)
    {
        return (str == null) || (str.length() == 0);
    }

    /**
     * <p>
     * Checks if a String is not empty ("") and not null.
     * </p>
     *
     * @param str the String to check, may be null
     *
     * @return <code>true</code> if the String is not empty and not null
     *
     * @since Commons Lang v2.1, svn 240418
     */
    static boolean isNotEmpty(String str)
    {
        return (str != null) && (str.length() > 0);
    }

    /**
     * <p>
     * Validate an argument, throwing <code>IllegalArgumentException</code> if the argument is <code>null</code>.
     * </p>
     *
     * @param object the object to check is not <code>null</code>
     * @param message the exception message you would like to see if the object is <code>null</code>
     *
     * @throws IllegalArgumentException if the object is <code>null</code>
     *
     * @since Commons Lang v2.1, svn 201930
     */
    static void notNull(Object object, String message)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * <p>
     * Creates a random string whose length is the number of characters specified.
     * </p>
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
    static String randomAlphabetic(int count)
    {
        return random(count, 0, 0, true, false, null, RANDOM);
    }

    /**
     * <p>
     * Creates a random string based on a variety of options, using supplied source of randomness.
     * </p>
     *
     * <p>
     * If start and end are both <code>0</code>, start and end are set to <code>' '</code> and <code>'z'</code>,
     * the ASCII printable characters, will be used, unless letters and numbers are both <code>false</code>,
     * in which case, start and end are set to <code>0</code> and <code>Integer.MAX_VALUE</code>.
     * </p>
     *
     * <p>
     * If set is not <code>null</code>, characters between start and end are chosen.
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
     * @param chars the set of chars to choose randoms from. If <code>null</code>,
     *              then it will use the set of all chars.
     * @param random a source of randomness.
     *
     * @return the random string
     *
     * @throws IllegalArgumentException if <code>count</code> &lt; 0.
     *
     * @since Commons Lang v2.1, svn 201930
     */
    private static String random(
        int count,
        int start,
        int end,
        boolean letters,
        boolean numbers,
        char [] chars,
        Random random)
    {
        if (count == 0)
        {
            return "";
        }
        else if (count < 0)
        {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }

        if ((start == 0) && (end == 0))
        {
            end = 'z' + 1;
            start = ' ';

            if (!letters && !numbers)
            {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }

        StringBuffer buffer = new StringBuffer();
        int gap = end - start;

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
     * Convenience method to write a MimeMessage into a file.
     *
     * @param resultFile the file containing the MimeMessgae
     * @param mimeMessage the MimeMessage to write
     * @throws IOException writing the MimeMessage failed
     * @throws MessagingException writing the MimeMessage failed
     */
    static void writeMimeMessage(File resultFile, MimeMessage mimeMessage) throws IOException, MessagingException
    {
        MimeMessageUtils.writeMimeMessage(mimeMessage, resultFile);
    }
}
