
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
import java.util.HashMap;
import java.util.Map;

/**
 * Small wrapper class on top of HtmlEmail which encapsulates the required logic
 * to retrieve images that are contained in "<img src=../>" elements in the HTML
 * code. This is done by replacing all img-src-elements with "cid:"-entries and
 * embedding images in the email.
 * For local files the class tries to either load them via an absolute path or -
 * if available - use a relative path starting from a base directory. For files
 * that are not found locally, the implementation tries to download
 * the element and link it in.
 * The image loading is done by an instance of {@code DataSourceResolver}
 * which has to be provided by the caller.
 * @since 1.3
 */
public class ImageHtmlEmail extends HtmlEmail
{
    private DataSourceResolver dataSourceResolver;

    public DataSourceResolver getDataSourceResolver()
    {
        return dataSourceResolver;
    }

    public void setDataSourceResolver(final DataSourceResolver dataSourceResolver)
    {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            String htmlContent = super.html;
            htmlContent = processHtmlContent(htmlContent);
            setHtmlMsg(htmlContent);
            super.buildMimeMessage();
        }
        catch (final IOException e)
        {
            throw new EmailException("Building the MimeMessage failed", e);
        }
    }

    private String processHtmlContent(final String htmlContent) throws IOException, EmailException
    {
        try {
            Document document = Jsoup.parse(htmlContent);
            processElements(document.select("img[src]"), "src");
            processElements(document.select("script[src]"), "src");
            return document.toString();
        } catch (IOException | EmailException e) {
            // Log the exception, handle it, or rethrow it if needed
            throw e;
        }
    }

    private void processElements(Elements elements, String attributeKey) throws IOException, EmailException
    {
        Map<String, String> cidCache = new HashMap<>();
        Map<String, DataSource> dataSourceCache = new HashMap<>();

        for (Element element : elements)
        {
            String resourceLocation = element.attr(attributeKey);

            DataSource dataSource = dataSourceCache.computeIfAbsent(resourceLocation, k -> 
                getDataSourceResolver().resolve(resourceLocation)
            );

            if (dataSource != null)
            {
                String name = dataSource.getName() != null && !dataSource.getName().isEmpty() ? dataSource.getName() : resourceLocation;
                String cid = cidCache.computeIfAbsent(name, n -> embed(dataSource, n));
                element.attr(attributeKey, "cid:" + cid);
            }
        }
    }
}
