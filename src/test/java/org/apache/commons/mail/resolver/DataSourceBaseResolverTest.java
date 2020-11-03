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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * notice for the test data:
 * every String in isCidFalseArray shall never pass all the 3 (actually 5) functions.
 * also for isFileUrlFalseArray and isHttpUrlFalseArray.
 */
public class DataSourceBaseResolverTest {
    final DataSourceFileResolver resolver = new DataSourceFileResolver();
    final String[] isCidTrueArray = new String[]{
            "cid:",
            "cid:test",
    };

    final String[] isCidFalseArray = new String[]{
            " cid:",
            "\tcid:/",
            "cd:test",
            "",
            "a://",
    };

    final String[] isFileUrlTrueArray = new String[]{
            "file:/",
            "file:/test",
            "file:////",
    };

    final String[] isFileUrlFalseArray = new String[]{
            " file:/",
            "\tfile:/",
            "fle:/test",
            "",
            "a://",
    };

    final String[] isHttpUrlTrueArray = new String[]{
            "https://",
            "http://",
            "https://test",
            "http://test",
            "https:////",
            "http:////",
    };

    final String[] isHttpUrlFalseArray = new String[]{
            "httpss://",
            "httpsss://",
            "http:/ /",
            "https:/ /",
            "htps:/",
            "htp:/",
            " https://",
            " http://",
            "\thttps://",
            "\thttp://",
            "htt://test",
            "",
            "a:/",
    };

    @Test
    public void testIsCid() {
        for (String au : isCidTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isCid(au));
        }
        for (String au : isCidFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isCid(au));
        }
    }

    @Test
    public void testIsFileUrl() {
        for (String au : isFileUrlTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isFileUrl(au));
        }
        for (String au : isFileUrlFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isFileUrl(au));
        }
    }

    @Test
    public void testIsHttpUrl() {
        for (String au : isHttpUrlTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isHttpUrl(au));
        }
        for (String au : isHttpUrlFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isHttpUrl(au));
        }
    }

    @Test
    public void testIsCidOrHttpUrl() {
        for (String au : isCidTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isCidOrHttpUrl(au));
        }
        for (String au : isHttpUrlTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isCidOrHttpUrl(au));
        }

        for (String au : isCidFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isCidOrHttpUrl(au));
        }
        for (String au : isHttpUrlFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isCidOrHttpUrl(au));
        }
        for (String au : isFileUrlTrueArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isCidOrHttpUrl(au));
        }
    }

    @Test
    public void testIsFileUrlOrHttpUrl() {
        for (String au : isFileUrlTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isFileUrlOrHttpUrl(au));
        }
        for (String au : isHttpUrlTrueArray) {
            assertTrue("must be true : \"" + au + "\"", resolver.isFileUrlOrHttpUrl(au));
        }

        for (String au : isFileUrlFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isFileUrlOrHttpUrl(au));
        }
        for (String au : isHttpUrlFalseArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isFileUrlOrHttpUrl(au));
        }
        for (String au : isCidTrueArray) {
            assertFalse("must be false : \"" + au + "\"", resolver.isFileUrlOrHttpUrl(au));
        }
    }
}
