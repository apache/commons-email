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
package org.apache.commons.mail2.jakarta.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.mail2.jakarta.DataSourceResolver;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for DataSourceUrlResolver.
 */
public class DataSourceUrlResolverTest extends AbstractDataSourceResolverTest {

    /**
     * Shows how the DataSourceUrlResolver can resolve files as well but this should be done using a DataSourceFileResolver.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testResolvingFilesLenient() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new File("./src/test/resources").toURI().toURL(), true);
        assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("images/asf_logo_wide.gif")).length);
        assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("./images/asf_logo_wide.gif")).length);
        assertNull(dataSourceResolver.resolve("./images/does-not-exist.gif"));
        assertNull(dataSourceResolver.resolve("/images/asf_logo_wide.gif"));
    }

    /**
     * Tests resolving resources over HTTP.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testResolvingHttpLenient() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new URL("https://www.apache.org"), true);
        assertTrue(toByteArray(dataSourceResolver.resolve("https://www.apache.org/images/feather-small.gif")).length > 1);
        assertTrue(toByteArray(dataSourceResolver.resolve("images/feather-small.gif")).length > 1);
        assertTrue(toByteArray(dataSourceResolver.resolve("./images/feather-small.gif")).length > 1);
        assertTrue(toByteArray(dataSourceResolver.resolve("/images/feather-small.gif")).length > 1);
    }

    /**
     * Tests resolving resources over HTTP.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testResolvingHttpLenientHost() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new URL("http://does.not.exist"), true);
        assertNull(toByteArray(dataSourceResolver.resolve("/images/does-not-exist.gif")));
    }

    /**
     * Tests resolving resources over HTTP.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testResolvingHttpNonLenient() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new URL("http://does.not.exist"), false);
        assertThrows(IOException.class, () -> dataSourceResolver.resolve("images/asf_logo_wide.gif"));
        assertThrows(IOException.class, () -> dataSourceResolver.resolve("images/does-not-exist.gif"));
    }

}
