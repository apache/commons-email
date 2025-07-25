/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail2.jakarta.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.mail2.jakarta.DataSourceResolver;
import org.junit.jupiter.api.Test;

/**
 * JUnit test case for DateSourceResolver.
 */
class DataSourcePathResolverTest extends AbstractDataSourceResolverTest {

    @Test
    void testResolveLenient() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourcePathResolver(Paths.get("./src/test/resources"), true);
        assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("images/asf_logo_wide.gif")).length);
        assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("./images/asf_logo_wide.gif")).length);
        assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("../resources/images/asf_logo_wide.gif")).length);
        assertNull(toByteArray(dataSourceResolver.resolve("/images/does-not-exist.gif")));
        assertNull(dataSourceResolver.resolve("./images/does-not-exist.gif"));
    }

    @Test
    void testResolveStrict() throws Exception {
        final DataSourceResolver dataSourceResolver = new DataSourcePathResolver(Paths.get("."), false);
        assertNotNull(dataSourceResolver.resolve("./src/test/resources/images/asf_logo_wide.gif"));

        assertThrows(IOException.class, () -> dataSourceResolver.resolve("asf_logo_wide.gif"));
    }

}
