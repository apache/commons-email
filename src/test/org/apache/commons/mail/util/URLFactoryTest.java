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

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

/**
 * JUnit test case for DefaultAuthenticator Class
 *
 * @since 1.0
 * @author <a href="mailto:corey.scott@gmail.com">Corey Scott</a>
 * @version $Id: DefaultAuthenticatorTest.java 480401 2006-11-29 04:40:04Z bayard $
 */

public class URLFactoryTest extends TestCase
{
    public URLFactoryTest(String name)
    {
        super(name);
    }

    // ======================================================================
    // Start of Tests
    // ======================================================================
    
    public void testCreateUrl() throws Exception
    {
        URL url;
        URL baseUrl;
        String location;

        // base URL + relative file name
        location = "pom.xml";
        baseUrl = new File("").toURI().toURL();
        url = URLFactory.createUrl(baseUrl, location);
        assertEquals(new File("pom.xml").toURI().toURL().toExternalForm(), url.toExternalForm());

        // base URL  + file URL
        location = new File("pom.xml").toURI().toURL().toExternalForm();
        baseUrl = new File("").toURI().toURL();
        url = URLFactory.createUrl(baseUrl, location);
        assertEquals(new File("pom.xml").toURI().toURL().toExternalForm(), url.toExternalForm());

        // file URL only
        location = new File("pom.xml").toURI().toURL().toExternalForm();
        baseUrl = null;
        url = URLFactory.createUrl(baseUrl, location);
        assertEquals(new File("pom.xml").toURI().toURL().toExternalForm(), url.toExternalForm());
    }
}
