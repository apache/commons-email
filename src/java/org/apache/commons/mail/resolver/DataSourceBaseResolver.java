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

import org.apache.commons.mail.DataSourceResolver;

/**
 * Base class for various resolvers.
 *
 * @since 1.3
 */
public abstract class DataSourceBaseResolver implements DataSourceResolver
{
    /** shall we ignore resources not found or complain with an exception */
    private final boolean lenient;

    /**
     * Constructor.
     */
    public DataSourceBaseResolver()
    {
        this.lenient = false;
    }

    /**
     * Constructor.
     *
     * @param lenient shall we ignore resources not found or throw an exception?
     */
    public DataSourceBaseResolver(final boolean lenient)
    {
        this.lenient = lenient;
    }

    /**
     * Shall we ignore resources not found or throw an exception?
     *
     * @return the lenient flag
     */
    public boolean isLenient()
    {
        return lenient;
    }

    /**
     * Is this a content id?
     *
     * @param resourceLocation the resource location
     * @return true if it is a CID
     */
    protected boolean isCid(final String resourceLocation)
    {
        return resourceLocation.startsWith("cid:");
    }

    /**
     * Is this a file URL?
     *
     * @param urlString the URL string
     * @return true if it is a file URL
     */
    protected boolean isFileUrl(final String urlString)
    {
        return urlString.startsWith("file:/");
    }

    /**
     * Is this a HTTP/HTTPS URL?
     *
     * @param urlString the URL string
     * @return true if it is a HTTP/HTTPS URL
     */
    protected boolean isHttpUrl(final String urlString)
    {
        return urlString.startsWith("http://") || urlString.startsWith("https://");
    }
}
