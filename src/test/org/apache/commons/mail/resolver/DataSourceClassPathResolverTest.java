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

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.DataSourceResolver;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * JUnit test case for DataSourceClassPathResolver..
 *
 * @since 1.3
 */

public class DataSourceClassPathResolverTest extends TestCase
{
    private final int IMG_SIZE = 5866;

    public DataSourceClassPathResolverTest(String name)
    {
        super(name);
    }

    // ======================================================================
    // Start of Tests
    // ======================================================================
    
    public void testResolvingClassPathLenient() throws Exception
    {
        DataSourceResolver dataSourceResolver;

        dataSourceResolver = new DataSourceClassPathResolver("/", true);
        assertTrue(toByteArray(dataSourceResolver.resolve("images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("./images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("/images/asf_logo_wide.gif")).length == IMG_SIZE);
        assertNull(dataSourceResolver.resolve("/asf_logo_wide.gif"));

        dataSourceResolver = new DataSourceClassPathResolver("/images", true);
        assertTrue(toByteArray(dataSourceResolver.resolve("asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("./asf_logo_wide.gif")).length == IMG_SIZE);
        assertTrue(toByteArray(dataSourceResolver.resolve("/asf_logo_wide.gif")).length == IMG_SIZE);
        assertNull(dataSourceResolver.resolve("./images/asf_logo_wide.gif"));
    }

    public void testResolvingClassPathNonLenient() throws Exception
    {
        DataSourceResolver dataSourceResolver;

        dataSourceResolver = new DataSourceClassPathResolver("/", false);
        assertNotNull(dataSourceResolver.resolve("images/asf_logo_wide.gif"));

        try
        {
            dataSourceResolver.resolve("asf_logo_wide.gif");
            fail("Expecting an IOException");
        }
        catch(IOException e)
        {
            return;
        }
    }

    private byte[] toByteArray(DataSource dataSource) throws IOException
    {
        if(dataSource != null)
        {
            InputStream is = dataSource.getInputStream();
            return IOUtils.toByteArray(is);
        }
        else
        {
            return null;
        }
    }

}
