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

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case for EmailAttachment Class.
 *
 * @since 1.0
 * @version $Id$
 */
public class EmailAttachmentTest extends AbstractEmailTest
{
    private EmailAttachment attachment;

    @Before
    public void setUpAttachmentTest()
    {
        attachment = new EmailAttachment();
    }

    @Test
    public void testGetSetDescription()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            attachment.setDescription(testCharsValid[i]);
            assertEquals(testCharsValid[i], attachment.getDescription());
        }
    }

    @Test
    public void testGetSetName()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            attachment.setName(testCharsValid[i]);
            assertEquals(testCharsValid[i], attachment.getName());
        }
    }

    @Test
    public void testGetSetPath()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            attachment.setPath(testCharsValid[i]);
            assertEquals(testCharsValid[i], attachment.getPath());
        }
    }

    @Test
    public void testGetSetURL() throws Exception
    {
        String[] tests =
            {
                "http://localhost/",
                "http://www.apache.org/",
                "http://foo.notexisting.org" };

        for (int i = 0; i < tests.length; i++)
        {
            URL testURL = new URL(tests[i]);
            attachment.setURL(testURL);
            assertEquals(testURL, attachment.getURL());
        }
    }

    @Test
    public void testGetSetDisposition()
    {
        for (int i = 0; i < testCharsValid.length; i++)
        {
            attachment.setDisposition(testCharsValid[i]);
            assertEquals(testCharsValid[i], attachment.getDisposition());
        }
    }

}
