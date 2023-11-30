import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.activation.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageHtmlEmail extends HtmlEmail {
    // DataSource resolver
    private DataSourceResolver dataSourceResolver;

    // Getters and setters for dataSourceResolver
    public DataSourceResolver getDataSourceResolver() {
        return dataSourceResolver;
    }

    public void setDataSourceResolver(final DataSourceResolver dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    @Override
    public void buildMimeMessage() throws EmailException {
        try {
            // Process the HTML content
            String processedHtml = processHtml(super.getHtmlMsg());
            setHtmlMsg(processedHtml);
            super.buildMimeMessage();
        } catch (final IOException e) {
            throw new EmailException("Building the MimeMessage failed", e);
        }
    }

    private String processHtml(final String htmlMessage) throws IOException, EmailException {
        Document document = Jsoup.parse(htmlMessage);

        // Process <img> and <script> tags
        processTags(document, "img[src]", "src");
        processTags(document, "script[src]", "src");

        return document.toString();
    }

    private void processTags(Document document, String cssQuery, String attributeKey) throws IOException, EmailException {
        Elements elements = document.select(cssQuery);

        for (Element element : elements) {
            String resourceLocation = element.attr(attributeKey);

            DataSource dataSource = getDataSourceResolver().resolve(resourceLocation);
            if (dataSource != null) {
                String cid = embed(dataSource, dataSource.getName());
                element.attr(attributeKey, "cid:" + cid);
            }
        }
    }
}
