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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for EmailAttachment Class.
 */
public class EmailAttachmentTest extends AbstractEmailTest {
    private EmailAttachment attachment;

    @BeforeEach
    public void setUpAttachmentTest() {
        attachment = new EmailAttachment();
    }

    @Test
    public void testGetSetDescription() {
        for (final String validChar : testCharsValid) {
            attachment.setDescription(validChar);
            assertEquals(validChar, attachment.getDescription());
        }
    }

    @Test
    public void testGetSetDisposition() {
        for (final String validChar : testCharsValid) {
            attachment.setDisposition(validChar);
            assertEquals(validChar, attachment.getDisposition());
        }
    }

    @Test
    public void testGetSetName() {
        for (final String validChar : testCharsValid) {
            attachment.setName(validChar);
            assertEquals(validChar, attachment.getName());
        }
    }

    @Test
    public void testGetSetPath() {
        for (final String validChar : testCharsValid) {
            attachment.setPath(validChar);
            assertEquals(validChar, attachment.getPath());
        }
    }

    @Test
    public void testGetSetURL() throws Exception {
        final String[] tests = { "https://localhost/", "http://www.apache.org/", "http://foo.notexisting.org" };

        for (final String urlString : tests) {
            final URL testURL = new URL(urlString);
            attachment.setURL(testURL);
            assertEquals(testURL, attachment.getURL());
        }
    }

}
