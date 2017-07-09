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

import java.io.UnsupportedEncodingException;

import org.junit.Test;

/**
 * JUnit test case for EmailUtils Class
 *
 * @since 1.3
 */
public class EmailUtilsTest {

    @Test
    public void testClearEndOfLineCharacters() {
        assertEquals(null, EmailUtils.replaceEndOfLineCharactersWithSpaces(null));
        assertEquals("", EmailUtils.replaceEndOfLineCharactersWithSpaces(""));
        assertEquals("   ", EmailUtils.replaceEndOfLineCharactersWithSpaces("   "));
        assertEquals("abcdefg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abcdefg"));
        assertEquals("abc defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\rdefg"));
        assertEquals("abc defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\ndefg"));
        assertEquals("abc  defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\r\ndefg"));
        assertEquals("abc  defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\n\rdefg"));
    }

    @Test
    public void testUrlEncoding() throws UnsupportedEncodingException {
        assertEquals("abcdefg", EmailUtils.encodeUrl("abcdefg"));
        assertEquals("0123456789", EmailUtils.encodeUrl("0123456789"));
        assertEquals("Test%20CID", EmailUtils.encodeUrl("Test CID"));
        assertEquals("joe.doe@apache.org", EmailUtils.encodeUrl("joe.doe@apache.org"));
        assertEquals("joe+doe@apache.org", EmailUtils.encodeUrl("joe+doe@apache.org"));
        assertEquals("peter%26paul%26mary@oldmusic.org", EmailUtils.encodeUrl("peter&paul&mary@oldmusic.org"));
    }
}
