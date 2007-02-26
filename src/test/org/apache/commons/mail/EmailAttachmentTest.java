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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * JUnit test case for EmailAttachment Class
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id$
 */

public class EmailAttachmentTest extends BaseEmailTestCase
{
    /** */
    private EmailAttachment attachment;

    /**
     * @param name name
     */
    public EmailAttachmentTest(String name)
    {
        super(name);
    }

    /**
     * @throws Exception  */
    protected void setUp() throws Exception
    {
        super.setUp();
        // reusable objects to be used across multiple tests
        this.attachment = new EmailAttachment();
    }

    /** */
    public void testGetSetDescription()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.attachment.setDescription(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.attachment.getDescription());
        }
    }

    /** */
    public void testGetSetName()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.attachment.setName(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.attachment.getName());
        }
    }

    /** */
    public void testGetSetPath()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.attachment.setPath(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.attachment.getPath());
        }
    }

    /** */
    public void testGetSetURL()
    {
        String[] tests =
            {
                "http://localhost/",
                "http://www.apache.org/",
                "http://bad.url.com" };

        for (int i = 0; i < tests.length; i++)
        {
            // TODO: Document why malformed url is ok, or remove the catch
            try
            {
                URL testURL = new URL(tests[i]);
                this.attachment.setURL(testURL);
                assertEquals(testURL, this.attachment.getURL());
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                continue;
            }
        }
    }

    /** */
    public void testGetSetDisposition()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            this.attachment.setDisposition(testCharsValid[i]);
            assertEquals(testCharsValid[i], this.attachment.getDisposition());
        }
    }

}
