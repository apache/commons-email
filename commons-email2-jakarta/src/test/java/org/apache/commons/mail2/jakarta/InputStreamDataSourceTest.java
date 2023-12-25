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
package org.apache.commons.mail2.jakarta;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.mail2.jakarta.activation.InputStreamDataSource;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link InputStreamDataSource}.
 */
public class InputStreamDataSourceTest {

    @Test
    public void testGetContentType() {
        assertEquals("text/plain", new InputStreamDataSource(null, "text/plain").getContentType());
    }

    @Test
    public void testGetInputStream() throws IOException {
        final byte[] testData = "Test data for InputStream".getBytes();
        final InputStream testInputStream = new ByteArrayInputStream(testData);
        final InputStreamDataSource dataSource = new InputStreamDataSource(testInputStream, "application/octet-stream");
        try (InputStream inputStream = dataSource.getInputStream()) {
            final byte[] readData = new byte[testData.length];
            final int bytesRead = inputStream.read(readData);
            assertEquals(testData.length, bytesRead);
            assertArrayEquals(testData, readData);
        }
    }

    @Test
    public void testGetName() {
        assertEquals("document.pdf", new InputStreamDataSource(null, "application/pdf", "document.pdf").getName());
    }

    @Test
    public void testGetOutputStream() {
        final InputStreamDataSource dataSource = new InputStreamDataSource(null, "text/html");
        assertThrows(UnsupportedOperationException.class, dataSource::getOutputStream);
    }

    @Test
    public void testSetName() {
        assertEquals("image.jpg", new InputStreamDataSource(null, "image/jpeg", "image.jpg").getName());
    }

}