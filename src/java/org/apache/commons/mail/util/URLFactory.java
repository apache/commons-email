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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Create an URL for embedding a resource.
 *
 * @since 1.3
 */
public final class URLFactory
{
    /**
     * Instances should NOT be constructed in standard programming.
     */
    private URLFactory()
    {
        super();
    }

    /**
     * Create an URL based on a base URL and a resource location suitable for loading
     * the resource.
     *
     * @param baseUrl an optional base URL
     * @param resource a resource location
     * @return the corresponding URL
     * @throws java.net.MalformedURLException creating the URL failed
     */
    public static URL createUrl(final URL baseUrl, final String resource) throws MalformedURLException
    {
        // if we get an non-existing base url than the resource can
        // be directly used to create an URL
        if (baseUrl == null)
        {
            return new URL(resource);
        }

        // if we get an non-existing location what we shall do?
        if (resource == null)
        {
            throw new IllegalArgumentException("No resource defined");
        }

        // if we get a stand-alone resource than ignore the base url
        if (isFileUrl(resource) || isHttpUrl(resource))
        {
            return new URL(resource);
        }

        // crate a file URL
        String baseUrlString = baseUrl.toExternalForm();
        if (isFileUrl(baseUrlString))
        {
            return new File(toFile(baseUrl), resource).toURI().toURL();
        }
        else
        {
            return new URL(baseUrl, resource.replaceAll("&amp;", "&"));
        }
    }

    /**
     * Is this a file URL?
     *
     * @param urlString the URL string
     * @return true if it is a file URL
     */
    private static boolean isFileUrl(String urlString)
    {
        return urlString.startsWith("file:/");
    }

    /**
     * Is this a HTTP/HTTPS URL?
     *
     * @param urlString the URL string
     * @return true if it is a HTTP/HTTPS URL
     */
    private static boolean isHttpUrl(String urlString)
    {
        return urlString.startsWith("http://") || urlString.startsWith("https://");
    }

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     *
     * @param url  the file URL to convert, <code>null</code> returns <code>null</code>
     * @return the equivalent <code>File</code> object, or <code>null</code>
     *  if the URL's protocol is not <code>file</code>
     * @throws IllegalArgumentException if the file is incorrectly encoded
     */
    private static File toFile(URL url)
    {
        if (url == null || !url.getProtocol().equals("file"))
        {
            return null;
        }
        else
        {
            String filename = url.getFile().replace('/', File.separatorChar);
            int pos = 0;
            while ((pos = filename.indexOf('%', pos)) >= 0)
            {
                if (pos + 2 < filename.length())
                {
                    String hexStr = filename.substring(pos + 1, pos + 3);
                    char ch = (char) Integer.parseInt(hexStr, 16);
                    filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
                }
            }
            return new File(filename);
        }
    }
}
