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
import java.net.URL;

/**
 * JUnit test case for DateSourceResolver.
 *
 * @since 1.3
 */

public class DataSourceCompositeResolverTest extends TestCase
{
    public DataSourceCompositeResolverTest(String name)
    {
        super(name);
    }

    public void testResolvingFilesLenient() throws Exception
    {
        DataSourceResolver urlResolver = new DataSourceUrlResolver(new URL("http://www.apache.org"), false);
        DataSourceResolver classPathResolver = new DataSourceClassPathResolver("/images", false);
        DataSourceResolver[] dataSourceResolvers = new DataSourceResolver[] { urlResolver, classPathResolver };
        DataSourceResolver dataSourceResolver = new DataSourceCompositeResolver(dataSourceResolvers, true);

        // resolve using HTTP
        assertTrue(toByteArray(dataSourceResolver.resolve("/images/feather-small.gif")).length > 0);

        // resolve using class path
        assertTrue(toByteArray(dataSourceResolver.resolve("/contentTypeTest.gif")).length > 0);
    }

    public void testResolvingFilesNonLenient() throws Exception
    {
        DataSourceResolver urlResolver = new DataSourceUrlResolver(new URL("http://www.apache.org"), false);
        DataSourceResolver classPathResolver = new DataSourceClassPathResolver("/images", false);
        DataSourceResolver[] dataSourceResolvers = new DataSourceResolver[] { urlResolver, classPathResolver };
        DataSourceResolver dataSourceResolver = new DataSourceCompositeResolver(dataSourceResolvers, false);

        try
        {
            dataSourceResolver.resolve("./image/does-not-exist.gif");
            fail("Expected an IOException");
        }
        catch(IOException e)
        {
            // expected
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
