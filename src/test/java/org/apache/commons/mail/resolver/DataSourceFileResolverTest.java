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

import java.io.File;
import java.io.IOException;

import org.apache.commons.mail.DataSourceResolver;
import org.junit.Test;

/**
 * JUnit test case for DateSourceResolver.
 *
 * @since 1.3
 */
public class DataSourceFileResolverTest extends AbstractDataSourceResolverTest
{

    @Test
    public void testResolvingFileLenient() throws Exception
    {
        final DataSourceResolver dataSourceResolver = new DataSourceFileResolver(new File("./src/test/resources"), true);
        assertTrue(toByteArray(dataSourceResolver.resolve("images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("./images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("../resources/images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertNull(toByteArray(dataSourceResolver.resolve("/images/does-not-exist.gif")));
        assertNull(dataSourceResolver.resolve("./images/does-not-exist.gif"));
    }

    @Test(expected = IOException.class)
    public void testResolvingFileNonLenient() throws Exception
    {
        final DataSourceResolver dataSourceResolver = new DataSourceFileResolver(new File("."), false);
        assertNotNull(dataSourceResolver.resolve("./src/test/resources/images/asf_logo_wide.gif"));

        dataSourceResolver.resolve("asf_logo_wide.gif");
    }

}
