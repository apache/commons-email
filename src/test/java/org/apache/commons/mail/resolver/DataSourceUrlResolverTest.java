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
package org.apache.commons.mail.resolver;

import static org.junit.Assert.*;

import org.apache.commons.mail.DataSourceResolver;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * JUnit test case for DataSourceUrlResolver.
 *
 * @since 1.3
 */
public class DataSourceUrlResolverTest extends AbstractDataSourceResolverTest
{

    /**
     * Shows how the DataSourceUrlResolver can resolve files as well but this should
     * be done using a DataSourceFileResolver.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testResolvingFilesLenient() throws Exception
    {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new File("./src/test/resources").toURI().toURL(), true);
        assertTrue(toByteArray(dataSourceResolver.resolve("images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("./images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertNull(dataSourceResolver.resolve("./images/does-not-exist.gif"));
        assertNull(dataSourceResolver.resolve("/images/asf_logo_wide.gif"));
    }

    /**
     * Tests resolving resources over HTTP.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testResolvingHttpLenient() throws Exception
    {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new URL("http://www.apache.org"), true);
        assertTrue(toByteArray(dataSourceResolver.resolve("http://www.apache.org/images/feather-small.gif")).length > 1);
        assertTrue(toByteArray(dataSourceResolver.resolve("images/feather-small.gif")).length > 1);
        assertTrue(toByteArray(dataSourceResolver.resolve("./images/feather-small.gif")).length > 1);
        assertTrue(toByteArray(dataSourceResolver.resolve("/images/feather-small.gif")).length > 1);
        assertNull(toByteArray(dataSourceResolver.resolve("/images/does-not-exist.gif")));
    }

    /**
     * Tests resolving resources over HTTP.
     *
     * @throws Exception the test failed
     */
    @Test(expected = IOException.class)
    public void testResolvingHttpNonLenient() throws Exception
    {
        final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(new URL("http://www.apache.org"), false);
        assertNotNull(dataSourceResolver.resolve("images/asf_logo_wide.gif"));

        dataSourceResolver.resolve("images/does-not-exist.gif");
    }

}
