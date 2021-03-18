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
package org.apache.commons.mail;

import jakarta.activation.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Small wrapper class on top of HtmlEmail which encapsulates the required logic
 * to retrieve images that are contained in "&lt;img src=../&gt;" elements in the HTML
 * code. This is done by replacing all img-src-elements with "cid:"-entries and
 * embedding images in the email.
 * </p>
 * <p>
 * For local files the class tries to either load them via an absolute path or -
 * if available - use a relative path starting from a base directory. For files
 * that are not found locally, the implementation tries to download
 * the element and link it in.
 * </p>
 * <p>
 * The image loading is done by an instance of {@code DataSourceResolver}
 * which has to be provided by the caller.
 * </p>
 *
 * @since 1.3
 */
public class ImageHtmlEmail extends HtmlEmail
{
    // Regular Expression to find all <IMG SRC="..."> entries in an HTML
    // document.It needs to cater for various things, like more whitespaces
    // including newlines on any place, HTML is not case sensitive and there
    // can be arbitrary text between "IMG" and "SRC" like IDs and other things.

    /** Regexp for extracting {@code <img>} tags */
    public static final String REGEX_IMG_SRC =
            "(<[Ii][Mm][Gg]\\s*[^>]*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])";

    /** regexp for extracting {@code <script>} tags */
    public static final String REGEX_SCRIPT_SRC =
            "(<[Ss][Cc][Rr][Ii][Pp][Tt]\\s*.*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])";

    // this pattern looks for the HTML image tag which indicates embedded images,
    // the grouping is necessary to allow to replace the element with the CID

    /** pattern for extracting <img> tags */
    private static final Pattern IMG_PATTERN = Pattern.compile(REGEX_IMG_SRC);

    /** pattern for extracting <script> tags */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(REGEX_SCRIPT_SRC);

    /** resolve the images and script resources to a DataSource */
    private DataSourceResolver dataSourceResolver;

    /**
     * Get the data source resolver.
     *
     * @return the resolver
     */
    public DataSourceResolver getDataSourceResolver()
    {
        return dataSourceResolver;
    }

    /**
     * Set the data source resolver.
     *
     * @param dataSourceResolver the resolver
     */
    public void setDataSourceResolver(final DataSourceResolver dataSourceResolver)
    {
        this.dataSourceResolver = dataSourceResolver;
    }

     /**
      * Does the work of actually building the MimeMessage.
      *
      * @see org.apache.commons.mail.HtmlEmail#buildMimeMessage()
      * @throws EmailException building the MimeMessage failed
      */
    @Override
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            // embed all the matching image and script resources within the email
            String temp = replacePattern(super.html, IMG_PATTERN);
            temp = replacePattern(temp, SCRIPT_PATTERN);
            setHtmlMsg(temp);
            super.buildMimeMessage();
        }
        catch (final IOException e)
        {
            throw new EmailException("Building the MimeMessage failed", e);
        }
    }

    /**
     * Replace the regexp matching resource locations with "cid:..." references.
     *
     * @param htmlMessage the HTML message to analyze
     * @param pattern the regular expression to find resources
     * @return the HTML message containing "cid" references
     * @throws EmailException creating the email failed
     * @throws IOException resolving the resources failed
     */
    private String replacePattern(final String htmlMessage, final Pattern pattern)
            throws EmailException, IOException
    {
        DataSource dataSource;
        final StringBuffer stringBuffer = new StringBuffer();

        // maps "cid" --> name
        final Map<String, String> cidCache = new HashMap<>();

        // maps "name" --> dataSource
        final Map<String, DataSource> dataSourceCache = new HashMap<>();

        // in the String, replace all "img src" with a CID and embed the related
        // image file if we find it.
        final Matcher matcher = pattern.matcher(htmlMessage);

        // the matcher returns all instances one by one
        while (matcher.find())
        {
            // in the RegEx we have the <src> element as second "group"
            final String resourceLocation = matcher.group(2);

            // avoid loading the same data source more than once
            if (dataSourceCache.get(resourceLocation) == null)
            {
                // in lenient mode we might get a 'null' data source if the resource was not found
                dataSource = getDataSourceResolver().resolve(resourceLocation);

                if (dataSource != null)
                {
                    dataSourceCache.put(resourceLocation, dataSource);
                }
            }
            else
            {
                dataSource = dataSourceCache.get(resourceLocation);
            }

            if (dataSource != null)
            {
                String name = dataSource.getName();
                if (EmailUtils.isEmpty(name))
                {
                    name = resourceLocation;
                }

                String cid = cidCache.get(name);

                if (cid == null)
                {
                    cid = embed(dataSource, name);
                    cidCache.put(name, cid);
                }

                // if we embedded something, then we need to replace the URL with
                // the CID, otherwise the Matcher takes care of adding the
                // non-replaced text afterwards, so no else is necessary here!
                matcher.appendReplacement(stringBuffer,
                        Matcher.quoteReplacement(matcher.group(1) + "cid:" + cid + matcher.group(3)));
            }
        }

        // append the remaining items...
        matcher.appendTail(stringBuffer);

        cidCache.clear();
        dataSourceCache.clear();

        return stringBuffer.toString();
    }
}
