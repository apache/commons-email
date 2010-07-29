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

import org.apache.commons.mail.impl.URLFactory;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small wrapper class on top of HtmlEmail which encapsulates the required logic
 * to retrieve images that are contained in "<img src=../>" elements in the HTML
 * code. This is done by replacing all img-src-elements with "cid:"-entries and
 * embedding images in the email.
 *
 * For local files the class tries to either load them via an absolute path or -
 * if available - use a relative path starting from a base directory.For files
 * that are not found locally, the implementation tries to download
 * the element and link it in.
 *
 * This code was submitted to commons-email under the Apache 2.0 license, see
 * https://issues.apache.org/jira/browse/EMAIL-92
 *
 */
public class ImageHtmlEmail extends HtmlEmail
{
    // Regular Expression to find all <IMG SRC="..."> entries in an HTML
    // document.It needs to cater for various things, like more whitespaces
    // including newlines on any place, HTML is not case sensitive and there
    // can be arbitrary text between "IMG" and "SRC" like IDs and other things.

    /** regexp for extracting <img> tags */
    public static final String REGEX_IMG_SRC = "(<[Ii][Mm][Gg]\\s*[^>]*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])";

    /** regexp for extracting <script> tags */
    public static final String REGEX_SCRIPT_SRC = "(<[Ss][Cc][Rr][Ii][Pp][Tt]\\s*.*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])";

    // this pattern looks for the HTML image tag which indicates embedded images,
    // the grouping is necessary to allow to replace the element with the CID

    /** pattern for extracting <img> tags */
    private static final Pattern IMG_PATTERN = Pattern.compile(REGEX_IMG_SRC);

    /** pattern for extracting <script> tags */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(REGEX_SCRIPT_SRC);

    /**
     * Set the HTML message and try to add any image that is linked in the HTML
     * source.
     *
     * @param htmlMessage the HTML message
     * @return An HtmlEmail.
     * @throws EmailException assembling the email failed
     */
    public HtmlEmail setHtmlMsg(final String htmlMessage)
            throws EmailException
    {
        URL currentWorkingDirectoryUrl;

        try
        {
            currentWorkingDirectoryUrl = new File("").toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            throw new EmailException("Unable to create URL for current working directory", e);
        }

        return setHtmlMsg(htmlMessage, currentWorkingDirectoryUrl, false);
    }

    /**
     * Set the HTML content and try to add any image that is linked in the HTML
     * source.  The 'baseUrl' is either a "http://..." or a "file://" in order
     * to resolve relative image resources.
     *
     * @param htmlMessage the HTML message.
     * @param baseUrl an base URL that is used as starting point for resolving images that are embedded in the HTML
     * @param isLenient shall we ignore resources not found or throw an exception?
     * @return a HTML email
     * @throws EmailException creating the email failed
     */
    public HtmlEmail setHtmlMsg(final String htmlMessage, final URL baseUrl, boolean isLenient)
            throws EmailException
    {
        // if there is no useful HTML then simply route it through to the super class
        if (htmlMessage == null || htmlMessage.isEmpty())
        {
            return super.setHtmlMsg(htmlMessage);
        }

        // replace images
        String temp = replacePattern(htmlMessage, IMG_PATTERN, baseUrl, isLenient);

        // replace scripts
        temp = replacePattern(temp, SCRIPT_PATTERN, baseUrl, isLenient);

        // finally set the resulting HTML with all images replaced if possible
        return super.setHtmlMsg(temp);
    }

    /**
     * Replace the regexp matching resource locations with "cid:..." references.
     *
     * @param htmlMessage the HTML message to analyze
     * @param pattern the regular expression to find resources
     * @param baseUrl the starting point for resolving relative resource paths
     * @param isLenient shall we ignore resources not found or throw an exception?
     * @return the HTML message containing "cid" references
     * @throws EmailException creating the email failed
     */
    private String replacePattern(final String htmlMessage, final Pattern pattern, final URL baseUrl, final boolean isLenient)
            throws EmailException
    {
        DataSource imageDataSource;
        StringBuffer stringBuffer = new StringBuffer();

        // maps "cid" --> name
        Map cidCache = new HashMap();

        // maps "name" --> dataSource
        Map dataSourceCache = new HashMap();

        // in the String, replace all "img src" with a CID and embed the related
        // image file if we find it.
        Matcher matcher = pattern.matcher(htmlMessage);

        // the matcher returns all instances one by one
        while (matcher.find())
        {
            // in the RegEx we have the <src> element as second "group"
            String image = matcher.group(2);

            // avoid loading the same data source more than once
            if (dataSourceCache.get(image) == null)
            {
                // in lenient mode we might get a 'null' data source if the resource was not found
                imageDataSource = resolve(baseUrl, image, isLenient);

                if (imageDataSource != null)
                {
                    dataSourceCache.put(image, imageDataSource);
                }
            }
            else
            {
                imageDataSource = (DataSource) dataSourceCache.get(image);
            }

            if (imageDataSource != null)
            {
                String name = imageDataSource.getName();
                String cid = (String) cidCache.get(name);

                if (cid == null)
                {
                    cid = embed(imageDataSource, imageDataSource.getName());
                    cidCache.put(name, cid);
                }

                // if we embedded something, then we need to replace the URL with
                // the CID, otherwise the Matcher takes care of adding the
                // non-replaced text afterwards, so no else is necessary here!
                matcher.appendReplacement(stringBuffer, matcher.group(1) + "cid:" + cid + matcher.group(3));
            }
        }

        // append the remaining items...
        matcher.appendTail(stringBuffer);

        cidCache.clear();
        dataSourceCache.clear();

        return stringBuffer.toString();
    }


    /**
     * Resolve a resource location to be embedded into the email. When using
     * the lenient mode a resource which can't be resolved returns "null".
     * When using the non-lenient mode an exception would be thrown.
     *
     * @param baseUrl the base url of the resourceLocation
     * @param resourceLocation the location of the resource
     * @param isLenient shall we ignore resources not found?
     * @return the data source containing the resource
     * @throws EmailException resolving the resource failed
     */
    protected DataSource resolve(final URL baseUrl, final String resourceLocation, final boolean isLenient)
            throws EmailException
    {
        DataSource result = null;

        try
        {
            if (!resourceLocation.startsWith("cid:"))
            {
                URL url = URLFactory.createUrl(baseUrl, resourceLocation);
                result = new URLDataSource(url);
                result.getInputStream();
            }

            return result;
        }
        catch (IOException e)
        {
            if (!isLenient)
            {
                throw new EmailException("Resolving the resource failed : " + resourceLocation, e);
            }
            else
            {
                return null;
            }
        }
    }
}
