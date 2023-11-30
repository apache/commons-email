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
 * <p>Class extends HtmlEmail to encapsulate logic to retrieve images and scripts
 * contained in "&lt;img src=../&gt;" and "&lt;script src=...&gt;" elements in HTML code.
 * Replaces all img and script src-elements with "cid:"-entries and embeds them in the email.
 * </p>
 *
 * @since 1.3
 */
public class ImageHtmlEmail extends HtmlEmail {
    private DataSourceResolver dataSourceResolver;

    // Getters and setters for dataSourceResolver
    public DataSourceResolver getDataSourceResolver() {
        return dataSourceResolver;
    }

    public void setDataSourceResolver(final DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    protected void buildMimeMessage() throws EmailException {
        try {
            // Use Jsoup to parse the HTML and process <img> and <script> tags
            Document document = Jsoup.parse(getHtmlMsg());
            processElements(document, "img[src]", "src");
            processElements(document, "script[src]", "src");

            // Set the processed HTML back to the email content
            setHtmlMsg(document.outerHtml());
            super.buildMimeMessage();
        } catch (IOException e) {
            throw new EmailException("Failed to process the HTML content", e);
        }
    }

    private void processElements(Document document, String cssQuery, String attribute) throws IOException, EmailException {
        Elements elements = document.select(cssQuery);
        for (Element element : elements) {
            String src = element.attr(attribute);
            DataSource dataSource = dataSourceResolver.resolve(src);
            if (dataSource != null) {
                // Replace the src attribute with the content ID (cid) of the embedded resource
                String cid = embed(dataSource, src);
                element.attr(attribute, "cid:" + cid);
            }
        }
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
