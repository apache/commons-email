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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.activation.DataSource;
import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

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

    public static final String REGEX_IMG_SRC = "img[src]";
    public static final String REGEX_SCRIPT_SRC = "script[src]";

    private static final String IMG_PATTERN = REGEX_IMG_SRC;
    private static final String SCRIPT_PATTERN = REGEX_SCRIPT_SRC;

    private DataSourceResolver dataSourceResolver;

    public DataSourceResolver getDataSourceResolver() {
        return dataSourceResolver;
    }

    public void setDataSourceResolver(final DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    public void buildMimeMessage() throws EmailException {
        try {
            String temp = replacePattern(super.html, IMG_PATTERN);
            temp = replacePattern(temp, SCRIPT_PATTERN);
            setHtmlMsg(temp);
            super.buildMimeMessage();
        } catch (final IOException e) {
            throw new EmailException("Building the MimeMessage failed", e);
        }
    }

    private String replacePattern(final String htmlMessage, final String pattern)
            throws EmailException, IOException {
        Document doc = Jsoup.parse(htmlMessage);
        Elements elements = doc.select(pattern);

        for (Element element : elements) {
            String src = element.attr("src");
            DataSource dataSource = getDataSourceResolver().resolve(src);

            if (dataSource != null) {
                String cid = embed(dataSource, dataSource.getName());
                element.attr("src", "cid:" + cid);
            }
        }

        return doc.toString();
    }
}
