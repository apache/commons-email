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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class AttachmentDataSourceTest
{
    @Test
    public void testGetInputStream() throws IOException
    {
        byte[] testData = "Test data for InputStream".getBytes();
        InputStream testInputStream = new ByteArrayInputStream(testData);

        AttachmentDataSource dataSource = new AttachmentDataSource(testInputStream, "application/octet-stream");
        InputStream inputStream = dataSource.getInputStream();

        byte[] readData = new byte[testData.length];
        int bytesRead = inputStream.read(readData);

        assertEquals(testData.length, bytesRead);
        assertArrayEquals(testData, readData);
    }

    @Test
    public void testGetContentType()
    {
        AttachmentDataSource dataSource = new AttachmentDataSource(null, "text/plain");
        assertEquals("text/plain", dataSource.getContentType());
    }

    @Test
    public void testGetName()
    {
        AttachmentDataSource dataSource = new AttachmentDataSource(null, "application/pdf", "document.pdf");
        assertEquals("document.pdf", dataSource.getName());
    }

    @Test
    public void testSetName()
    {
        AttachmentDataSource dataSource = new AttachmentDataSource(null, "image/jpeg");
        dataSource.setName("image.jpg");
        assertEquals("image.jpg", dataSource.getName());
    }

    @Test
    public void testGetOutputStream()
    {
        AttachmentDataSource dataSource = new AttachmentDataSource(null, "text/html");
        assertThrows(UnsupportedOperationException.class, dataSource::getOutputStream);
    }

}