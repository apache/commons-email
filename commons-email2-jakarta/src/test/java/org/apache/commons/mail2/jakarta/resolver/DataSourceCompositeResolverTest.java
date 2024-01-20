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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.mail2.jakarta.DataSourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for DateSourceResolver.
 */
public class DataSourceCompositeResolverTest extends AbstractDataSourceResolverTest {

    private DataSourceResolver[] dataSourceResolvers;
    private DataSourceResolver[] dataSourceResolversMissing;

    @BeforeEach
    public void setUp() throws Exception {
        final DataSourceUrlResolver urlResolver = new DataSourceUrlResolver(new URL("https://www.apache.org"), false);
        final DataSourceUrlResolver urlResolverMissing = new DataSourceUrlResolver(new URL("https://does.not.exist"), false);
        final DataSourceClassPathResolver classPathResolver = new DataSourceClassPathResolver("/images", false);
        dataSourceResolvers = new DataSourceResolver[] { urlResolver, classPathResolver };
        dataSourceResolversMissing = new DataSourceResolver[] { urlResolverMissing };
    }

    @Test
    public void testExternalModification() throws Exception {
        final DataSourceCompositeResolver dataSourceResolver = new DataSourceCompositeResolver(dataSourceResolvers, true);

        final DataSourceResolver[] arr = dataSourceResolver.getDataSourceResolvers();

        // modify an element in the returned array
        arr[0] = null;

        final DataSourceResolver[] arr2 = dataSourceResolver.getDataSourceResolvers();

        // assert that the external modification is not propagated to the internal array
        assertNotNull(arr2[0]);
    }

    @Test
    public void testResolvingFilesLenient() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourceCompositeResolver(dataSourceResolvers, true);

        // resolve using HTTP
        assertTrue(toByteArray(dataSourceResolver.resolve("/images/feather-small.gif")).length > 0);

        // resolve using class path
        assertTrue(toByteArray(dataSourceResolver.resolve("/contentTypeTest.gif")).length > 0);
    }

    @Test
    public void testResolvingFilesNonLenient() {
        final DataSourceResolver dataSourceResolver = new DataSourceCompositeResolver(dataSourceResolversMissing, false);

        assertThrows(IOException.class, () -> dataSourceResolver.resolve("./image/does-not-exist.gif"));
    }

}
